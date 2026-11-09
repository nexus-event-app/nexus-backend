-- Perfil público de cada usuario: titular, presentación, tecnologías, enlaces profesionales y foto.
-- Todo es opcional: el perfil muestra solo lo que la persona haya rellenado.
alter table users add column headline varchar(80);
alter table users add column bio varchar(300);
alter table users add column technologies varchar(400);
alter table users add column github_url varchar(200);
alter table users add column linkedin_url varchar(200);
alter table users add column avatar_url varchar(200);
