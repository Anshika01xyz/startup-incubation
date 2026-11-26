# Day 14 quality gate and test checklist

## Build/environment
- [ ] JDK 21 and Maven 3.9+ installed
- [ ] MySQL 8 database created from `database/schema.sql`
- [ ] Application DB connection works with local credentials
- [ ] `mvn clean test` passes
- [ ] `mvn clean javafx:run` launches the desktop app

## Authentication and roles
- [ ] First admin can be created once; second bootstrap attempt is blocked
- [ ] Password verification accepts the correct password and rejects an incorrect one
- [ ] Founder, investor and approved mentor can sign in
- [ ] Pending mentor cannot sign in
- [ ] Evaluator account is created by admin
- [ ] Service methods reject disallowed roles

## Founder and startup
- [ ] Duplicate email is rejected by the unique constraint
- [ ] Founder can create and list their own startup; other founders' startups are not shown in their own-startup list
- [ ] Founder can add team members and link an existing registered co-founder
- [ ] Founder can record progress updates
- [ ] A founder cannot submit an application or funding request for another founder's startup
- [ ] Startup document is copied to uploads and metadata is stored

## Incubation and mentor
- [ ] Admin can create incubator and programme
- [ ] Founder can submit one application per startup/programme
- [ ] Evaluator can submit four scores from 0 through 10 and a recommendation
- [ ] Evaluation and application status update together
- [ ] Mentor is pending after registration and cannot be assigned before approval
- [ ] Approval activates mentor login; rejected mentor remains blocked
- [ ] Only assigned approved mentor or admin can update milestone status
- [ ] Mentor sees their assigned startup milestones
- [ ] Milestone status and update note persist together
- [ ] Founder receives a notification when evaluation or milestone status changes

## Investor and data
- [ ] Investor can save sector interests and minimum/maximum ticket range
- [ ] Match list ranks sector preference and ticket-range fit transparently
- [ ] Founder can create a positive-amount funding request for owned startup and attach supporting documents
- [ ] Active investors receive a notification when a funding request is opened
- [ ] Investor can commit a positive amount against a valid request
- [ ] Concurrent or oversized commitments cannot exceed the remaining funding request amount
- [ ] Founder receives a notification when an investment commitment is recorded
- [ ] Invalid foreign keys and duplicate records are rejected
- [ ] CSV importer maps the published 19-column dataset schema
- [ ] First import inserts records; second import skips duplicate round IDs
- [ ] Import rollback is verified by testing a deliberately malformed row/schema in a disposable DB
- [ ] External historical rounds never become live funding requests automatically

## Reporting and coursework features
- [ ] Dashboard totals match database counts
- [ ] Dashboard top-readiness ranking, startup status distribution and sector distribution match MySQL records
- [ ] Startup 360° aggregates linked records and readiness score
- [ ] Milestone health distinguishes overdue, due soon, on track and completed
- [ ] Activity timeline records important actions
- [ ] Uploaded documents are persisted on disk with metadata in MySQL
- [ ] Java serialization snapshot is created under reports/ and can be read back through the filtered deserializer
- [ ] Notifications can be viewed and marked as read
- [ ] Application log file is created under logs/

Record date, tester, environment, expected result, actual result and pass/fail for each test before final submission.
