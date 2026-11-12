# Module 1: Startup & Founder Management
Owner: Anshika

## 1. Startup fields

| Field | Description | Example |
|---|---|---|
| startup_id | unique ID | 1 |
| startup_name | name of the startup | GreenRoute |
| sector | industry | AgriTech |
| stage | Idea / Prototype / MVP / Revenue | MVP |
| description | short summary | Route planning for farm produce |
| website | link (optional) | greenroute.in |
| location | city | Chandigarh |
| founded_date | date | 2025-03-10 |
| funding_needed | amount | 500000 |
| status | position in the process | DRAFT |
| created_at | when it was added | timestamp |

## 2. Founder fields

| Field | Description | Example |
|---|---|---|
| founder_id | unique ID | 1 |
| user_id | links to shared users table (name, email, password stored there) | 5 |
| phone | contact number | 9876543210 |
| qualification | education | B.Tech CSE |
| experience_years | years of experience | 3 |
| linkedin_url | profile link (optional) | linkedin.com/in/... |
| bio | short intro | Passionate about AgriTech |

### Link table: startup_founders

| Field | Description |
|---|---|
| startup_id | which startup |
| founder_id | which founder |
| role_title | e.g. CEO, Co-founder |

**To confirm with team:** `user_id` and the users table (Shaffy).
