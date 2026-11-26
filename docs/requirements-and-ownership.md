# Requirements, boundaries and ownership

## Member 1 — Startup & Founder Management
- Founder registration and profile
- Startup CRUD and founder ownership
- Team/document/progress records
- Startup profile data consumed by every other module
- Shared DB connection/schema conventions and DB setup guide

## Member 2 — Incubation & Evaluation
- Incubator and programme records
- Startup programme applications
- Evaluator accounts and scorecard
- Mentor self-registration, approval and assignment
- Milestones and milestone updates
- Shared authentication/role contract

## Member 3 — Investor & Funding
- Investor profile, sector preferences and min/max ticket range
- Funding requests and supporting document uploads
- Explainable sector and ticket-range investor matching
- Investment commitments, remaining-balance checks and transaction records
- Shared JavaFX shell/navigation/theme and dashboard frame

## Shared integration rules
- One Git repository and one MySQL database
- Agree IDs, table names, status vocabulary and service signatures before parallel coding
- No module-specific duplicate startup/user tables
- Use PreparedStatement, try-with-resources and transactions
- Do not commit local credentials
- Public historical data and internal live platform data stay distinguishable

## Controlled scope
No payment gateway, AI prediction, external authentication provider or complex web deployment. Matching and readiness are transparent rule-based indicators appropriate for a coursework project.
