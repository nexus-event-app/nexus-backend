-- Palabras excluidas por fuente: lo que contenga alguna no se importa.
alter table import_sources add column exclude_keywords varchar(500);

-- Aplica a las bases de datos que ya existían los cambios en las fuentes recomendadas. En una base de datos vacía
-- no hace nada: las fuentes las crea la aplicación al arrancar con los valores actuales.

-- Genbeta: filtro más estricto, solo si conserva el filtro antiguo (no lo ha personalizado un admin).
update import_sources
   set keywords = 'programación, programador, desarrollador, desarrolladora, software, código, código abierto, open source, GitHub, Python, JavaScript, TypeScript, Java, Rust, Linux, Docker, DevOps, API, framework, algoritmo, base de datos, bases de datos, SQL, PostgreSQL, MongoDB, ciberseguridad, vulnerabilidad, modelo de lenguaje, LLM, machine learning, aprendizaje automático, agentes de IA, IA generativa, nube, cloud, developer, ingeniero de software'
 where name = 'Genbeta'
   and keywords = 'programación, programador, programadora, desarrollador, desarrolladora, desarrollo, software, código, open source, código abierto, GitHub, Python, JavaScript, Java, Linux, inteligencia artificial, IA, ChatGPT, developer, ciberseguridad, empleo, Microsoft, Google, API, nube';

update import_sources
   set exclude_keywords = 'contenido para adultos, porno, pornografía, sexual, apuestas, casino, préstamo, préstamos, hipoteca, criptomoneda, criptomonedas, bitcoin, NFT, aerolínea, Palantir, Bezos, Elon Musk, Trump, elecciones, partido político, sindicatos, atraco, robo'
 where name = 'Genbeta' and exclude_keywords is null;

-- DEV Community publica en inglés y mezcla temas: queda apagada por defecto.
update import_sources set active = false
 where name = 'DEV Community · #beginners' and url = 'https://dev.to/feed/tag/beginners';

-- Jobicy (empleo en remoto en Europa), si no existe ya.
insert into import_sources (name, type, url, news_category, auto_publish, active, remote_only, keywords,
                            exclude_keywords, interval_minutes, created_at, updated_at)
select 'Jobicy · Europa', 'JOBICY_JOBS', 'https://jobicy.com/api/v2/remote-jobs?count=100&geo=europe&industry=dev',
       null, true, true, false, null, null, 360, current_timestamp, current_timestamp
 where exists (select 1 from import_sources)
   and not exists (select 1 from import_sources where name = 'Jobicy · Europa');
