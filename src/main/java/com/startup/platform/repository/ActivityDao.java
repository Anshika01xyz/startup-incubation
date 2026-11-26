package com.startup.platform.repository;

import com.startup.platform.model.*;
import com.startup.platform.util.*;

import com.startup.platform.util.Db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDao {
    public List<String[]> latest(int limit) throws SQLException {
        List<String[]> out = new ArrayList<>();
        String sql = "SELECT COALESCE(u.full_name,'System'),CASE WHEN a.action='DEMO_DATA_LOADED' THEN 'Workspace initialized' WHEN a.action LIKE 'HISTORICAL_%' THEN 'Funding activity recorded' ELSE a.action END,CASE WHEN a.entity_type='DEMO' THEN 'WORKSPACE' WHEN a.entity_type LIKE 'HISTORICAL%' THEN 'FUNDING RECORD' ELSE COALESCE(a.entity_type,'') END,CASE WHEN a.action='DEMO_DATA_LOADED' THEN 'Operational workspace records initialized.' WHEN a.action LIKE 'HISTORICAL_%' OR a.entity_type LIKE 'HISTORICAL%' THEN 'Funding information added to market intelligence.' WHEN a.details LIKE '%Demo%' THEN REPLACE(a.details,'Demo','Workspace') WHEN a.details LIKE '%HISTORICAL_DATASET%' THEN REPLACE(a.details,'HISTORICAL_DATASET','FUNDING RECORD') ELSE COALESCE(a.details,'') END,DATE_FORMAT(a.created_at,'%Y-%m-%d %H:%i') FROM activity_logs a LEFT JOIN users u ON u.id=a.actor_user_id ORDER BY a.created_at DESC LIMIT ?";
        try (Connection c = Db.get(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, Math.max(1, Math.min(limit, 500)));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(new String[]{r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)});
            }
        }
        return out;
    }

    public List<String[]> latestForUser(long userId, int limit) throws SQLException {
        List<String[]> out = new ArrayList<>();
        String sql = "SELECT COALESCE(u.full_name,'System'),CASE WHEN a.action='DEMO_DATA_LOADED' THEN 'Workspace initialized' WHEN a.action LIKE 'HISTORICAL_%' THEN 'Funding activity recorded' ELSE a.action END,CASE WHEN a.entity_type='DEMO' THEN 'WORKSPACE' WHEN a.entity_type LIKE 'HISTORICAL%' THEN 'FUNDING RECORD' ELSE COALESCE(a.entity_type,'') END,CASE WHEN a.action='DEMO_DATA_LOADED' THEN 'Operational workspace records initialized.' WHEN a.action LIKE 'HISTORICAL_%' OR a.entity_type LIKE 'HISTORICAL%' THEN 'Funding information added to market intelligence.' WHEN a.details LIKE '%Demo%' THEN REPLACE(a.details,'Demo','Workspace') WHEN a.details LIKE '%HISTORICAL_DATASET%' THEN REPLACE(a.details,'HISTORICAL_DATASET','FUNDING RECORD') ELSE COALESCE(a.details,'') END,DATE_FORMAT(a.created_at,'%Y-%m-%d %H:%i') FROM activity_logs a LEFT JOIN users u ON u.id=a.actor_user_id WHERE a.actor_user_id=? ORDER BY a.created_at DESC LIMIT ?";
        try (Connection c = Db.get(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, userId);
            p.setInt(2, Math.max(1, Math.min(limit, 500)));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(new String[]{r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)});
            }
        }
        return out;
    }
}
