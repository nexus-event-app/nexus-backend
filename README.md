<div align="center">

# Primer Commit · Backend

**API REST de [Primer Commit](https://primercommit.es), una comunidad para dar el primer paso en el mundo tech.**

Eventos, ofertas de empleo importadas automáticamente, foro, rutas de aprendizaje, noticias, testimonios, perfiles,
notificaciones, moderación automática, panel de administración y SEO, en castellano, catalán e inglés.

[**primercommit.es**](https://primercommit.es) · [Frontend](https://github.com/nexus-event-app/nexus-frontend) ·
[Contrato de la API](docs/API.md) · [Guía de despliegue](deploy/README.md)

[![CI](https://github.com/nexus-event-app/nexus-backend/actions/workflows/ci.yml/badge.svg?branch=dev)](https://github.com/nexus-event-app/nexus-backend/actions/workflows/ci.yml)
![Java 21](https://img.shields.io/badge/Java-21-e76f00?logo=openjdk&logoColor=white)
![Spring Boot 3.5](https://img.shields.io/badge/Spring_Boot-3.5-6db33f?logo=springboot&logoColor=white)
![PostgreSQL 17](https://img.shields.io/badge/PostgreSQL-17-4169e1?logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/migraciones-Flyway-cc0200?logo=flyway&logoColor=white)
![Estado: beta](https://img.shields.io/badge/estado-beta-f59e0b)

</div>

<p align="center">
  <img src="https://raw.githubusercontent.com/nexus-event-app/nexus-frontend/dev/docs/screenshots/portada.webp" alt="Portada de Primer Commit" width="820" />
</p>

---

## Índice

- [Visión general](#visión-general)
- [Funcionalidades](#funcionalidades)
- [Arquitectura](#arquitectura)
- [Puesta en marcha](#puesta-en-marcha)
- [Variables de entorno](#variables-de-entorno)
- [Base de datos y migraciones](#base-de-datos-y-migraciones)
- [Pruebas y CI](#pruebas-y-ci)
- [API](#api)
- [Importación automática](#importación-automática)
- [Cuentas y emails](#cuentas-y-emails)
- [Idiomas](#idiomas)
- [Seguridad y privacidad](#seguridad-y-privacidad)
- [Despliegue](#despliegue)

## Visión general

| | |
| --- | --- |
| **Lenguaje y framework** | Java 21 · Spring Boot 3.5 (Web, Security, Data JPA, Validation, Mail, Actuator) |
| **Autenticación** | JWT sin estado · inicio de sesión con Google (OAuth 2.0) |
| **Base de datos** | PostgreSQL en producción · H2 en archivo para desarrollo · esquema con Flyway |
| **Importación** | RSS/Atom y APIs públicas de empleo (Adzuna, Get on Board, Remotive, Jobicy, Arbeitnow) |
| **Pruebas** | JUnit 5 · Spring Boot Test · MockMvc · migraciones verificadas en PostgreSQL real |
| **Despliegue** | Docker Compose (PostgreSQL, backend, frontend y Caddy con HTTPS automático) |

## Funcionalidades

- **Eventos** compartidos por la comunidad: calendario, filtros por modalidad, ciudad y día, bloque para comunidades
  de mujeres, «Me apunto» (cuántos van, nunca quién), límite diario, sin duplicados y aviso por email a moderación.
- **Empleo junior y prácticas** con prioridad para España (Adzuna) y filtros por nivel, modalidad y contrato;
  clasificador de nivel en varios idiomas, caducidad automática y ofertas guardadas.
- **Foro**: discusiones, comentarios, «me gusta», mejor respuesta, edición, borrado y **reportes** con cola de
  moderación.
- **Rutas de aprendizaje** con progreso por persona.
- **Noticias** importadas de fuentes de confianza y **clasificadas por tema** automáticamente (IA, programación,
  ciberseguridad, carrera...), con detección de idioma, búsqueda y relacionadas.
- **Moderación automática** (`moderation/`): un filtro de lenguaje en castellano, catalán e inglés bloquea los
  insultos graves y retiene las palabrotas leves (también escritas con trucos como «m1erd4» o «p.u.t.o») como caso
  de reporte para revisar; tres reportes de personas distintas ocultan el contenido hasta que se revisa, y descartar
  los reportes lo vuelve a publicar.
- **Testimonios** con moderación previa y aviso por email.
- **Perfiles públicos** que solo muestran lo que la persona pone en ellos.
- **Notificaciones** de respuestas y mejores respuestas, y **búsqueda global**.
- **Cuentas**: registro con verificación de email, recuperación de contraseña, Google, borrado de la propia cuenta
  (con anonimización en el foro).
- **Panel de administración**: métricas, moderación de todo el contenido, usuarios, reportes y fuentes de
  importación. La administración no tiene límites de publicación.
- **SEO**: `sitemap.xml` y `robots.txt` con las URLs de los tres idiomas y `hreflang`.

## Arquitectura

```
src/main/java/com/nexus/backend
├── config/       Seguridad, límite de peticiones, CORS, reloj (Europe/Madrid), admin inicial
├── controller/   Endpoints públicos, de usuario y de administración (Admin*)
├── dto/          Peticiones y respuestas (PageResponse, AuthResponse...)
├── entity/       Entidades JPA con índices
├── enums/        Categorías, modalidades, niveles, tipos de reporte
├── exception/    Excepciones de dominio y GlobalExceptionHandler (errores JSON traducidos)
├── i18n/         Idiomas y tabla de rutas por idioma
├── importer/     Proveedores RSS y de empleo, clasificadores, cliente HTTP seguro y planificador
├── mail/         Envío en segundo plano y plantillas de email
├── mapper/       Entidad ↔ DTO
├── moderation/   Filtro de lenguaje y retención de contenido para revisar
├── repository/   Repositorios, Specifications y proyecciones
├── security/     JWT, filtros, política de contraseñas, tokens de un solo uso y Google
├── seo/          sitemap.xml y robots.txt
├── service/      Interfaces y service/impl con la lógica de negocio
└── util/         Slugs, texto, paginación, ciudades españolas
src/main/java/db/migration   Migraciones Flyway en Java (cuando hace falta lógica)
src/main/resources/db/migration   Migraciones Flyway en SQL
deploy/           Docker Compose, Caddy, scripts de preparación, actualización y copias
```

Capas claras: los controladores solo validan y delegan; la lógica vive en los servicios; el acceso a datos, en
repositorios con `Specification` para los filtros. Los errores se lanzan con una **clave** de mensaje y se traducen
en `GlobalExceptionHandler`.

## Puesta en marcha

Requisitos: **JDK 21** (o solo Docker). Maven no hace falta: el proyecto trae el wrapper `./mvnw`.

```bash
export ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD='Cambiame123'   # opcional: crea el primer admin
./mvnw spring-boot:run -Dmaven.test.skip=true
```

La API queda en `http://localhost:8080`. Con el perfil `dev` la base de datos es H2 en `./data/nexusdb` (no se
versiona) y la consola H2 está en `/h2-console`. En desarrollo no se imprimen las consultas SQL; para depurar, arranca
con `SHOW_SQL=true` o `LOG_LEVEL=DEBUG`.

Para tener la web completa, arranca también el [frontend](https://github.com/nexus-event-app/nexus-frontend)
(`npm run dev`), que hace de proxy de `/api` hacia este backend.

## Variables de entorno

Plantilla completa en [`.env.example`](.env.example) (desarrollo) y [`deploy/.env.example`](deploy/.env.example)
(producción). Los secretos **nunca** van en el repositorio.

| Variable | Descripción | Por defecto |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` o `prod` | `dev` |
| `JWT_SECRET` | Secreto de firma (mín. 32 bytes). **Obligatorio fuera de dev/test** | aleatorio en dev |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME` | Crean el primer administrador al arrancar si no existe | sin admin |
| `APP_PUBLIC_URL` | URL pública de la web (sitemap, robots, enlaces de los emails) | `http://localhost:5173` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por comas | `http://localhost:5173` |
| `SPRING_DATASOURCE_URL`, `_USERNAME`, `_PASSWORD` | PostgreSQL (perfil `prod`) | obligatorias en prod |
| `FORWARD_HEADERS_STRATEGY` | `native` detrás de un proxy de confianza (IP real para el límite de peticiones) | `none` |
| `UPLOAD_DIR` | Directorio de imágenes subidas | `./uploads` |
| `MAIL_ENABLED` | Envía emails por SMTP; con `false` se escriben en el log | `false` |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | Servidor SMTP y remitente | puerto `587` |
| `MODERATION_EMAIL` | Recibe el aviso de cada testimonio y evento nuevo, y del contenido retenido u oculto | administradores |
| `REQUIRE_VERIFIED_EMAIL` | Exige email verificado para publicar | `true` |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Activan «Continuar con Google»; sin ellas, el botón no aparece | vacías |
| `ADZUNA_APP_ID`, `ADZUNA_APP_KEY` | Ofertas de empleo de España (Adzuna) | vacías |
| `IMPORT_ENABLED` | Activa la importación programada | `true` |
| `IMPORT_CHECK_INTERVAL`, `IMPORT_INITIAL_DELAY` | Frecuencia y espera inicial (ISO-8601) | `PT15M`, `PT1M` |
| `IMPORT_SEED_DEFAULTS` | Crea las fuentes recomendadas si no hay ninguna | `true` |

## Base de datos y migraciones

[Flyway](https://documentation.red-gate.com/flyway) crea y actualiza el esquema al arrancar. Hibernate solo valida
que las entidades coinciden con las tablas (`ddl-auto=validate`), así que nunca modifica la base de datos por su
cuenta.

Para cambiar el esquema:

1. Crea un script con el siguiente número de versión (`V28__lo_que_hace.sql`), o una migración en Java en
   `src/main/java/db/migration` si hace falta lógica. **Nunca edites un script ya aplicado.**
2. Escribe SQL compatible con PostgreSQL y H2.
3. Actualiza la entidad y ejecuta `./mvnw verify`: si no coinciden, las pruebas fallan.

Los enums se guardan como `varchar` sin `CHECK`, así que añadir un valor no requiere migración.

## Pruebas y CI

```bash
./mvnw verify
```

- **~310 pruebas**: integración de cada área con MockMvc (permisos, validaciones, privacidad, límites), servicios,
  importadores, clasificadores, emails, SEO y migraciones en Java.
- Perfil `test`: H2 en memoria con las migraciones de Flyway.
- `PostgresMigrationTest` aplica todas las migraciones sobre un **PostgreSQL real** si defines
  `NEXUS_TEST_POSTGRES_URL` (usa una base de datos exclusiva: se vacía al empezar):

  ```bash
  NEXUS_TEST_POSTGRES_URL=jdbc:postgresql://localhost:5432/nexus_test ./mvnw verify
  ```

- `MessageBundlesTest` comprueba que los tres idiomas tienen las mismas claves y `LanguagesTest`, que la tabla de
  rutas coincide con la del frontend.
- **Integración continua** (GitHub Actions) en cada push y pull request, con PostgreSQL de servicio.

## API

El contrato completo (rutas, DTOs, errores y paginación) está en [`docs/API.md`](docs/API.md).

| Área | Rutas principales |
| --- | --- |
| Cuentas | `/api/auth/register`, `login`, `me`, `verify-email`, `forgot-password`, `reset-password`, `providers` · `/api/oauth2/authorize/google` |
| Perfil | `/api/profile` (ver, editar, foto, borrar cuenta) · `/api/users/{id}` (perfil público) |
| Noticias | `GET /api/news`, `/api/news/latest`, `/api/news/categories`, `/api/news/{id}` |
| Empleo | `GET /api/jobs`, `/api/jobs/latest`, `/api/jobs/{id}` · `/api/saved-jobs` |
| Foro | `/api/discussions` (CRUD, «me gusta», mejor respuesta) · `/api/comments` |
| Eventos | `GET /api/events`, `/api/events/calendar`, `/api/events/cities` · `POST /api/events`, `/api/events/{id}/interest` |
| Rutas | `/api/learning/progress` |
| Comunidad | `/api/testimonials` · `/api/reports` · `/api/notifications` · `/api/search` |
| Administración | `/api/admin/stats`, `news`, `jobs`, `discussions`, `testimonials`, `events`, `reports`, `users`, `uploads/image`, `import-sources` |
| SEO | `/sitemap.xml`, `/robots.txt` |

Listas paginadas con `page` (desde 0) y `size` (por defecto 12, máximo 50). Errores siempre en JSON
`{ status, error, message, fieldErrors }`, en el idioma de la petición.

## Importación automática

El paquete `importer/` consulta periódicamente las fuentes y guarda lo nuevo como noticias u ofertas. Se gestionan en
el panel (`/admin/importacion`).

| Tipo | Qué importa |
| --- | --- |
| `RSS_NEWS` | Cualquier feed RSS 2.0 o Atom, con clasificación automática por tema |
| `ADZUNA_JOBS` | Ofertas junior y prácticas de España |
| `GETONBRD_JOBS`, `REMOTIVE_JOBS`, `JOBICY_JOBS`, `ARBEITNOW_JOBS` | APIs públicas de empleo tech, respetando sus límites de consulta |

- **Noticias:** solo un resumen en texto plano, la imagen y el enlace al original; filtros por palabras clave y
  exclusiones; detección de idioma.
- **Empleo:** solo prácticas y junior del sector tech; caducan y se limpian solas.
- **Sin duplicados**, y lo que borra la administración no vuelve a importarse.
- **Atribución:** lo importado se marca con su fuente, enlaza al original y no entra en el sitemap.
- **Seguridad:** solo URLs públicas `http(s)` (también en cada redirección), tiempo y tamaño máximos, XML sin
  DOCTYPE y HTML convertido a texto plano con jsoup.

## Cuentas y emails

- Verificación de email (enlace válido 48 h): hasta verificar se puede leer, pero no publicar (`EMAIL_NOT_VERIFIED`).
- Recuperación de contraseña con enlace de un solo uso (1 h); la respuesta es la misma exista o no la cuenta. Al
  cambiarla, los JWT anteriores dejan de valer.
- Los tokens tienen 256 bits aleatorios y solo se guarda su hash SHA-256.
- Los emails se envían en segundo plano, tras confirmar la transacción. En desarrollo (`MAIL_ENABLED=false`)
  aparecen en la consola con su enlace.

## Idiomas

El servidor responde en el idioma de `Accept-Language` (castellano, catalán o inglés; castellano por defecto):
errores y validaciones (`messages*.properties`), emails y sitemap. Las URLs de catalán e inglés llevan la sección
traducida (`/ca/noticies`, `/en/jobs`) según la tabla de `i18n/Languages`, que coincide con la del frontend. El
contenido escrito por la comunidad o importado no se traduce.

## Seguridad y privacidad

- Identidad solo por JWT; el filtro recarga a la persona en cada petición, así que desactivar o borrar una cuenta
  invalida sus sesiones al momento.
- El registro público siempre crea usuarios normales; la administración solo se crea desde variables de entorno.
- Contraseñas con BCrypt y política de longitud; límite de peticiones por IP en login, registro y publicaciones.
- Filtro de lenguaje en el servidor (no se puede saltar desde el navegador) y ocultación automática con tres
  reportes. Dependabot revisa cada semana las dependencias de Maven, GitHub Actions y Docker.
- El contenido de usuario se guarda como texto plano. Subida de imágenes solo para administración, con lista blanca
  de tipos, comprobación de firma y nombres aleatorios.
- **Privacidad:** la actividad de cada persona solo la ve ella; los perfiles públicos muestran solo lo que la persona
  ha puesto; los eventos no dicen quién los compartió ni quién va. Borrar la cuenta elimina los datos personales y
  deja el foro anonimizado.
- Sin datos personales en el código: los datos legales y los secretos viven solo en el `.env` del servidor.

## Despliegue

Todo está preparado en [`deploy/`](deploy/README.md) para un servidor Ubuntu con Docker (probado en un Hetzner Cloud
CPX12 de 2 GB):

- `docker-compose.yml`: PostgreSQL, backend, frontend y **Caddy**, que pone y renueva el certificado HTTPS solo.
- `preparar-servidor.sh`: Docker, cortafuegos, actualizaciones de seguridad, memoria de reserva, código y secretos
  generados, en un solo comando.
- `actualizar.sh`: publica lo último de la rama `dev` (hace copia antes y no tumba la web si la compilación falla).
- `copia-seguridad.sh`: copia de la base de datos y las imágenes cada noche, durante 14 días.

Imagen suelta del backend:

```bash
docker build -t nexus-backend .
docker run --rm -p 8080:8080 --env-file .env -v nexus-uploads:/app/uploads nexus-backend
```

Multi-etapa, usuario sin privilegios, perfil `prod` y healthcheck en `/actuator/health`.

---

<p align="center">
  Hecho con cuidado para quienes empiezan · <a href="https://primercommit.es">primercommit.es</a>
</p>
