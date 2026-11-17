-- Cuentas eliminadas por su dueña o dueño: la fila se conserva anonimizada (sin email real, contraseña ni datos de
-- perfil) para que las discusiones y respuestas del foro sigan en pie, firmadas como «Cuenta eliminada».
alter table users add column deleted_at timestamp;
