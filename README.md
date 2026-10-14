# Contact Management System

A contact management application with a Spring Boot REST API and a React (Vite)
single-page frontend. Users register or log in with an email or a phone number,
then create, read, update, delete, search and page through their own contacts.
Each contact can hold multiple labelled emails and phone numbers.

## Tech stack

Backend
- Java 21, Spring Boot 4.1.0, Maven
- Spring Web MVC, Spring Data JPA, Spring Validation, Spring Security
- MySQL (runtime), H2 (tests), Flyway migrations
- JJWT 0.12.6 for JWT creation/parsing
- BCrypt password hashing

Frontend
- React 19, React Router 7, Axios
- Vite 8 (dev server, build), ESLint 10

## Project structure

```
backend/                      Spring Boot API (Maven project)
  src/main/java/com/example/contactmanagementsystem/
    User / Contact / Email / Phone            JPA entities
    *Repository                                Spring Data repositories
    services/                                  UserService, ContactService
    controllers/                               AuthController, ContactController
    dto/                                       request/response payloads
    security/                                  JWT filter, util, UserDetailsService
    config/SecurityConfig                      filter chain and PasswordEncoder
    exception/GlobalExceptionHandler           maps exceptions to HTTP status
  src/main/resources/
    application.properties, application-dev.properties
    db/migration/V1..V6__*.sql                 Flyway migrations
  src/test/java/...                            unit, web, and repository tests

frontend/                     React SPA (Vite project)
  src/api/                    Axios client + auth/contacts endpoints
  src/pages/                  Login, Register, Contacts, Profile
  src/components/             forms, modals, navbar, route guards
  src/context/                AuthProvider (login/register/logout)
  src/lib/                    validators, session storage, JWT helpers
```

## Features

Backend
- Registration and login using either an email or a phone number; login accepts
  one identifier field.
- BCrypt-hashed passwords; password policy: at least 8 characters, with at least
  one letter and one digit (enforced in `UserService`).
- Email and phone share one unique login-identifier namespace. Registration
  rejects an identifier that already exists in either column.
- JWT (HS256) issued on register/login; stateless authentication via a bearer
  token processed by `JwtAuthFilter`.
- Contact CRUD scoped to the authenticated user; a user cannot read or modify
  another user's contacts.
- Partial contact updates: omitted scalar fields and omitted email/phone lists
  keep their stored values.
- Search contacts by first or last name (case-insensitive).
- Paginated listing, sorted by ascending id, with a maximum page size of 100.

Frontend
- Route guards: `/login` and `/register` for guests; `/contacts` and `/profile`
  require authentication; unknown paths redirect to `/contacts`.
- The bearer token is kept in memory only; the non-sensitive user profile is
  stored in `localStorage`. A 401/403 response clears the session.
- Contacts screen: paginated list (10 per page), debounced name search, create
  and edit form (multiple emails/phones with labels), delete confirmation.
- Profile screen: account details, change-password modal, log out.
- Client-side validation for email, phone (7–15 digits), and password strength.

## Prerequisites

- JDK 21
- Maven 3.9+
- MySQL 8 (a running server and the application database)
- Node.js 22+ and npm

## Configuration and environment variables

Backend (`backend/src/main/resources/application.properties`):

| Variable | Purpose | Example |
| --- | --- | --- |
| `DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/contact_management_db` |
| `DB_USERNAME` | Database user | `root` |
| `DB_PASSWORD` | Database password | `<your-db-password>` |
| `JWT_SECRET` | JWT signing key (>= 32 bytes) | `<your-jwt-secret>` |
| `SPRING_PROFILES_ACTIVE` | Active profile (default `dev`) | `dev` |

`JWT_SECRET` is required in production. The `dev` profile supplies fallback
username/password and a development-only secret so the app can start locally
without exported variables.

Frontend:

| Variable | Purpose | Example |
| --- | --- | --- |
| `VITE_API_BASE` | API base URL; must use `https:` when set | `https://localhost:8080/api` |

When `VITE_API_BASE` is unset the frontend calls the relative `/api` path, which
the Vite dev server proxies to the backend (see `frontend/vite.config.js`).

## Database

Schema is managed by Flyway (`spring.jpa.hibernate.ddl-auto=validate`). Create
the `contact_management_db` database first; the migrations create the `users`,
`contact`, `email` and `phone` tables and their foreign keys. Migrations V5 and
V6 add a preflight check that aborts with an error if orphaned rows exist before
enforcing NOT NULL ownership columns.

## Running the backend

```
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. To skip the test compile during a
local run use `mvn spring-boot:run -Dmaven.test.skip=true`.

## Running the frontend

```
cd frontend
npm install
npm run dev
```

The dev server runs over HTTPS (self-signed certificate via
`@vitejs/plugin-basic-ssl`); accept the browser trust prompt on first visit.
Other scripts: `npm run build`, `npm run preview`, `npm run lint`.

## API endpoints

Authentication (`/api/auth`)
- `POST /register` — create a user, returns a token and profile fields
- `POST /login` — authenticate by identifier, returns a token and profile fields
- `POST /change-password` — change the current user's password (authenticated)

Contacts (`/api/contacts`, all require a bearer token)
- `POST /` — create a contact
- `GET /` — list the user's contacts
- `GET /{id}` — fetch one contact
- `PUT /{id}` — update a contact (partial)
- `DELETE /{id}` — delete a contact
- `GET /search?query=` — search by first or last name
- `GET /paginated?page=&size=` — paginated list

Errors are returned as JSON `{"error": "..."}`. Status codes: 404 (not found),
409 (duplicate), 401 (invalid credentials), 400 (bad request / malformed JSON),
500 (unhandled).

## Tests

```
cd backend
mvn test
```

62 tests: MockMvc slice tests for the controllers, `@DataJpaTest` tests for the
repositories, and Mockito-based service tests. Tests run against an in-memory H2
database with Flyway disabled (`src/test/resources/application.properties`).

## Development approach

<!-- Fill in: how this project was built and reviewed. -->

## Questions for the author

- The frontend keeps the JWT in memory only while the backend issues it as a
  response field (not a cookie). Was an HttpOnly cookie intended, and if so,
  where does the token reach the browser?
- `spring.jpa.open-in-view` is enabled. Is that intentional, or should it be
  disabled with explicit fetching in the service layer?
- Contacts have no validation on `firstName`/`lastName` in `ContactRequest`.
  Should a contact require at least a name?
- `JwtUtil.extractSubject` is still present but unused. Should it be removed?
- Are roles or authorities planned, or is authentication-only intended?
