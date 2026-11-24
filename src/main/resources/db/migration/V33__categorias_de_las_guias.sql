alter table guides add column category varchar(30) not null default 'PRIMEROS_PASOS';

update guides set category = 'EMPLEO' where slug = 'como-preparar-tu-primera-entrevista-tecnica';
