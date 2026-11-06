-- Fecha en que cada usuario aceptó el aviso legal, las condiciones de uso y la política de privacidad
-- al registrarse. Queda vacía para las cuentas anteriores y para el administrador inicial.
alter table users add column terms_accepted_at timestamp(6);
