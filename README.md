# Primer Commit · Backend

API REST de **Primer Commit**, una comunidad tech en español para perfiles junior: noticias, ofertas de empleo, foro y testimonios, con panel de administración y SEO (sitemap y robots) servidos desde el propio backend.

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
| `MAIL_ENABLED` | Envía emails por SMTP. Con `false` se escriben en el log | `false` |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Servidor SMTP del proveedor (Brevo, Resend, Amazon SES...) | puerto `587` |
| `MAIL_FROM` | Remitente, p. ej. `Primer Commit <hola@tu-dominio.com>` | `Primer Commit <no-reply@localhost>` |
| `REQUIRE_VERIFIED_EMAIL` | Exige email verificado para publicar en el foro y enviar testimonios | `true` |
| `IMPORT_ENABLED` | Activa la importación automática programada | `true` |
| `IMPORT_CHECK_INTERVAL` | Cada cuánto se revisa qué fuentes toca ejecutar (ISO-8601) | `PT15M` |
| `IMPORT_INITIAL_DELAY` | Espera tras arrancar antes de la primera revisión | `PT1M` |
| `IMPORT_SEED_DEFAULTS` | Crea las fuentes por defecto si no hay ninguna | `true` |

## Arranque en desarrollo

```bash
export ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD='Cambiame123' # opcional
./mvnw spring-boot:run
```

Con el perfil `dev` la base de datos es H2 en `./data/nexusdb` (no se versiona), la consola H2 está en `/h2-console` y se muestran las consultas SQL. La API queda en `http://localhost:8080`.

Si ya tenías una base de datos H2 creada antes de introducir Flyway, bórrala (`rm -r data/`) antes de arrancar: Flyway necesita crear el esquema desde cero.

## Base de datos y migraciones (Flyway)

El esquema lo crea y actualiza [Flyway](https://documentation.red-gate.com/flyway) al arrancar, con los scripts de `src/main/resources/db/migration`. Hibernate solo valida que las entidades coinciden con las tablas (`ddl-auto=validate`), así que nunca modifica la base de datos por su cuenta.

Para cambiar el esquema:

1. Crea un script nuevo con el siguiente número de versión, por ejemplo `V2__perfil_de_usuario.sql`. **Nunca edites un script que ya se haya aplicado** en algún entorno: Flyway lo detecta y no arranca.
2. Escribe SQL compatible con PostgreSQL y H2 (tipos estándar: `varchar`, `text`, `boolean`, `bigint`, `timestamp(6)`).
3. Actualiza la entidad y ejecuta `./mvnw verify`: si el script y la entidad no coinciden, los tests fallan.

Los enums se guardan como `varchar` sin `CHECK` (`@JdbcTypeCode(SqlTypes.VARCHAR)` en las entidades), así que añadir un valor nuevo a un enum no requiere migración.

## Tests

```bash
./mvnw test
```

Usan el perfil `test` (H2 en memoria con las migraciones de Flyway y un secreto JWT de prueba).

`PostgresMigrationTest` aplica las migraciones sobre un PostgreSQL real y solo se ejecuta si defines `NEXUS_TEST_POSTGRES_URL` (por defecto usuario y contraseña `nexus`; cámbialos con `NEXUS_TEST_POSTGRES_USER` y `NEXUS_TEST_POSTGRES_PASSWORD`). Vacía esa base de datos al empezar, así que usa una exclusiva para tests:

```bash
NEXUS_TEST_POSTGRES_URL=jdbc:postgresql://localhost:5432/nexus_test ./mvnw test
```

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
├── importer/     importación automática: proveedores, clasificador, cliente HTTP y planificador
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
| Admin (solo ADMIN) | `/api/admin/stats`, `news`, `jobs`, `discussions`, `testimonials`, `users`, `uploads/image`, `import-sources` |
| SEO / estáticos | `/sitemap.xml`, `/robots.txt`, `/uploads/news/{archivo}` |

Todas las listas paginadas usan `page` (desde 0) y `size` (por defecto 12, máximo 50), y los errores siguen el formato JSON descrito en `docs/API.md`.

## Cuentas: verificación de email y recuperación de contraseña

- Al registrarse se envía un enlace de verificación (válido 48 horas). Hasta verificar, se puede iniciar sesión y leer, pero publicar debates, comentarios o testimonios responde 403 con el código `EMAIL_NOT_VERIFIED`.
- «¿Has olvidado tu contraseña?» envía un enlace válido 1 hora y de un solo uso. La respuesta es siempre la misma, exista o no la cuenta, para no revelar qué emails están registrados. Al cambiar la contraseña, todos los JWT emitidos antes dejan de valer (`users.credentials_changed_at`).
- Los tokens se generan con 256 bits aleatorios y en la base de datos solo se guarda su hash SHA-256 (`user_tokens`). Al emitir uno nuevo se invalidan los anteriores del mismo tipo, y los caducados se borran cada día.
- Los emails se envían en segundo plano y solo después de confirmar la transacción.

**En desarrollo** no hace falta ningún servicio de correo: con `MAIL_ENABLED=false` el email completo, con su enlace, aparece en la consola del backend entre líneas `===== EMAIL`. Copia el enlace en el navegador para probar el flujo.

**En producción**, crea una cuenta en un proveedor SMTP, verifica tu dominio (registros SPF y DKIM que te indicará) y define `MAIL_ENABLED=true`, `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD` y `MAIL_FROM`.

## Importación automática de noticias y empleo

El paquete `importer/` consulta periódicamente fuentes externas y guarda lo nuevo como noticias u ofertas. Las fuentes se gestionan en el panel (`/admin/importacion`) o en `/api/admin/import-sources`.

| Tipo | Qué importa | Intervalo mínimo |
|---|---|---|
| `RSS_NEWS` | Cualquier feed RSS 2.0 o Atom | 30 min |
| `GETONBRD_JOBS` | API pública de [Get on Board](https://www.getonbrd.com) (empleo tech en español), por categoría | 2 h |
| `REMOTIVE_JOBS` | API pública de [Remotive](https://remotive.com/api-documentation) (empleo en remoto) | 6 h (máximo 4 consultas al día según sus condiciones) |
| `ARBEITNOW_JOBS` | API pública de [Arbeitnow](https://www.arbeitnow.com/blog/job-board-api) (Europa) | 1 h |

Reglas que aplica en cada ejecución:

- **Noticias:** solo se guarda un resumen en texto plano (máximo 600 caracteres), la imagen y el enlace al artículo original. Se descartan las de más de 14 días y, si la fuente tiene palabras clave, las que no contengan ninguna. Máximo 10 nuevas por fuente y ejecución.
- **Empleo:** solo ofertas de prácticas o junior del sector tech (`JobClassifier`, con pistas de nivel en español, inglés, alemán y francés). Se descartan las de más de 30 días; las importadas caducan a los 30 días de publicarse y se borran 60 días después de caducar.
- **Duplicados:** cada elemento tiene un `externalId` único. Si un admin borra algo importado, se guarda en `import_exclusions` y no vuelve a importarse.
- **SEO y atribución:** lo importado sale marcado con su fuente y enlaza al original, no entra en el sitemap y el frontend lo publica con `noindex` y sin datos estructurados (Remotive prohíbe expresamente que sus ofertas acaben en Google Jobs).
- **Seguridad:** solo URLs `http`/`https` públicas (se rechazan localhost y redes privadas, también en cada redirección), tiempo de espera de 25 s, respuestas de 8 MB como máximo, XML sin DOCTYPE y HTML convertido a texto plano con jsoup.
- **Revisión manual:** con `autoPublish=false` lo importado queda oculto hasta que un admin lo publique.

El planificador está pensado para una sola instancia. Si despliegas varias réplicas, deja `IMPORT_ENABLED=true` solo en una.

## Seguridad en breve

- La identidad sale únicamente del JWT (`Authorization: Bearer ...`); el filtro recarga al usuario de la BD en cada petición, por lo que desactivar una cuenta invalida sus tokens.
- El registro público siempre crea usuarios `USER`; solo se crea un ADMIN desde las variables de entorno.
- El contenido de usuario se guarda como texto plano; el frontend debe pintarlo como texto.
- Subida de imágenes solo para ADMIN, con lista blanca de extensión y content-type, comprobación de firma y nombres generados con UUID.
- Límite de peticiones por IP en login, registro, discusiones, comentarios y testimonios.
