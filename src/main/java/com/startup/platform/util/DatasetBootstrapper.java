package com.startup.platform.util;

import com.startup.platform.config.PlatformConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;

/** Downloads the publisher's public sample when absent and imports it once into the historical-data table. */
public final class DatasetBootstrapper {
    private static final URI SOURCE = URI.create("https://fliarbi.com/datasets/files/india-startup-funding/india_startup_funding_sample.csv");
    private DatasetBootstrapper() {}

    public static String loadIfNeeded() throws Exception {
        long existing = countRows();
        if (existing == 5006) return "Complete historical market dataset ready: all 5,006 records are available.";

        PlatformConfig config = PlatformConfig.load();
        Path csv = Path.of(config.datasetPath()).toAbsolutePath().normalize();
        Files.createDirectories(csv.getParent());
        if (!Files.isRegularFile(csv) || Files.size(csv) == 0) {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest request = HttpRequest.newBuilder(SOURCE).timeout(Duration.ofSeconds(90)).header("User-Agent", "Foundry-Educational-Platform/1.0").GET().build();
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(csv));
            if (response.statusCode() != 200 || Files.size(csv) < 1024) {
                Files.deleteIfExists(csv);
                throw new java.io.IOException("Publisher dataset download failed (HTTP " + response.statusCode() + "). Check internet access and retry.");
            }
        }
        try (var reader = Files.newBufferedReader(csv)) {
            int sourceRows = CsvImporter.parseCsv(reader).size() - 1;
            if (sourceRows != 5006) throw new IllegalStateException("DatasetCompletenessException: expected 5,006 publisher rows but the CSV contains " + sourceRows + ". The application will not invent or silently substitute records.");
        }
        CsvImporter.ImportResult result = new CsvImporter().importWithReport(csv.toFile());
        long total = countRows();
        AppLog.info("Historical dataset bootstrap: inserted=" + result.inserted() + ", duplicates=" + result.duplicates() + ", skipped=" + result.skipped() + ", total=" + total);
        if (total != 5006) throw new IllegalStateException("DatasetCompletenessException: expected 5,006 records but the database contains " + total + ". Automatic bootstrap will retry on the next sign-in.");
        return "Complete historical market dataset loaded: all 5,006 records are ready.";
    }

    private static long countRows() throws Exception {
        try (Connection c = Db.get(); PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM external_funding_rounds"); ResultSet r = p.executeQuery()) {
            r.next(); return r.getLong(1);
        }
    }
}
