package com.startup.platform.repository;

import com.startup.platform.util.Db;
import java.sql.*;
import java.util.*;

/** Aggregations over the publisher-sourced historical funding-round dataset. */
public final class HistoricalAnalyticsDao {
    public List<String[]> kpis() throws SQLException {
        String sql = "SELECT COUNT(*), COUNT(DISTINCT startup_name), COUNT(DISTINCT COALESCE(NULLIF(sector,''),'Unclassified')), COUNT(DISTINCT COALESCE(NULLIF(country,''),'Unknown')), COALESCE(SUM(amount_usd),0) FROM external_funding_rounds";
        try(Connection c=Db.get(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            r.next(); return java.util.Collections.singletonList(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5)});
        }
    }
    public List<String[]> sectors(int limit) throws SQLException {
        return query("SELECT COALESCE(NULLIF(sector,''),'Unclassified'),COUNT(*) FROM external_funding_rounds GROUP BY 1 ORDER BY 2 DESC LIMIT ?",limit);
    }
    public List<String[]> stages(int limit) throws SQLException {
        return query("SELECT COALESCE(NULLIF(funding_stage,''),'Undisclosed'),COUNT(*) FROM external_funding_rounds GROUP BY 1 ORDER BY 2 DESC LIMIT ?",limit);
    }
    public List<String[]> yearly() throws SQLException {
        List<String[]> rows=new ArrayList<>();
        String sql="SELECT announcement_year,COUNT(*) FROM external_funding_rounds WHERE announcement_year IS NOT NULL GROUP BY announcement_year ORDER BY announcement_year";
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()) { while(r.next()) rows.add(new String[]{r.getString(1),r.getString(2)}); }
        return rows;
    }
    public List<String[]> topCompanies(int limit) throws SQLException {
        String sql="SELECT startup_name,COALESCE(sector,'—'),COALESCE(city,'—'),COUNT(*) rounds,COALESCE(SUM(amount_usd),0) total_usd,MAX(announced_date) latest FROM external_funding_rounds GROUP BY startup_name,sector,city ORDER BY total_usd DESC,rounds DESC LIMIT ?";
        List<String[]> rows=new ArrayList<>(); try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,limit);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});}} return rows;
    }
    public List<String[]> latest(int limit) throws SQLException {
        String sql="SELECT startup_name,COALESCE(country,'—'),COALESCE(city,'—'),COALESCE(sector,'—'),COALESCE(funding_stage,'—'),COALESCE(CAST(amount_usd AS CHAR),'Undisclosed'),COALESCE(lead_investor,'—'),COALESCE(CAST(announced_date AS CHAR),'—') FROM external_funding_rounds ORDER BY announced_date DESC,id DESC LIMIT ?";
        List<String[]> rows=new ArrayList<>();try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,limit);try(ResultSet r=p.executeQuery()){while(r.next()){String[] x=new String[8];for(int i=0;i<8;i++)x[i]=r.getString(i+1);rows.add(x);}}}return rows;
    }
    private List<String[]> query(String sql,int limit) throws SQLException {
        List<String[]> rows=new ArrayList<>();try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,limit);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new String[]{r.getString(1),r.getString(2)});}}return rows;
    }
}
