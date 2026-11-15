# Authentication and Authorization Design

**Owner:** Shaffy
**Project:** UCS553 Startup Incubation & Investment Management Platform
**Status:** Draft for team review. Frozen once all three members approve.

Everything below is a *proposal* for the team to review. Items marked **[CONFIRM]** depend on decisions by Anshika or Aastha.

---

## 1. What this contract covers

| Item | Where |
|---|---|
| Role list and role-to-module permissions | Sections 2 and 3 |
| Login / register / logout method signatures | `auth/AuthService.java` |
| Permission checks used by all services | `auth/AuthorizationService.java`, `auth/Permission.java` |
| Shared user model and DAO contract | `auth/User.java`, `auth/UserDAO.java` |
| Database tables (`users`, `roles`, `user_roles`) | `database/auth_schema.sql` |
| Exceptions | `exceptions/*.java` |

Implementations (`AuthServiceImpl`, `AuthorizationServiceImpl`, `UserDAOImpl`) are written in the implementation phase. Method signatures are frozen now so others can code against them.

---

## 2. Roles

| Role | Module access | Notes |
|---|---|---|
| `ADMIN` | All modules + user management | Creates incubators, evaluators, mentors |
| `FOUNDER` | Startup, Incubation (own applications only) | Cannot call evaluator or investor operations |
| `INCUBATOR_MANAGER` | Incubation, read-only Startup | Decides applications, assigns mentors, creates milestones |
| `EVALUATOR` | Incubation (evaluation screens only) | Scores assigned applications |
| `MENTOR` | Incubation (assigned startups and milestones only) | Gives milestone feedback |
| `INVESTOR` | Investor, read-only approved startups | Never sees pending or rejected applications |

A user may hold several roles (`user_roles` junction table).

---

## 3. Role-to-Module Permission Matrix

Legend: **F** = full, **R** = read only, **O** = own records only, **-** = no access

| Module / Feature | ADMIN | FOUNDER | INCUBATOR_MANAGER | EVALUATOR | MENTOR | INVESTOR |
|---|---|---|---|---|---|---|
| Startup profile, team, documents | F | F (O) | R | R | R (assigned) | R (approved only) |
| Submit application | - | F (O) | - | - | - | - |
| View applications | F | R (O) | F | R (assigned) | R (assigned) | - |
| Review / shortlist / accept / reject | - | - | F | - | - | - |
| Submit evaluation and scores | - | - | - | F | - | - |
| View readiness scorecard | F | R (O) | F | F | R | - |
| Manage incubators / programs | F | - | F (own incubator) | - | - | - |
| Manage mentors / evaluators | F | - | R | - | - | - |
| Assign mentor | - | - | F | - | - | - |
| Create milestones | - | - | F | - | - | - |
| Update milestones | - | - | F | - | F (assigned) | - |
| View milestones | F | R (O) | F | - | R (assigned) | - |
| Investor profile and preferences | F | - | - | - | - | F (O) |
| View matched startups | F | - | - | - | - | F |
| Funding request (create / view) | F | F (O) | R | - | - | R |
| Review / approve funding request | F | - | F | - | - | - |
| View readiness score of an approved startup (score only, no evaluator comments) | F | R (O) | F | - | - | R |
| Record investment | F | - | - | - | - | F (O) |
| User management | F | - | - | - | - | - |
| Activity timeline | F | R (O) | R | - | - | - |

**[CONFIRM]** Anshika and Aastha: please check the rows for your modules and tell me what to change.

---

## 4. Contract Summary (signatures)

```java
// AuthService
User login(String email, String plainPassword) throws AuthenticationException;
void logout();
User register(String fullName, String email, String plainPassword, Role role) throws DuplicateUserException;
Optional<User> getCurrentUser();
Optional<User> findById(int userId);
void changePassword(int userId, String oldPassword, String newPassword) throws AuthenticationException;
List<User> listUsers(User actor);
void assignRole(User actor, int userId, Role role);
void deactivateUser(User actor, int userId);
void activateUser(User actor, int userId);

// AuthorizationService
boolean hasPermission(User user, Permission permission);
void requirePermission(User user, Permission permission);      // throws UnauthorizedException
void requireAnyRole(User user, Role... roles);                 // throws UnauthorizedException
boolean canAccessModule(User user, AppModule module);
Set<AppModule> getAccessibleModules(User user);
Set<Permission> getPermissions(Role role);
```

### How other members use it

```java
// In any service method, first line:
authz.requirePermission(actor, Permission.STARTUP_CREATE);

// In the JavaFX shell (Aastha) to build the menu:
Set<AppModule> menu = authz.getAccessibleModules(SessionContext.getCurrentUser().get());
```

### Security rules

- Passwords are hashed with **BCrypt** (library `org.mindrot:jbcrypt:0.4`). Plaintext is never stored, logged or displayed.
- Login failure message is the same for "unknown email" and "wrong password".
- Inactive users are rejected at login and at every `requirePermission` call.
- Permissions are checked in **services**, not only by hiding UI buttons.
- Users are deactivated, never deleted (audit history).

Maven dependency to add to `pom.xml` (Anshika owns the pom; ask her to merge):

```xml
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
```

---

## 5. Conventions Proposed for Team Freeze

| Topic | Proposal |
|---|---|
| Table names | `snake_case`, plural (`startups`, `applications`) |
| Primary keys | `<entity>_id`, `INT AUTO_INCREMENT` (`startup_id`, `user_id`) |
| Foreign keys | Same column name as the referenced PK; named `fk_<table>_<ref>` |
| Timestamps | `created_at`, `updated_at` as `DATETIME`; calendar deadlines as `DATE` |
| Timezone | One convention for the whole project: **[CONFIRM]** (proposal: local server time, set in the JDBC URL) |
| Money | `DECIMAL(15,2)` |
| Status values | `UPPER_SNAKE_CASE` strings, restricted with `CHECK`, mirrored by a Java enum in `common/` |
| Soft delete | `is_active BOOLEAN` for users, incubators, evaluators, mentors |
| DAO naming | Interface `XxxDAO` + implementation `XxxDAOImpl` in the module's `dao` package |
| DAO methods | `insert`, `findById` (returns `Optional`), `findAll` (returns `List`), `update`, `setActive` |
| DAO errors | Throw unchecked `DataAccessException`; services decide what to show the user |
| Base package | `com.startup.platform` |
| Git workflow | Branch per member (`shaffy-...`, `anshika-...`, `aastha-...`), pull request, one reviewer, small commits, prefix `docs:` / `feat:` / `fix:` |

### Status vocabulary for Module 2 (already in `module2_requirements.md`)

- Application: `DRAFT, SUBMITTED, UNDER_REVIEW, SHORTLISTED, ACCEPTED, REJECTED, ACTIVE_INCUBATION, COMPLETED, WITHDRAWN`
- Milestone: `NOT_STARTED, IN_PROGRESS, BLOCKED, COMPLETED` (`OVERDUE` is derived from `due_date`)
- Program: `OPEN, CLOSED, ONGOING, COMPLETED`

---

## 6. Module 2 Tables and Foreign Keys (for the shared ER diagram)

```
users ──< user_roles >── roles
incubators ──< programs ──< applications >── startups (Module 1)
applications ──< evaluations >── evaluators >── users
evaluations ──< evaluation_scores >── evaluation_criteria
applications ──< mentor_assignments >── mentors >── users
applications ──< milestones ──< milestone_updates
```

Cross-module links Shaffy depends on:

| My table | Column | References | Owner |
|---|---|---|---|
| `applications` | `startup_id` | `startups(startup_id)` | Anshika **[CONFIRM]** |
| all module tables | `*_user_id` | `users(user_id)` | Shaffy |

Cross-module links others depend on:

| Their need | Provided by |
|---|---|
| "Approved startup" for investors | `applications.status IN ('ACCEPTED','ACTIVE_INCUBATION','COMPLETED')` **[CONFIRM with Aastha]** |
| Readiness score for Startup 360° | `evaluations.weighted_score` (average per application) |
| Milestone progress for Startup 360° | `milestones` + `milestone_updates` |

---

## 7. Schema Run Order

1. `database/auth_schema.sql` (users, roles, user_roles)
2. Anshika's startup schema (startups, founders, ...)
3. `database/module2_schema.sql`
4. Aastha's investor schema
5. `sample_data.sql`

---

## 8. Interface-Change Rule

Once the contract is frozen, any change to these signatures needs a short note in the team chat or a pull request titled `interface-change: <what>`, approved by the other two members.

---

## 9. Review Checklist (Shaffy)

- [x] Users / roles / login contract defined
- [x] Authentication and authorization method signatures written
- [x] Role-to-module permission matrix drafted
- [x] `users`, `roles`, `user_roles` SQL written
- [x] Module 2 tables written (ready to merge into the shared `schema.sql`)
- [ ] Anshika reviews matrix rows for the Startup module and confirms the `startups(startup_id)` FK
- [ ] Aastha reviews matrix rows for the Investor module and confirms the "approved startup" rule (section 10)
- [ ] Team freezes conventions in section 5

---

## 10. Reconciliation with Module 1 (Anshika) and Module 3 (Aastha)

Based on `module1_requirements.md` and `investor-module-read.md`.

### Already consistent

| Topic | Result |
|---|---|
| `users` table | Anshika and Aastha both link to `users(user_id)`. Mine is `INT AUTO_INCREMENT`, with `full_name`, `email` and `password_hash` stored there. This answers Anshika's open question. |
| `startups` primary key | `startup_id INT`. My `applications.startup_id` references it. |
| Application journey statuses | Anshika's list matches mine. I additionally include `WITHDRAWN`. |
| Foreign keys | `investors.user_id`, `founders.user_id` both point to `users`. |

### Conflicts and decisions proposed

| # | Issue | Proposal | Who decides |
|---|---|---|---|
| 1 | **Two status fields.** Anshika has `startups.status` with journey values; I have `applications.status`. They can drift apart. | `applications.status` is the **single source of truth**. When it changes, my service calls Anshika's "update startup status" method in the **same transaction** so `startups.status` mirrors it. Anshika must publish that method (`updateStatus(User actor, int startupId, String status)`). Her CHECK/status list must contain all 9 values including `WITHDRAWN`. | Shaffy + Anshika |
| 2 | **Role names.** Aastha uses "Incubator" and "Administrator"; I use `INCUBATOR_MANAGER` and `ADMIN`. Aastha's document also has no `EVALUATOR` or `MENTOR`. | Keep my names (they are a superset). Aastha should write `INCUBATOR_MANAGER` and `ADMIN` in her code. | Aastha |
| 3 | **"Approved startup" is undefined** in Aastha's document. | Approved = application status `ACCEPTED`, `ACTIVE_INCUBATION` or `COMPLETED`. Aastha calls `ApplicationService.findApprovedStartupIds()` instead of writing her own joins. | Aastha |
| 4 | **Stage and sector values.** Anshika uses Idea / Prototype / MVP / Revenue. Aastha's example says "Early Stage". Investor preferences must use the same vocabulary or matching will never work. | Both use one shared list of stage and sector values, kept as constants in `common/` (Anshika owns the list; Aastha reads it). | Anshika + Aastha |
| 5 | **Funding statuses** (`request_status`, `investment_status`) are not listed. | Aastha lists them. I suggest `PENDING, UNDER_REVIEW, APPROVED, REJECTED, CLOSED` for requests. | Aastha |
| 6 | **Incubator role in funding.** Aastha lets incubators "coordinate funding opportunities" and approve requests. My first matrix gave them no funding access. | Fixed. `INCUBATOR_MANAGER` now has read access and the new `FUNDING_REQUEST_REVIEW` permission. | Done |
| 7 | **Investor-facing evaluation summary.** Aastha's Startup Investment Profile shows the evaluation summary "where authorized". | New permission `EVALUATION_SUMMARY_VIEW`. Investors see the readiness score only, never evaluator comments. Provided by `ApplicationService.getAverageReadinessScore(startupId)`. | Done |

### Methods I provide to the other modules

See `shaffy/service/ApplicationService.java`:

- `submitApplication(...)` is used by Anshika when a founder submits a startup to a program.
- `findApprovedStartupIds()` is used by Aastha for investor discovery.
- `getAverageReadinessScore(...)` is used by Anshika (Startup 360°) and Aastha (investment profile).
- `getApplicationStatus(...)` is used by Anshika's Startup 360° and the founder dashboard.

### Methods I need

- From Anshika: `StartupService.updateStatus(User actor, int startupId, String status)` and `getStartupById(int startupId)`.
- From Aastha: the shell's navigation entry point and the alert helper signatures.
