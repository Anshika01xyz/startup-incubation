package com.startup.platform.util;

import com.startup.platform.config.*;

import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Imports the publisher's CSV into the separate historical-rounds table in one JDBC transaction. */
public final class CsvImporter {
    public record ImportResult(int inserted, int duplicates, int skipped) {}

    public int importFile(File file) throws Exception {
        return importWithReport(file).inserted();
    }

    public ImportResult importWithReport(File file) throws Exception {
        if (file == null || !file.isFile() || !file.canRead())
            throw new IOException("Select a readable CSV file.");
        List<String[]> rows;
        try (var reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            rows = parseCsv(reader);
        }
        if (rows.isEmpty()) throw new IllegalArgumentException("CSV is empty.");

        String[] header = rows.getFirst();
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < header.length; i++) columns.put(normalize(header[i]), i);
        String company = required(columns, "company_name", "startup_name", "startup", "name");
        String roundId = column(columns, "round_id");
        String announced = column(columns, "announced_date");
        String announcedYear = column(columns, "year", "announcement_year");
        String domain = column(columns, "company_domain");
        String country = column(columns, "country");
        String sector = column(columns, "sector", "industry");
        String city = column(columns, "city", "location");
        String year = column(columns, "founded_year");
        String stage = column(columns, "stage", "funding_stage");
        String amount = column(columns, "amount_usd", "amount", "funding_amount");
        String amountStatus = column(columns, "amount_status");
        String investor = column(columns, "lead_investor");
        String investorCount = column(columns, "investor_count");
        String newsMentions = column(columns, "news_mentions");
        String newsSource = column(columns, "news_source");
        String newsUrl = column(columns, "news_url", "source_url", "url");
        String excerpt = column(columns, "news_excerpt");
        String profile = column(columns, "fliar_profile_url");

        int inserted = 0, duplicates = 0, skipped = 0;
        String sql = "INSERT IGNORE INTO external_funding_rounds(round_id,announced_date,announcement_year,startup_name,company_domain,country,sector,city,founded_year,funding_stage,amount_usd,amount_status,lead_investor,investor_count,news_mentions,news_source,source_url,news_excerpt,profile_url,dataset_key) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = Db.get()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int rowNumber = 1; rowNumber < rows.size(); rowNumber++) {
                    String[] row = rows.get(rowNumber);
                    String name = value(row, columns.get(company));
                    if (name.isBlank()) { skipped++; continue; }
                    String publicId = value(row, index(columns, roundId));
                    String fingerprint = (name + "|" + value(row, index(columns, announced)) + "|"
                            + value(row, index(columns, stage)) + "|" + value(row, index(columns, amount)))
                            .toLowerCase(Locale.ROOT);
                    String key = publicId.isBlank() ? "fliar:" + UUID.nameUUIDFromBytes(fingerprint.getBytes(StandardCharsets.UTF_8)) : "fliar:" + publicId;

                    statement.setString(1, blankToNull(publicId));
                    setDate(statement, 2, value(row, index(columns, announced)));
                    setInteger(statement, 3, value(row, index(columns, announcedYear)));
                    statement.setString(4, name);
                    statement.setString(5, blankToNull(value(row, index(columns, domain))));
                    statement.setString(6, blankToNull(value(row, index(columns, country))));
                    statement.setString(7, blankToNull(value(row, index(columns, sector))));
                    statement.setString(8, blankToNull(value(row, index(columns, city))));
                    setInteger(statement, 9, value(row, index(columns, year)));
                    statement.setString(10, blankToNull(value(row, index(columns, stage))));
                    setDecimal(statement, 11, value(row, index(columns, amount)));
                    statement.setString(12, blankToNull(value(row, index(columns, amountStatus))));
                    statement.setString(13, blankToNull(value(row, index(columns, investor))));
                    setInteger(statement, 14, value(row, index(columns, investorCount)));
                    setInteger(statement, 15, value(row, index(columns, newsMentions)));
                    statement.setString(16, blankToNull(value(row, index(columns, newsSource))));
                    statement.setString(17, blankToNull(value(row, index(columns, newsUrl))));
                    statement.setString(18, blankToNull(value(row, index(columns, excerpt))));
                    statement.setString(19, blankToNull(value(row, index(columns, profile))));
                    statement.setString(20, key);
                    if (statement.executeUpdate() == 1) inserted++; else duplicates++;
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
        return new ImportResult(inserted, duplicates, skipped);
    }

    public static List<String[]> parseCsv(java.io.Reader source) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (PushbackReader reader = new PushbackReader(source, 1)) {
            List<String> fields = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean quoted = false;
            int current;
            while ((current = reader.read()) != -1) {
                char ch = (char) current;
                if (quoted) {
                    if (ch == '"') {
                        int next = reader.read();
                        if (next == '"') field.append('"');
                        else { quoted = false; if (next != -1) reader.unread(next); }
                    } else field.append(ch);
                } else if (ch == '"' && field.isEmpty()) {
                    quoted = true;
                } else if (ch == ',') {
                    fields.add(field.toString()); field.setLength(0);
                } else if (ch == '\n' || ch == '\r') {
                    if (ch == '\r') { int next = reader.read(); if (next != '\n' && next != -1) reader.unread(next); }
                    fields.add(field.toString()); field.setLength(0);
                    rows.add(fields.toArray(String[]::new)); fields.clear();
                } else field.append(ch);
            }
            if (quoted) throw new IOException("CSV contains an unclosed quoted field.");
            if (!fields.isEmpty() || !field.isEmpty()) { fields.add(field.toString()); rows.add(fields.toArray(String[]::new)); }
        }
        if (!rows.isEmpty() && rows.getFirst().length > 0)
            rows.getFirst()[0] = rows.getFirst()[0].replace("\uFEFF", "").trim();
        return rows;
    }

    private static String required(Map<String, Integer> columns, String... aliases) {
        String result = column(columns, aliases);
        if (result == null) throw new IllegalArgumentException("CSV is missing the company_name column.");
        return result;
    }
    private static String column(Map<String, Integer> columns, String... aliases) {
        for (String alias : aliases) if (columns.containsKey(normalize(alias))) return normalize(alias);
        return null;
    }
    private static Integer index(Map<String, Integer> columns, String key) { return key == null ? null : columns.get(key); }
    private static String normalize(String value) { return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ""); }
    private static String value(String[] row, Integer index) { return index == null || index >= row.length ? "" : row[index].trim(); }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
    private static void setInteger(PreparedStatement p, int index, String value) throws SQLException {
        try { p.setInt(index, Integer.parseInt(value.replace(",", "").trim())); }
        catch (NumberFormatException ex) { p.setNull(index, Types.INTEGER); }
    }
    private static void setDecimal(PreparedStatement p, int index, String value) throws SQLException {
        try { p.setBigDecimal(index, new java.math.BigDecimal(value.replaceAll("[$,\\s]", "").trim())); }
        catch (NumberFormatException ex) { p.setNull(index, Types.DECIMAL); }
    }
    private static void setDate(PreparedStatement p, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) { p.setNull(index, Types.DATE); return; }
        for (DateTimeFormatter formatter : List.of(DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("d/M/uuuu"), DateTimeFormatter.ofPattern("d-M-uuuu"),
                DateTimeFormatter.ofPattern("M/d/uuuu"))) {
            try { p.setDate(index, java.sql.Date.valueOf(LocalDate.parse(value.trim(), formatter))); return; }
            catch (DateTimeParseException ignored) { /* try the next published date format */ }
        }
        p.setNull(index, Types.DATE);
    }
}
