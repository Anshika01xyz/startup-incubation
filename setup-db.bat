@echo off
setlocal
set /p DBUSER=MySQL username [root]: 
if "%DBUSER%"=="" set DBUSER=root
mysql -u %DBUSER% -p < database\schema.sql
if errorlevel 1 (
  echo Database schema setup failed. Check MySQL is running and credentials are correct.
  pause
  exit /b 1
)
mysql -u %DBUSER% -p < database\migrate_dashboard_v2.sql
if errorlevel 1 (
  echo Database migration failed. Check the MySQL connection and existing schema.
  pause
  exit /b 1
)
mysql -u %DBUSER% -p < database\sample_data.sql
if errorlevel 1 (
  echo Demo data setup failed. Check the schema completed successfully.
  pause
  exit /b 1
)
echo Foundry database, compatibility migration and six demo accounts are ready.
echo Demo password for all accounts: Demo@12345
pause
