insert into guides (slug, category, title, description, content, title_ca, description_ca, content_ca,
                    title_en, description_en, content_en, keywords, keywords_ca, keywords_en,
                    published, published_at, created_at, updated_at)
values ('docker-para-principiantes', 'HERRAMIENTAS',
        'Docker para principiantes: qué es, para qué sirve y tus primeros comandos',
        'Qué es Docker, por qué lo piden en tantas ofertas junior y cómo empezar a usarlo con tus primeros contenedores, explicado paso a paso.',
        '«En mi ordenador funciona». Si has programado en equipo, seguro que has oído (o dicho) esta frase. Docker nació justo para acabar con ella, y por eso aparece en tantas ofertas de empleo tech, también en las junior.

En esta píldora te explicamos qué es Docker con ejemplos sencillos y te damos los comandos para tu primer contenedor.

## 1. Qué es Docker

Docker es una herramienta que **empaqueta una aplicación con todo lo que necesita para funcionar**: el código, el lenguaje, las librerías y la configuración. Ese paquete funciona igual en tu ordenador, en el de cualquier persona del equipo y en el servidor.

Piensa en un **contenedor de barco**: da igual lo que haya dentro, todos tienen la misma forma y se pueden mover de un barco a otro sin abrirlos. Docker hace lo mismo con las aplicaciones.

## 2. Por qué lo piden en las ofertas

- **Todo el equipo trabaja con lo mismo:** se acabó el «en mi ordenador funciona».
- **Montar un proyecto es rápido:** en lugar de instalar una base de datos a mano, se arranca con un comando.
- **Se usa para desplegar:** muchas empresas suben sus aplicaciones a producción con Docker.

Para un perfil junior no te pedirán ser experto, pero sí **entender qué es y saber usar los comandos básicos**.

## 3. Imágenes y contenedores

Son las dos palabras que más vas a oír:

- **Imagen:** la «receta» o plantilla con todo lo necesario. Por ejemplo, una imagen de PostgreSQL o de Node.
- **Contenedor:** una imagen **en marcha**. De una misma imagen puedes arrancar varios contenedores.

Si lo comparas con la cocina: la imagen es la receta y el contenedor es el plato que preparas con ella.

## 4. Instálalo

Descarga **Docker Desktop** desde la web oficial de Docker (Mac, Windows o Linux) e instálalo. Para comprobar que funciona, abre la terminal y escribe:

- `docker --version` para ver la versión instalada.
- `docker run hello-world` para arrancar tu primer contenedor, que solo muestra un mensaje de bienvenida.

## 5. Tus primeros comandos

- `docker pull postgres`: descarga una imagen.
- `docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=secreto postgres`: arranca un contenedor de PostgreSQL en segundo plano.
- `docker ps`: lista los contenedores que están en marcha.
- `docker stop` seguido del nombre o el id: para un contenedor.
- `docker logs` seguido del nombre: muestra lo que está pasando dentro.

Con esto ya tienes una base de datos funcionando **sin instalarla en tu ordenador**.

## 6. El Dockerfile: tu propia imagen

Un **Dockerfile** es un archivo de texto con los pasos para crear la imagen de tu aplicación: qué lenguaje usa, qué archivos copia y cómo se arranca. Con `docker build` conviertes ese archivo en una imagen.

No hace falta que lo domines al principio: lo importante es saber **leer uno** y entender qué hace cada línea.

## 7. Docker Compose: varios contenedores a la vez

Casi todas las aplicaciones tienen varias piezas: el frontend, el backend y la base de datos. **Docker Compose** permite describirlas todas en un archivo `docker-compose.yml` y arrancarlas juntas con `docker compose up`.

Primer Commit, por ejemplo, funciona así en su servidor: la web, la API, la base de datos y el servidor web son contenedores que se levantan a la vez con Docker Compose.

## 8. Cómo practicar

- Arranca una base de datos con Docker para tu próximo proyecto, en lugar de instalarla.
- Añade un Dockerfile a un proyecto de tu portfolio y explícalo en el README.
- Cuenta en las entrevistas que lo has usado y para qué: es un punto a favor.

Y si te atascas, pregunta en el [foro](/foro): seguro que a alguien le ha pasado lo mismo.

## Preguntas frecuentes

### ¿Docker es una máquina virtual?

No exactamente. Una máquina virtual simula un ordenador entero, con su propio sistema operativo. Un contenedor comparte el sistema de tu ordenador y solo lleva lo que necesita la aplicación, por eso es mucho más ligero y arranca en segundos.

### ¿Necesito saber Docker para mi primer empleo tech?

No siempre es obligatorio, pero aparece en muchas ofertas junior. Saber qué es, arrancar contenedores y entender un Dockerfile y un docker-compose ya te hace destacar.

### ¿Docker es gratis?

Sí, para uso personal, para aprender y para proyectos pequeños. Las empresas grandes pagan una licencia de Docker Desktop, pero eso no te afecta mientras aprendes.',
        'Docker per a principiants: què és, per a què serveix i les teves primeres ordres',
        'Què és Docker, per què el demanen en tantes ofertes júnior i com començar a fer-lo servir amb els teus primers contenidors, explicat pas a pas.',
        '«Al meu ordinador funciona». Si has programat en equip, segur que has sentit (o dit) aquesta frase. Docker va néixer justament per acabar-hi, i per això apareix en tantes ofertes de feina tech, també en les júnior.

En aquesta píndola t’expliquem què és Docker amb exemples senzills i et donem les ordres per al teu primer contenidor.

## 1. Què és Docker

Docker és una eina que **empaqueta una aplicació amb tot el que necessita per funcionar**: el codi, el llenguatge, les llibreries i la configuració. Aquest paquet funciona igual al teu ordinador, al de qualsevol persona de l’equip i al servidor.

Pensa en un **contenidor de vaixell**: tant és el que hi hagi a dins, tots tenen la mateixa forma i es poden moure d’un vaixell a un altre sense obrir-los. Docker fa el mateix amb les aplicacions.

## 2. Per què el demanen a les ofertes

- **Tot l’equip treballa amb el mateix:** s’ha acabat el «al meu ordinador funciona».
- **Muntar un projecte és ràpid:** en lloc d’instal·lar una base de dades a mà, s’engega amb una ordre.
- **Es fa servir per desplegar:** moltes empreses pugen les seves aplicacions a producció amb Docker.

En un perfil júnior no et demanaran ser expert, però sí **entendre què és i saber fer servir les ordres bàsiques**.

## 3. Imatges i contenidors

Són les dues paraules que més sentiràs:

- **Imatge:** la «recepta» o plantilla amb tot el necessari. Per exemple, una imatge de PostgreSQL o de Node.
- **Contenidor:** una imatge **en marxa**. D’una mateixa imatge pots engegar diversos contenidors.

Si ho compares amb la cuina: la imatge és la recepta i el contenidor és el plat que prepares amb ella.

## 4. Instal·la’l

Descarrega **Docker Desktop** del web oficial de Docker (Mac, Windows o Linux) i instal·la’l. Per comprovar que funciona, obre el terminal i escriu:

- `docker --version` per veure la versió instal·lada.
- `docker run hello-world` per engegar el teu primer contenidor, que només mostra un missatge de benvinguda.

## 5. Les teves primeres ordres

- `docker pull postgres`: descarrega una imatge.
- `docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=secreto postgres`: engega un contenidor de PostgreSQL en segon pla.
- `docker ps`: llista els contenidors que estan en marxa.
- `docker stop` seguit del nom o l’id: atura un contenidor.
- `docker logs` seguit del nom: mostra el que passa a dins.

Amb això ja tens una base de dades funcionant **sense instal·lar-la al teu ordinador**.

## 6. El Dockerfile: la teva pròpia imatge

Un **Dockerfile** és un fitxer de text amb els passos per crear la imatge de la teva aplicació: quin llenguatge fa servir, quins fitxers copia i com s’engega. Amb `docker build` converteixes aquest fitxer en una imatge.

No cal que el dominis al principi: l’important és saber **llegir-ne un** i entendre què fa cada línia.

## 7. Docker Compose: diversos contenidors alhora

Gairebé totes les aplicacions tenen diverses peces: el frontend, el backend i la base de dades. **Docker Compose** permet descriure-les totes en un fitxer `docker-compose.yml` i engegar-les juntes amb `docker compose up`.

Primer Commit, per exemple, funciona així al seu servidor: el web, l’API, la base de dades i el servidor web són contenidors que s’engeguen alhora amb Docker Compose.

## 8. Com practicar

- Engega una base de dades amb Docker per al teu proper projecte, en lloc d’instal·lar-la.
- Afegeix un Dockerfile a un projecte del teu portfolio i explica’l al README.
- Explica a les entrevistes que l’has fet servir i per a què: és un punt a favor.

I si t’encalles, pregunta al [fòrum](/foro): segur que a algú li ha passat el mateix.

## Preguntes freqüents

### Docker és una màquina virtual?

No exactament. Una màquina virtual simula un ordinador sencer, amb el seu propi sistema operatiu. Un contenidor comparteix el sistema del teu ordinador i només porta el que necessita l’aplicació, per això és molt més lleuger i s’engega en segons.

### Necessito saber Docker per a la meva primera feina tech?

No sempre és obligatori, però apareix en moltes ofertes júnior. Saber què és, engegar contenidors i entendre un Dockerfile i un docker-compose ja et fa destacar.

### Docker és gratuït?

Sí, per a ús personal, per aprendre i per a projectes petits. Les empreses grans paguen una llicència de Docker Desktop, però això no t’afecta mentre aprens.',
        'Docker for beginners: what it is, what it is for and your first commands',
        'What Docker is, why so many junior job ads ask for it and how to start using it with your first containers, explained step by step.',
        '“It works on my machine.” If you have coded in a team, you have surely heard (or said) this. Docker was born precisely to put an end to it, and that is why it shows up in so many tech job ads, junior ones included.

In this tech bite we explain what Docker is with simple examples and give you the commands for your first container.

## 1. What Docker is

Docker is a tool that **packages an application with everything it needs to run**: the code, the language, the libraries and the configuration. That package works the same on your computer, on anyone else’s in the team and on the server.

Think of a **shipping container**: whatever is inside, they all have the same shape and can be moved from one ship to another without opening them. Docker does the same with applications.

## 2. Why job ads ask for it

- **The whole team works with the same setup:** no more “it works on my machine”.
- **Setting up a project is fast:** instead of installing a database by hand, you start it with one command.
- **It is used to deploy:** many companies ship their applications to production with Docker.

For a junior role nobody expects you to be an expert, but you should **understand what it is and know the basic commands**.

## 3. Images and containers

These are the two words you will hear most:

- **Image:** the “recipe” or template with everything needed. For example, a PostgreSQL or Node image.
- **Container:** an image **that is running**. You can start several containers from the same image.

If you compare it to cooking: the image is the recipe and the container is the dish you make with it.

## 4. Install it

Download **Docker Desktop** from the official Docker website (Mac, Windows or Linux) and install it. To check that it works, open the terminal and type:

- `docker --version` to see the installed version.
- `docker run hello-world` to start your first container, which just shows a welcome message.

## 5. Your first commands

- `docker pull postgres`: downloads an image.
- `docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=secreto postgres`: starts a PostgreSQL container in the background.
- `docker ps`: lists the running containers.
- `docker stop` followed by the name or id: stops a container.
- `docker logs` followed by the name: shows what is happening inside.

With this you already have a database running **without installing it on your computer**.

## 6. The Dockerfile: your own image

A **Dockerfile** is a text file with the steps to build your application’s image: which language it uses, which files it copies and how it starts. With `docker build` you turn that file into an image.

You don’t need to master it at first: what matters is being able to **read one** and understand what each line does.

## 7. Docker Compose: several containers at once

Almost every application has several pieces: the frontend, the backend and the database. **Docker Compose** lets you describe them all in a `docker-compose.yml` file and start them together with `docker compose up`.

Primer Commit, for example, runs like this on its server: the website, the API, the database and the web server are containers started together with Docker Compose.

## 8. How to practise

- Start a database with Docker for your next project instead of installing it.
- Add a Dockerfile to a project in your portfolio and explain it in the README.
- Mention in interviews that you have used it and what for: it counts in your favour.

And if you get stuck, ask in the [forum](/foro): someone has surely been through the same.

## Frequently asked questions

### Is Docker a virtual machine?

Not exactly. A virtual machine simulates a whole computer, with its own operating system. A container shares your computer’s system and only carries what the application needs, which is why it is much lighter and starts in seconds.

### Do I need Docker for my first tech job?

It is not always required, but it appears in many junior job ads. Knowing what it is, starting containers and understanding a Dockerfile and a docker-compose file already makes you stand out.

### Is Docker free?

Yes, for personal use, learning and small projects. Large companies pay for a Docker Desktop licence, but that does not affect you while you learn.',
        'Docker, Contenedores, Imágenes, Dockerfile, Docker Compose, Terminal, Despliegue, DevOps, Primer empleo, Portfolio',
        'Docker, Contenidors, Imatges, Dockerfile, Docker Compose, Terminal, Desplegament, DevOps, Primera feina, Portfolio',
        'Docker, Containers, Images, Dockerfile, Docker Compose, Terminal, Deployment, DevOps, First job, Portfolio',
        true, current_timestamp, current_timestamp, current_timestamp);
