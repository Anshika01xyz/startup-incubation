package com.startup.platform.repository;

import com.startup.platform.model.*;
import com.startup.platform.util.*;

import com.startup.platform.util.Db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC data-access object for live funding opportunities and imported historical rounds. */
public class FundingDao {
    public List<String[]> findFundingRequests() throws SQLException {
        List<String[]> out=new ArrayList<>();try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT fr.id,s.name,fr.amount,fr.currency,fr.purpose,fr.status FROM funding_requests fr JOIN startups s ON s.id=fr.startup_id ORDER BY fr.id ASC");ResultSet r=p.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});
        }return out;
    }

    public List<String[]> findFundingRequestsForFounder(long userId) throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="SELECT fr.id,s.name,fr.amount,fr.currency,fr.purpose,fr.status FROM funding_requests fr JOIN startups s ON s.id=fr.startup_id WHERE s.founder_user_id=? ORDER BY fr.id ASC";
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,userId);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});}
        }return out;
    }

    public List<String[]> findOpenFundingRequestsForInvestor() throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="SELECT fr.id,s.name,fr.amount,fr.currency,fr.purpose,fr.status FROM funding_requests fr JOIN startups s ON s.id=fr.startup_id WHERE s.status IN ('INCUBATED','FUNDRAISING') AND fr.status IN ('OPEN','IN_REVIEW') ORDER BY fr.id ASC";
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});
        }return out;
    }

    /** Returns a database-backed page of historical rounds; no artificial row cap. */
    public List<String[]> findExternalRounds(String search, String sector, String stage, int limit, int offset) throws SQLException {
        List<String[]> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT startup_name,country,city,sector,announced_date,funding_stage,amount_usd,lead_investor,investor_count,news_mentions,source_url FROM external_funding_rounds WHERE 1=1");
        if (search != null && !search.isBlank()) sql.append(" AND (startup_name LIKE ? OR country LIKE ? OR city LIKE ? OR lead_investor LIKE ?)");
        if (sector != null && !sector.isBlank()) sql.append(" AND sector LIKE ?");
        if (stage != null && !stage.isBlank()) sql.append(" AND funding_stage LIKE ?");
        sql.append(" ORDER BY announced_date DESC, id DESC LIMIT ? OFFSET ?");
        try (Connection c = Db.get(); PreparedStatement p = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (search != null && !search.isBlank()) { String q = "%" + search.trim() + "%"; for (int n=0;n<4;n++) p.setString(i++,q); }
            if (sector != null && !sector.isBlank()) p.setString(i++, "%" + sector.trim() + "%");
            if (stage != null && !stage.isBlank()) p.setString(i++, "%" + stage.trim() + "%");
            p.setInt(i++, limit); p.setInt(i, offset);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getString(7),r.getString(8),r.getString(9),r.getString(10),r.getString(11)});
            }
        }
        return out;
    }

    public long countExternalRounds(String search, String sector, String stage) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM external_funding_rounds WHERE 1=1");
        if (search != null && !search.isBlank()) sql.append(" AND (startup_name LIKE ? OR country LIKE ? OR city LIKE ? OR lead_investor LIKE ?)");
        if (sector != null && !sector.isBlank()) sql.append(" AND sector LIKE ?");
        if (stage != null && !stage.isBlank()) sql.append(" AND funding_stage LIKE ?");
        try (Connection c = Db.get(); PreparedStatement p = c.prepareStatement(sql.toString())) {
            int i=1;
            if (search != null && !search.isBlank()) { String q="%"+search.trim()+"%"; for(int n=0;n<4;n++)p.setString(i++,q); }
            if (sector != null && !sector.isBlank()) p.setString(i++,"%"+sector.trim()+"%");
            if (stage != null && !stage.isBlank()) p.setString(i,"%"+stage.trim()+"%");
            try (ResultSet r=p.executeQuery()) { r.next(); return r.getLong(1); }
        }
    }

    public long countExternalRounds() throws SQLException {
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM external_funding_rounds");ResultSet r=p.executeQuery()) { r.next(); return r.getLong(1); }
    }

}
