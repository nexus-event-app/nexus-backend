-- Idioma de las noticias y de sus fuentes: «es» (castellano) o «en» (inglés). Las noticias en inglés se marcan en
-- la web, se pueden filtrar y la portada da prioridad a las que están en castellano.
alter table import_sources add column language varchar(5) default 'es' not null;
alter table news add column language varchar(5) default 'es' not null;

-- DEV Community publica en inglés: la fuente y lo que ya se importó de ella.
update import_sources set language = 'en' where url like 'https://dev.to/%';
update news set language = 'en'
 where external_id is not null
   and source in (select name from import_sources where language = 'en');

-- Fuentes en castellano que publican a menudo. Solo en bases de datos que ya tenían fuentes (en una vacía las crea
-- la aplicación al arrancar) y si no existen ya con ese nombre.
insert into import_sources (name, type, url, news_category, auto_publish, active, remote_only, keywords,
                            exclude_keywords, use_source_images, language, interval_minutes, created_at, updated_at)
select 'Paradigma Digital', 'RSS_NEWS', 'https://www.paradigmadigital.com/feed/', 'DESARROLLO', true, true, false,
       null, null, true, 'es', 180, current_timestamp, current_timestamp
 where exists (select 1 from import_sources)
   and not exists (select 1 from import_sources where name = 'Paradigma Digital');

insert into import_sources (name, type, url, news_category, auto_publish, active, remote_only, keywords,
                            exclude_keywords, use_source_images, language, interval_minutes, created_at, updated_at)
select 'WeLiveSecurity en español', 'RSS_NEWS', 'https://www.welivesecurity.com/es/rss/feed/', 'GENERAL', true, true,
       false, null, null, true, 'es', 180, current_timestamp, current_timestamp
 where exists (select 1 from import_sources)
   and not exists (select 1 from import_sources where name = 'WeLiveSecurity en español');

insert into import_sources (name, type, url, news_category, auto_publish, active, remote_only, keywords,
                            exclude_keywords, use_source_images, language, interval_minutes, created_at, updated_at)
select 'Hipertextual', 'RSS_NEWS', 'https://hipertextual.com/feed', 'GENERAL', true, true, false,
       'programación, programador, desarrollador, desarrolladora, software, código, código abierto, open source, GitHub, Python, JavaScript, TypeScript, Java, Rust, Linux, Docker, DevOps, API, framework, algoritmo, base de datos, bases de datos, SQL, PostgreSQL, MongoDB, ciberseguridad, vulnerabilidad, modelo de lenguaje, LLM, machine learning, aprendizaje automático, agentes de IA, IA generativa, nube, cloud, developer, ingeniero de software',
       'contenido para adultos, porno, pornografía, sexual, apuestas, casino, préstamo, préstamos, hipoteca, criptomoneda, criptomonedas, bitcoin, NFT, aerolínea, Palantir, Bezos, Elon Musk, Trump, elecciones, partido político, sindicatos, atraco, robo, chollo, chollos, oferta, ofertas, descuento, rebaja, rebajas, Black Friday, Prime Day, precio mínimo, cupón',
       true, 'es', 180, current_timestamp, current_timestamp
 where exists (select 1 from import_sources)
   and not exists (select 1 from import_sources where name = 'Hipertextual');
