# Known limitations and verification boundary

- This is a JavaFX desktop coursework application, not a production investment or payment platform. Investment entries record commitments and generated transaction references; there is no payment gateway or money transfer.
- Investor matching is intentionally explainable: exact sector preference matches and whether the funding request fits the investor's minimum/maximum ticket range. It is not an AI or predictive investment model.
- The public funding dataset is downloaded separately from its publisher; the CSV is not redistributed in this ZIP. Keep the Fliar attribution and CC BY-NC 4.0 non-commercial license notice.
- The public dataset contains historic funding rounds, not live funding requests, founder profiles, mentor assignments or incubation records. Historic rounds remain in their own table.
- Some screens use numeric IDs entered from the adjacent list instead of searchable relational pickers. This keeps the student project manageable but is less polished than an autocomplete selector.
- The current preparation environment could compile and run the 10-check non-JavaFX verification script, but it did not have Maven, JavaFX runtime or MySQL. Full Maven build, GUI launch, database integration and live CSV import must be tested on the team's configured machine.
- Use fictional test accounts and non-sensitive test documents for demonstration. Do not store actual investor financial credentials or real financial information.
