 Investor & Funding Management Module
## Startup Incubation & Investment Management Platform



---

## 1. Module Overview

The Investor & Funding Management Module is responsible for connecting investors with promising startups registered on the platform.

It enables investors to create and manage their profiles, define investment preferences, discover approved startups, evaluate funding opportunities, track funding requests and maintain investment records.

The module aims to make the investment discovery and tracking process organized, transparent and data-driven.

It integrates with the Startup Management Module and Incubation Management Module to retrieve verified startup information, application approval status and relevant business details.

## 2. Objectives

The main objectives are:

1. Provide investors with a dedicated profile and account management system.
2. Allow investors to define preferred industries, startup stages and investment ranges.
3. Enable investors to discover approved startups.
4. Provide a rule-based startup-investor matching mechanism.
5. Allow investors to explore startup funding requirements.
6. Maintain funding request and investment records.
7. Provide portfolio summaries and investment history.
8. Maintain traceability of important investment-related activities.

## 3. Module Components

### 3.1 Investor Profile Management

Investors can:
- Register and maintain investor profiles.
- Add organization and contact details.
- Specify investor type, such as Angel Investor, Venture Capital Firm or Individual Investor.
- Update profile information.
- View their investment preferences.

### 3.2 Investment Preference Management

Investors can define:
- Preferred startup sectors.
- Preferred startup development stages.
- Minimum investment amount.
- Maximum investment amount.
- Preferred investment categories.

The preferences will be stored in the database and used by the matching mechanism.

### 3.3 Startup Discovery

Investors can:
- Browse approved startups.
- Search startups by name.
- Filter startups by industry and development stage.
- View startup descriptions and business information.
- View available funding requirements.
- Access relevant startup profiles.

Only startups meeting the platform's approval criteria should appear in the approved startup discovery section.

### 3.4 Investor–Startup Matching

The platform will provide a transparent, rule-based matching mechanism.

The matching system will compare:
- Investor's preferred sector with startup sector.
- Investor's preferred startup stage with startup stage.
- Investor's preferred investment range with the startup's funding requirement.

The system will calculate a compatibility score using defined matching rules.

It will also explain why a startup matches an investor's preferences.

Example:

An investor prefers:
- Sector: Healthcare
- Stage: Early Stage
- Investment range: ₹5 lakh–₹20 lakh

A healthcare startup seeking ₹10 lakh at the early stage may receive a high compatibility score.

The matching score will be a decision-support indicator, not a guarantee of investment suitability.

### 3.5 Funding Request Management

The platform will maintain funding requirements submitted by startups.

Investors can:
- View funding requests from approved startups.
- View requested funding amounts.
- Understand the purpose of funding.
- View funding stages.
- Track request statuses.
- Record investment-related decisions.

### 3.6 Investment Portfolio Management

The module will maintain investment records.

Investors can:
- View recorded investments.
- View startup-wise investment details.
- Track investment amounts and dates.
- View investment history.
- Access portfolio summaries.

The project will maintain records for demonstration and management purposes; it will not process real financial transactions.

## 4. User Roles and Permissions

| User Role | Permitted Operations |
|---|---|
| Investor | Manage own profile and preferences, discover approved startups, view funding opportunities and portfolio |
| Administrator | Manage investor accounts and oversee platform records |
| Incubator | View permitted investor-related summaries and coordinate funding opportunities |
| Founder | View relevant funding request and investment status information for their startup |

Access to operations must be validated through the authentication and authorization layer.

## 5. Investor Workflow

1. Investor logs into the platform.
2. Investor completes their profile.
3. Investor defines investment preferences.
4. System retrieves approved startups.
5. System compares investor preferences with startup attributes.
6. Investor views matching startups and compatibility explanations.
7. Investor explores startup profiles and funding requests.
8. Investment-related decisions are recorded.
9. Investment records are maintained.
10. Investor views portfolio summaries and investment history.

## 6. Database Design Requirements

The following entities are proposed for this module.

### 6.1 Investors

Stores investor profile information.

| Field | Description |
|---|---|
| investor_id | Primary key |
| user_id | Foreign key referencing the common user account |
| organization_name | Investor organization name |
| investor_type | Type of investor |
| contact_email | Contact email |
| website | Organization website |
| description | Investor description |
| created_at | Profile creation timestamp |
| updated_at | Last update timestamp |

### 6.2 Investor_Preferences

Stores investment preferences.

| Field | Description |
|---|---|
| preference_id | Primary key |
| investor_id | Foreign key referencing Investors |
| preferred_sector | Preferred industry |
| preferred_stage | Preferred startup stage |
| min_investment | Minimum investment amount |
| max_investment | Maximum investment amount |

Multiple preference records may be associated with one investor.

### 6.3 Funding_Requests

Stores funding requirements associated with startups.

| Field | Description |
|---|---|
| request_id | Primary key |
| startup_id | Foreign key referencing Startups |
| requested_amount | Required funding amount |
| funding_purpose | Purpose of funding |
| funding_stage | Funding stage |
| request_status | Current request status |
| created_at | Submission timestamp |
| updated_at | Last update timestamp |

The startup owner and authorized incubator workflows will control the creation and approval of funding requests.

### 6.4 Investments

Stores investment records.

| Field | Description |
|---|---|
| investment_id | Primary key |
| investor_id | Foreign key referencing Investors |
| startup_id | Foreign key referencing Startups |
| request_id | Related funding request |
| investment_amount | Recorded investment amount |
| investment_date | Investment date |
| investment_status | Current investment record status |

All financial amounts will use an appropriate decimal database type.

## 7. Database Relationships

- One user account can be associated with an investor profile.
- One investor can have multiple investment preferences.
- One startup can have multiple funding requests over time.
- One investor can have multiple investment records.
- One startup can receive investment records from multiple investors.
- An investment record may reference its related funding request.

All relationships will be implemented using primary keys and foreign keys.

## 8. Proposed Unique Features

### 8.1 Explainable Investor–Startup Matching

A rule-based matching system that evaluates sector, startup stage and funding-range compatibility.

It will display:
- Compatibility score.
- Matching criteria.
- Reasons for the recommendation.
- Criteria that do not match.

### 8.2 Investment Opportunity Dashboard

A dashboard displaying:
- Total approved startups available for discovery.
- Matching investment opportunities.
- Funding requests by status.
- Recorded investments.
- Investment history.

### 8.3 Startup Investment Profile

A consolidated investor-facing view containing:
- Startup overview.
- Industry and development stage.
- Incubation approval status.
- Funding requirement.
- Relevant evaluation summary, where authorized.
- Recorded investment information.

### 8.4 Investment Activity Tracking

Important events will be recorded, such as:
- Investor profile creation.
- Preference updates.
- Funding request review.
- Investment record creation.
- Investment status updates.

## 9. Validation and Business Rules

1. Investor email addresses must be valid.
2. Required profile information must not be empty.
3. Minimum investment must not exceed maximum investment.
4. Investment amounts must be positive.
5. Investors must only access permitted records.
6. Only approved startups should be displayed in the approved opportunity discovery section.
7. Investment records must reference valid investors and startups.
8. Funding requests must reference valid startup records.
9. Invalid operations must generate appropriate validation errors.
10. Important database operations must maintain referential integrity.

## 10. Technical Implementation

The module will use a layered architecture.

**JavaFX Presentation Layer**
- Investor dashboard.
- Investor profile forms.
- Preference management screens.
- Startup discovery screens.
- Funding request views.
- Investment portfolio screens.

**Service Layer**
- Investor business logic.
- Preference validation.
- Startup matching calculations.
- Funding workflow rules.
- Investment record management.

**DAO Layer**
- InvestorDAO.
- InvestorPreferenceDAO.
- FundingRequestDAO.
- InvestmentDAO.

**Database Layer**
- MySQL relational database.
- JDBC connection management.
- PreparedStatement.
- ResultSet.
- Transactions.
- Primary and foreign key constraints.

SQL queries will remain inside DAO classes, and JavaFX controllers will communicate with the service layer rather than directly accessing the database.

## 11. Testing Requirements

The module will be tested for:

- Investor registration and profile updates.
- Preference creation and modification.
- Startup discovery and filtering.
- Matching score calculations.
- Funding request retrieval.
- Investment record creation.
- Invalid input handling.
- Database constraint enforcement.
- Unauthorized access prevention.
- Database transaction consistency.

## 12. Expected Deliverables

1. Investor module requirements document.
2. Investor database tables and relationships.
3. Investor model classes.
4. JDBC-based DAO implementations.
5. Investor service classes.
6. JavaFX investor dashboard and screens.
7. Startup discovery and matching functionality.
8. Funding request management.
9. Investment portfolio management.
10. Module test cases and documentation.


n and implementation planning
