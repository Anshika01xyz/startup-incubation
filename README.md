# FOUNDRY
## Startup Incubation & Investment Management Platform

A single integrated Java desktop application for the UCS553 Enterprise Web Application coursework. It connects founders, startups, incubators, evaluators, mentors and investors through one JavaFX interface and one MySQL database.

**Visual direction:** a light olive, lilac, beige and soft-orange palette. The workspace uses a compact left navigation, role-labelled sign-in and a product-style dashboard rather than a row of top tabs.

## 1. Technology
- Java 21
- JavaFX 21 (programmatic JavaFX UI)
- MySQL 8
- JDBC with `PreparedStatement`, `ResultSet`, transactions and try-with-resources
- Maven build
- CSV file import, file upload, Java serialization and Java Util Logging

## 2. Integrated modules

| Module | Main workflows |
|---|---|
| Founder & Startup | Self-registration, profile, startup creation, documents, incubation applications, funding requests |
| Incubation & Evaluation | Incubator/programme setup, application review, four-part evaluation scorecard, mentor self-registration and approval, mentor assignment, milestones |
| Investor & Funding | Investor registration, sector/stage preferences and ticket ranges, explainable 40/35/25 sector-stage-ticket fit matching, approved-startup opportunities, supporting documents and investment commitments |
| Shared platform | Authentication, role checks, MySQL database, transaction handling, notifications, dashboard, Startup 360°, readiness score, milestone health, activity timeline, reports and dataset import |

All three modules use the same database and the same application session. A startup created by a founder is the same startup evaluated by an evaluator, supported by an assigned mentor, and discovered by an investor. Live platform funding requests, investor ticket ranges and commitments are stored in INR; historical dataset funding amounts remain in USD as published.

## 3. First-time setup

### Prerequisites
Install JDK 21, Maven 3.9 or later, and MySQL 8. Make sure `java`, `mvn`, and `mysql` work in a terminal.

### Database
1. Start MySQL.
2. From the project root, run:
   `mysql -u root -p < database/schema.sql`
3. Edit `src/main/resources/application.properties` with your local MySQL username/password, or set environment variables `DB_URL`, `DB_USER`, and `DB_PASSWORD`. The checked-in password is a placeholder, not a real credential.
4. For the ready-to-demonstrate setup, run `mysql -u root -p < database/sample_data.sql` (or run `setup-db.bat`, which runs both scripts). It loads linked fictional operating records and six sample role accounts (ADMIN, FOUNDER, EVALUATOR, MENTOR, INVESTOR and INCUBATOR). All six use password `Demo@12345`: `admin.demo@foundry.local`, `founder.demo@foundry.local`, `evaluator.demo@foundry.local`, `mentor.demo@foundry.local`, `investor.demo@foundry.local`, and `incubator.demo@foundry.local`. If you load the demo accounts, sign in with `admin.demo@foundry.local`; the first-admin setup is no longer needed.
5. If upgrading a database created by an earlier Foundry ZIP, run `mysql -u root -p < database/migrate_dashboard_v2.sql` before loading the updated sample data. Fresh installations do not need this migration.
6. The schema creates shared roles, evaluation criteria and the relational tables automatically. After seeding, optional read-only checks are available with `mysql -u root -p < database/verify_live_workspace.sql`.

### Launch
Run `mvn clean javafx:run` from the project root. The first successful sign-in attempts the historical data bootstrap automatically.

### Create the first administrator
On the login screen, choose **First-time administrator setup**. This action is allowed only while no ADMIN role has been assigned. Create the administrator account. After that, this first-admin route is permanently disabled by the database check.

The administrator can create evaluator accounts and add incubators/programmes from Administration. Founders, mentors, investors and incubator managers can register themselves using Create an account. Mentor accounts remain pending until the administrator approves them; only approved mentors can sign in and be assigned. Incubator managers can create and manage programmes belonging to their own incubator account. The login role selector is checked against the role returned by authentication; selecting another role does not grant access.

## 4. Recommended demo sequence
1. Create the first administrator.
2. Sign in as admin; create an incubator and an incubation programme. Create an evaluator account.
3. Register a founder; sign in as founder and create a startup. Upload a supporting document.
4. Submit an incubation application using a programme ID shown in the Incubation tab.
5. Sign in as evaluator; score innovation, market, feasibility and team (0–10) and record a recommendation.
6. Register a mentor; sign in as admin and approve the pending mentor. Assign the approved mentor to the startup.
7. Create milestones. Sign in as the assigned mentor, view assigned milestones and update progress with a note. Check the founder notification.
8. Founder creates a funding request and uploads its supporting document. Register an investor, add a sector preference and ticket range, view startup matches and record an investment commitment. Confirm the founder notification and remaining-request balance check.
9. Open Startup 360° to show linked application, mentor, milestone, funding and readiness information.
10. After sign-in, the Overview dashboard opens first. It includes funding-record KPI cards, sector and year charts, funding-stage mix, company rankings, latest funding rounds, live role-scoped platform totals and recent activity. The Startup Directory, Funding & Investors and Startup 360° views present the live records alongside the imported market records in consistent labelled tables. Incubation programmes and applications, mentor profiles and assignments, and milestone tracking show linked operational rows. Market Intelligence includes a role-scoped table of current startups as well as all 5,006 market funding records, with full-database search/filter and 100-row pagination.
11. Open Reports & Activity to show platform totals, milestone health and activity timeline. Export the serializable snapshot, then reopen it to demonstrate deserialization.

## 5. Public dataset
The selected source is **Fliar BI, Indian Startup Funding Rounds 2015–2026 (sample release)**. It contains 5,006 sample rows from a 13,697-round full dataset, with 19 fields including company, city, sector, stage, amount, lead investor and source references. Coverage is January 2015–June 2026. The sample is licensed CC BY-NC 4.0; retain attribution and observe the non-commercial restriction. Source page: https://fliarbi.com/datasets/india-startup-funding/ . Dataset DOI: https://doi.org/10.5281/zenodo.23431359 .

On every successful sign-in, the application checks the database count first. If all 5,006 publisher rows are already present, it does not need a network download. Otherwise, it uses `dataset/india_startup_funding_sample.csv`, automatically downloading the publisher CSV when absent, validates that the source contains exactly 5,006 data rows, and imports missing rows in a single transaction. No import tab or manual CSV selection is presented. If download or validation fails, the error is logged and automatic bootstrap is retried on the next sign-in. The importer maps the published column names, uses `round_id` as its deduplication key and skips previously imported rows. It stores public funding rounds in `external_funding_rounds`, separate from live funding requests and investments. Imported market rounds remain read-only and are not presented as live fundraising opportunities.

After login, every current application role (ADMIN, FOUNDER, MENTOR, EVALUATOR and INVESTOR) lands directly on its Startup Intelligence / Market Intelligence workspace. The workspace displays a database-backed paginated table (100 rows per page) with a total-record counter, full-dataset search by startup/city/country/lead investor, sector and funding-stage filters, and next/previous navigation. Search and counts are run against the full database, not only the current page. Table columns are tailored to the authenticated role; the Java service checks the session role before returning records. The full dataset remains browseable across all pages, without loading all 5,006 rows into memory at once.

The dataset's public sample does not provide founder profiles, mentors, incubator applications, evaluation scores or milestone updates. Those operational records are created through the application; do not represent invented operational records as real-world data.

## 6. Database design
The readiness score is transparent and consistent in Java and MySQL: `(innovation + market + feasibility + team) / 4 × 10`, yielding 0–100. Investor fit is rule-based and explainable: sector compatibility contributes 40 points, startup-stage compatibility contributes 35 points, and a funding request within the investor's ticket range contributes 25 points. Only approved/incubated or fundraising startups are shown to investors.

See `database/schema.sql` for tables, constraints, indexes and views. The design uses foreign keys, unique constraints, status checks, transaction-safe workflows, DECIMAL currency columns, activity history and a separate historical dataset table. `database/reports.sql` contains reusable reporting queries.

## 7. Java syllabus implementation
The project deliberately demonstrates Java concepts in executable code, not only in documentation: OOP inheritance/polymorphism/interfaces/records, role-specific profile classes, generic repository-backed dashboard ranking, collections and streams, Optional, custom exceptions, Math-based readiness scoring, LocalDate/LocalDateTime, file input/output, robust CSV parsing, object serialization/deserialization, properties configuration, Java Util Logging, JDBC DAOs/transactions and JavaFX lifecycle/controls/event handlers. See `docs/syllabus-mapping.md` for exact source-file references. Run `scripts/verify-core.ps1` (Windows PowerShell) or `sh scripts/verify-core.sh` (macOS/Linux) for the repeatable 11-check core verification.

## 8. Project map
- `src/main/java/com/startup/platform/` — compact Java package structure matching the requested layout:
  - `controller/` — JavaFX application and screen/event handling
  - `service/` — shared business workflows, analytics, readiness and snapshot operations
  - `repository/` — JDBC DAOs and generic repository implementations
  - `model/` — domain models, profile hierarchy, session and serializable snapshot
  - `exception/` — application validation and authorization exceptions
  - `util/` — JDBC connection/transaction helper, security, CSV import, file and logging utilities
  - `config/` — application configuration loading
- `src/main/resources/static/css/app.css` — light professional JavaFX theme
- `src/main/resources/application.properties` — local DB configuration; `logging.properties` — Java Util Logging defaults
- `database/schema.sql` — complete schema and reporting views
- `database/sample_data.sql` — linked sample operating records and role-based sign-in accounts
- `database/reports.sql` — SQL reporting queries
- `database/verify_live_workspace.sql` — read-only checks for live data coverage and retained publisher rows
- `dataset/` — instructions for the separately sourced CSV
- `docs/` — workflows, ERD, architecture, testing, syllabus mapping and demo
- `uploads/`, `reports/`, `logs/` — runtime-generated files

## 9. Build and test
- `mvn clean test`
- `mvn clean javafx:run`

The repeatable core verification compiles all non-JavaFX production sources with JDK 21 and runs 11 checks. Run `sh scripts/verify-core.sh` (or the PowerShell equivalent) after changes. A full Maven/JavaFX launch and live MySQL end-to-end test still need to be run on a machine with Maven and MySQL configured. Follow `docs/testing.md` and record actual results before submission.

## 10. Recent update
- Replaced the top-tab shell with a compact role-specific left navigation and redesigned the sign-in/registration surfaces.
- Added a role selector on login with a server-authenticated role comparison.
- Removed the dataset import screen; the 5,006-row funding dataset is validated and auto-loaded into MySQL when needed. All six application roles retain their role-specific workspaces. Market Intelligence includes live, role-scoped startup activity and the publisher funding records, with role-tailored columns, full-database search, sector/stage filters, record totals and 100-row pagination. The former 1,000-row query cap was removed.
- Applied the requested light olive/lilac/beige/orange visual system.
- The UI error dialog continues to show the actual exception class, message and expandable stack trace; the same failure is written to logs.

## 11. Standalone backend runner
`BackendOnly.java` is a separate, single-file terminal backend demonstration. Run `java BackendOnly.java` for the interactive role-based menu, or `java BackendOnly.java --demo` for an automated lifecycle demonstration. It persists its local test state under `backend-data/`. This self-contained runner uses Java serialization so it can execute without MySQL or Maven; it is a terminal demonstration harness, while the actual application backend in `src/main/java/` uses MySQL/JDBC.

## 12. Security and scope notes
This is an educational coursework application, not a production investment service. It uses salted PBKDF2-HMAC-SHA256 password hashes, role checks and role-scoped data access in service methods, prepared statements, foreign keys and transaction rollback. UI error dialogs display the exception class and message, with technical stack details available in an expandable section and logs. Do not store real financial credentials or process actual payments. Investment entries are coursework records of commitments only; no payment gateway is connected. Configure database credentials locally and never commit real passwords.


## 5,006-row workspace integration

The application bootstraps the publisher dataset into `external_funding_rounds`. The imported market records are available in the Overview, Startup Directory, Funding & Investors, Startup 360° and Market Intelligence views, using the same labelled table layouts. Incubation and Mentor Network display linked live programmes, applications, approved mentor assignments, milestones and pending mentor approvals. Imported market rows remain read-only; no source-type marker is shown in the workspace tables.


## Applying the expanded workspace records

After replacing the project files, refresh the operational sample records from the project root with `mysql -u root -p < database/sample_data.sql`. The script is repeatable: it adds any missing linked records, normalizes older sample labels, and preserves the imported 5,006 funding rows in `external_funding_rounds`. Do not rerun `schema.sql` on an existing installation unless you are performing a first-time database setup.
