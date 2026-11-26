package com.startup.platform.repository;

import com.startup.platform.model.*;
import com.startup.platform.util.*;

import com.startup.platform.util.Db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC data-access object for incubation programmes, applications and mentor queue. */
public class IncubationDao {
    public List<String[]> findPrograms() throws SQLException {
        List<String[]> out=new ArrayList<>();try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT p.id,p.name,i.name,p.duration_months FROM programs p JOIN incubators i ON i.id=p.incubator_id ORDER BY p.id ASC");ResultSet r=p.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)});
        }return out;
    }
    public List<String[]> findProgramsForIncubator(long userId) throws SQLException {
        List<String[]> out=new ArrayList<>();String sql="SELECT p.id,p.name,i.name,p.duration_months FROM programs p JOIN incubators i ON i.id=p.incubator_id WHERE i.user_id=? ORDER BY p.id ASC";
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,userId);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)});}}return out;
    }
    public List<String[]> findApplications() throws SQLException {
        List<String[]> out=new ArrayList<>();try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT a.id,s.name,p.name,a.application_date,a.status FROM applications a JOIN startups s ON s.id=a.startup_id JOIN programs p ON p.id=a.program_id ORDER BY a.id ASC");ResultSet r=p.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5)});
        }return out;
    }


    public List<String[]> findApplicationsForIncubator(long userId) throws SQLException {
        List<String[]> out=new ArrayList<>();String sql="SELECT a.id,s.name,p.name,a.application_date,a.status FROM applications a JOIN startups s ON s.id=a.startup_id JOIN programs p ON p.id=a.program_id JOIN incubators i ON i.id=p.incubator_id WHERE i.user_id=? ORDER BY a.id ASC";
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,userId);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5)});}}return out;
    }
    public List<String[]> findApplicationsForFounder(long userId) throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="SELECT a.id,s.name,p.name,a.application_date,a.status FROM applications a JOIN startups s ON s.id=a.startup_id JOIN programs p ON p.id=a.program_id WHERE s.founder_user_id=? ORDER BY a.id ASC";
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,userId);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5)});}
        }return out;
    }
    public List<String[]> findAssignedMilestones(long userId) throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="SELECT ms.id,s.name,ms.title,ms.due_date,ms.status FROM mentor_assignments ma JOIN mentors m ON m.id=ma.mentor_id JOIN startups s ON s.id=ma.startup_id JOIN milestones ms ON ms.startup_id=s.id WHERE m.user_id=? AND m.approval_status='APPROVED' ORDER BY ms.due_date";
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,userId);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5)});}
        }return out;
    }
    public List<String[]> findAllMentors() throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="""
            SELECT m.id,u.full_name,COALESCE(m.expertise,''),m.experience_years,m.approval_status,
                   COUNT(DISTINCT ma.startup_id) AS assigned_startups
            FROM mentors m
            JOIN users u ON u.id=m.user_id
            LEFT JOIN mentor_assignments ma ON ma.mentor_id=m.id
            GROUP BY m.id,u.full_name,m.expertise,m.experience_years,m.approval_status
            ORDER BY m.id ASC
            """;
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});
        }
        return out;
    }

    public List<String[]> findPendingMentors() throws SQLException {
        List<String[]> out=new ArrayList<>();try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT m.id,u.full_name,m.expertise,m.experience_years FROM mentors m JOIN users u ON u.id=m.user_id WHERE m.approval_status='PENDING'");ResultSet r=p.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)});
        }return out;
    }
}
