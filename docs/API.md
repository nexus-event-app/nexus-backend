# API de Primer Commit

Referencia de la API REST tal y como está implementada en este backend (Spring Boot 3.5, Java 21). Es el contrato compartido con el frontend.

## 0. Idea del producto (guía de todas las decisiones)
Comunidad tech para perfiles junior en español:
- Noticias tech diarias: CRUD manual de admin más importación automática desde feeds RSS/Atom.
- Ofertas de empleo relacionadas con la temática (junior/prácticas). Sustituye al módulo de "eventos".
- Foro activo. Solo usuarios registrados pueden escribir. El admin tiene control absoluto (borrar, ocultar, bloquear usuarios).
- Testimonios. Solo usuarios registrados; los moderan los admins antes de publicarse.
- Prioridades: seguridad, código limpio y semántico, SEO, responsive, escalabilidad.

## 1. Decisiones transversales
- Solo español. Se elimina i18n.
- Roles: `USER` y `ADMIN`. El registro público SIEMPRE crea `USER`. El primer ADMIN se crea al arrancar desde variables de entorno (`ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME`).
- Identidad SOLO por JWT (`Authorization: Bearer <token>`). Se elimina por completo la cabecera `User-Id`.
- Todo texto de usuario se guarda como **texto plano** y el frontend lo pinta como texto (React lo escapa). Prohibido `dangerouslySetInnerHTML`. Se elimina el saneador HTML del backend (codificaba entidades y rompía código tipo `C++ & Java`).
- El frontend usa URLs relativas: `/api/...`, `/uploads/...`. En desarrollo Vite hace proxy a `http://localhost:8080`; en Docker, nginx hace proxy al backend (`BACKEND_URL`). `/sitemap.xml` y `/robots.txt` los sirve el backend y también pasan por el proxy.
- Paginación 0-based con `page` (por defecto 0) y `size` (por defecto 12, máximo 50). Orden fijo: más reciente primero. Excepción: las ofertas de empleo públicas se ordenan con las de España delante (`in_spain`, calculado con `SpainLocation` a partir de la ubicación) y, dentro de cada grupo, por fecha de publicación en la fuente (`publishedAt`), no por fecha de importación. En el panel de admin, las ofertas van por `publishedAt`.
- URLs públicas con slug: `/noticias/{id}-{slug}`, `/empleo/{id}-{slug}`, `/foro/{id}-{slug}`. El backend devuelve `slug` en las respuestas; el frontend extrae el `id` de los dígitos iniciales y redirige al canónico si el slug no coincide.
- Slug: minúsculas, sin acentos (Normalizer NFD), todo lo no alfanumérico pasa a `-`, sin guiones repetidos ni en los extremos, máximo 80 caracteres; si queda vacío, `"sin-titulo"`.

## 2. Formato común
### PageResponse<T>
```json
{ "content": [ ... ], "page": 0, "size": 12, "totalElements": 40, "totalPages": 4, "last": false }
```
### Error (siempre JSON)
```json
{ "status": 400, "error": "VALIDATION_ERROR", "message": "Datos no válidos", "fieldErrors": { "email": "El email debe ser válido" }, "timestamp": "2026-11-06T11:50:00", "path": "/api/auth/register" }
```
Códigos `error`: `VALIDATION_ERROR` (400), `BAD_REQUEST` (400), `UNAUTHORIZED` (401), `FORBIDDEN` (403), `EMAIL_NOT_VERIFIED` (403, al publicar sin haber verificado el email), `NOT_FOUND` (404), `CONFLICT` (409), `TOO_MANY_REQUESTS` (429), `INTERNAL_ERROR` (500). `fieldErrors` solo aparece en validación. Mensajes en español y sin detalles internos.

### Idioma
Los mensajes de error, de validación y los emails salen en el idioma de `Accept-Language` (`es` por defecto, `ca`, `en`). Los códigos de error (`error`) no cambian.

## 3. DTOs de respuesta
### UserResponse
`{ id, name, email, role: "USER"|"ADMIN", enabled: boolean, emailVerified: boolean, createdAt }`
### AuthResponse
`{ token, user: UserResponse }`
### NewsResponse
`{ id, slug, title, content, sourceUrl, imageUrl, author, category, source, imported, isActive, createdAt, updatedAt }`
`source` es el nombre de la fuente de importación (o `null`); `imported` es `true` si la noticia se importó (su `content` es solo un resumen).
`category`: `DESARROLLO | ENTREVISTAS | NETWORKING | RECURSOS | GENERAL`
### JobResponse
`{ id, slug, title, company, description, location, workMode, contractType, level, tags: string[], applyUrl, source, imported, isActive, publishedAt, expiresAt, createdAt, updatedAt }`
- `imported`: `true` si la oferta viene de una fuente externa (el frontend no la indexa ni la marca como JobPosting).
- `workMode`: `REMOTE | HYBRID | ONSITE`
- `contractType`: `FULL_TIME | PART_TIME | INTERNSHIP | FREELANCE | TEMPORARY`
- `level`: `INTERN | JUNIOR | MID`
- `source` y `expiresAt` pueden ser `null`. `tags` se guarda como texto separado por comas y se expone como array.
### DiscussionResponse
`{ id, slug, title, content, category, authorId, authorName, isActive, commentCount, likesCount, isLikedByCurrentUser, createdAt, updatedAt }`
`category`: `AYUDA_TECNICA | RECURSOS | EXPERIENCIAS | NETWORKING | GENERAL`
`isLikedByCurrentUser` es `false` si no hay sesión.
### CommentResponse
`{ id, content, discussionId, authorId, authorName, likesCount, isLikedByCurrentUser, createdAt, updatedAt }`
### TestimonialResponse
`{ id, name, role, location, content, isActive, isPending, isHighlighted, createdAt, updatedAt }`
### ImportSourceResponse (admin)
`{ id, name, type, url, newsCategory, autoPublish, active, remoteOnly, keywords, intervalMinutes, minIntervalMinutes, lastRunAt, lastStatus: "SUCCESS"|"FAILED"|null, lastMessage, lastImportedCount, running, createdAt }`
`type`: `RSS_NEWS | GETONBRD_JOBS | REMOTIVE_JOBS | JOBICY_JOBS | ARBEITNOW_JOBS`
### ImportRunResponse (admin)
`{ status, fetched, imported, message, source: ImportSourceResponse }`
### StatsResponse (admin)
`{ users, news, jobs, discussions, comments, pendingTestimonials }` (todos enteros)

## 4. DTOs de petición (validación en es/ca/en)
- `RegisterRequest { name (2–50), email (válido, ≤100, se normaliza a minúsculas y trim), password (8–72, al menos una letra y un número), acceptTerms (obligatorio, true) }`. Se guarda la fecha de aceptación en `users.terms_accepted_at`.
- `LoginRequest { email, password }`
- `CreateNewsRequest / UpdateNewsRequest { title (≤200), content (≤20000), sourceUrl (http/https, opcional), imageUrl (opcional), author (≤100, opcional), category (por defecto GENERAL) }`
- `CreateJobRequest / UpdateJobRequest { title (≤200), company (≤120), description (≤20000), location (≤120), workMode, contractType, level, tags: string[] (máx 10, cada una ≤30), applyUrl (obligatoria, http/https), source (opcional ≤100), expiresAt (opcional, ISO local date-time) }`. En `Update*` se puede además enviar `isActive`.
- `CreateDiscussionRequest { title (5–200), content (10–10000), category }`
- `UpdateDiscussionRequest { title, content, category }` (mismas reglas)
- `CreateCommentRequest { discussionId, content (3–5000) }`
- `CreateTestimonialRequest { name (2–100), role (≤100, opcional), location (≤100, opcional), content (50–300) }`
- `UpdateTestimonialRequest { isActive, isPending, isHighlighted }` (todos opcionales; solo admin)
- `UpdateUserStatusRequest { enabled: boolean }`
- `ImportSourceRequest { name (2–100, único), type, url (http/https pública, ≤500), newsCategory (solo noticias, por defecto GENERAL), autoPublish (por defecto true), active (por defecto true), remoteOnly (solo empleo), keywords (≤500, separadas por comas), intervalMinutes (30–10080; si es menor que el mínimo del tipo se eleva a ese mínimo) }`. El `type` no se puede cambiar al editar.

## 5. Endpoints
### Públicos (sin sesión)
| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/news?page&size&category&q` | `PageResponse<NewsResponse>` (solo activas) |
| GET | `/api/news/latest?limit` (por defecto 3, máx 12) | `NewsResponse[]` |
| GET | `/api/news/{id}` | `NewsResponse` (404 si no existe o está inactiva) |
| GET | `/api/jobs?page&size&level&workMode&contractType&q` | `PageResponse<JobResponse>` (activas y no caducadas) |
| GET | `/api/jobs/latest?limit` (por defecto 3, máx 12) | `JobResponse[]` |
| GET | `/api/jobs/{id}` | `JobResponse` |
| GET | `/api/discussions?page&size&category&q` | `PageResponse<DiscussionResponse>` (solo activas) |
| GET | `/api/discussions/{id}` | `DiscussionResponse` |
| GET | `/api/comments/discussion/{discussionId}` | `CommentResponse[]` |
| GET | `/api/testimonials` | `TestimonialResponse[]` (activos y no pendientes; destacados primero) |
| GET | `/api/testimonials/latest` | hasta 8, mismo criterio |
| GET | `/sitemap.xml`, `/robots.txt`, `/uploads/**` | XML / texto / imágenes |
| POST | `/api/auth/register` | 201 `AuthResponse` (rol USER) |
| POST | `/api/auth/login` | `AuthResponse` (401 si credenciales inválidas o cuenta desactivada) |
| POST | `/api/auth/forgot-password` | `{ email }` → 202 siempre, exista o no la cuenta; si existe y está activa, envía un enlace válido 1 hora |
| POST | `/api/auth/reset-password` | `{ token, password }` → 204; 400 si el enlace no es válido, ya se usó o caducó. Invalida los JWT anteriores |
| POST | `/api/auth/verify-email` | `{ token }` → 204; 400 si el enlace no es válido, ya se usó o caducó (48 horas) |

En los GET de discusiones y comentarios, si llega un token válido se rellena `isLikedByCurrentUser`; si no hay token o es inválido se trata como anónimo (nunca error).

### Usuario autenticado (USER o ADMIN)
| Método | Ruta | Notas |
|---|---|---|
| GET | `/api/auth/me` | `UserResponse` |
| POST | `/api/auth/resend-verification` | 202; envía un nuevo enlace de verificación e invalida el anterior. 400 si el email ya está verificado |
| GET | `/api/discussions/mine?page&size` | `PageResponse<DiscussionResponse>` |
| POST | `/api/discussions` | 201 `DiscussionResponse` |
| PUT | `/api/discussions/{id}` | solo autor o ADMIN |
| DELETE | `/api/discussions/{id}` | 204; solo autor o ADMIN; borra también sus comentarios y likes |
| POST | `/api/discussions/{id}/like` | alterna like → `DiscussionResponse` |
| POST | `/api/comments` | 201 `CommentResponse` |
| DELETE | `/api/comments/{id}` | 204; solo autor o ADMIN |
| POST | `/api/comments/{id}/like` | alterna like → `CommentResponse` |
| POST | `/api/testimonials` | 201 `TestimonialResponse`; queda `isPending=true`, `isActive=false`; máximo uno cada 24 h por usuario (409 si no) |

### Solo ADMIN (`/api/admin/**`)
| Método | Ruta | Notas |
|---|---|---|
| GET | `/api/admin/stats` | `StatsResponse` |
| GET/POST | `/api/admin/news` | GET: `PageResponse<NewsResponse>` con inactivas incluidas; POST: 201 |
| PUT/DELETE | `/api/admin/news/{id}` | |
| GET/POST | `/api/admin/jobs` | igual que noticias |
| PUT/DELETE | `/api/admin/jobs/{id}` | |
| GET | `/api/admin/discussions?page&size&q` | incluye inactivas |
| GET | `/api/admin/testimonials` | `TestimonialResponse[]` de todos, pendientes incluidos |
| PUT | `/api/admin/testimonials/{id}` | aprobar, ocultar o destacar |
| DELETE | `/api/admin/testimonials/{id}` | |
| GET | `/api/admin/users?page&size&q` | `PageResponse<UserResponse>` |
| PUT | `/api/admin/users/{id}/status` | `{enabled}`; un admin no puede desactivarse a sí mismo ni a otro ADMIN |
| POST | `/api/admin/uploads/image` | multipart campo `file` → `{ imageUrl, fileName }` |
| DELETE | `/api/admin/uploads/image/{fileName}` | 204 |
| GET | `/api/admin/import-sources` | `ImportSourceResponse[]` ordenadas por nombre |
| POST | `/api/admin/import-sources` | 201 `ImportSourceResponse`; 409 si el nombre ya existe |
| PUT/DELETE | `/api/admin/import-sources/{id}` | borrar una fuente no borra lo ya importado |
| POST | `/api/admin/import-sources/{id}/run` | ejecuta ahora → `ImportRunResponse`; 400 si se consultó hace menos del tiempo mínimo del tipo (Remotive 6 h, Get on Board y Arbeitnow 5 min, RSS 1 min) |

Las imágenes se sirven en `/uploads/news/{fileName}`. El borrado de discusiones y comentarios por parte de un admin usa los mismos endpoints de usuario (`DELETE /api/discussions/{id}`, `DELETE /api/comments/{id}`).

## 6. Rutas del frontend (todas en español)
`/` · `/noticias` · `/noticias/:idSlug` · `/empleo` · `/empleo/:idSlug` · `/foro` · `/foro/nueva` · `/foro/:idSlug` · `/testimonios` · `/login` · `/registro` · `/admin` · `/admin/noticias` · `/admin/empleo` · `/admin/foro` · `/admin/testimonios` · `/admin/usuarios` · `/admin/importacion` · `*` → página 404 real (no redirigir a la home).
Las páginas de admin, login y registro llevan `noindex`.
Los filtros y la paginación de listados van en la query de la URL (`?page=2&categoria=...`) para que sean enlazables y rastreables.

## 7. Seguridad: resumen de comportamiento esperado
- Cuenta con `enabled=false`: no puede iniciar sesión y sus tokens dejan de valer (el filtro JWT comprueba el estado actual en BD).
- 401 sin token o con token inválido en rutas protegidas; 403 si el rol no basta. Ambos en JSON con el formato de error.
- Límites de peticiones por IP real (sin fiarse de `X-Forwarded-For` salvo configuración explícita): login, registro, crear discusión, crear comentario, crear testimonio.
- Sin secretos en el repo: `JWT_SECRET` obligatorio en prod; en dev se genera uno aleatorio al arrancar.

## 8. Notas de implementación (ampliaciones sobre el contrato)
- `PUT /api/discussions/{id}` acepta además `isActive` (opcional). Solo se aplica si quien edita es ADMIN; permite ocultar o restaurar una discusión.
- Códigos de error adicionales: `METHOD_NOT_ALLOWED` (405), `UNSUPPORTED_MEDIA_TYPE` (415) y `NOT_ACCEPTABLE` (406).
- Una subida que supera el tamaño máximo (5 MB) responde 400 `BAD_REQUEST`.
- `GET /api/discussions/mine` devuelve solo las discusiones activas del usuario.
- `GET /api/admin/news` y `GET /api/admin/jobs` aceptan los mismos filtros que los listados públicos (`category`/`q` y `level`/`workMode`/`contractType`/`q`), incluyendo elementos inactivos y caducados.
- `GET /api/testimonials` devuelve como máximo 100 testimonios; `/latest` devuelve 8.
- Límites de peticiones por IP (POST): login 10 / 15 min, registro 5 / hora, recuperar contraseña 5 / hora, restablecer contraseña 10 / hora, verificar email 20 / hora, reenviar verificación 5 / hora, crear discusión 10 / hora, crear comentario 30 / hora, crear testimonio 5 / hora. Al superarlos: 429 `TOO_MANY_REQUESTS` con cabecera `Retry-After` (segundos).
- `GET /actuator/health` es público y es lo único que expone Actuator (`{"status":"UP"}`).
- `isLikedByCurrentUser` se calcula con una única consulta por página; `commentCount` con una única consulta agrupada.
- `/sitemap.xml` y `/robots.txt` se cachean 10 minutos en memoria; el sitemap incluye hasta 5000 URLs por tipo (noticias activas, ofertas vigentes, discusiones activas; sin contenido importado).
- `/sitemap.xml` excluye noticias y ofertas importadas (`externalId` no nulo): son resúmenes de contenido ajeno.
- `/sitemap.xml` por idioma: las páginas traducidas (`/`, `/noticias`, `/empleo`, `/foro`, `/testimonios`) se listan con prefijo `/ca` y `/en` y enlaces `hreflang`; los detalles de noticias, ofertas y debates y las páginas legales solo en castellano, sin `hreflang`.
