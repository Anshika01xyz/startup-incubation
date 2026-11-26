# UCS553 Java syllabus implementation map

This map points to executable Java code and project workflows. The source code is the evidence; this document is only an index for the viva/demo.

## Unit 1 — Java foundations, OOP and build tools

| Syllabus concept | Concrete implementation | How to demonstrate |
|---|---|---|
| JDK, JRE and JVM | JDK 21 toolchain; Maven compiler targets Java 21 | Show `java -version`, `javac -version`, then run Maven goal |
| Classes, objects and constructors | `PlatformService`, `Db`, `Security`, domain model constructors | Create service/model objects and execute app workflows |
| Encapsulation and access modifiers | Private fields/final state in `AbstractProfile`; public getters and controlled constructors | Inspect profile classes and their immutable state |
| Inheritance | `FounderProfile`, `MentorProfile`, `InvestorProfile` extend `AbstractProfile`; `PlatformService.register` creates the matching profile subtype | Register each account type and inspect its role-specific profile summary in the activity log |
| Abstract classes and polymorphism | `AbstractProfile` declares abstract `profileType()` and `summary()`; registration stores the subtype through an `AbstractProfile` reference | Inspect the shared profile reference and role-specific summary |
| Interfaces | `Identifiable`, `CrudRepository`, `Db.SqlWork`; `StartupSummary` implements `Identifiable` and dashboard ranking uses `CrudRepository` | Show the dashboard's temporary typed summary repository |
| Records | `Session`, `PlatformSnapshot`, `StartupSummary`, `EvaluationScorecard` | Show compact immutable data models |
| Packages and modifiers | `com.startup.platform` package tree with `controller`, `service`, `repository`, `model`, `exception`, `util` and `config` | Inspect package separation by model, DAO, service, config and utilities |
| Maven | `pom.xml` declares JavaFX, MySQL JDBC, JUnit and build plugins | Run `mvn clean test` and `mvn javafx:run` |

## Unit 2 — Exceptions, collections, generics and utility APIs

| Syllabus concept | Concrete implementation | How to demonstrate |
|---|---|---|
| Built-in and custom exceptions | `ValidationException`, `AuthorizationException`; validation and role checks in `PlatformService` | Try invalid scores, invalid amounts and forbidden role actions |
| Collections | `List`, `Map`, `Set` in DAOs, services and analytics | Display dashboard collections and sector counts |
| Generics | `CrudRepository<T, ID>`, `InMemoryRepository<T, ID>`, generic `CollectionExercises.nonNullCopy` | Save and retrieve a `StartupSummary` using a typed repository |
| Optional | `CrudRepository.findById` and `PlatformService.startupSummary` | Demonstrate present and empty startup lookup results |
| Streams and collectors | `StartupAnalyticsService` stream sorting/grouping; `CollectionExercises` status aggregation; `DatasetFileService` file-line stream | Show top-readiness sorting, status distribution and sector grouping on the live dashboard |
| Math utilities | `ReadinessCalculator` uses `Math.clamp` and rounding | Evaluate four scores and display normalized readiness out of 100 |
| Date/time API | `LocalDate` for milestones and `LocalDateTime` for profile/snapshot timestamps | Add milestone due date and export a timestamped snapshot |

## Unit 3 — File handling, serialization, configuration and logging

| Syllabus concept | Concrete implementation | How to demonstrate |
|---|---|---|
| File input | `CsvImporter`, `DatasetFileService`, `Files.newBufferedReader`, `Files.lines` | Preview and import the external CSV |
| File output and uploads | `PlatformService.uploadStartupDocument`, `uploadFundingDocument` | Upload a supporting document and show its saved path/DB metadata |
| CSV parsing | `CsvImporter` handles quoted fields, escaped quotes, commas and embedded newlines | Import publisher CSV; show inserted/duplicate/skipped counts |
| Serialization | `PlatformSnapshot`, `SnapshotService.export` and filtered `SnapshotService.read` | Export a `.ser` snapshot, then read and verify it in the UI |
| Configuration | `PlatformConfig` loads `application.properties` with environment overrides | Change DB config or set `DB_URL`, `DB_USER`, `DB_PASSWORD` |
| Logging | `AppLog` writes rotating logs under `logs/` | Cause a validation error and inspect `logs/application-*.log` |

## Unit 4 — JDBC and relational database programming

| Syllabus concept | Concrete implementation | How to demonstrate |
|---|---|---|
| JDBC connection | `Db.get()` uses MySQL Connector/J | Sign in and load dashboard data |
| CRUD and DAO pattern | `StartupDao`, `IncubationDao`, `FundingDao`, `ActivityDao` | Create startup/application/funding records and reload them |
| PreparedStatement | All user-supplied SQL values use parameterized statements | Inspect DAO/service source |
| ResultSet | DAO/service query methods map result rows into Java collections | Refresh startup, application, funding and activity lists |
| Transactions | `Db.transaction` wraps account creation, evaluations, investments and multi-step workflows | Demonstrate successful investment or force an invalid action and verify rollback |
| Commit and rollback | `Db.transaction` and `CsvImporter.importWithReport` | Verify import rollback in a disposable test DB |
| Relational integrity | Foreign keys, unique keys, checks and indexes in `database/schema.sql` | Attempt duplicate email, duplicate application and invalid relationship |
| Aggregation and views | `v_startup_360`, `v_milestone_health`, `v_funding_summary` | Show Startup 360°, milestone health and funding summary |

## Unit 5 — JavaFX desktop application

| Syllabus concept | Concrete implementation | How to demonstrate |
|---|---|---|
| Application lifecycle | `Main extends Application`, `init()`, `start(Stage)`, `stop()` and `main` | Launch the desktop application |
| Stage, Scene and Scene Graph | `Main.setScene`, `Scene`, root layouts and controls | Navigate from login to role dashboard |
| Layout panes | `BorderPane`, `VBox`, `HBox`, `ScrollPane` | Show main dashboard and forms |
| Controls | `TextField`, `PasswordField`, `Button`, `ComboBox`, `ListView`, `TabPane`, `Separator` | Use registration, evaluation, milestone and investor controls |
| Event handling | `setOnAction` handlers | Submit forms and refresh database-backed lists |
| Dialogs | JavaFX `Alert` for success/error messages | Trigger a successful save and a validation error |
| File chooser | `FileChooser` for CSV and supporting documents | Select/import CSV and upload documents |
| CSS styling | `src/main/resources/static/css/app.css` | Show the ivory/sage/pale-stone theme |

## Repeatable core-code verification

Run `scripts/verify-core.sh` from the project root. It compiles the non-JavaFX Java code with JDK 21 and executes 11 checks covering password hashing, readiness calculation, generic repository/Optional use, streams/collections, CSV file preview and filtered serialization. It does not replace the full Maven/JavaFX/MySQL integration test; see `test-results-at-build.md`.
