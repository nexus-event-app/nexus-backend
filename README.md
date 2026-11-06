# Nexus Backend

API REST de **Nexus**, una comunidad tech en español para perfiles junior: noticias, ofertas de empleo, foro y testimonios, con panel de administración y SEO (sitemap y robots) servidos desde el propio backend.

- Spring Boot 3.5, Java 21, Spring Security (JWT sin estado), Spring Data JPA
- H2 en archivo para desarrollo, PostgreSQL en producción
- Contrato completo de la API en [`docs/API.md`](docs/API.md)

## Requisitos

- JDK 21 y Maven 3.9 (o el wrapper `./mvnw`)
- Docker (opcional)
- PostgreSQL 14+ para el perfil `prod`

## Variables de entorno

Hay una plantilla completa en [`.env.example`](.env.example).

| Variable | Descripción | Por defecto |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` o `prod` | `dev` |
| `JWT_SECRET` | Secreto de firma (mín. 32 bytes). **Obligatorio fuera de dev/test** | en dev se genera uno aleatorio |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME` | Crean el primer ADMIN al arrancar si no existe | sin admin |
| `APP_PUBLIC_URL` | URL pública del frontend (sitemap, robots) | `http://localhost:5173` |
| `CORS_ALLOWED_ORIGINS` | Orígenes CORS separados por comas | `http://localhost:5173` |
| `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | PostgreSQL (perfil `prod`) | obligatorias en prod |
| `FORWARD_HEADERS_STRATEGY` | `none` o `native` (detrás de un proxy de confianza) | `none` |
| `UPLOAD_DIR` | Directorio de imágenes subidas | `./uploads` |
| `JPA_DDL_AUTO` | Estrategia de esquema de Hibernate en prod | `update` |

## Arranque en desarrollo

```bash
export ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD='Cambiame123' # opcional
./mvnw spring-boot:run
```

Con el perfil `dev` la base de datos es H2 en `./data/nexusdb` (no se versiona), la consola H2 está en `/h2-console` y se muestran las consultas SQL. La API queda en `http://localhost:8080`.

Si ya tenías una base de datos H2 anterior, bórrala (`rm -r data/`) antes de arrancar: cambia el rol de usuarios (`USER`/`ADMIN`) y las credenciales de H2.

## Tests

```bash
./mvnw test
```

Usan el perfil `test` (H2 en memoria y un secreto JWT de prueba).

## Docker

```bash
docker build -t nexus-backend .
docker run --rm -p 8080:8080 --env-file .env -v nexus-uploads:/app/uploads nexus-backend
```

La imagen es multi-etapa, se ejecuta con un usuario sin privilegios, arranca con el perfil `prod` y comprueba su salud en `/actuator/health`.

Detrás de nginx u otro proxy de confianza define `FORWARD_HEADERS_STRATEGY=native` para que el límite de peticiones use la IP real del cliente. No actives esta opción si el backend está expuesto directamente.

## Estructura

```
src/main/java/com/nexus/backend
├── config/       SecurityConfig, RateLimitFilter, WebConfig, AdminInitializer
├── controller/   endpoints públicos, de usuario y de administración (Admin*)
├── dto/          request / response (PageResponse, AuthResponse...)
├── entity/       entidades JPA con índices
├── enums/        categorías, modalidades, niveles
├── exception/    excepciones de dominio y GlobalExceptionHandler
├── mapper/       entidad <-> DTO
├── repository/   repositorios, specifications y proyecciones
├── security/     JWT, filtro y UserDetail
├── seo/          sitemap.xml y robots.txt
├── service/      interfaces y service/impl
└── util/         SlugUtil, TextUtil, Pagination
```

## Endpoints (resumen)

| Área | Rutas |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` |
| Noticias | `GET /api/news`, `/api/news/latest`, `/api/news/{id}` |
| Empleo | `GET /api/jobs`, `/api/jobs/latest`, `/api/jobs/{id}` |
| Foro | `GET/POST /api/discussions`, `GET /api/discussions/mine`, `GET/PUT/DELETE /api/discussions/{id}`, `POST /api/discussions/{id}/like` |
| Comentarios | `GET /api/comments/discussion/{id}`, `POST /api/comments`, `DELETE /api/comments/{id}`, `POST /api/comments/{id}/like` |
| Testimonios | `GET /api/testimonials`, `/api/testimonials/latest`, `POST /api/testimonials` |
| Admin (solo ADMIN) | `/api/admin/stats`, `news`, `jobs`, `discussions`, `testimonials`, `users`, `uploads/image` |
| SEO / estáticos | `/sitemap.xml`, `/robots.txt`, `/uploads/news/{archivo}` |

Todas las listas paginadas usan `page` (desde 0) y `size` (por defecto 12, máximo 50), y los errores siguen el formato JSON descrito en `docs/API.md`.

## Seguridad en breve

- La identidad sale únicamente del JWT (`Authorization: Bearer ...`); el filtro recarga al usuario de la BD en cada petición, por lo que desactivar una cuenta invalida sus tokens.
- El registro público siempre crea usuarios `USER`; solo se crea un ADMIN desde las variables de entorno.
- El contenido de usuario se guarda como texto plano; el frontend debe pintarlo como texto.
- Subida de imágenes solo para ADMIN, con lista blanca de extensión y content-type, comprobación de firma y nombres generados con UUID.
- Límite de peticiones por IP en login, registro, discusiones, comentarios y testimonios.
