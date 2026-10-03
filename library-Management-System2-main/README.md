# Sharma College — Library Management System

A full-stack Library Management System built with **Java Spring Boot**, **MySQL**,
and a **plain HTML/CSS/JavaScript** frontend (no frameworks, no build step).

Flow implemented: Student Registration → Login → Book Search → Book Availability
→ Issue Request → Librarian Approval/Issue → Book Issued → Due Date → Return →
Fine Calculation → Payment/Settlement → History.

---

## 1. Requirements

- **Java 17** or newer (`java -version`)
- **Maven 3.8+** (`mvn -version`) — or use an IDE (IntelliJ / Eclipse / VS Code) that has Maven built in
- **MySQL 8.x** running locally (or update the connection URL to point at any MySQL server)

---

## 2. Database setup

You do **not** need to manually create tables — Hibernate creates/updates them
automatically on startup (`spring.jpa.hibernate.ddl-auto=update`), and
`data.sql` seeds three demo accounts + all book categories.

You only need to make sure a MySQL server is running and reachable. The app
will auto-create the `library_management_system` database itself
(`createDatabaseIfNotExist=true` in the JDBC URL) — you just need a MySQL
**user** with permission to create databases, e.g. `root`.

If you'd rather create the database yourself first:

```sql
CREATE DATABASE library_management_system;
```

### Configure your credentials

Open `src/main/resources/application.properties` and edit these three lines
to match your MySQL setup:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_management_system?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```

---

## 3. Running the project

### Option A — Command line (Maven)

```bash
cd library-management-system
mvn spring-boot:run
```

### Option B — Build a jar and run it

```bash
mvn clean package
java -jar target/library-management-system.jar
```

### Option C — IDE

Import the folder as a Maven project in IntelliJ IDEA / Eclipse / VS Code,
let it download dependencies, and run `LibraryManagementApplication.java`.

---

Once running, open your browser at:

```
http://localhost:8080
```

The frontend (login page) is served automatically — the whole app (backend
API + frontend pages) runs from this single Spring Boot process on port 8080.
There's nothing else to start separately.

---

## 4. Demo accounts (seeded automatically)

| Role      | Email                   | Password       |
|-----------|--------------------------|----------------|
| Admin     | admin@library.com        | admin123       |
| Librarian | librarian@library.com    | librarian123   |
| Student   | student@library.com      | student123     |

You can also click **"Create an account"** on the login page to self-register
as a new student.

---

## 5. What's included

### Backend (Spring Boot, package `com.library.lms`)
- **JWT-based authentication** with role-based authorization (ADMIN / LIBRARIAN / STUDENT)
- Entities: `User`, `Category`, `Book`, `BookCopy`, `BookIssue`, `Reservation`, `Fine`, `Notification`
- Full REST API under `/api/**` — see the controller classes in
  `src/main/java/com/library/lms/controller` for every endpoint
- Business rules:
  - Configurable fine per day and max fine (`application.properties` → `library.fine.*`)
  - Configurable default loan period (`library.issue.default-days`, default 14 days)
  - Automatic book-copy generation when a book is added (e.g. `Total Copies: 10` creates 10 `BookCopy` rows)
  - Reservation queue with automatic notification when a copy becomes free
  - Overdue status is refreshed on-demand (no separate scheduler needed) whenever a dashboard or report is loaded

### Frontend (`src/main/resources/static`)
Plain HTML + CSS + vanilla JS, no build tools required — just static files
served by Spring Boot.

- `index.html` / `register.html` — login & student self-registration
- `student-*.html` — student dashboard, book search & request, my books, reservations, fines, notifications
- `librarian-*.html` — librarian dashboard, issue requests (+ walk-in direct issue), issued/overdue books & returns, books & categories, student lookup, fine management, reports
- `admin-*.html` — admin dashboard, books & categories, students, librarian account management, issues & returns, fines, reports

---

## 6. Project structure

```
library-management-system/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/library/lms/
    │   ├── config/          → SecurityConfig
    │   ├── controller/      → REST controllers
    │   ├── dto/             → request/response DTOs
    │   ├── entity/          → JPA entities + enums
    │   ├── exception/       → custom exceptions + global handler
    │   ├── repository/      → Spring Data JPA repositories
    │   ├── security/        → JWT filter/util, user details
    │   └── service/         → business logic
    └── resources/
        ├── application.properties
        ├── data.sql          → seed data (demo users + categories)
        └── static/           → the entire frontend (HTML/CSS/JS)
```

---

## 7. Notes / known limitations (be aware of these before your demo/viva)

- **Password hashes in `data.sql`** are BCrypt hashes generated for the
  passwords listed above — do not edit them unless you regenerate new hashes.
- Reducing a book's **Total Copies** below the number of copies currently
  issued is not blocked by validation — avoid doing this in the demo, or add
  a validation check if you need it for your submission.
- There is no email/SMS integration — "notifications" are stored in the
  database and shown in-app only (see the Notifications page for students).
- This project was assembled and written outside of a live Maven/MySQL
  environment, so **please run `mvn clean package` (or `mvn spring-boot:run`)
  yourself as the first step** and fix/report anything that doesn't compile —
  everything was written and reviewed carefully by hand, but it has not been
  compiled in this exact environment.
- CORS is fully open (`*`) for convenience during development — tighten
  `SecurityConfig.corsConfigurationSource()` before any real deployment.

---

## 8. Quick feature checklist (matches your original spec)

- [x] Authentication & role management (Admin / Librarian / Student)
- [x] Student management (fields, search)
- [x] Book management (fields, Book vs Book Copy as separate entities)
- [x] Book categories (CRUD)
- [x] Search & filter (keyword, category, author, publisher, language, availability)
- [x] Book issue system (request → librarian approval → issued), plus walk-in direct issue
- [x] Return system with automatic late-day & fine calculation
- [x] Fine management (per-day rate, max cap, pending/paid/waived)
- [x] Book reservation with queue position + availability notification
- [x] Notifications (in-app)
- [x] Student / Librarian / Admin dashboards
- [x] Reports (most borrowed books, most active students, category-wise, department-wise, summary stats)
- [x] Database design with separate `Book` and `BookCopy` tables, as specified

---

## 9. Deployment on Railway

This project can be deployed as a single Spring Boot service with a MySQL
database.

### Run with Docker locally

If Docker Desktop is installed, run the app and MySQL together with:

```bash
docker compose up --build
```

Then open:

```text
http://localhost:8080
```

Stop the containers with:

```bash
docker compose down
```

### Deploy to Railway

1. Push the latest code to GitHub.
2. Open Railway and create a new project from this GitHub repository.
3. Add a MySQL database service to the same Railway project.
4. In the Spring Boot service, add these variables:

```properties
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JWT_SECRET=<base64-encoded-256-bit-secret>
```

5. Deploy the Spring Boot service.
6. In the service Networking settings, generate a public domain.

The included `Dockerfile` builds the Maven project and runs
`target/library-management-system.jar`. Railway will use it automatically when
deploying from GitHub.
