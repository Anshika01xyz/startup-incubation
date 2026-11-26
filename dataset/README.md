# Historical Indian startup funding data

Foundry uses the real Fliar BI **Indian Startup Funding Rounds 2015–2026** sample dataset (5,006 rows; January 2015–June 2026). It is publisher data, not generated demo data.

- Publisher: Fliar BI
- Source: https://fliarbi.com/datasets/india-startup-funding/
- DOI: https://doi.org/10.5281/zenodo.23431359
- Licence: CC BY-NC 4.0 (attribution required; non-commercial use only)
- Currency: USD (historical rounds only)

On the first successful sign-in, `DatasetBootstrapper` checks for `india_startup_funding_sample.csv`. If absent it downloads the publisher's CSV automatically. If the historical table is empty, the CSV is imported through JDBC in a transaction. Duplicate rows are ignored using the public round ID. There is no dataset-import tab and users do not manually select a file.

The current build environment could not retrieve the 2.5 MB CSV while packaging this ZIP, so the actual CSV is fetched from the publisher on first run. Internet access is required for that initial download. After download, the CSV remains in this folder and the database import is one-time/idempotent.

Citation: Fliar BI. (2026). *Indian Startup Funding Rounds 2015–2026 (Sample release)* [Data set]. Zenodo. https://doi.org/10.5281/zenodo.23431359
