#!/usr/bin/env bash
set -e
read -r -p "MySQL username [root]: " DBUSER
DBUSER=${DBUSER:-root}
mysql -u "$DBUSER" -p < database/schema.sql
mysql -u "$DBUSER" -p < database/migrate_dashboard_v2.sql
mysql -u "$DBUSER" -p < database/sample_data.sql
echo "Foundry database, compatibility migration and six demo accounts are ready."
echo "Demo password for all accounts: Demo@12345"
