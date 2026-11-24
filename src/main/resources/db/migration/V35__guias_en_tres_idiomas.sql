alter table guides add column title_ca varchar(200);
alter table guides add column title_en varchar(200);
alter table guides add column description_ca varchar(300);
alter table guides add column description_en varchar(300);
alter table guides add column content_ca text;
alter table guides add column content_en text;

update guides
set title_ca = 'Què fer després d’un bootcamp de programació: guia pas a pas',
    description_ca = 'Has acabat el bootcamp, i ara què? Passos clars per continuar aprenent, preparar el teu portfolio i aconseguir la primera feina tech.',
    content_ca = 'Durant setmanes has tingut classes, companyes i companys, horaris i projectes amb data d’entrega. I, de sobte, l’últim dia, et trobes davant de l’ordinador amb una pregunta enorme: **i ara què faig?**

És completament normal sentir-se així. Gairebé tothom passa per aquest moment de vertigen en acabar. La bona notícia és que no ho has de tenir tot resolt demà: només necessites un pla i fer passos petits cada setmana. En aquesta guia t’expliquem quins.

## 1. Pren-te uns dies i fes balanç

Abans de llançar-te a enviar currículums, atura’t un moment. Repassa el que has après i pregunta’t:

- Quina part he gaudit més: el frontend, el backend, les bases de dades…?
- Quin projecte del bootcamp em sento capaç d’explicar amb orgull?
- Quins conceptes encara em costen?

Apunta-ho. Aquesta llista serà la teva brúixola per decidir què estudiar i quin tipus d’ofertes buscar.

## 2. No deixis de programar (encara que sigui poc)

El risc més gran després d’un bootcamp és deixar de practicar. El que no fas servir s’oblida ràpid. No cal programar vuit hores al dia: **és millor una hora diària que un dia sencer a la setmana**.

Algunes idees:

- Reprèn un projecte del bootcamp i millora’l: afegeix-hi proves, millora el disseny o puja’l a internet.
- Fes petits reptes de programació per mantenir la lògica en forma.
- Segueix una de les nostres [rutes d’aprenentatge](/rutas) per avançar amb ordre i sense perdre’t.

## 3. Construeix un portfolio que parli per tu

Quan no tens experiència laboral, el portfolio és la teva carta de presentació. Val més **un projecte acabat i ben explicat que cinc a mitges**.

Un bon projecte de portfolio:

- **Resol un problema real**, encara que sigui petit o teu.
- **Es pot provar sense instal·lar res**: està publicat a internet.
- **Té un bon README**: què fa, amb quines tecnologies, captures i què vas aprendre.
- **El seu codi és a GitHub**, ordenat i amb commits que s’entenen.

Si no saps quin projecte fer, pregunta al [fòrum](/foro): segur que algú de la comunitat et dona idees.

## 4. Cuida el teu GitHub i el teu LinkedIn

Moltes empreses et buscaran abans de trucar-te. Fes que el que trobin jugui a favor teu:

- **GitHub:** fixa els teus millors projectes, afegeix-hi una foto i una breu presentació.
- **LinkedIn:** posa un titular clar (per exemple, «Desenvolupadora web júnior | React · Java»), explica els teus projectes i esmenta el bootcamp.
- **Comparteix el que aprens:** un post explicant un projecte o un problema que vas resoldre demostra més que qualsevol currículum.

## 5. Surt de casa: ves a esdeveniments i coneix gent

Moltes oportunitats arriben per persones, no per portals de feina. Anar a **meetups, xerrades i hackathons** t’ajuda a:

- Aprendre de gent que ja treballa al sector.
- Practicar a parlar de tecnologia, una cosa que després agrairàs a les entrevistes.
- Assabentar-te d’ofertes abans que es publiquin.

Al principi fa vergonya, però ningú espera que ho sàpigues tot. A l’[agenda d’esdeveniments](/eventos) tens meetups, xerrades i hackathons, també de comunitats de dones en tech.

## 6. Busca feina amb estratègia, no a cegues

Enviar cent currículums iguals rarament funciona. Millor:

- **Filtra bé:** busca ofertes de pràctiques i júnior, i no t’espantis si no compleixes el 100 % dels requisits. Si en compleixes bona part, presenta-t’hi.
- **Adapta el CV** a cada oferta: destaca el que encaixa.
- **Porta un registre** d’on t’has presentat i en quin punt és cada procés.
- **Prepara les entrevistes tècniques:** repassa els fonaments i practica explicar els teus projectes en veu alta. T’ho expliquem pas a pas a [com preparar la teva primera entrevista tècnica](/guias/como-preparar-tu-primera-entrevista-tecnica).

A [Feina](/empleo) reunim pràctiques i ofertes júnior, i les pots desar per revisar-les amb calma.

## 7. Aprèn a conviure amb els «no»

Rebràs rebuigs, i processos en què ningú et respon. **No vol dir que no vals**: vol dir que estàs en el camí. Cada entrevista, fins i tot les que surten malament, et prepara per a la següent.

Celebra els petits avenços: acabar un projecte, passar una primera trucada, entendre per fi aquell concepte que se’t resistia.

## 8. No ho facis sola ni sol

El més difícil d’acabar un bootcamp no és la part tècnica: és sentir que de sobte estàs sola o sol. Envoltar-te de gent que és al mateix punt que tu ho canvia tot: compartiu dubtes, recursos, ofertes… i ànims.

Per això neix [Primer Commit](/registro): una comunitat gratuïta on trobar esdeveniments, ofertes júnior i un fòrum per resoldre dubtes entre totes i tots.

## En resum

1. Fes balanç del que has après i del que t’agrada.
2. Continua programant una mica cada dia.
3. Construeix un portfolio amb un projecte acabat i ben explicat.
4. Cuida el teu GitHub i el teu LinkedIn.
5. Ves a esdeveniments i coneix gent del sector.
6. Busca feina amb estratègia.
7. No et desanimis amb els «no».
8. Envolta’t d’una comunitat.

El teu primer commit no ha de ser perfecte. Només ha d’existir. 💜',
    title_en = 'What to do after a coding bootcamp: a step-by-step guide',
    description_en = 'You have finished your bootcamp, now what? Clear steps to keep learning, build your portfolio and land your first tech job.',
    content_en = 'For weeks you have had classes, classmates, timetables and projects with deadlines. And suddenly, on the last day, you find yourself in front of your computer with one huge question: **what do I do now?**

It is completely normal to feel this way. Almost everyone goes through that moment of vertigo when they finish. The good news is that you don’t need to have it all figured out tomorrow: you just need a plan and small steps every week. In this guide we tell you which ones.

## 1. Take a few days and look back

Before you rush into sending CVs, stop for a moment. Go over what you have learned and ask yourself:

- Which part did I enjoy most: frontend, backend, databases…?
- Which bootcamp project would I be proud to explain?
- Which concepts do I still find hard?

Write it down. That list will be your compass to decide what to study and which kind of jobs to look for.

## 2. Keep coding (even a little)

The biggest risk after a bootcamp is to stop practising. What you don’t use, you forget quickly. You don’t need to code eight hours a day: **one hour a day is better than a whole day once a week**.

Some ideas:

- Pick up a bootcamp project and improve it: add tests, polish the design or put it online.
- Do small coding challenges to keep your logic sharp.
- Follow one of our [learning paths](/rutas) to move forward step by step without getting lost.

## 3. Build a portfolio that speaks for you

When you have no work experience, your portfolio is your calling card. **One finished, well-explained project is worth more than five half-done ones**.

A good portfolio project:

- **Solves a real problem**, even a small or personal one.
- **Can be tried without installing anything**: it is published online.
- **Has a good README**: what it does, which technologies, screenshots and what you learned.
- **Has its code on GitHub**, tidy and with commits that make sense.

If you don’t know which project to build, ask in the [forum](/foro): someone in the community will surely give you ideas.

## 4. Take care of your GitHub and LinkedIn

Many companies will look you up before calling you. Make sure what they find works in your favour:

- **GitHub:** pin your best projects, add a photo and a short introduction.
- **LinkedIn:** use a clear headline (for example, “Junior web developer | React · Java”), describe your projects and mention the bootcamp.
- **Share what you learn:** a post about a project or a problem you solved shows more than any CV.

## 5. Get out there: go to events and meet people

Many opportunities come through people, not job boards. Going to **meetups, talks and hackathons** helps you:

- Learn from people who already work in the industry.
- Practise talking about tech, something you will appreciate in interviews.
- Hear about jobs before they are published.

It feels awkward at first, but nobody expects you to know everything. In the [events calendar](/eventos) you will find meetups, talks and hackathons, including women in tech communities.

## 6. Look for a job with a strategy, not blindly

Sending a hundred identical CVs rarely works. Instead:

- **Filter well:** look for internships and junior roles, and don’t panic if you don’t meet 100% of the requirements. If you meet a good part of them, apply.
- **Tailor your CV** to each job: highlight what fits.
- **Keep track** of where you have applied and where each process stands.
- **Prepare for technical interviews:** review the fundamentals and practise explaining your projects out loud. We walk you through it in [how to prepare for your first technical interview](/guias/como-preparar-tu-primera-entrevista-tecnica).

In [Jobs](/empleo) we gather internships and junior roles, and you can save them to review calmly.

## 7. Learn to live with the “no”s

You will get rejections, and processes where nobody replies. **It doesn’t mean you are not good enough**: it means you are on your way. Every interview, even the ones that go badly, prepares you for the next.

Celebrate small wins: finishing a project, getting through a first call, finally understanding that concept that was resisting you.

## 8. Don’t do it alone

The hardest part of finishing a bootcamp isn’t the technical side: it’s suddenly feeling alone. Surrounding yourself with people at the same stage changes everything: you share questions, resources, jobs… and encouragement.

That is why [Primer Commit](/registro) exists: a free community where you can find events, junior jobs and a forum to work through questions together.

## Summary

1. Look back at what you have learned and what you enjoy.
2. Keep coding a little every day.
3. Build a portfolio with one finished, well-explained project.
4. Take care of your GitHub and LinkedIn.
5. Go to events and meet people in the industry.
6. Look for a job with a strategy.
7. Don’t get discouraged by the “no”s.
8. Surround yourself with a community.

Your first commit doesn’t have to be perfect. It just has to exist. 💜',
    updated_at = current_timestamp
where slug = 'que-hacer-despues-de-un-bootcamp';

update guides
set title_ca = 'Com preparar la teva primera entrevista tècnica: guia pas a pas',
    description_ca = 'La teva primera entrevista tècnica s’acosta. Quines fases té, què solen preguntar als perfils júnior i com preparar-la per arribar-hi amb calma.',
    content_ca = 'T’han trucat per a la teva primera entrevista tècnica. Primer, l’alegria: **algú ha vist el teu perfil i et vol conèixer**. Després, els nervis: què em preguntaran?, hauré de programar davant d’algú?, i si em quedo en blanc?

És normal sentir tot això. La bona notícia és que una entrevista tècnica es pot preparar, i molt. En aquesta guia t’expliquem com solen ser i què fer abans, durant i després.

## 1. Entén com és el procés

Cada empresa té la seva manera de fer-ho, però la majoria de processos per a perfils júnior segueixen un camí semblant:

- **Trucada inicial:** una conversa curta amb algú de selecció per conèixer-te, la teva motivació i les teves condicions.
- **Prova tècnica:** un exercici per fer a casa, un test en línia o un petit repte en directe.
- **Entrevista tècnica:** parles amb algú de l’equip sobre la prova, els teus projectes i conceptes bàsics.
- **Entrevista final:** de vegades amb la persona responsable de l’equip, per veure si encaixeu.

Quan et truquin, pregunta sense problema quantes fases té el procés i quin tipus de prova faràs. És una pregunta habitual i t’ajudarà a preparar-te millor.

## 2. Investiga l’empresa i l’oferta

Abans de l’entrevista, dedica una estona a conèixer l’empresa:

- A què es dedica i quins productes té?
- Quines tecnologies apareixen a l’oferta?
- Té blog tècnic, perfil a GitHub o xerrades en esdeveniments?

Apunta les tecnologies de l’oferta que coneixes i les que no. Les que coneixes, repassa-les; les que no, mira almenys què són. Si alguna paraula no la coneixes, busca-la al nostre [glossari](/glosario).

## 3. Repassa els fonaments

A un perfil júnior no se li demana saber-ho tot, però sí tenir clares les bases. Segons el teu perfil, repassa:

- **Fonaments del llenguatge:** variables, tipus, funcions, bucles, arrays i objectes.
- **Programació orientada a objectes**, si treballes amb Java o similars: classes, herència i interfícies.
- **Web:** què és HTTP, què és una API REST i què passa quan escrius una adreça al navegador.
- **Bases de dades:** consultes SQL bàsiques i per a què serveix cada tipus de base de dades.
- **Git:** commit, branca, merge i pull request.

No cal memoritzar definicions: **és molt millor entendre els conceptes i saber-los explicar amb les teves paraules**.

## 4. Prepara els teus projectes per explicar-los

Segur que et demanaran que parlis d’algun projecte. Tria’n un o dos i prepara’t per explicar:

- **Quin problema resol** i per a qui.
- **Amb quines tecnologies el vas fer** i per què les vas triar.
- **Què va ser el més difícil** i com ho vas resoldre.
- **Què milloraries** si tinguessis més temps.

Practica explicant-ho en veu alta, en dos o tres minuts. Explicar els problemes que vas trobar i com els vas superar diu molt de com treballes.

## 5. Afronta la prova tècnica amb mètode

Si la prova és per fer a casa:

- **Llegeix bé l’enunciat** i pregunta si alguna cosa no queda clara.
- **Lliura alguna cosa que funcioni** abans d’afegir-hi extres.
- **Cuida el codi:** noms clars, ordenat i amb un README que expliqui com provar-lo.
- **Respecta el termini.** Si necessites més temps, avisa amb antelació.

Si la prova és en directe:

- **Pensa en veu alta:** explica què penses fer i per què. Importa tant com raones com el resultat.
- **Comença per una solució senzilla** i millora-la després.
- **Si t’encalles, digues-ho:** «ara mateix no recordo com es fa això, però ho buscaria així…» és una resposta perfectament vàlida.

## 6. Prepara també la part personal

Les entrevistes no són només tècniques. Prepara respostes sinceres per a preguntes com:

- «Parla’m de tu.»
- «Per què vols treballar en tecnologia?»
- «Per què t’interessa aquesta empresa?»
- «Explica’m alguna vegada que t’equivoquessis i què en vas aprendre.»

Si véns d’un altre sector, **explica-ho com un valor**: el que hi vas aprendre (tracte amb clients, organització, treball en equip…) també suma.

## 7. Porta les teves pròpies preguntes

Al final gairebé sempre et preguntaran: «Tens alguna pregunta?». Tenir-ne alguna de preparada demostra interès. Per exemple:

- Com és un dia normal a l’equip?
- Quines tecnologies feu servir en el dia a dia?
- Hi haurà algú que m’acompanyi en començar?
- Què espereu d’una persona júnior els primers mesos?

## 8. Cuida els detalls del dia

- **Si és en línia:** prova la càmera, el micròfon i la connexió abans. Busca un lloc tranquil i amb bona llum.
- **Si és presencial:** mira com arribar-hi i surt amb temps de sobres.
- **Tingues a mà** el teu CV, l’oferta i unes notes amb el que vols explicar.
- **Respira.** A l’altra banda hi ha persones que també van tenir la seva primera entrevista.

## 9. Després de l’entrevista

- **Envia un missatge breu d’agraïment** si tens el contacte: suma més del que sembla.
- **Apunta què et van preguntar** i què et va costar respondre. És la teva llista de repàs per a la següent.
- **Si no surt, demana opinió.** No sempre te la donaran, però quan ho fan és molt valuosa.

I recorda: cada entrevista, surti com surti, et deixa més ben preparada o preparat per a la següent.

## 10. Practica amb la comunitat

La millor manera de perdre els nervis és practicar. Fes simulacres amb algú de confiança, explica els teus projectes en veu alta i comparteix la teva experiència al [fòrum](/foro): segur que algú ha passat per un procés semblant i et pot donar pistes.

Als [esdeveniments](/eventos) també pots conèixer gent que ja treballa al sector i preguntar-li com són les entrevistes a la seva empresa.

## En resum

1. Entén les fases del procés.
2. Investiga l’empresa i l’oferta.
3. Repassa els fonaments i entén-los.
4. Prepara un o dos projectes per explicar-los.
5. A la prova tècnica, ves amb mètode i pensa en veu alta.
6. Prepara també les preguntes personals.
7. Porta les teves pròpies preguntes.
8. Cuida els detalls del dia.
9. Aprèn de cada entrevista.
10. Practica i recolza’t en la comunitat.

La teva primera entrevista no ha de ser perfecta. Només ha de ser la primera. 💜',
    title_en = 'How to prepare for your first technical interview: a step-by-step guide',
    description_en = 'Your first technical interview is coming up. Its stages, what juniors are usually asked and how to prepare so you arrive calm.',
    content_en = 'You have been called for your first technical interview. First comes the joy: **someone has seen your profile and wants to meet you**. Then the nerves: what will they ask?, will I have to code in front of someone?, what if my mind goes blank?

It is normal to feel all of that. The good news is that a technical interview can be prepared for, a lot. In this guide we explain what they are usually like and what to do before, during and after.

## 1. Understand how the process works

Every company does it its own way, but most processes for junior roles follow a similar path:

- **First call:** a short conversation with someone from recruitment to get to know you, your motivation and your conditions.
- **Technical test:** a take-home exercise, an online test or a small live challenge.
- **Technical interview:** you talk with someone from the team about your test, your projects and basic concepts.
- **Final interview:** sometimes with the team lead, to see if you are a good fit.

When they call you, feel free to ask how many stages the process has and what kind of test you will do. It is a common question and it will help you prepare better.

## 2. Research the company and the role

Before the interview, spend some time getting to know the company:

- What does it do and what products does it have?
- Which technologies appear in the job description?
- Does it have a tech blog, a GitHub profile or talks at events?

Write down the technologies in the job description that you know and those you don’t. Review the ones you know; for the others, at least find out what they are. If there is a word you don’t know, look it up in our [glossary](/glosario).

## 3. Review the fundamentals

Nobody expects a junior to know everything, but you should be clear on the basics. Depending on your profile, review:

- **Language fundamentals:** variables, types, functions, loops, arrays and objects.
- **Object-oriented programming**, if you work with Java or similar: classes, inheritance and interfaces.
- **Web:** what HTTP is, what a REST API is and what happens when you type an address into the browser.
- **Databases:** basic SQL queries and what each type of database is for.
- **Git:** commit, branch, merge and pull request.

You don’t need to memorise definitions: **it is much better to understand the concepts and be able to explain them in your own words**.

## 4. Get ready to talk about your projects

You will almost certainly be asked about a project. Pick one or two and be ready to explain:

- **What problem it solves** and for whom.
- **Which technologies you used** and why you chose them.
- **What was hardest** and how you solved it.
- **What you would improve** with more time.

Practise telling it out loud in two or three minutes. Talking about the problems you ran into and how you overcame them says a lot about how you work.

## 5. Approach the technical test with a method

If it is a take-home test:

- **Read the brief carefully** and ask if something is unclear.
- **Deliver something that works** before adding extras.
- **Take care of the code:** clear names, tidy, and a README explaining how to run it.
- **Respect the deadline.** If you need more time, let them know in advance.

If it is a live test:

- **Think out loud:** say what you plan to do and why. How you reason matters as much as the result.
- **Start with a simple solution** and improve it afterwards.
- **If you get stuck, say so:** “right now I don’t remember how to do this, but I would look it up like this…” is a perfectly valid answer.

## 6. Prepare the personal side too

Interviews are not only technical. Prepare honest answers to questions such as:

- “Tell me about yourself.”
- “Why do you want to work in tech?”
- “Why are you interested in this company?”
- “Tell me about a time you made a mistake and what you learned.”

If you come from another field, **present it as an asset**: what you learned there (customer service, organisation, teamwork…) counts too.

## 7. Bring your own questions

At the end you will almost always be asked: “Do you have any questions?”. Having some ready shows interest. For example:

- What is a normal day like in the team?
- Which technologies do you use day to day?
- Will someone support me when I start?
- What do you expect from a junior in the first months?

## 8. Take care of the details on the day

- **If it is online:** test your camera, microphone and connection beforehand. Find a quiet, well-lit place.
- **If it is in person:** check how to get there and leave with plenty of time.
- **Have at hand** your CV, the job description and a few notes on what you want to say.
- **Breathe.** On the other side there are people who also had their first interview once.

## 9. After the interview

- **Send a short thank-you message** if you have their contact: it counts more than it seems.
- **Write down what they asked** and what you found hard to answer. It is your revision list for the next one.
- **If it doesn’t work out, ask for feedback.** They won’t always give it, but when they do it is very valuable.

And remember: every interview, however it goes, leaves you better prepared for the next.

## 10. Practise with the community

The best way to lose your nerves is to practise. Do mock interviews with someone you trust, explain your projects out loud and share your experience in the [forum](/foro): someone has surely been through a similar process and can give you tips.

At [events](/eventos) you can also meet people who already work in the industry and ask them what interviews are like at their company.

## Summary

1. Understand the stages of the process.
2. Research the company and the role.
3. Review the fundamentals and understand them.
4. Prepare one or two projects to talk about.
5. In the technical test, use a method and think out loud.
6. Prepare the personal questions too.
7. Bring your own questions.
8. Take care of the details on the day.
9. Learn from every interview.
10. Practise and lean on the community.

Your first interview doesn’t have to be perfect. It just has to be the first one. 💜',
    updated_at = current_timestamp
where slug = 'como-preparar-tu-primera-entrevista-tecnica';

insert into guides (slug, category, title, description, content, title_ca, description_ca, content_ca,
                    title_en, description_en, content_en, published, published_at, created_at, updated_at)
values ('git-y-github-para-principiantes', 'HERRAMIENTAS',
        'Git y GitHub para principiantes: tu primer commit paso a paso', 'Qué son Git y GitHub, los comandos imprescindibles y cómo hacer y subir tu primer commit, explicado paso a paso para quien empieza.', 'Git aparece en casi todas las ofertas de empleo tech, y es normal que al principio cueste: ramas, commits, conflictos… parece un idioma nuevo. La buena noticia es que para empezar solo necesitas unos pocos comandos y entender la idea que hay detrás.

En esta guía te explicamos qué son Git y GitHub, los comandos imprescindibles y cómo hacer tu primer commit paso a paso.

## 1. Qué es Git y qué es GitHub

Aunque se nombran juntos, no son lo mismo:

- **Git** es una herramienta que guarda el historial de cambios de tu proyecto. Cada vez que guardas un punto importante, haces un **commit**: una foto del código en ese momento, con un mensaje que explica qué cambiaste.
- **GitHub** es una web donde subes esos proyectos para tener una copia en internet, compartirlos y trabajar con otras personas.

Dicho de otra forma: Git trabaja en tu ordenador y GitHub es el sitio donde lo guardas y lo enseñas. Tu perfil de GitHub, además, es parte de tu carta de presentación.

## 2. Prepara tu ordenador

Antes de empezar:

- **Instala Git** desde su web oficial (o con el gestor de paquetes de tu sistema).
- **Crea una cuenta en GitHub** con un nombre de usuario profesional.
- **Dile a Git quién eres**, con tu nombre y el email de tu cuenta: `git config --global user.name "Tu nombre"` y `git config --global user.email "tu@email.com"`.

Solo tienes que hacerlo una vez.

## 3. Los comandos imprescindibles

Con estos comandos puedes hacer casi todo al principio:

- `git init`: convierte una carpeta en un proyecto con Git.
- `git status`: te dice en qué rama estás y qué archivos has cambiado. Úsalo siempre que tengas dudas.
- `git add .`: prepara todos los cambios para guardarlos.
- `git commit -m "mensaje"`: guarda esos cambios con un mensaje.
- `git push`: sube tus commits a GitHub.
- `git pull`: baja los últimos cambios de GitHub.

No hace falta aprenderlos de memoria: con usarlos a diario se quedan solos.

## 4. Tu primer commit, paso a paso

1. Crea una carpeta para tu proyecto y entra en ella desde la terminal.
2. Escribe `git init` para empezar a usar Git en esa carpeta.
3. Crea un archivo, por ejemplo un `README.md` que diga de qué va tu proyecto.
4. Comprueba los cambios con `git status`.
5. Prepáralos con `git add .`.
6. Guárdalos con `git commit -m "Primer commit"`.

¡Enhorabuena! Ya tienes tu primer commit. 🎉

## 5. Súbelo a GitHub

1. En GitHub, crea un repositorio nuevo, vacío y con el mismo nombre que tu proyecto.
2. GitHub te mostrará la dirección del repositorio. Conéctalo con `git remote add origin` seguido de esa dirección.
3. Sube tu trabajo con `git push -u origin main`.

A partir de ahí, cada vez que hagas commits nuevos, bastará con `git push`.

## 6. Escribe buenos mensajes de commit

Un buen mensaje ayuda a entender el historial, a ti y a quien revise tu código (también en una entrevista):

- **Explica qué cambia**, no cómo te sentías: «Añade el formulario de contacto» mejor que «cambios».
- **Sé breve:** una línea suele bastar.
- **Haz commits pequeños**, de un cambio cada uno. Es más fácil revisarlos y deshacerlos.

## 7. Ramas: prueba cosas sin romper nada

Una **rama** es una línea de trabajo paralela. Te permite probar cosas sin tocar la versión que ya funciona:

- `git switch -c nueva-funcion`: crea una rama y te cambia a ella.
- Trabaja y haz commits como siempre.
- Cuando todo funcione, únela a la rama principal con un **merge** o, en GitHub, con una **pull request**.

Las pull requests son la forma habitual de trabajar en equipo: alguien revisa tus cambios antes de unirlos. Practicarlas en tus proyectos te prepara para tu primer trabajo.

## 8. Los errores más comunes (y cómo salir de ellos)

- **«No sé en qué rama estoy»:** `git status` te lo dice.
- **Olvidaste añadir un archivo al commit:** añádelo con `git add` y haz otro commit. No pasa nada.
- **Conflicto al unir ramas:** Git te marca las líneas en conflicto dentro del archivo. Elige qué versión dejar, borra las marcas, guarda y haz commit.
- **Subiste algo que no debías**, como una contraseña: cámbiala cuanto antes y añade ese archivo a `.gitignore` para que Git no lo vuelva a tener en cuenta.

Equivocarse con Git es parte de aprenderlo. Casi todo tiene arreglo.

## 9. Sigue practicando

- Usa Git en **todos tus proyectos**, aunque sean pequeños.
- Fija tus mejores repositorios en tu perfil de GitHub y cuida sus README.
- Si alguna palabra no la conoces, búscala en nuestro [glosario](/glosario).
- Si te atascas, cuéntalo en el [foro](/foro): alguien de la comunidad habrá pasado por lo mismo.

## En resumen

1. Git guarda el historial de tu código; GitHub lo guarda en internet.
2. Instala Git, crea tu cuenta de GitHub y configura tu nombre y email.
3. Aprende los comandos básicos: `status`, `add`, `commit`, `push` y `pull`.
4. Haz tu primer commit y súbelo a GitHub.
5. Escribe mensajes claros y haz commits pequeños.
6. Usa ramas para probar sin romper nada.
7. Equivocarse es normal: casi todo tiene arreglo.

Tu primer commit no tiene que ser perfecto. Solo tiene que existir. 💜',
        'Git i GitHub per a principiants: el teu primer commit pas a pas', 'Què són Git i GitHub, les ordres imprescindibles i com fer i pujar el teu primer commit, explicat pas a pas per a qui comença.', 'Git apareix a gairebé totes les ofertes de feina tech, i és normal que al principi costi: branques, commits, conflictes… sembla un idioma nou. La bona notícia és que per començar només necessites unes quantes ordres i entendre la idea que hi ha al darrere.

En aquesta guia t’expliquem què són Git i GitHub, les ordres imprescindibles i com fer el teu primer commit pas a pas.

## 1. Què és Git i què és GitHub

Encara que s’anomenen junts, no són el mateix:

- **Git** és una eina que desa l’historial de canvis del teu projecte. Cada vegada que deses un punt important, fas un **commit**: una foto del codi en aquell moment, amb un missatge que explica què has canviat.
- **GitHub** és un web on puges aquests projectes per tenir-ne una còpia a internet, compartir-los i treballar amb altres persones.

Dit d’una altra manera: Git treballa al teu ordinador i GitHub és el lloc on ho guardes i ho ensenyes. El teu perfil de GitHub, a més, forma part de la teva carta de presentació.

## 2. Prepara el teu ordinador

Abans de començar:

- **Instal·la Git** des del seu web oficial (o amb el gestor de paquets del teu sistema).
- **Crea un compte a GitHub** amb un nom d’usuari professional.
- **Digues a Git qui ets**, amb el teu nom i l’email del teu compte: `git config --global user.name "El teu nom"` i `git config --global user.email "tu@email.com"`.

Només ho has de fer una vegada.

## 3. Les ordres imprescindibles

Amb aquestes ordres pots fer gairebé de tot al principi:

- `git init`: converteix una carpeta en un projecte amb Git.
- `git status`: et diu en quina branca ets i quins fitxers has canviat. Fes-la servir sempre que tinguis dubtes.
- `git add .`: prepara tots els canvis per desar-los.
- `git commit -m "missatge"`: desa aquests canvis amb un missatge.
- `git push`: puja els teus commits a GitHub.
- `git pull`: baixa els últims canvis de GitHub.

No cal aprendre-les de memòria: fent-les servir cada dia s’acaben quedant.

## 4. El teu primer commit, pas a pas

1. Crea una carpeta per al teu projecte i entra-hi des del terminal.
2. Escriu `git init` per començar a fer servir Git en aquesta carpeta.
3. Crea un fitxer, per exemple un `README.md` que expliqui de què va el teu projecte.
4. Comprova els canvis amb `git status`.
5. Prepara’ls amb `git add .`.
6. Desa’ls amb `git commit -m "Primer commit"`.

Enhorabona! Ja tens el teu primer commit. 🎉

## 5. Puja’l a GitHub

1. A GitHub, crea un repositori nou, buit i amb el mateix nom que el teu projecte.
2. GitHub et mostrarà l’adreça del repositori. Connecta-l’hi amb `git remote add origin` seguit d’aquesta adreça.
3. Puja la teva feina amb `git push -u origin main`.

A partir d’aquí, cada vegada que facis commits nous, n’hi haurà prou amb `git push`.

## 6. Escriu bons missatges de commit

Un bon missatge ajuda a entendre l’historial, a tu i a qui revisi el teu codi (també en una entrevista):

- **Explica què canvia**, no com et senties: «Afegeix el formulari de contacte» millor que «canvis».
- **Sigues breu:** sol bastar una línia.
- **Fes commits petits**, d’un canvi cadascun. És més fàcil revisar-los i desfer-los.

## 7. Branques: prova coses sense trencar res

Una **branca** és una línia de treball paral·lela. Et permet provar coses sense tocar la versió que ja funciona:

- `git switch -c nova-funcio`: crea una branca i et canvia a ella.
- Treballa i fes commits com sempre.
- Quan tot funcioni, uneix-la a la branca principal amb un **merge** o, a GitHub, amb una **pull request**.

Les pull requests són la manera habitual de treballar en equip: algú revisa els teus canvis abans d’unir-los. Practicar-les als teus projectes et prepara per a la teva primera feina.

## 8. Els errors més comuns (i com sortir-ne)

- **«No sé en quina branca soc»:** `git status` t’ho diu.
- **T’has oblidat d’afegir un fitxer al commit:** afegeix-lo amb `git add` i fes un altre commit. No passa res.
- **Conflicte en unir branques:** Git et marca les línies en conflicte dins del fitxer. Tria quina versió deixar, esborra les marques, desa i fes commit.
- **Has pujat alguna cosa que no havies de pujar**, com una contrasenya: canvia-la com més aviat millor i afegeix aquest fitxer a `.gitignore` perquè Git no el torni a tenir en compte.

Equivocar-se amb Git forma part d’aprendre’l. Gairebé tot té solució.

## 9. Continua practicant

- Fes servir Git a **tots els teus projectes**, encara que siguin petits.
- Fixa els teus millors repositoris al teu perfil de GitHub i cuida’n els README.
- Si alguna paraula no la coneixes, busca-la al nostre [glossari](/glosario).
- Si t’encalles, explica-ho al [fòrum](/foro): algú de la comunitat haurà passat pel mateix.

## En resum

1. Git desa l’historial del teu codi; GitHub el guarda a internet.
2. Instal·la Git, crea el teu compte de GitHub i configura el teu nom i email.
3. Aprèn les ordres bàsiques: `status`, `add`, `commit`, `push` i `pull`.
4. Fes el teu primer commit i puja’l a GitHub.
5. Escriu missatges clars i fes commits petits.
6. Fes servir branques per provar sense trencar res.
7. Equivocar-se és normal: gairebé tot té solució.

El teu primer commit no ha de ser perfecte. Només ha d’existir. 💜',
        'Git and GitHub for beginners: your first commit step by step', 'What Git and GitHub are, the essential commands and how to make and push your first commit, explained step by step for beginners.', 'Git shows up in almost every tech job description, and it is normal to find it hard at first: branches, commits, conflicts… it feels like a new language. The good news is that to get started you only need a handful of commands and to understand the idea behind them.

In this guide we explain what Git and GitHub are, the essential commands and how to make your first commit step by step.

## 1. What Git is and what GitHub is

Although they are mentioned together, they are not the same:

- **Git** is a tool that keeps the history of changes in your project. Every time you save an important point, you make a **commit**: a snapshot of the code at that moment, with a message explaining what you changed.
- **GitHub** is a website where you upload those projects to keep a copy online, share them and work with other people.

In other words: Git works on your computer and GitHub is where you store and show your work. Your GitHub profile is also part of your calling card.

## 2. Set up your computer

Before you start:

- **Install Git** from its official website (or with your system’s package manager).
- **Create a GitHub account** with a professional username.
- **Tell Git who you are**, with your name and your account email: `git config --global user.name "Your name"` and `git config --global user.email "you@email.com"`.

You only need to do this once.

## 3. The essential commands

With these commands you can do almost everything at first:

- `git init`: turns a folder into a Git project.
- `git status`: tells you which branch you are on and which files you have changed. Use it whenever you are unsure.
- `git add .`: stages all your changes to be saved.
- `git commit -m "message"`: saves those changes with a message.
- `git push`: uploads your commits to GitHub.
- `git pull`: downloads the latest changes from GitHub.

No need to memorise them: using them every day makes them stick.

## 4. Your first commit, step by step

1. Create a folder for your project and open it in the terminal.
2. Type `git init` to start using Git in that folder.
3. Create a file, for example a `README.md` explaining what your project is about.
4. Check your changes with `git status`.
5. Stage them with `git add .`.
6. Save them with `git commit -m "First commit"`.

Congratulations! You have made your first commit. 🎉

## 5. Upload it to GitHub

1. On GitHub, create a new, empty repository with the same name as your project.
2. GitHub will show you the repository address. Connect it with `git remote add origin` followed by that address.
3. Upload your work with `git push -u origin main`.

From then on, whenever you make new commits, `git push` is all you need.

## 6. Write good commit messages

A good message helps you, and whoever reviews your code (in an interview too), understand the history:

- **Explain what changes**, not how you felt: “Add the contact form” is better than “changes”.
- **Keep it short:** one line is usually enough.
- **Make small commits**, one change each. They are easier to review and undo.

## 7. Branches: try things without breaking anything

A **branch** is a parallel line of work. It lets you try things without touching the version that already works:

- `git switch -c new-feature`: creates a branch and switches to it.
- Work and commit as usual.
- When everything works, join it to the main branch with a **merge** or, on GitHub, with a **pull request**.

Pull requests are the usual way to work in a team: someone reviews your changes before they are merged. Practising them in your own projects prepares you for your first job.

## 8. The most common mistakes (and how to fix them)

- **“I don’t know which branch I’m on”:** `git status` tells you.
- **You forgot to add a file to the commit:** add it with `git add` and make another commit. No big deal.
- **Conflict when merging branches:** Git marks the conflicting lines inside the file. Choose which version to keep, remove the markers, save and commit.
- **You uploaded something you shouldn’t have**, like a password: change it as soon as possible and add that file to `.gitignore` so Git ignores it from now on.

Making mistakes with Git is part of learning it. Almost everything can be fixed.

## 9. Keep practising

- Use Git in **all your projects**, even small ones.
- Pin your best repositories to your GitHub profile and take care of their READMEs.
- If there is a word you don’t know, look it up in our [glossary](/glosario).
- If you get stuck, share it in the [forum](/foro): someone in the community will have been through the same.

## Summary

1. Git keeps your code’s history; GitHub stores it online.
2. Install Git, create your GitHub account and set your name and email.
3. Learn the basic commands: `status`, `add`, `commit`, `push` and `pull`.
4. Make your first commit and upload it to GitHub.
5. Write clear messages and make small commits.
6. Use branches to try things without breaking anything.
7. Mistakes are normal: almost everything can be fixed.

Your first commit doesn’t have to be perfect. It just has to exist. 💜',
        true, current_timestamp, current_timestamp, current_timestamp);

insert into guides (slug, category, title, description, content, title_ca, description_ca, content_ca,
                    title_en, description_en, content_en, published, published_at, created_at, updated_at)
values ('tu-primer-meetup-tech', 'COMUNIDAD',
        'Tu primer meetup tech: qué esperar y cómo aprovecharlo', 'Todo lo que necesitas para tu primer meetup tech: cómo elegirlo, qué hacer antes, durante y después, y cómo hablar de ti si estás empezando.', 'Has visto un meetup tech que te interesa, pero te entran las dudas: ¿será para mí?, ¿y si no conozco a nadie?, ¿y si hablan de cosas que no entiendo? Es normal. Casi todo el mundo siente eso antes de su primer evento.

La buena noticia es que los meetups son de los sitios más acogedores del sector: la mayoría de la gente va a aprender y a conocer a otras personas, igual que tú. En esta guía te contamos qué esperar y cómo sacarle partido.

## 1. Qué es un meetup (y por qué merece la pena)

Un meetup es un encuentro, normalmente gratuito, organizado por una comunidad alrededor de una tecnología o un tema: JavaScript, Java, datos, diseño, mujeres en tech… Suele tener una o dos charlas cortas y un rato para hablar después.

Ir a meetups te ayuda a:

- **Aprender** de gente que usa esas tecnologías cada día.
- **Conocer a personas** del sector, también de empresas que contratan.
- **Enterarte de oportunidades** que a veces no llegan a publicarse.
- **Sentirte parte** de algo: ver que no eres la única persona que está empezando.

## 2. Elige tu primer evento

Para el primero, busca algo que te lo ponga fácil:

- **Un tema que conozcas un poco**, para seguir las charlas con comodidad.
- **Charlas para principiantes** o eventos que digan que son para todos los niveles.
- **Comunidades cercanas**, en tu ciudad o en línea.
- **Comunidades de mujeres en tech**, si te apetece un ambiente especialmente cercano.

En nuestra [agenda de eventos](/eventos) tienes meetups, charlas y hackathons, y puedes filtrarlos por ciudad o ver los que son en línea.

## 3. Antes de ir

- **Apúntate** en la web del evento: muchas veces hay plazas limitadas y así saben cuánta gente va.
- **Mira quién da la charla** y de qué va. Si alguna palabra no la conoces, búscala en nuestro [glosario](/glosario).
- **Piensa en una o dos preguntas** que te gustaría hacer, sobre la charla o sobre su trabajo.
- **Prepara una presentación corta** de ti: tu nombre, en qué punto estás y qué te interesa. Por ejemplo: «Soy Laura, acabo de terminar un bootcamp de desarrollo web y me interesa mucho el frontend».

## 4. Durante el evento

- **Llega un poco antes:** con menos gente es más fácil empezar a hablar.
- **Escucha las charlas y toma notas** de lo que te llame la atención.
- **Acércate a alguien que esté sola o solo**: seguro que agradece que le hables.
- **Pregunta por lo que hacen:** «¿A qué te dedicas?» o «¿Qué te ha parecido la charla?» son buenas formas de empezar.
- **Habla con quien dio la charla:** decirle qué te gustó es un buen inicio de conversación.

No hace falta hablar con todo el mundo. Con una o dos conversaciones de verdad, el evento ya ha merecido la pena.

## 5. Habla de ti con naturalidad

Puede que te pregunten a qué te dedicas. Si estás empezando, dilo con tranquilidad:

- Cuenta **qué estás aprendiendo** y **qué proyecto tienes entre manos**.
- Si vienes de otro sector, **cuéntalo como un valor**: aporta una mirada diferente.
- Si estás buscando trabajo, **dilo sin rodeos**: mucha gente estará encantada de darte pistas o pasar tu perfil.

## 6. Después del evento

- **Conecta en LinkedIn** con las personas con las que hablaste, con un mensaje corto recordando dónde os conocisteis.
- **Comparte lo que aprendiste:** un post sobre la charla ayuda a fijar lo aprendido y da visibilidad a tu perfil.
- **Apunta ideas** para tus proyectos o temas que quieras estudiar.
- **Vuelve:** en el segundo y el tercer evento ya reconocerás caras y todo será más fácil.

## 7. Da el paso de compartir

Cuando lleves un tiempo, también puedes aportar:

- **Comparte eventos** que conozcas en nuestra [agenda](/eventos): ayudas a quien viene detrás.
- **Ofrécete a ayudar** en la organización de una comunidad.
- **Anímate a dar una charla corta** contando un proyecto o algo que aprendiste. No hace falta ser experta o experto: contar tu camino también inspira.

## 8. Si no puedes ir en persona

No todo el mundo puede ir a eventos presenciales, y no pasa nada:

- Muchas comunidades hacen **eventos en línea**: en la agenda puedes verlos por separado.
- Participa en **comunidades digitales** y en nuestro [foro](/foro): también se aprende y se conoce gente.
- Muchas charlas se **graban y se publican** después.

## En resumen

1. Los meetups son gratuitos, cercanos y llenos de gente que también está aprendiendo.
2. Para el primero, elige un tema que conozcas y una charla para todos los niveles.
3. Apúntate, mira de qué va y prepara una presentación corta.
4. Llega pronto y busca una o dos conversaciones de verdad.
5. Habla de ti con naturalidad, también si estás empezando.
6. Conecta después y vuelve al siguiente.
7. Cuando puedas, comparte eventos y tu experiencia.

Tu primer meetup no tiene que ser perfecto. Solo tiene que ser el primero. 💜',
        'El teu primer meetup tech: què esperar-ne i com aprofitar-lo', 'Tot el que necessites per al teu primer meetup tech: com triar-lo, què fer abans, durant i després, i com parlar de tu si comences.', 'Has vist un meetup tech que t’interessa, però et vénen els dubtes: serà per a mi?, i si no conec ningú?, i si parlen de coses que no entenc? És normal. Gairebé tothom sent això abans del seu primer esdeveniment.

La bona notícia és que els meetups són dels llocs més acollidors del sector: la majoria de la gent hi va a aprendre i a conèixer altres persones, igual que tu. En aquesta guia t’expliquem què esperar-ne i com treure’n profit.

## 1. Què és un meetup (i per què val la pena)

Un meetup és una trobada, normalment gratuïta, organitzada per una comunitat al voltant d’una tecnologia o un tema: JavaScript, Java, dades, disseny, dones en tech… Sol tenir una o dues xerrades curtes i una estona per parlar després.

Anar a meetups t’ajuda a:

- **Aprendre** de gent que fa servir aquestes tecnologies cada dia.
- **Conèixer persones** del sector, també d’empreses que contracten.
- **Assabentar-te d’oportunitats** que de vegades no arriben a publicar-se.
- **Sentir-te part** d’alguna cosa: veure que no ets l’única persona que comença.

## 2. Tria el teu primer esdeveniment

Per al primer, busca alguna cosa que t’ho posi fàcil:

- **Un tema que coneguis una mica**, per seguir les xerrades amb comoditat.
- **Xerrades per a principiants** o esdeveniments que diguin que són per a tots els nivells.
- **Comunitats properes**, a la teva ciutat o en línia.
- **Comunitats de dones en tech**, si et ve de gust un ambient especialment proper.

A la nostra [agenda d’esdeveniments](/eventos) tens meetups, xerrades i hackathons, i pots filtrar-los per ciutat o veure els que són en línia.

## 3. Abans d’anar-hi

- **Apunta-t’hi** al web de l’esdeveniment: moltes vegades hi ha places limitades i així saben quanta gent hi va.
- **Mira qui fa la xerrada** i de què va. Si alguna paraula no la coneixes, busca-la al nostre [glossari](/glosario).
- **Pensa una o dues preguntes** que t’agradaria fer, sobre la xerrada o sobre la seva feina.
- **Prepara una presentació curta** de tu: el teu nom, en quin punt ets i què t’interessa. Per exemple: «Soc la Laura, acabo de fer un bootcamp de desenvolupament web i m’interessa molt el frontend».

## 4. Durant l’esdeveniment

- **Arriba una mica abans:** amb menys gent és més fàcil començar a parlar.
- **Escolta les xerrades i pren notes** del que et cridi l’atenció.
- **Apropa’t a algú que estigui sol o sola**: segur que agraeix que li parlis.
- **Pregunta pel que fan:** «A què et dediques?» o «Què t’ha semblat la xerrada?» són bones maneres de començar.
- **Parla amb qui ha fet la xerrada:** dir-li què t’ha agradat és un bon inici de conversa.

No cal parlar amb tothom. Amb una o dues converses de debò, l’esdeveniment ja haurà valgut la pena.

## 5. Parla de tu amb naturalitat

Potser et preguntaran a què et dediques. Si comences, digues-ho amb tranquil·litat:

- Explica **què estàs aprenent** i **quin projecte tens entre mans**.
- Si véns d’un altre sector, **explica-ho com un valor**: aporta una mirada diferent.
- Si busques feina, **digues-ho clarament**: molta gent estarà encantada de donar-te pistes o passar el teu perfil.

## 6. Després de l’esdeveniment

- **Connecta a LinkedIn** amb les persones amb qui vas parlar, amb un missatge curt recordant on us vau conèixer.
- **Comparteix el que vas aprendre:** un post sobre la xerrada ajuda a fixar el que has après i dona visibilitat al teu perfil.
- **Apunta idees** per als teus projectes o temes que vulguis estudiar.
- **Torna-hi:** al segon i al tercer esdeveniment ja reconeixeràs cares i tot serà més fàcil.

## 7. Fes el pas de compartir

Quan portis un temps, també hi pots aportar:

- **Comparteix esdeveniments** que coneguis a la nostra [agenda](/eventos): ajudes qui ve darrere.
- **Ofereix-te a ajudar** en l’organització d’una comunitat.
- **Anima’t a fer una xerrada curta** explicant un projecte o alguna cosa que hagis après. No cal ser experta o expert: explicar el teu camí també inspira.

## 8. Si no hi pots anar en persona

No tothom pot anar a esdeveniments presencials, i no passa res:

- Moltes comunitats fan **esdeveniments en línia**: a l’agenda els pots veure per separat.
- Participa en **comunitats digitals** i al nostre [fòrum](/foro): també s’aprèn i es coneix gent.
- Moltes xerrades es **graven i es publiquen** després.

## En resum

1. Els meetups són gratuïts, propers i plens de gent que també està aprenent.
2. Per al primer, tria un tema que coneguis i una xerrada per a tots els nivells.
3. Apunta-t’hi, mira de què va i prepara una presentació curta.
4. Arriba aviat i busca una o dues converses de debò.
5. Parla de tu amb naturalitat, també si comences.
6. Connecta després i torna al següent.
7. Quan puguis, comparteix esdeveniments i la teva experiència.

El teu primer meetup no ha de ser perfecte. Només ha de ser el primer. 💜',
        'Your first tech meetup: what to expect and how to make the most of it', 'Everything you need for your first tech meetup: how to choose it, what to do before, during and after, and how to talk about yourself if you are starting out.', 'You have seen a tech meetup you like, but doubts creep in: is it for me?, what if I don’t know anyone?, what if they talk about things I don’t understand? That is normal. Almost everyone feels this before their first event.

The good news is that meetups are among the most welcoming places in the industry: most people go to learn and meet others, just like you. In this guide we tell you what to expect and how to make the most of it.

## 1. What a meetup is (and why it is worth it)

A meetup is a gathering, usually free, organised by a community around a technology or topic: JavaScript, Java, data, design, women in tech… It usually has one or two short talks and some time to chat afterwards.

Going to meetups helps you:

- **Learn** from people who use those technologies every day.
- **Meet people** in the industry, including from companies that hire.
- **Hear about opportunities** that sometimes never get published.
- **Feel part** of something: see that you are not the only one starting out.

## 2. Choose your first event

For your first one, look for something that makes it easy:

- **A topic you know a little**, so you can follow the talks comfortably.
- **Beginner talks** or events that say they are for all levels.
- **Nearby communities**, in your city or online.
- **Women in tech communities**, if you fancy an especially friendly atmosphere.

In our [events calendar](/eventos) you will find meetups, talks and hackathons, and you can filter them by city or see the online ones.

## 3. Before you go

- **Sign up** on the event page: there are often limited places and it helps them know how many people are coming.
- **Check who is speaking** and what it is about. If there is a word you don’t know, look it up in our [glossary](/glosario).
- **Think of one or two questions** you would like to ask, about the talk or about their work.
- **Prepare a short introduction** about yourself: your name, where you are and what interests you. For example: “I’m Laura, I’ve just finished a web development bootcamp and I’m really into frontend”.

## 4. During the event

- **Arrive a bit early:** with fewer people it is easier to start talking.
- **Listen to the talks and take notes** on what catches your attention.
- **Approach someone who is on their own**: they will surely appreciate it.
- **Ask about what they do:** “What do you work on?” or “What did you think of the talk?” are good ways to start.
- **Talk to the speaker:** telling them what you liked is a great conversation starter.

You don’t need to talk to everyone. With one or two real conversations, the event has already been worth it.

## 5. Talk about yourself naturally

You may be asked what you do. If you are starting out, say so calmly:

- Share **what you are learning** and **which project you are working on**.
- If you come from another field, **present it as an asset**: it brings a different perspective.
- If you are looking for a job, **say it clearly**: many people will be happy to give you tips or pass on your profile.

## 6. After the event

- **Connect on LinkedIn** with the people you talked to, with a short message reminding them where you met.
- **Share what you learned:** a post about the talk helps you remember it and gives your profile visibility.
- **Write down ideas** for your projects or topics you want to study.
- **Go back:** at your second and third events you will recognise faces and everything will be easier.

## 7. Take the step to share

After a while, you can contribute too:

- **Share events** you know about in our [calendar](/eventos): you help whoever comes next.
- **Offer to help** organise a community.
- **Give a short talk** about a project or something you learned. You don’t need to be an expert: sharing your journey inspires others too.

## 8. If you can’t go in person

Not everyone can attend in-person events, and that is fine:

- Many communities run **online events**: you can see them separately in the calendar.
- Join **online communities** and our [forum](/foro): you learn and meet people there too.
- Many talks are **recorded and published** afterwards.

## Summary

1. Meetups are free, friendly and full of people who are also learning.
2. For your first one, choose a topic you know and an all-levels talk.
3. Sign up, check what it is about and prepare a short introduction.
4. Arrive early and aim for one or two real conversations.
5. Talk about yourself naturally, even if you are just starting.
6. Connect afterwards and go to the next one.
7. When you can, share events and your experience.

Your first meetup doesn’t have to be perfect. It just has to be the first one. 💜',
        true, current_timestamp, current_timestamp, current_timestamp);
