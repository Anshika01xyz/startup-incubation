# Verification report — 17 November 2026

## Checks executed in the preparation environment

| Check | Result | Evidence / notes |
|---|---|---|
| Compile all non-JavaFX production Java sources with JDK 21 | PASS | `scripts/verify-core.sh` compiles the application service, JDBC/DAO code, models, utilities, importer and analytics |
| Readiness score calculation | PASS | Four equal scores of 8 produce 80/100 |
| PBKDF2 password verification | PASS | Correct password accepted; incorrect password rejected |
| Generic repository and Optional lookup | PASS | Insert, lookup and delete exercised |
| Stream grouping and sorting | PASS | Sector counts and readiness ranking exercised |
| Map/collection helper | PASS | Status aggregation exercised |
| CSV file preview | PASS | Header and logical data-row count verified against a temporary CSV |
| CSV parser | PASS | Quoted commas and embedded newlines verified |
| Filtered Java serialization read | PASS | Snapshot serialized and deserialized through the object filter |
| Full Maven build and JUnit execution | NOT RUN | Maven is not installed and dependency download is unavailable in this environment |
| JavaFX compile and GUI launch | NOT RUN | JavaFX libraries and a desktop runtime are unavailable in this environment |
| Live MySQL schema and workflow integration | NOT RUN | No MySQL server is available in this environment |
| Real publisher CSV import | NOT RUN | Requires the separately downloaded publisher CSV and configured MySQL |

## Repeatable local check

From the project root, run one of these:

- Windows PowerShell: `powershell -ExecutionPolicy Bypass -File scripts\verify-core.ps1`
- macOS/Linux: `sh scripts/verify-core.sh`

Expected final line: `PASS: 11 core checks completed.`

## Required before coursework submission

Run `mvn clean test`, launch with `mvn javafx:run`, apply `database/schema.sql` to MySQL 8, and complete the scenarios in `testing.md`. Record actual results from the target Windows machine. No full application runtime or MySQL integration pass is claimed by this report.
