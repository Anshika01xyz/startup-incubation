# Architecture and module integration

## Layering
- **Presentation:** JavaFX Stage, Scene, TabPane, forms, lists, dialogs and FileChooser.
- **Application/service:** `PlatformService` validates role permissions and coordinates business workflows.
- **Data access:** JDBC `PreparedStatement` and `ResultSet` operations through `Db`; `Db.transaction` commits or rolls back a unit of work.
- **Persistence:** MySQL 8 relational schema with foreign keys, unique constraints, indexes and reporting views.
- **External data:** CSV importer writes public historical rounds to a dedicated table; the app's operational records remain in their own normalized tables.

## Integration contracts
- Every registered account is stored in `users` and assigned one or more rows in `user_roles`.
- Module tables reference the shared user/startup/application identifiers; modules do not keep disconnected copies of startup records.
- A founder-owned startup ID is reused by applications, documents, mentor assignments, milestones and funding requests.
- An application is linked to a startup and a programme. An evaluation references that application.
- A mentor must have `approval_status='APPROVED'` before assignment. Mentor login is activated only after approval.
- Investments reference an existing funding request and investor profile.
- Activity entries record the actor, action, entity and timestamp.

## Transaction boundaries
- Account creation: user + role + role-specific profile + activity entry.
- Evaluation: evaluation record + application status + activity entry.
- First admin: user + ADMIN role + activity entry; only if no administrator exists.
- CSV import: all accepted rows commit together; SQL failure rolls the import back.
- Milestone update: current status and progress-history entry are written in one connection transaction.

## Explainable matching
Investor discovery ranks active internal startups using exact case-insensitive sector-interest matches (two ranking points per match) plus one fit point when an open funding request falls within the investor's saved minimum/maximum ticket range. The UI exposes both match components so the recommendation is inspectable. It is a simple coursework rule, not a prediction of investment success. Historical public rounds are market context, not eligible internal funding requests.
