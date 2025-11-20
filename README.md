# Nomadia

Plataforma web compuesta por un **backend en Java/Spring Boot** y un **frontend en Angular 20 con SCSS**. En la raíz del repositorio se centraliza el tooling Node para **Husky** y **commitlint**, que gestionan los git hooks y se integran con el pipeline de **CI/CD**.

> ⚠️ **Muy importante**: después de clonar el repositorio, ejecuta siempre `npm install` en la **raíz del proyecto** antes de empezar a codear (frontend, backend, para todo). Esto instala las dependencias de tooling y configura los git hooks (Husky + commitlint).

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
    - Se encuentra en la carpeta `Nomadia/` del repositorio.
    - Utiliza Angular standalone components y organización por páginas, servicios, modelos y estilos.

En la **raíz del repo** (donde está este `README.md`) se encuentran:

- `package.json` con el tooling Node (Husky + commitlint).
- `.husky/` con los hooks de Git.
- `commitlint.config.js` con la configuración del formato de los mensajes de commit.

El backend Java/Spring Boot reside fuera de la carpeta `Nomadia/` (en su propia estructura Maven), mientras que el frontend Angular 20 está contenido dentro de `Nomadia/`.

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

Por defecto, Spring Boot suele levantar la API en `http://localhost:8080` (ajusta si tu configuración usa otro puerto).

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

Angular suele levantar la app en `http://localhost:4200` (ajusta según tu configuración). El frontend se conectará al backend usando la URL que tengas configurada (por ejemplo, `http://localhost:8080`).

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

## Variables de entorno

El proyecto puede usar variables de entorno tanto en backend como en frontend.

### Backend (Spring Boot)

- Configuración en `application.properties` o `application.yml`.
- Variables habituales:
    - `SPRING_PROFILES_ACTIVE` – perfil activo (`dev`, `prod`, etc.).
    - `SERVER_PORT` – puerto de la API (por defecto 8080).

### Frontend (Angular 20)

- Configuración en `environment.ts`, `environment.prod.ts`, etc.
- Una variable común sería:
    - `API_BASE_URL` – URL base del backend que consume el frontend.

Ejemplo de tabla que puedes ir completando:

| Variable                | Capa       | Obligatoria | Descripción                                      | Ejemplo                    |
|-------------------------|-----------|------------|--------------------------------------------------|----------------------------|
| `SPRING_PROFILES_ACTIVE`| Backend   | No         | Perfil activo de Spring Boot                     | `dev`                      |
| `SERVER_PORT`           | Backend   | No         | Puerto en el que corre la API                    | `8080`                     |
| `API_BASE_URL`          | Frontend  | Sí         | URL base de la API usada por Angular             | `http://localhost:8080`    |

---

## Tests

### Backend

- Framework típico: JUnit/Spring Test.
- Comando estándar:

  ```bash
  mvn test
  # o
  ./mvnw test   # según configuración
  ```

### Frontend

- Angular 20 utiliza `ng test` (normalmente envuelto en `npm test`).

  ```bash
  cd Nomadia
  npm test
  # o
  ng test
  ```

### Raíz del repo

- `npm test` en la raíz actualmente es solo un placeholder de tooling y no ejecuta una suite de tests real.

Cuando tengas definida la estrategia completa de testing (unit, integración, e2e) para backend y frontend, amplía esta sección con detalles de frameworks, reports de cobertura, etc.

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

Un flujo típico de CI para este proyecto puede incluir:

1. Checkout del código.
2. Configuración de JDK 21 y Node.js.
3. Instalación de tooling raíz:
    - `npm install` en la raíz (husky, commitlint, etc.).
4. Backend:
    - Instalación de dependencias y ejecución de tests con Maven (`mvn test`).
5. Frontend:
    - `cd Nomadia && npm install`.
    - `npm test` / `ng test`.
    - `npm run build` / `ng build`.

Para la parte de **CD** (despliegue), los workflows pueden encargarse de:

- Desplegar el frontend (por ejemplo, a un hosting estático o servicio de frontend).
- Desplegar el backend Spring Boot (por ejemplo, a un servidor, contenedor Docker, Kubernetes, etc.).
- Usar secretos configurados en *GitHub Secrets* (tokens, URLs, credenciales).

Consulta los archivos de `.github/workflows/` para ver los jobs concretos configurados y mantén esta sección alineada con ellos.

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

Este proyecto está licenciado bajo la licencia **ISC** (según el campo `license` de `package.json`).

Si se añade un archivo de licencia específico al repositorio, consulta ese archivo para más detalles.
