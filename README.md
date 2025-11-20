# Nomadia

Plataforma compuesta por un **backend en Java/Spring Boot** y un **frontend en Angular 20 con SCSS**, apoyados en tooling Node (Husky + commitlint) para la gestión de git hooks y la integración con CI/CD.

> Importante: después de clonar el repositorio, ejecuta siempre `npm install` en la **raíz del proyecto** antes de empezar a trabajar. Esto instala dependencias de tooling y configura los git hooks (Husky + commitlint).

---

## Tabla de contenidos

- [Arquitectura general](#arquitectura-general)
- [Requisitos previos](#requisitos-previos)
- [Instalación y configuración inicial](#instalación-y-configuración-inicial)
- [Scripts de npm](#scripts-de-npm)
- [Estructura de carpetas](#estructura-de-carpetas)
- [Uso en desarrollo](#uso-en-desarrollo)
- [Variables de entorno](#variables-de-entorno)
- [Tests](#tests)
- [Estilo de código, linting y formateo](#estilo-de-código-linting-y-formateo)
- [Git hooks: Husky y commitlint](#git-hooks-husky-y-commitlint)
- [CI/CD con GitHub Actions](#cicd-con-github-actions)
- [Guía rápida de contribución](#guía-rápida-de-contribución)
- [Licencia](#licencia)

---

## Arquitectura general

Nomadia está compuesta por dos grandes bloques:

- **Backend**: servicio REST construido con **Java 21 (JDK 21)** y **Spring Boot**, gestionado con **Maven**.
- **Frontend**: aplicación web en **Angular 20** con **SCSS** como preprocesador de estilos.

En la raíz del repositorio se centralizan las herramientas de integración y calidad:

- Tooling Node para **Husky** y **commitlint** (git hooks y validación de mensajes de commit).
- Configuración base para integrarse con pipelines de **CI/CD** en GitHub Actions.

El detalle de endpoints de API, módulos de Angular y estructura interna de cada capa debe documentarse sobre esta base a medida que la implementación avance.

---

## Requisitos previos

Para trabajar con este proyecto necesitas tener instalado:

### Backend (Spring Boot)

- **Java Development Kit (JDK) 21**.
- **Maven** (o el wrapper de Maven incluido en el proyecto cuando esté disponible, por ejemplo `mvnw`).

### Frontend (Angular 20)

- **Node.js** (versión recomendada: LTS reciente, por ejemplo 20.x).
- **npm** (incluido con Node.js).

### Tooling común

- **Node.js + npm** también se utilizan en la raíz del proyecto para:
  - Instalar devDependencies de tooling (`husky`, `@commitlint/*`).
  - Ejecutar scripts relacionados con git hooks y validación.
- [Git](https://git-scm.com/).

Recomendado:

- Un editor con buen soporte para Java y TypeScript/Angular, por ejemplo:
  - IntelliJ IDEA / WebStorm.
  - Visual Studio Code (con extensiones para Java, Angular, ESLint/Prettier cuando se añadan).

Sistema operativo soportado:

- Windows, macOS o Linux.

> Recuerda: siempre ejecuta `npm install` en la **raíz** del proyecto después de clonar, para que Husky pueda instalar los hooks de Git y se preparen las herramientas de commitlint.

---

## Instalación y configuración inicial

1. **Clonar el repositorio**

   ```bash
   git clone https://github.com/Mattew0800/Nomadia.git
   cd Nomadia
   ```

2. **Instalar dependencias de tooling en la raíz (Node)**

   ```bash
   npm install
   ```

   Esto hará dos cosas:

   - Instalar las dependencias de desarrollo (`husky`, `@commitlint/*`, etc.).
   - Ejecutar automáticamente el script `prepare`, que instala los hooks de Husky en la carpeta `.husky/`.

3. **Instalar dependencias del backend (Java / Spring Boot)**

   Desde la carpeta del backend (ajusta el nombre de la carpeta según tu estructura real, por ejemplo `backend/`):

   ```bash
   cd <carpeta-backend>
   mvn clean install
   ```

   O usando el wrapper de Maven si el proyecto lo incluye:

   ```bash
   cd <carpeta-backend>
   ./mvnw clean install   # Linux / macOS
   .\mvnw clean install   # Windows
   ```

4. **Instalar dependencias del frontend (Angular 20)**

   Desde la carpeta del frontend (por ejemplo `frontend/`):

   ```bash
   cd <carpeta-frontend>
   npm install
   ```

   Esto instalará las dependencias de Angular 20 y el tooling necesario (Angular CLI, etc.).

Ajusta las rutas `<carpeta-backend>` y `<carpeta-frontend>` a la estructura efectiva de tu proyecto.

---

## Scripts de npm

En la **raíz del proyecto** (`package.json`) están definidos actualmente los siguientes scripts relacionados con el tooling:

- `npm test`
  - Actualmente muestra el mensaje `"Error: no test specified"` y finaliza con error.
  - Está reservado para integrar, en el futuro, una suite de tests de tooling (por ejemplo, validaciones adicionales antes de commits o integraciones de CI).

- `npm run prepare`
  - Ejecuta `husky install` y configura los git hooks en la carpeta `.husky/`.
  - Se ejecuta automáticamente después de `npm install` gracias al campo `"prepare"` en `scripts`.
  - Normalmente no es necesario llamarlo manualmente, salvo para reinstalar hooks si fuese necesario.

### Scripts habituales del backend (Spring Boot)

En el proyecto de backend (Java/Spring Boot con Maven) es común disponer de scripts/comandos como:

- `mvn spring-boot:run`
  - Levanta la aplicación Spring Boot en modo desarrollo.
- `mvn test`
  - Ejecuta los tests unitarios e integrados del backend.
- `mvn clean package`
  - Compila el proyecto y genera el artefacto ejecutable (por ejemplo, un `jar`).

Si el proyecto usa `mvnw` (wrapper de Maven), los mismos comandos se ejecutan como `./mvnw ...` o `.\mvnw ...` según el sistema operativo.

### Scripts habituales del frontend (Angular 20)

En el proyecto de frontend (Angular 20) suelen definirse scripts en su `package.json`, como por ejemplo:

- `npm start` o `npm run start`
  - Suele ejecutar `ng serve` y levanta la aplicación Angular en modo desarrollo.
- `npm run build`
  - Genera el build de producción de la aplicación (`ng build`).
- `npm test`
  - Ejecuta los tests del frontend (Karma/Jasmine o el framework que se configure).
- `npm run lint`
  - Ejecuta el linter sobre el código Angular.

Consulta el `package.json` del frontend para ver los scripts exactos disponibles y actualiza esta sección si es necesario.

---

## Estructura de carpetas

### Estructura actual (simplificada en la raíz)

A grandes rasgos, la raíz del proyecto se organiza así:

- `.git/` – metadatos internos de Git.
- `.gitignore` – archivos y carpetas ignorados por Git.
- `.husky/` – configuración de los git hooks gestionados por Husky.
- `commitlint.config.js` – configuración de commitlint para validar mensajes de commit.
- `node_modules/` – dependencias de tooling instaladas mediante npm en la raíz.
- `package.json` – metadatos del proyecto, scripts y dependencias de tooling.
- `README.md` – documentación principal del proyecto.

### Estructura objetivo (frontend + backend)

A nivel de arquitectura lógica, el proyecto incluye (o incluirá) al menos estas partes:

- `<carpeta-backend>/` – proyecto de backend Java/Spring Boot (con su `pom.xml` y estructura típica `src/main/java`, `src/main/resources`, etc.).
- `<carpeta-frontend>/` – proyecto Angular 20 (con su `angular.json`, `src/app`, etc.).
- `config/` – configuraciones compartidas (por ejemplo, archivos de configuración de CI, plantillas de entornos, etc.).
- `docs/` – documentación adicional (diagramas, decisiones de arquitectura).

Sustituye `<carpeta-backend>` y `<carpeta-frontend>` por los nombres reales de las carpetas cuando actualices la estructura final, y mantén esta sección alineada con el proyecto.

---

## Uso en desarrollo

El flujo de trabajo típico en desarrollo es levantar backend y frontend en paralelo.

### 1. Preparación común

Desde la raíz del repositorio:

```bash
npm install
```

Esto asegura que los hooks de Husky y commitlint están correctamente instalados.

### 2. Levantar el backend (Java / Spring Boot)

Desde la carpeta del backend:

```bash
cd <carpeta-backend>

# Opción 1: Maven instalado en el sistema
mvn spring-boot:run

# Opción 2: usando el wrapper de Maven
./mvnw spring-boot:run   # Linux / macOS
.\mvnw spring-boot:run   # Windows
```

Por defecto, una aplicación Spring Boot suele exponerse en `http://localhost:8080` (ajusta esta URL si tu configuración utiliza otro puerto).

### 3. Levantar el frontend (Angular 20)

En otra terminal, desde la carpeta del frontend:

```bash
cd <carpeta-frontend>

# Dependiendo de cómo esté configurado el package.json del frontend
npm start
# o
npm run start
# o directamente
ng serve
```

Angular suele levantar la aplicación en `http://localhost:4200` por defecto (ajusta este valor según tu configuración real).

El frontend se comunicará con el backend usando la URL configurada (por ejemplo, a través de una variable de entorno o configuración en Angular environment).

---

## Variables de entorno

El proyecto puede utilizar variables de entorno tanto para el backend (Spring Boot) como para el frontend (Angular). La convención recomendada es:

- Backend (Spring Boot):
  - Archivos como `application.properties` o `application.yml`, y variables externas (`SPRING_PROFILES_ACTIVE`, etc.).
- Frontend (Angular):
  - Ficheros de entorno (`environment.ts`, `environment.prod.ts`) y, opcionalmente, variables inyectadas en tiempo de build.
- Compartido:
  - Archivos `.env`, `.env.development`, `.env.production` si se usan en scripts o tooling.

Ejemplo de tabla de variables de entorno que se puede ir completando:

| Variable              | Capa       | Obligatoria | Descripción                                           | Ejemplo                    |
|-----------------------|-----------|------------|-------------------------------------------------------|----------------------------|
| `SPRING_PROFILES_ACTIVE` | Backend   | No         | Perfil de Spring Boot activo (`dev`, `prod`, etc.).   | `dev`                      |
| `SERVER_PORT`         | Backend    | No         | Puerto en el que corre la API Spring Boot.           | `8080`                     |
| `API_BASE_URL`        | Frontend   | Sí         | URL base del backend consumida por el frontend.      | `http://localhost:8080`    |

Amplía y adapta esta tabla conforme se definan las variables reales del proyecto.

---

## Tests

La estrategia de testing puede organizarse en varios niveles:

- **Backend (Spring Boot)**
  - Tests unitarios y de integración con JUnit, Spring Test, etc.
  - Comando típico: `mvn test` o `./mvnw test`.
- **Frontend (Angular 20)**
  - Tests unitarios de componentes/servicios (Karma/Jasmine u otra herramienta configurada).
  - Comando típico: `npm test` o `ng test` desde la carpeta del frontend.
- **End-to-end (E2E)**
  - Tests que validan flujos completos a través de frontend y backend (por ejemplo, con Cypress, Playwright, etc.).

En la raíz del repositorio, el comando `npm test` actualmente solo muestra un mensaje de error estándar y no ejecuta tests reales; su propósito es reservar el nombre del script para tooling futuro.

Cuando se añada la suite de tests concreta en backend y frontend, este README deberá actualizarse para describir:

- Qué frameworks de test se usan.
- Cómo ejecutar los distintos tipos de tests.
- Cómo generar y consultar informes de cobertura.

---

## Estilo de código, linting y formateo

Aunque todavía no se han añadido todas las configuraciones de linters o formateadores al repositorio raíz, se recomienda adoptar de forma progresiva herramientas como:

- **Backend (Java)**
  - Checkstyle, SpotBugs, SonarLint u otras herramientas de análisis estático.
- **Frontend (Angular / TypeScript)**
  - ESLint para asegurar un estilo de código consistente.
  - Prettier para estandarizar el formateo.

Una vez integradas estas herramientas, es buena práctica añadir scripts como:

- `npm run lint` – ejecuta el linter sobre el código Angular.
- `npm run lint:fix` – intenta corregir automáticamente los problemas detectados (en el frontend).
- Tareas de Maven específicas para análisis estático en el backend.

Esta sección debe ampliarse cuando se definan las reglas concretas (por ejemplo, guías de estilo de Java y TypeScript, convenciones de SCSS, etc.).

---

## Git hooks: Husky y commitlint

El proyecto utiliza **Husky** y **commitlint** para mantener una calidad mínima de código y mensajes de commit.

### Husky

[Husky](https://typicode.github.io/husky) permite gestionar los hooks de Git desde el proyecto.

- Tras ejecutar `npm install` en la raíz, se lanza automáticamente el script `prepare`, que ejecuta:
  - `husky install`
- Esto crea/configura la carpeta `.husky/` y habilita los hooks definidos en ella.
- En este repositorio ya existe, por ejemplo, un hook `pre-commit` en `.husky/pre-commit` (puede ejecutar linters, tests u otras tareas antes de permitir el commit).

Si en algún momento los hooks dejan de funcionar (por ejemplo, después de borrar `node_modules`), puedes reinstalarlos manualmente con:

```bash
npm run prepare
```

### commitlint

[commitlint](https://commitlint.js.org/) valida que los mensajes de commit sigan un formato coherente (por ejemplo, **Conventional Commits**).

- La configuración se encuentra en `commitlint.config.js`.
- El proyecto usa `@commitlint/config-conventional` como base para las reglas.
- Normalmente se integra con un hook `commit-msg` gestionado por Husky, que ejecuta `commitlint` cada vez que haces `git commit`.

Formato básico esperado para los mensajes de commit (estilo Conventional Commits):

```text
<tipo>(<scope opcional>): <descripción breve>
```

Ejemplos de tipos habituales:

- `feat`: nueva funcionalidad.
- `fix`: corrección de bug.
- `chore`: tareas de mantenimiento (configs, dependencias, etc.).
- `docs`: cambios en documentación.
- `refactor`: cambios internos sin alterar el comportamiento externo.
- `test`: añadir o modificar tests.

Ejemplos de mensajes válidos:

- `feat(trip): agrega creación de viajes`
- `fix(auth): corrige validación de tokens`
- `chore(ci): actualiza pipeline de GitHub Actions`

Si el mensaje de commit no respeta las reglas, el hook `commit-msg` fallará y el commit no se creará; deberás corregir el mensaje y volver a ejecutar `git commit`.

---

## CI/CD con GitHub Actions

El proyecto está preparado para trabajar con pipelines de **CI/CD** usando **GitHub Actions**. La configuración concreta de los workflows se define en archivos YAML dentro de la ruta estándar:

- `.github/workflows/*.yml`

Un pipeline típico de CI para este proyecto puede incluir pasos como:

1. **Checkout** del código fuente.
2. Configuración de la versión de Node.js y JDK 21.
3. Instalación de dependencias:
   - `npm install` en la raíz (tooling, hooks).
   - Dependencias del backend (por ejemplo `mvn clean install`).
   - Dependencias del frontend (por ejemplo `npm install` en la carpeta del frontend).
4. Ejecución de:
   - Tests del backend (`mvn test`).
   - Tests del frontend (`npm test`/`ng test`).
   - Build del frontend (`npm run build`/`ng build`).

Cuando se configuren los workflows concretos, es recomendable documentar aquí:

- Nombres de los workflows (por ejemplo, `ci.yml`, `deploy.yml`).
- En qué eventos se ejecutan (push, pull_request, tags, etc.).
- Reglas de protección de la rama principal (por ejemplo, requerir que la CI pase antes de poder hacer merge).

Para la parte de **CD** (despliegue), también se puede documentar:

- Dónde se despliega el frontend (Vercel, Netlify, servidor propio, etc.).
- Dónde se despliega el backend (servidor Spring Boot dedicado, contenedores, Kubernetes, etc.).
- Qué secretos se configuran en *GitHub Secrets* (tokens de despliegue, URLs, credenciales, etc.).

---

## Guía rápida de contribución

1. Crea una rama a partir de la rama de desarrollo principal (por ejemplo, `develop`):

   ```bash
   git checkout develop
   git pull
   git checkout -b feature/mi-feature
   ```

2. Asegúrate de tener las dependencias instaladas y los hooks activos:

   ```bash
   npm install
   ```

3. Realiza tus cambios y, antes de commitear, ejecuta los comandos de lint/test correspondientes (cuando estén definidos).

4. Usa mensajes de commit que sigan las reglas de commitlint (estilo Conventional Commits).

5. Abre un Pull Request describiendo claramente:
   - Qué problema resuelve o qué funcionalidad agrega.
   - Cómo probar los cambios.

6. Espera la revisión y realiza los ajustes necesarios hasta que la rama esté lista para ser mergeada.

---

## Licencia

Este proyecto está licenciado bajo la licencia **ISC**.

Para más detalles, consulta el archivo de licencia correspondiente si se añade al repositorio o revisa el campo `"license"` en `package.json`.
