# Nomadia

Plataforma web compuesta por un **backend desarrollado en Java/Spring Boot** y un **frontend en Angular 20 con SCSS**. Doble capa de validaciones con **Husky** y **commitlint**, que gestionan los git hooks y se integran con el pipeline de **CI/CD**.

> ⚠️ **Muy importante**: luego de clonar el repositorio, ejecuta siempre `npm install` en la **raíz del proyecto** antes de empezar a desarrollar (frontend, backend, para todo). Esto instala las dependencias y configura los git hooks (Husky + commitlint).

---

## Tabla de contenidos

- [Arquitectura general](#arquitectura-general)
- [Requisitos previos](#requisitos-previos)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Instalación y configuración inicial](#instalación-y-configuración-inicial)
- [Ejecución en desarrollo](#ejecución-en-desarrollo)
- [Scripts disponibles](#scripts-disponibles)
- [Variables de entorno](#variables-de-entorno)
- [Tests](#tests)
- [Git hooks: Husky y commitlint](#git-hooks-husky-y-commitlint)
- [CI/CD con GitHub Actions](#cicd-con-github-actions)
- [Guía rápida de contribución](#guía-rápida-de-contribución)
- [Licencia](#licencia)

---

## Arquitectura general

Nomadia se divide en dos partes principales:

- **Backend**
    - Implementado en **Java 21 (JDK 21)** con **Spring Boot** y **Maven**.
    - Expone los servicios REST que consumirá el frontend (gestión de usuarios, viajes, actividades, etc.).

- **Frontend**
    - Implementado en **Angular 20** con **SCSS**.
    - Utiliza Angular standalone components y organización por páginas, servicios, modelos y estilos.

En la **raíz del repositorio** se encuentran:

- `package.json` con el tooling Node (Husky + commitlint).
- `.husky/` con los hooks de Git.
- `commitlint.config.js` con la configuración del formato de los mensajes de commit.

---

## Requisitos previos

### Comunes

- [Git](https://git-scm.com/)
- Sistema operativo: Windows, macOS o Linux

### Backend (Java / Spring Boot)

- **Java Development Kit (JDK) 21**
- **Maven** (o el wrapper `mvnw` que se suela incluir en el proyecto backend)

### Frontend (Angular 20)

- **Node.js** (recomendado LTS reciente, por ejemplo 20.x)
- **npm** (incluido con Node.js)

### Tooling raíz

- **Node.js + npm** también se usan en la raíz del proyecto para:
    - Instalar `husky` y `@commitlint/*`.
    - Configurar y ejecutar los git hooks.

> Recuerda: antes de trabajar en cualquier parte del proyecto, ejecuta `npm install` en la raíz para asegurar que los hooks de Git están activos.

---

## Estructura del repositorio

A grandes rasgos, la estructura relevante es:

- `README.md` – este archivo.
- `package.json` – tooling Node (husky, commitlint) en la raíz.
- `commitlint.config.js` – reglas de commitlint.
- `.husky/` – scripts de git hooks gestionados por Husky.
- `Nomadia/` – **proyecto frontend Angular 20**.
    - `angular.json` – configuración del workspace de Angular.
    - `package.json` – dependencias y scripts del frontend.
    - `tsconfig*.json` – configuración de TypeScript.
    - `public/` – recursos estáticos (imágenes, favicon, etc.).
    - `src/` – código fuente del frontend:
        - `index.html`
        - `main.ts` – punto de entrada de la app Angular.
        - `styles.scss` – estilos globales.
        - `app/` – módulo principal de la aplicación:
            - `app.ts`, `app.html`, `app.config.ts`, `app.routes.ts`, `app.css`
            - `core/` – funcionalidad central (por ejemplo, interceptores HTTP de autenticación).
            - `guards/` – guards de routing (`auth.guard.ts`, `public.guard.ts`, etc.).
            - `models/` – modelos y DTOs usados por el frontend (por ejemplo `ActivityCreate.ts`, `ActivityResponse.ts`, `LoginResponse.ts`, etc.).
            - `pages/` – componentes de páginas (listado de viajes, login, registro, etc.).
            - `services/` – servicios Angular para comunicación con el backend.
            - `styles/` – estilos SCSS específicos de la app.

El proyecto de **backend** (Spring Boot) se estructura de forma estándar Maven (`pom.xml`, `src/main/java`, `src/main/resources`, etc.) en su propia carpeta de backend, y se integra con este repositorio a nivel de control de versiones y CI/CD.

---

## Instalación y configuración inicial

### 1. Clonar el repositorio

```bash
git clone https://github.com/Mattew0800/Nomadia.git
cd Nomadia
```

### 2. Instalar tooling raíz (Husky + commitlint)

Desde la raíz del repositorio:

```bash
npm install
```

Esto:

- Instala `husky` y `@commitlint/*`.
- Ejecuta automáticamente el script `prepare` que instala los hooks en `.husky/`.

### 3. Instalar dependencias del frontend (Angular 20)

Desde la carpeta `Nomadia/`:

```bash
cd Nomadia
npm install
```

Esto instala todas las dependencias del proyecto Angular 20 (Angular CLI, dependencias de la app, etc.).

### 4. Instalar dependencias del backend (Java / Spring Boot)

Desde la carpeta del backend (ajusta la ruta a la ubicación real de tu proyecto Spring Boot):

```bash
cd <carpeta-backend>
mvn clean install
```

O usando el wrapper (`mvnw`) si está disponible:

```bash
./mvnw clean install   # Linux / macOS
.\mvnw clean install   # Windows
```

---

## Ejecución en desarrollo

### 1. Preparar el entorno común

En la raíz del repositorio:

```bash
npm install
```

Con esto te aseguras de que los hooks de Git funcionan correctamente.

### 2. Levantar el backend (Spring Boot)

Desde la carpeta del backend:

```bash
cd <carpeta-backend>

# Si tienes Maven instalado en el sistema
mvn spring-boot:run

# O usando el wrapper
./mvnw spring-boot:run   # Linux / macOS
.\mvnw spring-boot:run   # Windows
```

Puerto disponible `http://localhost:8080`

### 3. Levantar el frontend (Angular 20)

En otra terminal, desde la carpeta `Nomadia/` del frontend:

```bash
cd Nomadia
npm start
# o, según el package.json del front
npm run start
# o
ng serve
```

Puerto disponible `http://localhost:4200`

---

## Scripts disponibles

### Raíz del repositorio (`package.json` raíz)

- `npm test`
    - Actualmente muestra el mensaje `"Error: no test specified"` y finaliza con error.
    - Reservado para futuras tareas de tooling o validaciones adicionales.

- `npm run prepare`
    - Ejecuta `husky install`, instalando los hooks en `.husky/`.
    - Se ejecuta automáticamente después de `npm install`.

### Frontend (carpeta `Nomadia/`)

En el `package.json` del frontend (dentro de `Nomadia/`) encontrarás scripts típicos de Angular 20, como por ejemplo:

- `npm start` / `npm run start`
    - Suele ejecutar `ng serve` para desarrollo.
- `npm run build`
    - Genera el build de producción mediante `ng build`.
- `npm test`
    - Ejecuta los tests del frontend (`ng test`).
- `npm run lint`
    - Ejecuta el linter configurado para el proyecto.

Consulta el `package.json` de `Nomadia/` para ver los scripts exactos y mantenlos documentados aquí si cambian.

### Backend

En el backend (Maven + Spring Boot) los comandos típicos son:

- `mvn spring-boot:run` – levanta el servidor en desarrollo.
- `mvn test` – ejecuta los tests.
- `mvn clean package` – construye el artefacto ejecutable (`jar`).

O sus equivalentes con `mvnw` si usas el wrapper.

---

## Git hooks: Husky y commitlint

El proyecto utiliza **Husky** y **commitlint** para garantizar calidad en los commits.

### Husky

- Tras `npm install` en la raíz, se ejecuta `npm run prepare`, que a su vez lanza `husky install`.
- Esto crea/configura la carpeta `.husky/` y habilita los hooks definidos (por ejemplo, `pre-commit`, `commit-msg`).
- Un hook típico `pre-commit` puede ejecutar linters o tests rápidos antes de permitir el commit.

Si los hooks dejan de funcionar (por ejemplo, tras borrar `node_modules`), puedes reinstalarlos manualmente con:

```bash
npm run prepare
```

### commitlint

- La configuración está en `commitlint.config.js` y se basa en `@commitlint/config-conventional`.
- Se integra con el hook `commit-msg` de Husky para validar el mensaje de cada commit.

Formato esperado (Conventional Commits):

```text
<tipo>(<scope opcional>): <descripción breve>
```

Ejemplos de tipos:

- `feat`: nueva funcionalidad
- `fix`: corrección de bug
- `chore`: mantenimiento/configuración
- `docs`: documentación
- `refactor`: cambios internos sin alterar comportamiento externo
- `test`: cambios en tests

Ejemplos válidos:

- `feat(trip): agrega creación de viajes`
- `fix(auth): corrige validación de tokens`
- `chore(ci): actualiza pipeline de GitHub Actions`

Si el mensaje no cumple las reglas, el hook `commit-msg` fallará y el commit no se creará; deberás corregir el mensaje y volver a ejecutar `git commit`.

---

## CI/CD con GitHub Actions

El proyecto cuenta con un pipeline de **CI/CD** basado en **GitHub Actions** (workflows definidos en `.github/workflows/*.yml`).

Consulta los archivos de `.github/workflows/` para ver los jobs concretos configurados.

---

## Guía rápida de contribución

1. Actualiza tu rama de trabajo desde la rama principal de desarrollo (por ejemplo, `develop`).
2. Crea una rama nueva para tu cambio:

   ```bash
   git checkout develop
   git pull
   git checkout -b feature/mi-feature
   ```

3. Ejecuta `npm install` en la raíz para asegurarte de que los hooks están activos.
4. Trabaja en backend y/o frontend según corresponda.
5. Antes de commitear, ejecuta los tests/lints relevantes (Maven para backend, npm/Angular para frontend).
6. Escribe mensajes de commit válidos según las reglas de commitlint.
7. Abre un Pull Request explicando claramente los cambios y cómo probarlos.

---

## Licencia

Este proyecto está licenciado bajo la licencia **ISC**.

Si se añade un archivo de licencia específico al repositorio, consulta ese archivo para más detalles.