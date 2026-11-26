alter table guides add column keywords varchar(300);
alter table guides add column keywords_ca varchar(300);
alter table guides add column keywords_en varchar(300);

update guides
set keywords    = 'Después del bootcamp, Portfolio, GitHub, LinkedIn, Primer empleo, Networking, Proyectos, Constancia, Comunidad, Aprender',
    keywords_ca = 'Després del bootcamp, Portfolio, GitHub, LinkedIn, Primera feina, Networking, Projectes, Constància, Comunitat, Aprendre',
    keywords_en = 'After the bootcamp, Portfolio, GitHub, LinkedIn, First job, Networking, Projects, Consistency, Community, Learning'
where slug = 'que-hacer-despues-de-un-bootcamp';

update guides
set keywords    = 'Entrevista técnica, Prueba técnica, Fundamentos, Proyectos, Live coding, Preguntas, Nervios, Feedback, Empresa, Practicar',
    keywords_ca = 'Entrevista tècnica, Prova tècnica, Fonaments, Projectes, Live coding, Preguntes, Nervis, Feedback, Empresa, Practicar',
    keywords_en = 'Technical interview, Coding test, Fundamentals, Projects, Live coding, Questions, Nerves, Feedback, Company, Practice'
where slug = 'como-preparar-tu-primera-entrevista-tecnica';

update guides
set keywords    = 'Git y GitHub, Commit, Repositorio, Rama, Push, Pull request, Merge, Terminal, README, Primer commit',
    keywords_ca = 'Git i GitHub, Commit, Repositori, Branca, Push, Pull request, Merge, Terminal, README, Primer commit',
    keywords_en = 'Git and GitHub, Commit, Repository, Branch, Push, Pull request, Merge, Terminal, README, First commit'
where slug = 'git-y-github-para-principiantes';

update guides
set keywords    = 'Meetup tech, Comunidad, Networking, Charlas, Presentarte, Contactos, LinkedIn, Eventos online, Compartir, Aprender',
    keywords_ca = 'Meetup tech, Comunitat, Networking, Xerrades, Presentar-te, Contactes, LinkedIn, Esdeveniments en línia, Compartir, Aprendre',
    keywords_en = 'Tech meetup, Community, Networking, Talks, Introduce yourself, Contacts, LinkedIn, Online events, Sharing, Learning'
where slug = 'tu-primer-meetup-tech';

update guides
set keywords    = 'Síndrome del impostor, Autoestima, Logros, Feedback, Mentoría, Perfeccionismo, Comparación, Primer empleo, Aprender, Comunidad',
    keywords_ca = 'Síndrome de l’impostor, Autoestima, Èxits, Feedback, Mentoria, Perfeccionisme, Comparació, Primera feina, Aprendre, Comunitat',
    keywords_en = 'Impostor syndrome, Self-esteem, Wins, Feedback, Mentoring, Perfectionism, Comparison, First job, Learning, Community'
where slug = 'sindrome-del-impostor';
