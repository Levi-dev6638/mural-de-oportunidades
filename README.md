# NextHub — Opportunities Board

**English** · [Português (Brasil)](README.pt-BR.md)

I built NextHub to bring internships, courses, freelance work, and events together in a searchable, personalized opportunities board for students.

## Problem and solution

I saw students finding opportunities scattered across social networks, group chats, and physical boards. I centralized those listings in NextHub so students can find relevant options, keep a résumé and interests on their profile, save opportunities, and track applications. I also built a flow for companies to publish listings and manage applicants.

## MVP features

- I implemented student and company registration, login, and logout.
- I created student profiles with courses, interests, education, preferred format, and availability.
- I added PDF résumé uploads (up to 5 MB) and restricted access to the applicant and the relevant company.
- I implemented opportunity listings, text search, and filters by type, format, subject, and area.
- I added opportunity details, explainable recommendations, favorites, and internal or external applications.
- I let companies create, edit, and close opportunities, review applicants, and update application statuses. Companies can open a résumé from the candidate profile and change the status alongside the profile action; students can withdraw their applications.
- I configured application-based notifications to become read when opened; the unread indicator appears only while unread items remain in the current browser.
- I built a responsive interface with a light/dark theme, role-specific navigation, and a fixed mobile navigation bar that accounts for device safe areas. I kept favorite-grid columns consistent with fewer items, added a sliding tab indicator, and made opportunity details a centered, internally scrollable modal.
- I prepared fictional local demonstration data: 20 opportunities, 10 users, and 14 applications.

I require a résumé for internal applications and send students to the published application URL for external listings. I calculate recommendations deterministically from profile interests, education, and preferred format; the application does not call an external AI service.

## Technology

- **Frontend:** I chose React 18, Vite 6, JavaScript, and CSS.
- **Backend:** I chose Java 25, Spring Boot 3.5, Spring Data JPA, Hibernate 6, and Bean Validation.
- **Persistence:** I use SQLite and local files for development; the hosting configuration uses PostgreSQL and private Supabase Storage.
- **Tests and quality:** I use JUnit 5, Spring Boot Test, Maven, ESLint, and GitHub Actions.

## Repository layout

```text
.
├── Back-end/
│   ├── src/main/java/br/com/nexthub/
│   ├── src/main/resources/application.properties
│   ├── data/nexthub.sqlite
│   ├── uploads/
│   └── pom.xml
├── frontend/
│   ├── src/
│   ├── public/
│   └── eslint.config.js
├── .github/workflows/ci.yml
├── .env.example
├── package.json              # Backend scripts and dependencies
└── README.md
```

I organized `frontend/` and `Back-end/` as the single active application. I import fictional data from `backend/data/seed.json` only on first initialization and keep runtime data in SQLite. I back up `Back-end/data/nexthub.sqlite` before manual maintenance.

## Prerequisites and setup

- Java 25 or newer.
- Node.js 22.13 or newer and npm.
- Maven Wrapper (included).

To set up the project, I run these commands from the repository root:

```sh
npm ci
npm --prefix frontend ci
copy .env.example .env
```

On macOS/Linux, use `cp .env.example .env` instead of `copy`. The root `.env` configures only the Vite proxy. The backend optionally loads `Back-end/.env` at startup; if it is absent, the defaults in `application.properties` apply. Existing operating-system variables take precedence. Do not set `SPRING_PROFILES_ACTIVE=prod` for local development; without that explicit setting, SQLite and local file storage remain in use.

## Run locally

To run NextHub, I open two terminals from the repository root.

Terminal 1 — API (PowerShell):

```sh
cd Back-end
.\mvnw.cmd spring-boot:run
```

In Git Bash, macOS, or Linux:

```sh
cd Back-end
bash ./mvnw spring-boot:run
```

Terminal 2 — Vite frontend:

```sh
npm run dev:frontend
```

Then I open <http://localhost:5173>; Vite proxies my `/api` requests to `http://localhost:8080`.

I configured the backend to create or update `Back-end/data/nexthub.sqlite` with Hibernate (`ddl-auto=update`) without deleting existing data. I can set the allowed CORS origin with `NEXTHUB_CORS_ALLOWED_ORIGIN` (default: `http://localhost:5173`).

For demonstrations, I use these fictional accounts, both with the password `demo1234`:

- Student: `ana@nexthub.demo`
- Company: `talentos@nexthub.demo`

## API endpoints

In the Java API, I preserved the contracts used by the frontend:

- Authentication: `POST /api/auth/register`, `/api/auth/login`, and `/api/auth/logout`; `GET /api/me`.
- Profile: `PATCH /api/profile`.
- Opportunities: `GET` and `POST /api/opportunities`; `PATCH` and `DELETE /api/opportunities/{id}`; `GET /api/company/opportunities`.
- Favorites: `GET /api/favorites` and `POST /api/favorites/{id}`.
- Applications: `GET /api/applications`, `POST /api/applications/{id}`, `PATCH /api/applications/{id}`, and `DELETE /api/applications/{id}` for the owning student to withdraw an application.
- Résumés: `POST /api/resume` and `GET /api/resume/{id}`.

## Tests and lint

```sh
npm run lint:frontend
npm run build:frontend
```

To validate the API, I run Java tests in a separate terminal. In PowerShell:

```sh
cd Back-end
.\mvnw.cmd test
```

In Git Bash, macOS, or Linux:

```sh
cd Back-end
bash ./mvnw test
```

With the backend integration tests, I verify registration, profile updates including course persistence, résumé upload, application submission and withdrawal, and company opportunity publishing. With the 14 frontend browser tests, I verify registration and login, student applications, company publishing, notification read state, candidate profile and résumé access, card interactions, tabs, dialogs, and mobile navigation while scrolling. To run the browser tests, I install Chromium once and run:

```sh
cd frontend
npm ci
npx playwright install chromium
npm run test:e2e
```

I configured Playwright to start the API and Vite automatically and to use a temporary SQLite database separate from my local database.

## CI

I configured GitHub Actions to run on pull requests targeting `main` and on pushes to `main`. The workflow installs both dependency sets, lints and builds the frontend, runs backend integration tests, and exercises frontend happy paths with Playwright. I keep this configuration in `.github/workflows/ci.yml`. The latest run I checked for `dev`, on commit `7be0a89`, completed successfully ([run 7](https://github.com/Levi-dev6638/NextHub/actions/runs/45915995752)).

## Persistence and security

I modeled the main tables for users, opportunities, applications, favorites, and résumés; I calculate recommendations dynamically from the student profile. Locally, I use Hibernate (`ddl-auto=update`) with SQLite and store files in `Back-end/uploads/`. The `prod` profile uses PostgreSQL with Flyway migrations and database-backed HTTP sessions. Résumés are stored in a private Supabase Storage bucket; only the API uses the service key, after checking student or company authorization. I import `backend/data/seed.json` only when enabled and when the database has no users. I protect passwords with BCrypt hashes and limit résumé uploads to 5 MB.

## Deployment preparation (not published)

I prepared hosting with Vercel for the frontend, Render for the Java API, and Supabase for PostgreSQL and résumés. This repository has not created or published any hosted services. The recommended setup is:

1. **Supabase:** create a project and a private Storage bucket named `nexthub-resumes`. Restrict it to PDFs and 5 MB. In the **Connect** panel, copy the **Shared Pooler — Session mode** connection details; this mode works with Render's IPv4 network and supports JDBC sessions and migrations. Use the pooler values to configure the JDBC URL, username, and password. Flyway applies the schema migration when the API starts for the first time.
2. **Render:** create a Docker Web Service connected to this repository. Set the root directory to `.` (build context at the repository root), Dockerfile to `Back-end/Dockerfile`, and health check to `/actuator/health`. Add the environment variables below in the service dashboard; do not put them in source or the frontend.
3. **Vercel:** import the repository with `frontend/` as the root directory. The Vite build outputs `dist/`; the app uses relative `/api` paths, so the deployment does not need `VITE_API_URL`. Once Render provides the public API URL, replace `https://your-nexthub-api.onrender.com` in `frontend/vercel.json` with the actual API origin. The rewrite proxies `/api/*` through the frontend domain so browser sessions remain same-origin.

Render service variables:

| Variable | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | `jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require` |
| `DATABASE_USERNAME` | Exact pooler username shown by Supabase |
| `DATABASE_PASSWORD` | Supabase database password |
| `SUPABASE_URL` | `https://<project-ref>.supabase.co` |
| `SUPABASE_STORAGE_BUCKET` | `nexthub-resumes` |
| `SUPABASE_SERVICE_ROLE_KEY` | Project service key; backend-only secret |
| `NEXTHUB_CORS_ALLOWED_ORIGIN` | Exact HTTPS URL of the Vercel frontend |
| `NEXTHUB_SEED_ENABLED` | `false` |
| `DATABASE_POOL_MAX` | `5` (optional) |

In `.env.example`, `VITE_API_URL` configures only the local Vite proxy; the other values are references and Spring does not load this file. Set production variables in the provider dashboards; never commit keys, passwords, or `.env` files. Keep the bucket private and never configure the service key in Vercel. Demo seeding is disabled by default in `prod`.

**Important free-tier limitations:** Render Free can suspend the API after 15 minutes without traffic, takes time to wake up, and has an ephemeral filesystem; local SQLite and files do not persist there. Supabase Free currently includes 500 MB of database and 1 GB of file storage, but projects may pause after a week without activity, and the tier is not a backup strategy. Limits can change, so check the provider links before setup. Render explicitly recommends against using Free instances for production applications. This setup is therefore suitable for demos and evaluation, not production reliability; use paid services and verified backups if the data matters. Since résumés contain personal information, use fictional data until consent, retention, and privacy requirements are established.

Official references: [Supabase plans](https://supabase.com/pricing), [Supabase PostgreSQL connections](https://supabase.com/docs/guides/database/connecting-to-postgres), [private buckets](https://supabase.com/docs/guides/storage/buckets/fundamentals), [Render Free limitations](https://render.com/docs/free), and [Vercel Hobby plan](https://vercel.com/docs/plans/hobby).

Before deploying, validate locally:

```sh
npm run lint:frontend
npm run build:frontend
npm test
cd Back-end
.\mvnw.cmd -Pproduction -DskipTests package
```

Also verify `/actuator/health`, login and session persistence, registration/application flows, and résumé upload/download in the hosted environment. Confirm that an unauthorized student or company cannot download another user's résumé. The Docker build and real Supabase integration must be verified in the provider environment because they require Docker and external services/credentials.

## Team

- **My class:** TDS — mixed groups 6570, 6756, and 6863
- **Tech lead:** Levi (`Levi-dev6638`)
- **Me, presentation lead:** Lucas (`martinszvx`)
- **My teammate:** Giovana (`Giovana-Bernardes`)

## Links

- **Repository:** <https://github.com/Levi-dev6638/mural-de-oportunidades>
- **Deployment:** Prepared, not published
