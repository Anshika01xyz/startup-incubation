package com.startup.platform.repository;

import com.startup.platform.util.Db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Read-only projections of publisher funding-round data into compatible workspace tables. */
public final class DatasetProjectionDao {
    /** Display sequence continues after operational startup IDs; it is not a writable database key. */
    public List<String[]> startupDirectory() throws SQLException {
        return query("""
            SELECT (SELECT COALESCE(MAX(id),0) FROM startups) + ROW_NUMBER() OVER (ORDER BY id),
                   startup_name,COALESCE(sector,''),COALESCE(city,''),
                   COALESCE(funding_stage,''),COALESCE(CAST(founded_year AS CHAR),'—')
            FROM external_funding_rounds ORDER BY id
            """, 6);
    }

    /** Historical market records are shown in the same funding table, with their actual source fields. */
    public List<String[]> fundingRequests() throws SQLException {
        return query("""
            SELECT (SELECT COALESCE(MAX(id),0) FROM funding_requests) + ROW_NUMBER() OVER (ORDER BY id),
                   startup_name,COALESCE(CAST(amount_usd AS CHAR),'—'),'USD',
                   CONCAT('Lead investor: ',COALESCE(NULLIF(lead_investor,''),'Not listed')),
                   COALESCE(NULLIF(funding_stage,''),'Not specified')
            FROM external_funding_rounds ORDER BY id
            """, 6);
    }

    /**
     * Publisher records do not contain operational application, mentor, milestone, readiness,
     * team or progress data. Those values are shown as unavailable, not misleading zeroes.
     */
    public List<String[]> startup360() throws SQLException {
        return query("""
            SELECT (SELECT COALESCE(MAX(id),0) FROM startups) + ROW_NUMBER() OVER (ORDER BY id),
                   startup_name,COALESCE(sector,''),COALESCE(city,''),
                   COALESCE(NULLIF(funding_stage,''),'Not specified'),
                   '—','—','—','—',COALESCE(CAST(amount_usd AS CHAR),'—'),
                   '—','—','—','—'
            FROM external_funding_rounds ORDER BY id
            """, 14);
    }

    public List<String[]> reportTotals() throws SQLException {
        List<String[]> out = new ArrayList<>();
        String sql = "SELECT COUNT(*),COUNT(DISTINCT startup_name),COUNT(DISTINCT COALESCE(NULLIF(sector,''),'Unclassified')),COUNT(DISTINCT COALESCE(NULLIF(country,''),'Unknown')) FROM external_funding_rounds";
        try (Connection c = Db.get(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            if (r.next()) out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)});
        }
        return out;
    }

    private List<String[]> query(String sql, int columns) throws SQLException {
        List<String[]> out = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                String[] row = new String[columns];
                for (int i = 0; i < columns; i++) row[i] = r.getString(i + 1);
                out.add(row);
            }
        }
        return out;
    }
}
