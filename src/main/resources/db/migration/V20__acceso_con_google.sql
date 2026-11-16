-- «Continuar con Google»: la cuenta guarda el identificador de Google (sub, estable aunque cambie el email) y puede no
-- tener contraseña (quien solo entra con Google). Si más adelante quiere una, la pone con «¿Olvidaste tu contraseña?».
alter table users alter column password drop not null;
alter table users add column google_subject varchar(255);
create unique index uk_users_google_subject on users (google_subject);
