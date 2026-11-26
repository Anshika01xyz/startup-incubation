package com.startup.platform.repository;

import com.startup.platform.model.*;
import com.startup.platform.util.*;

import com.startup.platform.util.Db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.math.BigDecimal;
import com.startup.platform.model.StartupSummary;

/** JDBC data-access object for startup directory and consolidated profile reads. */
public class StartupDao {
    public List<String[]> findAll() throws SQLException {
        List<String[]> out=new ArrayList<>();
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT id,name,COALESCE(sector,''),COALESCE(city,''),COALESCE(stage,''),COALESCE(CAST(founded_year AS CHAR),'—') FROM startups ORDER BY id ASC");ResultSet r=p.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});
        }return out;
    }

    public List<String[]> findAllForUser(long userId) throws SQLException {
        List<String[]> out=new ArrayList<>();
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT id,name,COALESCE(sector,''),COALESCE(city,''),COALESCE(stage,''),COALESCE(CAST(founded_year AS CHAR),'—') FROM startups WHERE founder_user_id=? ORDER BY id ASC")){
            p.setLong(1,userId);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});}
        }return out;
    }


    public List<String[]> findAllForRole(String role,long userId) throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="""
            SELECT s.id,s.name,COALESCE(s.sector,''),COALESCE(s.city,''),COALESCE(s.stage,''),COALESCE(CAST(s.founded_year AS CHAR),'—')
            FROM startups s
            WHERE (?='ADMIN'
                OR (?='FOUNDER' AND s.founder_user_id=?)
                OR (?='INVESTOR' AND s.status IN ('INCUBATED','FUNDRAISING'))
                OR (?='MENTOR' AND EXISTS (SELECT 1 FROM mentor_assignments ma JOIN mentors m ON m.id=ma.mentor_id WHERE ma.startup_id=s.id AND m.user_id=? AND m.approval_status='APPROVED'))
                OR (?='EVALUATOR' AND EXISTS (SELECT 1 FROM applications a WHERE a.startup_id=s.id AND a.status IN ('SUBMITTED','UNDER_REVIEW'))))
            ORDER BY s.id ASC
            """;
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){
            int n=1;p.setString(n++,role);p.setString(n++,role);p.setLong(n++,userId);
            p.setString(n++,role);p.setString(n++,role);p.setLong(n++,userId);p.setString(n++,role);
            try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});}
        }return out;
    }

    public List<String[]> findStartup360ForUser(long userId) throws SQLException {
        List<String[]> out=new ArrayList<>();
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT id,name,sector,city,status,application_count,mentor_count,milestone_count,completed_milestones,funding_requested,investment_committed,readiness_score,team_count,latest_progress FROM v_startup_360 WHERE id IN (SELECT id FROM startups WHERE founder_user_id=?) ORDER BY name")){
            p.setLong(1,userId);try(ResultSet r=p.executeQuery()){while(r.next()){String[] row=new String[14];for(int i=0;i<14;i++)row[i]=r.getString(i+1);out.add(row);}}
        }return out;
    }


    public List<String[]> findStartup360ForRole(String role,long userId) throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="""
            SELECT v.id,v.name,v.sector,v.city,v.stage,v.application_count,v.mentor_count,v.milestone_count,
                   v.completed_milestones,v.funding_requested,v.investment_committed,v.readiness_score,
                   v.team_count,v.latest_progress
            FROM v_startup_360 v
            JOIN startups s ON s.id=v.id
            WHERE (?='ADMIN'
                OR (?='FOUNDER' AND s.founder_user_id=?)
                OR (?='INVESTOR' AND s.status IN ('INCUBATED','FUNDRAISING'))
                OR (?='MENTOR' AND EXISTS (
                    SELECT 1 FROM mentor_assignments ma JOIN mentors m ON m.id=ma.mentor_id
                    WHERE ma.startup_id=s.id AND m.user_id=? AND m.approval_status='APPROVED'))
                OR (?='EVALUATOR' AND EXISTS (
                    SELECT 1 FROM applications a WHERE a.startup_id=s.id))
                OR (?='INCUBATOR' AND EXISTS (
                    SELECT 1 FROM applications a JOIN programs p ON p.id=a.program_id
                    JOIN incubators i ON i.id=p.incubator_id
                    WHERE a.startup_id=s.id AND i.user_id=?)))
            ORDER BY s.id ASC
            """;
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){
            int n=1;p.setString(n++,role);p.setString(n++,role);p.setLong(n++,userId);
            p.setString(n++,role);p.setString(n++,role);p.setLong(n++,userId);p.setString(n++,role);
            p.setString(n++,role);p.setLong(n++,userId);
            try(ResultSet r=p.executeQuery()){while(r.next()){String[] row=new String[14];for(int i=0;i<14;i++)row[i]=r.getString(i+1);out.add(row);}}
        }return out;
    }

    public List<StartupSummary> findSummaries(Long founderUserId) throws SQLException {
        List<StartupSummary> out = new ArrayList<>();
        String base = "SELECT v.id,v.name,v.sector,v.city,v.latest_progress,v.readiness_score,v.completed_milestones,v.milestone_count,v.funding_requested,v.investment_committed,v.updated_at FROM v_startup_360 v JOIN startups s ON s.id=v.id";
        String sql = founderUserId == null ? base + " ORDER BY v.name" : base + " WHERE s.founder_user_id=? ORDER BY v.name";
        try (Connection c = Db.get(); PreparedStatement p = c.prepareStatement(sql)) {
            if (founderUserId != null) p.setLong(1, founderUserId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Timestamp timestamp = r.getTimestamp(11);
                    LocalDateTime updatedAt = timestamp == null ? LocalDateTime.now() : timestamp.toLocalDateTime();
                    BigDecimal requested = r.getBigDecimal(9);
                    BigDecimal committed = r.getBigDecimal(10);
                    out.add(new StartupSummary(r.getLong(1), r.getString(2), r.getString(3), r.getString(4),
                            r.getDouble(5), r.getDouble(6), r.getInt(7), r.getInt(8), requested, committed, updatedAt));
                }
            }
        }
        return out;
    }

    public List<String> findStatuses(Long founderUserId) throws SQLException {
        List<String> out = new ArrayList<>();
        String sql = founderUserId == null ? "SELECT status FROM startups" : "SELECT status FROM startups WHERE founder_user_id=?";
        try (Connection c = Db.get(); PreparedStatement p = c.prepareStatement(sql)) {
            if (founderUserId != null) p.setLong(1, founderUserId);
            try (ResultSet r = p.executeQuery()) { while (r.next()) out.add(r.getString(1)); }
        }
        return out;
    }

    public List<String[]> findStartup360() throws SQLException {
        List<String[]> out=new ArrayList<>();
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT id,name,sector,city,status,application_count,mentor_count,milestone_count,completed_milestones,funding_requested,investment_committed,readiness_score,team_count,latest_progress FROM v_startup_360 ORDER BY name");ResultSet r=p.executeQuery()){
            while(r.next()){String[] row=new String[14];for(int i=0;i<14;i++)row[i]=r.getString(i+1);out.add(row);}
        }return out;
    }
}
