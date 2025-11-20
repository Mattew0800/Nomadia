# Nomadia

Proyecto monorepo basado en Node.js preparado para alojar tanto el frontend como el backend de Nomadia. En este repositorio se centraliza la configuración común (git hooks, linters, CI/CD, etc.) y el código de las aplicaciones.

> Importante: después de clonar el repositorio, ejecuta siempre `npm install` en la **raíz del proyecto** antes de empezar a trabajar (frontend, backend, cualquier cosa). Esto instala dependencias y configura los git hooks (Husky + commitlint).

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

Nomadia está planteado como un monorepo JavaScript/Node donde conviven:

- Una aplicación **backend** (API / servicios).
- Una aplicación **frontend** (cliente web).
- Posibles paquetes compartidos (utilidades, tipos, componentes comunes, etc.).

En el estado actual del repositorio se encuentra principalmente la configuración base del proyecto (tooling, hooks y metadatos), sobre la cual se apoya el código de frontend y backend que se desarrolla en ramas específicas.

La comunicación entre frontend y backend, el tipo de API (REST/GraphQL, etc.) y la base de datos se documentarán aquí de forma más detallada a medida que se consolide la implementación.

---

## Requisitos previos

Para trabajar con este proyecto necesitas tener instalado:

- [Node.js](https://nodejs.org/) (versión recomendada: LTS reciente).
- npm (incluido con Node.js).
- [Git](https://git-scm.com/).

Recomendado:

- Un editor con soporte para JavaScript/TypeScript y linters, por ejemplo:
  - WebStorm
  - Visual Studio Code (con extensiones para ESLint/Prettier cuando se añadan)

Sistema operativo soportado:

- Windows, macOS o Linux.

> Recuerda: siempre ejecuta `npm install` en la **raíz** del proyecto después de clonar, para que Husky pueda instalar los hooks de Git.

---

## Instalación y configuración inicial

1. **Clonar el repositorio**

   ```bash
   git clone https://github.com/Mattew0800/Nomadia.git
   cd Nomadia
   ```

2. **Instalar dependencias en la raíz**

   ```bash
   npm install
   ```

   Esto hará dos cosas:

   - Instalar las dependencias de desarrollo (`husky`, `@commitlint/*`, etc.).
   - Ejecutar automáticamente el script `prepare`, que instala los hooks de Husky en la carpeta `.husky/`.

3. **Instalar dependencias de frontend y backend (cuando existan carpetas de app)**

   Una vez que las aplicaciones de frontend y backend estén ubicadas en el monorepo (por ejemplo en `apps/frontend` y `apps/backend`), los pasos típicos serán:

   ```bash
   # Frontend
   cd apps/frontend
   npm install

   # Backend
   cd ../backend
   npm install
   ```

   Ajusta las rutas y comandos según la estructura real del proyecto cuando esté definida.

---

## Scripts de npm

En la **raíz del proyecto** (`package.json`) están definidos los siguientes scripts:

- `npm test`
  - Actualmente muestra el mensaje `"Error: no test specified"` y finaliza con error.
  - Está reservado para integrar la suite de tests (unitarios/integración/e2e) cuando se definan.

- `npm run prepare`
  - Ejecuta `husky install` y configura los git hooks en la carpeta `.husky/`.
  - Se ejecuta automáticamente después de `npm install` gracias al campo `"prepare"` en `scripts`.
  - Normalmente no es necesario llamarlo manualmente, salvo para reinstalar hooks si fuese necesario.

### Scripts habituales (frontend y backend)

Cuando el frontend y el backend estén integrados en este monorepo, es recomendable documentar scripts como:

- `npm run dev` / `npm start` – levantar la app en modo desarrollo.
- `npm run build` – generar el build de producción.
- `npm test` – ejecutar la suite de tests.
- `npm run lint` – ejecutar el linter.
- `npm run format` – formatear el código.

Estos scripts se documentarán en detalle (ubicación, uso, dependencias) conforme se vayan añadiendo a las diferentes aplicaciones.

---

## Estructura de carpetas

### Estructura actual (simplificada)

A grandes rasgos, el proyecto se organiza así:

- `.git/` – metadatos internos de Git.
- `.gitignore` – archivos y carpetas ignorados por Git.
- `.husky/` – configuración de los git hooks gestionados por Husky.
- `commitlint.config.js` – configuración de commitlint para validar mensajes de commit.
- `node_modules/` – dependencias instaladas mediante npm.
- `package.json` – metadatos del proyecto, scripts y dependencias.
- `README.md` – documentación principal del proyecto.

### Estructura objetivo (monorepo)

A medida que el proyecto crezca, la estructura puede evolucionar hacia algo similar a:

- `apps/frontend/` – aplicación web frontend.
- `apps/backend/` – API / servicios backend.
- `packages/shared/` – código compartido (tipos, utilidades, componentes, etc.).
- `config/` – configuraciones compartidas (ESLint, Prettier, tsconfig, etc.).
- `docs/` – documentación adicional (diagramas, decisiones de arquitectura).

Esta sección debe ir actualizándose para reflejar la estructura real a medida que se creen nuevas carpetas y aplicaciones.

---

## Uso en desarrollo

Mientras se consolida la estructura de frontend y backend en el monorepo, el flujo de trabajo general será:

1. Clonar el repositorio y ejecutar `npm install` en la raíz.
2. Instalar dependencias específicas en las carpetas de frontend/backend.
3. Levantar backend y frontend en paralelo.

Ejemplo de comandos típicos (los nombres concretos se documentarán cuando estén definidos):

```bash
# Backend
cd apps/backend
npm run dev

# Frontend (en otra terminal)
cd apps/frontend
npm run dev
```

El backend suele exponerse en un puerto tipo `http://localhost:3000` y el frontend en otro (por ejemplo `http://localhost:5173`); ajusta esta sección cuando tengas definidos los puertos reales.

---

## Variables de entorno

El proyecto puede utilizar variables de entorno tanto para el backend como para el frontend. La convención recomendada es:

- Usar archivos como `.env`, `.env.development`, `.env.production` según el entorno.
- Mantener un archivo de ejemplo (por ejemplo `.env.example`) con las claves esperadas pero sin valores sensibles.
- **No** commitear archivos `.env` reales al repositorio.

Ejemplo de tabla de variables de entorno que se puede ir completando:

| Variable        | Uso        | Obligatoria | Descripción                               | Ejemplo                |
|-----------------|------------|------------|-------------------------------------------|------------------------|
| `NODE_ENV`      | Backend    | Sí         | Entorno de ejecución (`development`, `production`, etc.). | `development`          |
| `PORT`          | Backend    | No         | Puerto en el que corre la API.           | `3000`                 |
| `API_BASE_URL`  | Frontend   | Sí         | URL base del backend consumida por el frontend. | `http://localhost:3000` |

Amplía y adapta esta tabla conforme se definan las variables reales del proyecto.

---

## Tests

La estrategia de testing se puede organizar en varios niveles:

- **Tests unitarios** – validan la lógica de funciones/módulos individuales.
- **Tests de integración** – validan la interacción entre componentes (p. ej., API + base de datos).
- **Tests end-to-end (E2E)** – validan flujos completos de usuario a través del frontend y backend.

En el estado actual:

- El comando `npm test` en la raíz simplemente muestra un mensaje de error estándar de Node y finaliza con código de salida `1`.
- Aún no se ha configurado un framework de tests concreto (Jest, Vitest, etc.), por lo que el comportamiento de `npm test` debe considerarse un placeholder.

Cuando se añada la suite de tests, este README deberá actualizarse para describir:

- Qué framework(s) de test se usan.
- Cómo ejecutar los distintos tipos de tests.
- Cómo generar y consultar informes de cobertura.

---

## Estilo de código, linting y formateo

Aunque todavía no se han añadido archivos de configuración de linters o formateadores al repositorio, se recomienda adoptar de forma progresiva herramientas como:

- **ESLint** – para asegurar un estilo de código consistente y detectar errores comunes.
- **Prettier** – para estandarizar el formateo de código.

Una vez integradas estas herramientas, es buena práctica añadir scripts como:

- `npm run lint` – ejecuta el linter sobre el código fuente.
- `npm run lint:fix` – intenta corregir automáticamente los problemas detectados.
- `npm run format` – formatea el código con Prettier.

Esta sección debe ampliarse cuando se definan las reglas concretas (por ejemplo, una configuración basada en Airbnb, StandardJS, etc.).

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
2. Configuración de la versión de Node.js.
3. Instalación de dependencias en la raíz (y, si aplica, en frontend y backend).
4. Ejecución de:
   - Linting.
   - Tests.
   - Build de frontend y backend.

Cuando se configuren los workflows concretos, es recomendable documentar aquí:

- Nombres de los workflows (por ejemplo, `ci.yml`, `deploy.yml`).
- En qué eventos se ejecutan (push, pull_request, tags, etc.).
- Reglas de protección de la rama principal (por ejemplo, requerir que la CI pase antes de poder hacer merge).

Para la parte de **CD** (despliegue), también se puede documentar:

- Dónde se despliega el frontend (Vercel, Netlify, etc.).
- Dónde se despliega el backend (Railway, Render, servidores propios, etc.).
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
