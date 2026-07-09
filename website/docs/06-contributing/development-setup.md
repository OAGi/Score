---
title: Development Setup
sidebar_position: 2
---

This guide takes you from a clean machine to an open pull request. The first half shows
how to run connectCenter from source — start the data services (a MariaDB-compatible
database and Redis) in Docker, run the backend (`score-http`) with the Maven wrapper,
and run the frontend (`score-web`) with the Angular dev server. The second half covers
the contributor workflow: forking, branching, running the test suites, code style, and
the pull-request process.

If you only want to *run* a published build rather than build from source, see
[Installation with Docker](../02-getting-started/installation-docker.md) instead.

## Prerequisites

Install the following before you start. The versions below are read from the actual
project configuration.

| Tool | Version | Used by | Source of truth |
|---|---|---|---|
| JDK | **17+** | `score-http` | `score-http/pom.xml` (`maven.compiler.source` = 17, Spring Boot parent 3.5.15) |
| Node.js | **22.x** | `score-web` | `score-web/package.json` (`engines.node`) |
| Docker (with Compose v2) | recent | database, Redis | `docker-compose.yml` |

The backend's Maven build uses jOOQ with a MariaDB SQL dialect and the
`mariadb-java-client` JDBC driver, so the database you run locally must be
MariaDB/MySQL-compatible. The `srt-repo` image satisfies this.

You do **not** need a system-wide Maven install — the repository ships a Maven
wrapper (`score-http/mvnw`).

## Run connectCenter from source

### 1. Start the database and Redis in Docker

The backend needs two backing services: a MariaDB-compatible database (the
connectCenter repository) and Redis (HTTP session store and cache). The easiest way
to get a database pre-seeded with the connectCenter schema and initial data is the
published `srt-repo` image.

Pull and run the two services. The `srt-repo` image bundles the schema, so you do
not have to apply SQL by hand:

```bash
# Database (MariaDB/MySQL-compatible, pre-seeded connectCenter repository)
docker run --name srt-repo -d -p 3306:3306 oagi1docker/srt-repo:3.5.2

# Redis (session store + cache)
docker run --name redis -d -p 6379:6379 redis:7.4
```

These two `docker run` commands publish the services on their default ports
(`3306` for the database, `6379` for Redis), which is what the backend expects in
development (see the next section). The production
[`docker-compose.yml`](../02-getting-started/deployment.md) publishes the *same* host ports (plus
`4200`/`8080` for the web UI and gateway) and is meant for running the whole stack
from published images — do not bring it up alongside these standalone containers
(the ports would collide), and it is not what you use when developing the backend
or frontend locally.

:::tip[docker compose vs docker-compose]
Use the modern **`docker compose`** (Compose v2 plugin) form rather than the legacy
`docker-compose` binary.
:::

### 2. Run the backend (`score-http`)

`score-http` is a Spring Boot 3.5.x application (built as a WAR) under the
`org.oagi.score.gateway.http.*` packages. Run it with the Maven wrapper and the
`dev` Spring profile.

From the `score-http` directory:

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The `spring-boot-maven-plugin` is configured to activate the **`dev`** profile by
default, which enables Spring DevTools live-reload and restart. No extra flags are
needed for the common case.

#### What the backend listens on

The dev server starts on **port 9000** (configured in
`score-http/src/main/resources/application.yml`), not 8080. Once it is up, the
frontend dev-server proxy can reach it (see step 3).

#### Database / Redis connection settings

The backend reads its connection settings from environment variables, falling back
to localhost defaults that match the `docker run` commands above. The relevant
defaults (from `application.yml`) are:

| Setting | Env var | Default |
|---|---|---|
| DB host | `DB_HOST` | `localhost` |
| DB port | `DB_PORT` | `3306` |
| DB name | `DB_DATABASE` | `oagi` |
| DB user | `DB_USERNAME` | `oagi` |
| DB password | `DB_PASSWORD` | *(the dev image's example password)* |
| Redis host | `REDIS_HOST` | `localhost` |
| Redis port | `REDIS_PORT` | `6379` |

So if you started the containers exactly as shown in step 1, the backend connects
with no extra configuration. To point at a different database, export the
corresponding variables before running, e.g.:

```bash
DB_HOST=127.0.0.1 DB_PORT=3306 DB_USERNAME=oagi DB_PASSWORD=<your-db-password> ./mvnw spring-boot:run
```

#### Database migrations (Flyway)

The backend manages its own schema with **Flyway** on startup
(`spring.flyway.enabled` defaults to `true`). Migration scripts ship on the
classpath under `score-http/src/main/resources/db/migration` (e.g.
`V3_5_0__upgrade_from_3_4_2.sql`, `V3_5_1__upgrade_from_3_5_0.sql`). Flyway is
configured to baseline at version **3.4.0** (`FLYWAY_BASELINE_VERSION`), so a
database seeded from `srt-repo:3.5.2` is brought up to the current schema
automatically when you start the backend.

:::note[Two sets of SQL scripts]
The classpath `db/migration` scripts above are what Flyway applies at runtime. The
[`score-repo/scripts`](../05-operations/database-migration.md) directory holds the
broader, human-maintained migration chain as standalone files
(`mig_2.4.0_to_3.0.0.sql`, `mig_3.0.4_to_3.1.0.sql`, ... `mig_3.4.2_to_3.5.0.sql`)
plus the full `score-schema.sql`. You generally don't run those by hand for local
dev — the `srt-repo` image and Flyway take care of it.
:::

#### Optional: regenerate jOOQ sources

jOOQ code generation is **skipped by default** (`jOOQ.configuration.skip` is `true`
in `pom.xml`), so the normal `spring-boot:run` build does not touch your database
schema. You only need to regenerate jOOQ entities if you change the database schema;
that is an advanced step and requires a reachable `oagi` database.

### 3. Run the frontend (`score-web`)

`score-web` is an Angular **21.2.x** single-page application (TypeScript). Install
dependencies and start the Angular dev server.

From the `score-web` directory:

```bash
npm install
npm start
```

`npm start` runs `ng serve`, which serves the app on Angular's default dev port
**4200** ([http://localhost:4200](http://localhost:4200)).

#### How the frontend reaches the backend

The dev server proxies API calls to the backend so you don't hit CORS issues. The
proxy is defined in `score-web/src/proxy.conf.json` and wired into `ng serve` via
`angular.json`. Its behavior:

- Requests to **`/api`** are forwarded to **`http://127.0.0.1:9000`** (the backend
  dev port from step 2).
- The `/api` prefix is stripped before forwarding (`pathRewrite` maps `^/api` to
  ``), so a UI call to `/api/account` reaches the backend as `/account`.

```json
{
  "/api": {
    "target": "http://127.0.0.1:9000",
    "secure": false,
    "pathRewrite": {
      "^/api": ""
    }
  },
  "logLevel": "debug"
}
```

:::tip[Order of startup]
Start the database and Redis (step 1) first, then the backend (step 2), then the
frontend (step 3). If the backend isn't running on port 9000, the dev-server proxy
will log connection errors when the UI makes API calls.
:::

### Optional companion services

These two services are *not* required to run the core connectCenter web application.
Bring them up only if you are working on their features, and follow their own READMEs
for setup details.

- **`score-external-api`** — a NestJS 11 external REST API layer (Node.js). See
  `score-external-api/README.md`.
- **`connect-center-api`** — a Python **FastAPI + FastMCP** companion service that
  exposes the connectCenter repository over a REST API and an MCP server, with an
  interactive docs/playground. It reads the *same* connectCenter database used by the
  rest of the platform and requires Python 3.11+ (and Node.js for its docs site). See
  `connect-center-api/README.md`.

## Repositories and modules

A contribution usually touches one or more of these modules. See the
[Architecture Overview](./architecture.md) for how they fit together; the short
version for contributors:

| Module | Stack | What you change here |
|---|---|---|
| `score-http` | Java 17+, Spring Boot 3.5.x, jOOQ, Redis (WAR) | Backend REST API, business logic, schema export |
| `score-web` | Angular 21.2.x, TypeScript | Frontend SPA |
| `score-external-api` | NestJS 11 (Node.js) | External REST API layer |
| `connect-center-api` | Python FastAPI + FastMCP | Companion REST/MCP service |
| `score-repo` | SQL | DB schema + migration scripts |
| `score-e2e` | Java, Selenium, JUnit 5 | End-to-end UI tests |

## Fork and branch

The upstream repository is on GitHub under the OAGi organization. The contribution
flow is fork-based:

1. **Create or find an issue** before you start. If one does not exist, open it and
   let it be discussed/labeled (bug, enhancement, or design change) before coding.
2. **Fork** the upstream repository to your own account.
3. **Clone your fork** and add the upstream as a remote so you can keep in sync:

   ```bash
   git clone git@github.com:<your-user>/Score.git
   cd Score
   git remote add upstream https://github.com/OAGi/Score.git
   git fetch upstream
   ```

4. **Branch off the right base.** The active development line is the **`develop`**
   branch (the released line is **`master`**). Create your work branch from the base
   your change targets.

### Branch naming

connectCenter uses **issue-scoped branch names**. In this repository you will see
two patterns in active use:

- `develop_#NNNN` — work for issue `#NNNN` that targets the `develop` line (e.g.
  `develop_#1715`, `develop_#1692`, `develop_#1311`).
- `vX.Y_#NNNN` / `vX.Y.Z_#NNNN` — work for issue `#NNNN` that targets a specific
  release/milestone branch (e.g. `v3.4.2_#1678`, `v3.2.2_#1604`). The branch is
  later merged into its `vX.Y[.Z]` branch upstream.

So a branch for issue `#1740` against `develop` would be `develop_#1740`:

```bash
git fetch upstream
git switch -c develop_#1740 upstream/develop
```

:::note[Naming is a convention, not enforced]
The `develop_#NNNN` / `vX.Y_#NNNN` scheme is the convention observed across the
repository's branches; it ties each branch to its issue and target line. Match the
existing style and the line you are merging into.
:::

## Running the tests

### Backend unit/integration tests (`score-http`)

`score-http` uses the standard Spring Boot test stack (`spring-boot-starter-test`,
`spring-security-test`, `spring-restdocs-mockmvc`). Run them with the Maven wrapper
from the `score-http` directory:

```bash
./mvnw test
```

### Frontend tests (`score-web`)

`score-web` uses **Vitest** for unit tests and **Playwright** for browser-level
tests. The scripts are defined in `score-web/package.json`:

```bash
npm test          # vitest run
npm run lint      # ng lint
npm run e2e       # playwright test (needs: npm run e2e:install first)
```

### End-to-end tests (`score-e2e`)

`score-e2e` is a separate Maven module of **Selenium + JUnit 5** UI tests, organized
into test suites (the `TS_*` test-suite groups, made up of `TC_*` test cases). These
drive a *running* connectCenter instance through a real browser, so the full stack
must be up first: the database, Redis, the backend, and the frontend.

The `score-e2e` POM versions itself with the CI-friendly `${revision}` property, which
defaults to the current release; pass `-Drevision` only to override it. Run a single
test method like this:

```bash
cd score-e2e
mvn test \
  -Dtest='TC_29_1_BIEUplifting#method_name' \
  -Dsurefire.failIfNoSpecifiedTests=false
```

:::warning[E2E tests need a live, seeded stack]
- The tests target the frontend (Selenium drives a real browser against the running
  UI) and read the database directly via jOOQ, so the database, Redis, backend, and
  frontend all have to be running and reachable.
- Test configuration (base URL, DB connection, browser/driver, headless flag) lives
  in `score-e2e/src/test/resources/score-e2e.properties`. Point it at your running
  dev stack before launching.
- Selenium 4.33 resolves the browser driver automatically via Selenium Manager.
- E2E runs are slow (a single heavy test can take several minutes). Surefire reports
  land in `score-e2e/target/surefire-reports/`.
:::

If you modify an existing feature, check the relevant test suite (and the test-case
documents under `docs/test_cases/`) for assertions that may need updating — changes
often affect more than one feature.

## Code style

There is no separate style guide checked into the repo; follow the conventions of
the code you are editing.

- **Backend (`score-http`)** — Java 17 source level (`maven.compiler.source = 17` in
  `score-http/pom.xml`). Uses Lombok, jOOQ-generated entities (under
  `org.oagi.score.gateway.http.common.repository.jooq.entity`), and Spring
  conventions. Do not hand-edit jOOQ-generated entity classes; regenerate them if
  the schema changes (codegen is skipped by default — see
  [Optional: regenerate jOOQ sources](#optional-regenerate-jooq-sources) above).
- **Frontend (`score-web`)** — Angular 21.2.x / TypeScript. The project ships an
  ESLint config (`@angular-eslint`); run `npm run lint` and keep it clean before
  opening a PR.
- **Database changes** — add a migration script to `score-repo/scripts` following
  the existing chain
  (`2.4.0 -> 3.0.0 -> 3.1.0 -> 3.2.0 -> 3.3.0 -> 3.4.0 -> 3.4.1 -> 3.4.2 -> 3.5.0`).
  See the [Architecture Overview](./architecture.md#database-structure) for how the
  schema and migrations are organized.

## Pull-request process

The project tracks work through a kanban board on the issue, but the essentials for a
contributor are:

1. **Work the issue on your branch**, writing/updating test assertions and the user
   guide alongside the code as needed.
2. **Keep your branch current** with the base line (rebase or merge from
   `upstream/develop` or the target `vX.Y` branch).
3. **Open a pull request** from your fork's branch against the matching upstream base
   branch (`develop` for `develop_#NNNN` work; the corresponding `vX.Y[.Z]` branch
   otherwise). Reference the issue in the PR description and note which task(s) the PR
   covers — you may open a PR per task rather than one big PR at the end.
4. **Address review feedback.** If a PR is not accepted, iterate on the branch and
   update the PR.
5. **On merge**, the merger closes the issue. If a merge breaks a routine test, open
   a new issue (optionally citing the original).

:::note[Source for this workflow]
The fork/issue/PR workflow above is summarized from the project's contributing guide
(`docs/CONTRIBUTING.md`) and the
[General Guideline to Issue Management](./issue-management.md). The guide also points
to the test-case documents (`docs/test_cases/`) and user guide — worth reading before
modifying existing functionality, since changes frequently affect other features.
:::

## See also

- [Contributor Prerequisites](./prerequisites.md) — the technology stack and domain
  background per module.
- [Architecture Overview](./architecture.md) — topology, API packages, and database
  structure.
- [Issue Management](./issue-management.md) — issue lifecycle and labeling.
- [Installation with Docker](../02-getting-started/installation-docker.md) — run a
  published build without compiling.
- [Deployment Topology](../02-getting-started/deployment.md) — the production compose
  file, its services, and its ports.
