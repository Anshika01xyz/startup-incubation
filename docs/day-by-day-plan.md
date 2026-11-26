# 15-day implementation and coursework coverage plan

| Day | Deliverable | Evidence in this project |
|---|---|---|
| 1 | Requirements, workflows, module ownership and scope | `requirements-and-ownership.md`, `workflows.md` |
| 2 | Shared project skeleton and database contract | Maven structure, `schema.sql`, shared naming/roles |
| 3 | Shared foundations, models and session | `Db`, `Session`, `Security`, service and DAO packages |
| 4 | Module tables and JDBC CRUD | Founder, incubation, mentor, investor, funding and dataset tables; JDBC DAOs |
| 5 | Core business workflows | Registration, startup creation, application, evaluation, approval, assignment and funding |
| 6 | JavaFX screens and validation | Login, registration, tabs, role-aware panels, forms and dialogs |
| 7 | Integrated checkpoint | Shared database and end-to-end startup identifiers across modules |
| 8 | Collections, generics and custom exceptions | Lists/maps, typed collections, validation and service exceptions |
| 9 | File handling and persistence | CSV importer, startup document upload and persistent MySQL records |
| 10 | Logging, configuration and JavaFX completion | `AppLog`, properties, CSS theme and navigation |
| 11 | Differentiating features | Startup 360°, readiness score, milestone health and explainable investor fit |
| 12 | Cross-module integration and transactions | `Db.transaction`, evaluation, account creation, CSV import and investment transaction |
| 13 | Dashboards, timeline and reports | Dashboard counts, activity timeline, report SQL and serialized snapshot |
| 14 | Testing and quality gate | `testing.md`, PBKDF2 test, core Java compilation and local acceptance checklist |
| 15 | Documentation and final demo | Architecture, ERD, diagrams, syllabus mapping, demo script and handover checklist |

The table maps the implementation to the 15-day plan; it does not claim that every machine-specific integration test has already been run. Record the actual results from the target team's MySQL/Maven environment in the test checklist.
