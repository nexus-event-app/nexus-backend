# Publicar Primer Commit (beta)

Guía paso a paso para poner la web en internet en un servidor propio. Todo lo técnico ya está preparado en esta
carpeta; lo que queda son cuentas y datos que solo puedes poner tú.

**Cómo queda montado:** un servidor Ubuntu con Docker y cuatro piezas: la base de datos (PostgreSQL), el backend, el
frontend y Caddy, que pone el candado HTTPS y lo renueva solo. Los datos (usuarios, foro, testimonios, imágenes) se
guardan en el servidor y se quedan para la versión definitiva. Cada noche se hace una copia de seguridad.

| Archivo                 | Para qué sirve                                                                      |
| ----------------------- | ----------------------------------------------------------------------------------- |
| `docker-compose.yml`    | Las cuatro piezas y cómo se conectan.                                               |
| `Caddyfile`             | HTTPS automático para tu dominio (y `www.` lleva a la principal).                   |
| `.env.example`          | Plantilla de la configuración. En el servidor se copia a `.env`, que nunca se sube. |
| `preparar-servidor.sh`  | Se ejecuta una vez en un servidor nuevo: Docker, cortafuegos, código y secretos.    |
| `actualizar.sh`         | Arranca la web la primera vez y la actualiza después (con copia previa).            |
| `copia-seguridad.sh`    | Copia de la base de datos y de las imágenes (cada noche a las 03:30, 14 días).      |

---

## Antes del lunes: lo que tienes que hacer tú (unos 30-45 minutos)

### 1. Servidor

Recomendado: **Hetzner Cloud** (centros de datos en la UE, buena relación precio/calidad; consulta el precio actual).

1. Crea la cuenta en hetzner.com/cloud y un proyecto «Primer Commit».
2. En tu Mac, crea una llave SSH si no tienes (pulsa Intro a todo):

   ```bash
   ssh-keygen -t ed25519 -C "primercommit"
   ```

3. Cópiala al portapapeles:

   ```bash
   pbcopy < ~/.ssh/id_ed25519.pub
   ```

4. «Add Server»: ubicación en Alemania o Finlandia, imagen **Ubuntu 24.04**, tipo **CPX12** (2 GB de RAM, x86) o mayor; el script añade memoria de reserva (swap) para que quepa la compilación, y en
   «SSH keys» pega la llave. Sin volúmenes ni extras.
5. Apunta la **IP** del servidor (algo como `203.0.113.10`).

### 2. Dominio

1. Compra el dominio en un registrador (por ejemplo, `primercommit.es` o el que prefieras).
2. En su panel de DNS crea dos registros **A** que apunten a la IP del servidor:
   - nombre `@` → IP del servidor
   - nombre `www` → IP del servidor
3. Puede tardar unos minutos (a veces horas) en funcionar. Para comprobarlo desde el Mac:

   ```bash
   dig +short tu-dominio.es
   ```

### 3. Email (para verificar cuentas y recuperar contraseñas)

Los emails salen con **Brevo** (plan gratis, 300 al día) desde `Primer Commit <hola@primercommit.es>`, con el dominio
autenticado para que lleguen a la bandeja de entrada y no a spam. Esa dirección solo envía: no tiene buzón.

1. En Brevo, «Remitentes, dominios e IP dedicadas» → «Dominios», añade `primercommit.es` en modo **manual** (sin
   subdominio con marca ni conexión automática con DonDominio).
2. En DonDominio, «Zona DNS» de `primercommit.es`, añade los cuatro registros que da Brevo, sin tocar el registro `A`
   del servidor. En «Host» va solo lo que se indica: DonDominio añade `.primercommit.es` por su cuenta.

   | Tipo | Host | Valor |
   |---|---|---|
   | TXT | `@` | `brevo-code:…` (el código que enseña Brevo) |
   | CNAME | `brevo1._domainkey` | `b1.primercommit-es.dkim.brevo.com` |
   | CNAME | `brevo2._domainkey` | `b2.primercommit-es.dkim.brevo.com` |
   | TXT | `_dmarc` | `v=DMARC1; p=none; rua=mailto:rua@dmarc.brevo.com` |

3. En Brevo, «Verificar registros» y «Autenticar dominio» (los cambios de DNS pueden tardar unos minutos), y después
   «Remitentes» → «Añadir remitente»: `Primer Commit`, `hola@primercommit.es`. Con el dominio autenticado queda
   verificado al momento.
4. En «SMTP y API» → «SMTP», genera una clave SMTP y guárdala en la app «Contraseñas» del Mac: Brevo solo la enseña una
   vez. El usuario es el «Iniciar sesión» de esa página (termina en `@smtp-brevo.com`).
5. En el `.env` del servidor, nunca en el chat ni en el código:

   ```
   MAIL_HOST=smtp-relay.brevo.com
   MAIL_PORT=587
   MAIL_USERNAME=el «Iniciar sesión» de Brevo
   MAIL_PASSWORD=la clave SMTP de Brevo
   MAIL_FROM="Primer Commit <hola@primercommit.es>"
   ```

   y reinicia el backend con `docker compose up -d backend`. Para comprobarlo sin enseñar la clave:
   `docker compose exec backend printenv MAIL_HOST MAIL_FROM`.
6. Prueba: crea una cuenta con email y contraseña (mejor de Outlook o Hotmail, que es el más estricto) y comprueba que
   el correo de confirmación llega a la bandeja de entrada.

El uso del día se ve en Brevo, en «Uso y plan». Si una persona no consigue confirmar su cuenta, la administración
puede confirmarla a mano (ver «Ver la base de datos con pgAdmin» o la consola de PostgreSQL).

### 4. Google (opcional)

El cliente que usamos en local tiene el secreto expuesto en una captura: **para producción, uno nuevo**.

1. Google Cloud → «APIs y servicios» → «Credenciales» → «Crear credenciales» → «ID de cliente de OAuth» → «Aplicación
   web», con el nombre **Primer Commit (producción)**.
2. Orígenes de JavaScript autorizados: `https://tu-dominio.es`
3. URI de redirección autorizada: `https://tu-dominio.es/api/oauth2/callback/google`
4. En «Pantalla de consentimiento» → «Público», pulsa **Publicar la aplicación** (pasa de «Prueba» a «En producción»;
   con nombre, email y foto no hace falta revisión de Google). Si no, solo podrían entrar las personas de prueba.
5. Apunta el ID de cliente y el secreto: van en el `.env` del servidor.

### 5. Datos legales

Para el aviso legal y la privacidad bastan **tu nombre y un email de contacto** (por ejemplo, el del proyecto). El NIF
y el domicilio pueden quedar vacíos: entonces no se muestran. Si algún día la web tiene actividad económica (anuncios,
servicios de pago...), la LSSI obliga a añadirlos.

---

## El lunes: lanzar (unos 20 minutos)

### 1. Preparar el servidor (solo la primera vez)

Desde el Terminal del Mac, entra en el servidor (cambia la IP por la tuya; la primera vez pregunta si te fías: `yes`):

```bash
ssh root@203.0.113.10
```

Ya dentro del servidor:

```bash
curl -fsSL https://raw.githubusercontent.com/nexus-event-app/nexus-backend/dev/deploy/preparar-servidor.sh | bash
```

Instala Docker, abre solo los puertos necesarios, activa las actualizaciones de seguridad, descarga el código en
`/opt/primercommit`, crea el `.env` con las contraseñas internas ya generadas y programa la copia de cada noche.

### 2. Rellenar la configuración

```bash
nano /opt/primercommit/nexus-backend/deploy/.env
```

Rellena: `DOMAIN`, `ADMIN_EMAIL` y `ADMIN_PASSWORD` (tu cuenta de administradora; contraseña larga y única), los datos
de email de Brevo (ver el paso 3), `MODERATION_EMAIL` (la dirección del proyecto: ahí llega el aviso de cada testimonio nuevo), `ADZUNA_APP_ID` y `ADZUNA_APP_KEY` (ofertas de empleo de España: las mismas claves que tienes en tu Mac), los de Google si los tienes y `VITE_LEGAL_OWNER_NAME`, `VITE_LEGAL_EMAIL` y
`VITE_LEGAL_HOSTING_PROVIDER` (por ejemplo, `Hetzner Online GmbH (Alemania)`). No toques `POSTGRES_PASSWORD` ni
`JWT_SECRET`: ya están generados. Guarda con `Ctrl+O`, `Intro`, y sal con `Ctrl+X`.

### 3. Arrancar

```bash
/opt/primercommit/nexus-backend/deploy/actualizar.sh
```

La primera vez tarda unos minutos (compila todo). Cuando termine escribe «Listo: https://tu-dominio.es».

### 4. Comprobar antes de mandar el enlace

1. Abre la web: candado HTTPS y aviso «Beta» bajo la cabecera.
2. Entra con tu cuenta de administradora y mira el panel.
3. Crea una cuenta de prueba con otro correo: llega el email de verificación y se puede publicar en el foro.
4. Prueba «Continuar con Google», si lo has configurado.
5. Mira el aviso legal y la privacidad: tu nombre y el email, sin «pendiente de configurar».
6. Publica tú lo primero (para que nadie llegue a una web vacía): una discusión «Feedback de la beta» en el foro,
   alguna pregunta y tu testimonio.
7. **Hilo del glosario:** hecho. «¡Añádela a la lista!» lleva a `/foro/1-que-palabra-le-falta-al-glosario-dejala-aqui`
   (`GLOSSARY_THREAD` en `nexus-frontend/src/shared/config/community.js`).

Y ya puedes mandar el enlace.

---

## Día a día

Todo desde el servidor (`ssh root@IP`), en la carpeta `deploy`:

```bash
cd /opt/primercommit/nexus-backend/deploy
```

- **Publicar cambios** subidos a `dev`: `./actualizar.sh` (hace copia antes).
- **Ver qué pasa** si algo falla: `docker compose logs --tail 100 backend` (o `frontend`, `caddy`, `db`).
- **Estado de las piezas**: `docker compose ps`.
- **Copia de seguridad manual**: `./copia-seguridad.sh`. Las copias están en `/opt/primercommit/copias`.
- **Descargar las copias a tu Mac** (desde el Terminal del Mac, no del servidor):

  ```bash
  scp -r root@203.0.113.10:/opt/primercommit/copias ~/Desktop/copias-primercommit
  ```

  Guarda solo las recientes y borra las antiguas: si alguien elimina su cuenta, sus datos no deben seguir en copias
  viejas (la política de privacidad promete un máximo de 14 días, como las del servidor).

- **Restaurar una copia** (sustituye lo que haya en la base de datos):

  ```bash
  gunzip -c /opt/primercommit/copias/db-FECHA.sql.gz | docker compose exec -T db psql -U nexus -d nexus
  ```

- **Terminar la beta**: en `.env`, `VITE_BETA=false`, y `./actualizar.sh`. Los datos se quedan tal cual.

## Ver la base de datos con pgAdmin

La base de datos solo escucha en el propio servidor (`127.0.0.1:5432`), nunca en internet. Para verla desde tu ordenador,
pgAdmin entra por un túnel SSH con tu misma clave:

- **General → Name:** Primer Commit
- **Connection:** host `127.0.0.1`, puerto `5432`, base de datos `nexus`, usuario `nexus` y la contraseña
  `POSTGRES_PASSWORD` del `.env` del servidor.
- **SSH Tunnel:** activado, host la IP del servidor, puerto `22`, usuario `root`, autenticación con
  *Identity file* (tu clave privada SSH) y su frase de paso.

Mirar no tiene riesgo; cualquier cambio (`update`, `delete`…) se aplica al momento en la web real.

## Seguridad

- `.env` lleva contraseñas: solo vive en el servidor (permisos 600) y está en `.gitignore`.
- Solo están abiertos SSH, http y https; la base de datos y el backend no son accesibles desde fuera.
- En producción no existe ninguna cuenta de prueba: la única administradora es la de `ADMIN_EMAIL`.
- Si cambias `ADMIN_PASSWORD` después del primer arranque no pasa nada: la cuenta ya existe y su contraseña se cambia
  desde la web.
