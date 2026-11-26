package com.startup.platform.service;

import com.startup.platform.model.*;
import com.startup.platform.exception.*;
import com.startup.platform.util.*;
import com.startup.platform.config.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import com.startup.platform.repository.*;
import com.startup.platform.service.ReadinessCalculator;
import com.startup.platform.model.EvaluationScorecard;
import com.startup.platform.exception.AuthorizationException;
import com.startup.platform.exception.ValidationException;
import com.startup.platform.service.StartupAnalyticsService;
import com.startup.platform.util.CollectionExercises;
import com.startup.platform.model.StartupSummary;
import com.startup.platform.model.AbstractProfile;
import com.startup.platform.model.FounderProfile;
import com.startup.platform.model.MentorProfile;
import com.startup.platform.model.InvestorProfile;
import com.startup.platform.repository.CrudRepository;
import com.startup.platform.repository.InMemoryRepository;

public class PlatformService {
    private final ReadinessCalculator readinessCalculator = new ReadinessCalculator();
    private final StartupAnalyticsService startupAnalytics = new StartupAnalyticsService();
    public Session login(String email, String password) throws Exception {
        try (Connection c=Db.get(); PreparedStatement p=c.prepareStatement(
            "SELECT u.id,u.full_name,u.email,u.password_hash,u.status,r.role_name FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id WHERE u.email=?")) {
            p.setString(1,email); try(ResultSet rs=p.executeQuery()) {
                while(rs.next()) if ("ACTIVE".equals(rs.getString("status")) && Security.verify(password,rs.getString("password_hash")))
                    return new Session(rs.getLong("id"),rs.getString("full_name"),rs.getString("email"),rs.getString("role_name"));
            }
        } return null;
    }

    public long bootstrapAdmin(String name,String email,String password) throws Exception {
        if(name == null || name.isBlank() || email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password == null || password.length()<10) throw new ValidationException("Enter a valid name/email and a password of at least 10 characters.");
        return Db.transaction(c->{
            try(PreparedStatement q=c.prepareStatement("SELECT COUNT(*) FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE r.role_name='ADMIN'" );ResultSet rs=q.executeQuery()){
                rs.next(); if(rs.getLong(1)>0) throw new IllegalStateException("An administrator already exists. Ask the existing administrator to create accounts.");
            }
            long id;
            try(PreparedStatement p=c.prepareStatement("INSERT INTO users(full_name,email,password_hash,status) VALUES(?,?,?,'ACTIVE')",Statement.RETURN_GENERATED_KEYS)){
                p.setString(1,name);p.setString(2,email);p.setString(3,Security.hash(password));p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){k.next();id=k.getLong(1);}
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE role_name='ADMIN'")){p.setLong(1,id);p.executeUpdate();}
            log(c,id,"BOOTSTRAP_ADMIN","USER",id,"First administrator created");return id;
        });
    }

    public List<String[]> programs(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","EVALUATOR","INCUBATOR");
        return s.role().equals("INCUBATOR") ? new IncubationDao().findProgramsForIncubator(s.userId()) : new IncubationDao().findPrograms();
    }
    public List<String[]> applications(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","EVALUATOR","INCUBATOR");
        return s.role().equals("FOUNDER") ? new IncubationDao().findApplicationsForFounder(s.userId()) : (s.role().equals("INCUBATOR") ? new IncubationDao().findApplicationsForIncubator(s.userId()) : new IncubationDao().findApplications());
    }


    public void updateStartupProgress(Session s,long startupId,double percent,String summary) throws Exception {
        require(s,"FOUNDER","ADMIN");
        if(!Double.isFinite(percent)||percent<0||percent>100||summary==null||summary.isBlank())
            throw new ValidationException("Progress must be 0–100 and a summary is required.");
        Db.transaction(c->{
            if(s.role().equals("FOUNDER"))try(PreparedStatement q=c.prepareStatement("SELECT 1 FROM startups WHERE id=? AND founder_user_id=?")){
                q.setLong(1,startupId);q.setLong(2,s.userId());try(ResultSet r=q.executeQuery()){if(!r.next())throw new AuthorizationException("You can only update your own startup.");}
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) VALUES(?,?,?,?)")){
                p.setLong(1,startupId);p.setDouble(2,percent);p.setString(3,summary.trim());p.setLong(4,s.userId());p.executeUpdate();
            }
            log(c,s.userId(),"UPDATE_PROGRESS","STARTUP",startupId,percent+"% — "+summary.trim());
            return null;
        });
    }

    public void addTeamMember(Session s,long startupId,String memberName,String role,String email) throws Exception {
        require(s,"FOUNDER","ADMIN");
        if(memberName==null||memberName.isBlank()||role==null||role.isBlank())throw new ValidationException("Team member name and role are required.");
        Db.transaction(c->{
            if(s.role().equals("FOUNDER"))try(PreparedStatement q=c.prepareStatement("SELECT 1 FROM startups WHERE id=? AND founder_user_id=?")){
                q.setLong(1,startupId);q.setLong(2,s.userId());try(ResultSet r=q.executeQuery()){if(!r.next())throw new AuthorizationException("You can only manage the team of your own startup.");}
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO startup_teams(startup_id,member_name,member_role,email,joined_on) VALUES(?,?,?,?,CURRENT_DATE)")){
                p.setLong(1,startupId);p.setString(2,memberName.trim());p.setString(3,role.trim());p.setString(4,email==null||email.isBlank()?null:email.trim());p.executeUpdate();
            }
            log(c,s.userId(),"ADD_TEAM_MEMBER","STARTUP",startupId,memberName.trim()+" — "+role.trim());return null;
        });
    }
    public void linkCoFounder(Session s,long startupId,String coFounderEmail,String role) throws Exception {
        require(s,"FOUNDER","ADMIN");
        if(coFounderEmail==null||coFounderEmail.isBlank()||role==null||role.isBlank())throw new ValidationException("Co-founder email and role are required.");
        Db.transaction(c->{
            if(s.role().equals("FOUNDER"))try(PreparedStatement q=c.prepareStatement("SELECT 1 FROM startups WHERE id=? AND founder_user_id=?")){
                q.setLong(1,startupId);q.setLong(2,s.userId());try(ResultSet r=q.executeQuery()){if(!r.next())throw new AuthorizationException("You can only manage your own startup.");}
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO startup_founders(startup_id,founder_id,founder_role) SELECT ?,f.id,? FROM founders f JOIN users u ON u.id=f.user_id JOIN user_roles ur ON ur.user_id=u.id JOIN roles ro ON ro.id=ur.role_id WHERE u.email=? AND u.status='ACTIVE' AND ro.role_name='FOUNDER'")){
                p.setLong(1,startupId);p.setString(2,role.trim());p.setString(3,coFounderEmail.trim());
                if(p.executeUpdate()!=1)throw new IllegalArgumentException("No active founder account found for that email, or this founder is already linked.");
            }
            log(c,s.userId(),"LINK_COFOUNDER","STARTUP",startupId,coFounderEmail.trim());return null;
        });
    }
    public void uploadStartupDocument(Session s,long startupId,java.io.File source,String documentType) throws Exception {
        require(s,"FOUNDER","ADMIN");
        try(Connection c=Db.get()){
            if(s.role().equals("FOUNDER"))try(PreparedStatement q=c.prepareStatement("SELECT 1 FROM startups WHERE id=? AND founder_user_id=?")){q.setLong(1,startupId);q.setLong(2,s.userId());try(ResultSet r=q.executeQuery()){if(!r.next())throw new SecurityException("You can only upload documents for your own startup.");}}
            java.nio.file.Path dir=java.nio.file.Path.of("uploads", "startup-"+startupId);java.nio.file.Files.createDirectories(dir);
            String safe=java.util.UUID.randomUUID()+"_"+source.getName().replaceAll("[^A-Za-z0-9._-]","_");
            java.nio.file.Path target=dir.resolve(safe);java.nio.file.Files.copy(source.toPath(),target,java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            try(PreparedStatement p=c.prepareStatement("INSERT INTO startup_documents(startup_id,file_name,stored_path,document_type) VALUES(?,?,?,?)")){p.setLong(1,startupId);p.setString(2,source.getName());p.setString(3,target.toString());p.setString(4,documentType);p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO activity_logs(actor_user_id,action,entity_type,entity_id,details) VALUES(?,?,?,?,?)")){p.setLong(1,s.userId());p.setString(2,"UPLOAD_DOCUMENT");p.setString(3,"STARTUP");p.setLong(4,startupId);p.setString(5,source.getName());p.executeUpdate();}
        }
    }

    public void createIncubator(Session s,String name,String city,String description) throws Exception {
        require(s,"ADMIN");
        if(name==null||name.isBlank())throw new ValidationException("Incubator name is required.");
        Db.transaction(c->{
            long id;
            try(PreparedStatement p=c.prepareStatement("INSERT INTO incubators(name,city,description) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                p.setString(1,name.trim());p.setString(2,city);p.setString(3,description);p.executeUpdate();
                try(ResultSet keys=p.getGeneratedKeys()){if(!keys.next())throw new SQLException("Incubator ID was not returned.");id=keys.getLong(1);}
            }
            log(c,s.userId(),"CREATE_INCUBATOR","INCUBATOR",id,name.trim());return null;
        });
    }
    public void createProgram(Session s,long incubatorId,String name,int durationMonths) throws Exception {
        require(s,"ADMIN","INCUBATOR");
        if(name==null||name.isBlank()||durationMonths<1||durationMonths>36)throw new ValidationException("Enter a programme name and a duration between 1 and 36 months.");
        Db.transaction(c->{
            if(s.role().equals("INCUBATOR")) try(PreparedStatement q=c.prepareStatement("SELECT 1 FROM incubators WHERE id=? AND user_id=?")){q.setLong(1,incubatorId);q.setLong(2,s.userId());try(ResultSet r=q.executeQuery()){if(!r.next())throw new AuthorizationException("You can only manage programmes for your own incubator.");}}
            long id;
            try(PreparedStatement p=c.prepareStatement("INSERT INTO programs(incubator_id,name,duration_months) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                p.setLong(1,incubatorId);p.setString(2,name.trim());p.setInt(3,durationMonths);p.executeUpdate();
                try(ResultSet keys=p.getGeneratedKeys()){if(!keys.next())throw new SQLException("Programme ID was not returned.");id=keys.getLong(1);}
            }
            log(c,s.userId(),"CREATE_PROGRAM","PROGRAM",id,name.trim());return null;
        });
    }
    public long ownIncubatorId(Session s) throws Exception {
        require(s,"INCUBATOR");
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT id FROM incubators WHERE user_id=?")){
            p.setLong(1,s.userId());try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalStateException("No incubator profile is linked to this account. Contact the administrator.");return r.getLong(1);}
        }
    }
    public List<String[]> incubators(Session s) throws Exception {
        require(s,"ADMIN");
        List<String[]> out=new ArrayList<>();try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT id,name,city FROM incubators ORDER BY name");ResultSet r=p.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3)});
        }return out;
    }

    public void createEvaluator(Session s,String name,String email,String password,String expertise) throws Exception {
        require(s,"ADMIN");if(name==null||name.isBlank()||email==null||!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")||password==null||password.length()<8)throw new ValidationException("Enter a valid evaluator name/email and a password of at least 8 characters.");
        Db.transaction(c->{long id;try(PreparedStatement p=c.prepareStatement("INSERT INTO users(full_name,email,password_hash,status) VALUES(?,?,?,'ACTIVE')",Statement.RETURN_GENERATED_KEYS)){
            p.setString(1,name);p.setString(2,email);p.setString(3,Security.hash(password));p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){k.next();id=k.getLong(1);}}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE role_name='EVALUATOR'")){p.setLong(1,id);p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO evaluators(user_id,expertise) VALUES(?,?)")){p.setLong(1,id);p.setString(2,expertise);p.executeUpdate();}
            log(c,s.userId(),"CREATE_EVALUATOR","USER",id,email);return null;});
    }


    /** All signed-in workspace roles may explore the public historical dataset. */
    public List<String[]> externalFundingRounds(Session s, String search, String sector, String stage, int page, int pageSize) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        if (page < 0 || pageSize < 1 || pageSize > 250) throw new ValidationException("Dataset page must be non-negative and page size must be between 1 and 250.");
        return new FundingDao().findExternalRounds(search, sector, stage, pageSize, Math.multiplyExact(page,pageSize));
    }
    public long externalFundingRoundCount(Session s, String search, String sector, String stage) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        return new FundingDao().countExternalRounds(search,sector,stage);
    }
    public long externalFundingRoundCount(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        return new FundingDao().countExternalRounds();
    }

    public List<String[]> liveStartupIntelligence(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        return new StartupDao().findStartup360ForRole(s.role(),s.userId());
    }
    public List<String[]> startup360(Session s) throws Exception {
        List<String[]> live = liveStartupIntelligence(s);
        live.addAll(new DatasetProjectionDao().startup360());
        return live;
    }
    public List<String[]> activityTimeline(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        return s.role().equals("ADMIN") ? new ActivityDao().latest(150) : new ActivityDao().latestForUser(s.userId(),150);
    }
    public List<String[]> milestoneHealth(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        List<String[]> out=new ArrayList<>();
        String sql="SELECT mh.id,mh.startup_name,mh.title,mh.due_date,mh.status,mh.health FROM v_milestone_health mh JOIN startups st ON st.id=mh.startup_id WHERE (?='ADMIN' OR (?='EVALUATOR' AND st.status IN ('SUBMITTED','INCUBATED')) OR (?='INVESTOR' AND st.status IN ('INCUBATED','FUNDRAISING')) OR st.founder_user_id=? OR EXISTS (SELECT 1 FROM mentor_assignments ma JOIN mentors me ON me.id=ma.mentor_id WHERE ma.startup_id=st.id AND me.user_id=?) OR (?='INCUBATOR' AND EXISTS (SELECT 1 FROM applications a JOIN programs pr ON pr.id=a.program_id JOIN incubators ic ON ic.id=pr.incubator_id WHERE a.startup_id=st.id AND ic.user_id=?))) ORDER BY mh.id ASC";
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,s.role());p.setString(2,s.role());p.setString(3,s.role());p.setLong(4,s.userId());p.setLong(5,s.userId());p.setString(6,s.role());p.setLong(7,s.userId());
            try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});}
        }
        return out;
    }

    public long register(String name,String email,String password,String role,String profile) throws Exception {
        return register(name,email,password,role,profile,0);
    }
    public long register(String name,String email,String password,String role,String profile,int experienceYears) throws Exception {
        if(name == null || name.isBlank() || email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password == null || password.length()<8) throw new ValidationException("Enter a valid name/email and a password of at least 8 characters.");
        if(role==null||!Set.of("FOUNDER","MENTOR","INVESTOR","INCUBATOR").contains(role)) throw new ValidationException("Unsupported registration role.");
        if(experienceYears<0||experienceYears>60)throw new ValidationException("Mentor experience must be between 0 and 60 years.");
        if(role.equals("MENTOR")&&(profile==null||profile.isBlank()))throw new ValidationException("Mentor expertise is required.");
        return Db.transaction(c -> {
            String status=role.equals("MENTOR")?"PENDING":"ACTIVE";
            long id;
            try(PreparedStatement p=c.prepareStatement("INSERT INTO users(full_name,email,password_hash,status) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                p.setString(1,name);p.setString(2,email);p.setString(3,Security.hash(password));p.setString(4,status);p.executeUpdate();
                try(ResultSet k=p.getGeneratedKeys()){k.next();id=k.getLong(1);}
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE role_name=?")){
                p.setLong(1,id);p.setString(2,role);p.executeUpdate();
            }
            if(role.equals("FOUNDER")) try(PreparedStatement p=c.prepareStatement("INSERT INTO founders(user_id,bio) VALUES(?,?)")){p.setLong(1,id);p.setString(2,profile);p.executeUpdate();}
            if(role.equals("MENTOR")) try(PreparedStatement p=c.prepareStatement("INSERT INTO mentors(user_id,expertise,experience_years,approval_status) VALUES(?,?,?,'PENDING')")){p.setLong(1,id);p.setString(2,profile);p.setInt(3,experienceYears);p.executeUpdate();}
            if(role.equals("INVESTOR")) try(PreparedStatement p=c.prepareStatement("INSERT INTO investors(user_id,organization) VALUES(?,?)")){p.setLong(1,id);p.setString(2,profile);p.executeUpdate();}
            if(role.equals("INCUBATOR")) try(PreparedStatement p=c.prepareStatement("INSERT INTO incubators(name,city,description,user_id) VALUES(?,NULL,?,?)")){p.setString(1,name.trim()+" Incubator");p.setString(2,profile);p.setLong(3,id);p.executeUpdate();}
            AbstractProfile domainProfile = switch (role) {
                case "FOUNDER" -> new FounderProfile(id,name,email,profile,java.time.LocalDateTime.now());
                case "MENTOR" -> new MentorProfile(id,name,email,profile,experienceYears,java.time.LocalDateTime.now());
                case "INVESTOR" -> new InvestorProfile(id,name,email,profile,java.time.LocalDateTime.now());
                case "INCUBATOR" -> new AbstractProfile(id,name,email,java.time.LocalDateTime.now()) { @Override public String profileType(){ return "INCUBATOR"; } @Override public String summary(){ return "Incubator profile: "+name; } };
                default -> throw new ValidationException("Unsupported registration role.");
            };
            log(c,id,"REGISTER",role,id,domainProfile.summary());
            return id;
        });
    }
    public long createStartup(Session s,String name,String sector,String city,String description,int year,String stage) throws Exception {
        require(s,"FOUNDER");
        if(name==null||name.isBlank()||sector==null||sector.isBlank()||year<1900||year>LocalDate.now().getYear()||stage==null||stage.isBlank())
            throw new ValidationException("Enter startup name, sector, valid founded year and stage.");
        return Db.transaction(c->{long id;try(PreparedStatement p=c.prepareStatement(
            "INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            p.setString(1,name);p.setString(2,sector);p.setString(3,city);p.setString(4,description);p.setInt(5,year);p.setString(6,stage);p.setLong(7,s.userId());p.executeUpdate();
            try(ResultSet k=p.getGeneratedKeys()){k.next();id=k.getLong(1);}}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO startup_founders(startup_id,founder_id,founder_role) SELECT ?,id,'Founder' FROM founders WHERE user_id=?")){p.setLong(1,id);p.setLong(2,s.userId());p.executeUpdate();}
            log(c,s.userId(),"CREATE_STARTUP","STARTUP",id,name);return id;});
    }
    public List<String[]> startups(Session session) throws Exception {
        require(session,"ADMIN","FOUNDER","EVALUATOR","MENTOR","INVESTOR");
        List<String[]> live = new StartupDao().findAllForRole(session.role(),session.userId());
        live.addAll(new DatasetProjectionDao().startupDirectory());
        return live;
    }
    public List<String[]> assignedMilestones(Session session) throws Exception {
        require(session,"MENTOR");
        return new IncubationDao().findAssignedMilestones(session.userId());
    }
    public void submitApplication(Session s,long startupId,long programId) throws Exception {
        require(s,"FOUNDER"); Db.transaction(c->{try(PreparedStatement p=c.prepareStatement(
            "INSERT INTO applications(startup_id,program_id,application_date,status) SELECT ?,?,CURRENT_DATE,'SUBMITTED' FROM startups WHERE id=? AND founder_user_id=?")){
            p.setLong(1,startupId);p.setLong(2,programId);p.setLong(3,startupId);p.setLong(4,s.userId());
            if(p.executeUpdate()!=1)throw new IllegalArgumentException("Startup not found or not owned by you.");
        }
        try(PreparedStatement p=c.prepareStatement("UPDATE startups SET status='SUBMITTED' WHERE id=?")){p.setLong(1,startupId);p.executeUpdate();}
        log(c,s.userId(),"SUBMIT_APPLICATION","STARTUP",startupId,"Application submitted");return null;});
    }
    public void evaluate(Session s,long applicationId,double innovation,double market,double feasibility,double team,String comments,String recommendation) throws Exception {
        require(s,"EVALUATOR","ADMIN");
        EvaluationScorecard scorecard = new EvaluationScorecard(innovation, market, feasibility, team);
        double readiness = readinessCalculator.calculate(scorecard);
        if (!Set.of("ACCEPT", "REJECT", "REVIEW").contains(recommendation))
            throw new ValidationException("Choose ACCEPT, REJECT or REVIEW.");
        Db.transaction(c->{long evaluationId;try(PreparedStatement p=c.prepareStatement("INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) VALUES(?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            p.setLong(1,applicationId);p.setLong(2,s.userId());p.setDouble(3,innovation);p.setDouble(4,market);p.setDouble(5,feasibility);p.setDouble(6,team);p.setString(7,comments);p.setString(8,recommendation);p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){k.next();evaluationId=k.getLong(1);}}
            double[] scores={innovation,market,feasibility,team};String[] criteria={"Innovation","Market","Feasibility","Team"};
            for(int n=0;n<criteria.length;n++)try(PreparedStatement p=c.prepareStatement("INSERT INTO evaluation_scores(evaluation_id,criterion_id,score) SELECT ?,id,? FROM evaluation_criteria WHERE criterion_name=?")){p.setLong(1,evaluationId);p.setDouble(2,scores[n]);p.setString(3,criteria[n]);p.executeUpdate();}
            String st=recommendation.equals("ACCEPT")?"ACCEPTED":recommendation.equals("REJECT")?"REJECTED":"UNDER_REVIEW";
            long startupId;
            try(PreparedStatement q=c.prepareStatement("SELECT startup_id FROM applications WHERE id=?")){q.setLong(1,applicationId);try(ResultSet r=q.executeQuery()){if(!r.next())throw new IllegalArgumentException("Application not found.");startupId=r.getLong(1);}}
            try(PreparedStatement p=c.prepareStatement("UPDATE applications SET status=? WHERE id=?")){p.setString(1,st);p.setLong(2,applicationId);p.executeUpdate();}
            if(recommendation.equals("ACCEPT"))try(PreparedStatement p=c.prepareStatement("UPDATE startups SET status='INCUBATED' WHERE id=?")){p.setLong(1,startupId);p.executeUpdate();}
            if(recommendation.equals("REJECT"))try(PreparedStatement p=c.prepareStatement("UPDATE startups SET status='DRAFT' WHERE id=?")){p.setLong(1,startupId);p.executeUpdate();}
            long founderUserId;
            try(PreparedStatement q=c.prepareStatement("SELECT founder_user_id FROM startups WHERE id=?")){q.setLong(1,startupId);try(ResultSet r=q.executeQuery()){r.next();founderUserId=r.getLong(1);}}
            notify(c,founderUserId,"Your incubation application #"+applicationId+" was reviewed: "+st+".");
            log(c,s.userId(),"EVALUATE","APPLICATION",applicationId,recommendation + " | readiness=" + readiness + "/100");return null;});
    }
    public void approveMentor(Session s,long mentorId,boolean approve) throws Exception {
        require(s,"ADMIN");
        Db.transaction(c->{long userId;
            try(PreparedStatement q=c.prepareStatement("SELECT user_id FROM mentors WHERE id=?")){q.setLong(1,mentorId);try(ResultSet r=q.executeQuery()){if(!r.next())throw new IllegalArgumentException("Mentor not found.");userId=r.getLong(1);}}
            try(PreparedStatement p=c.prepareStatement("UPDATE mentors SET approval_status=? WHERE id=?")){p.setString(1,approve?"APPROVED":"REJECTED");p.setLong(2,mentorId);p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("UPDATE users SET status=? WHERE id=?")){p.setString(1,approve?"ACTIVE":"REJECTED");p.setLong(2,userId);p.executeUpdate();}
            notify(c,userId,approve?"Your mentor profile has been approved. You can now sign in.":"Your mentor registration was not approved.");
            log(c,s.userId(),approve?"APPROVE_MENTOR":"REJECT_MENTOR","MENTOR",mentorId,"");return null;});
    }
    public void assignMentor(Session s,long mentorId,long startupId) throws Exception {
        require(s,"ADMIN");
        Db.transaction(c->{long mentorUserId;String startupName;
            try(PreparedStatement q=c.prepareStatement("SELECT user_id FROM mentors WHERE id=? AND approval_status='APPROVED'")){q.setLong(1,mentorId);try(ResultSet r=q.executeQuery()){if(!r.next())throw new IllegalArgumentException("Only approved mentors can be assigned.");mentorUserId=r.getLong(1);}}
            try(PreparedStatement q=c.prepareStatement("SELECT name FROM startups WHERE id=?")){q.setLong(1,startupId);try(ResultSet r=q.executeQuery()){if(!r.next())throw new IllegalArgumentException("Startup not found.");startupName=r.getString(1);}}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO mentor_assignments(mentor_id,startup_id,assigned_by) VALUES(?,?,?)")){p.setLong(1,mentorId);p.setLong(2,startupId);p.setLong(3,s.userId());p.executeUpdate();}
            notify(c,mentorUserId,"You have been assigned to support startup: "+startupName+".");
            log(c,s.userId(),"ASSIGN_MENTOR","STARTUP",startupId,"Mentor ID "+mentorId);return null;});
    }
    public void addMilestone(Session s,long startupId,String title,LocalDate due) throws Exception {
        require(s,"ADMIN","FOUNDER");
        if(title==null||title.isBlank()||due==null)throw new ValidationException("Milestone title and due date are required.");
        Db.transaction(c->{
            if(s.role().equals("FOUNDER"))try(PreparedStatement q=c.prepareStatement("SELECT 1 FROM startups WHERE id=? AND founder_user_id=?")){q.setLong(1,startupId);q.setLong(2,s.userId());try(ResultSet r=q.executeQuery()){if(!r.next())throw new AuthorizationException("You can only add milestones to your own startup.");}}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO milestones(startup_id,title,due_date) VALUES(?,?,?)")){p.setLong(1,startupId);p.setString(2,title.trim());p.setDate(3,java.sql.Date.valueOf(due));p.executeUpdate();}
            log(c,s.userId(),"CREATE_MILESTONE","STARTUP",startupId,title);return null;});
    }
    public void updateMilestone(Session s,long milestoneId,String status,String note) throws Exception {
        require(s,"ADMIN","MENTOR");
        if(!Set.of("NOT_STARTED","IN_PROGRESS","COMPLETED","BLOCKED").contains(status))throw new ValidationException("Invalid milestone status.");
        Db.transaction(c->{
            if(s.role().equals("MENTOR"))try(PreparedStatement q=c.prepareStatement(
                    "SELECT 1 FROM milestones ms JOIN mentor_assignments ma ON ma.startup_id=ms.startup_id JOIN mentors m ON m.id=ma.mentor_id WHERE ms.id=? AND m.user_id=? AND m.approval_status='APPROVED'")){
                q.setLong(1,milestoneId);q.setLong(2,s.userId());
                try(ResultSet r=q.executeQuery()){if(!r.next())throw new AuthorizationException("You can only update milestones for startups assigned to you.");}
            }long startupId;long founderUserId;
            try(PreparedStatement q=c.prepareStatement("SELECT ms.startup_id,st.founder_user_id FROM milestones ms JOIN startups st ON st.id=ms.startup_id WHERE ms.id=?")){q.setLong(1,milestoneId);try(ResultSet r=q.executeQuery()){if(!r.next())throw new IllegalArgumentException("Milestone not found.");startupId=r.getLong(1);founderUserId=r.getLong(2);}}
            if(s.role().equals("MENTOR"))try(PreparedStatement q=c.prepareStatement("SELECT 1 FROM mentor_assignments ma JOIN mentors m ON m.id=ma.mentor_id WHERE m.user_id=? AND ma.startup_id=? AND m.approval_status='APPROVED'")){q.setLong(1,s.userId());q.setLong(2,startupId);try(ResultSet r=q.executeQuery()){if(!r.next())throw new AuthorizationException("You are not assigned to this startup.");}}
            try(PreparedStatement p=c.prepareStatement("UPDATE milestones SET status=? WHERE id=?")){p.setString(1,status);p.setLong(2,milestoneId);p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO milestone_updates(milestone_id,update_text,updated_by) VALUES(?,?,?)")){p.setLong(1,milestoneId);p.setString(2,note==null?"":note);p.setLong(3,s.userId());p.executeUpdate();}
            notify(c,founderUserId,"A milestone for your startup was updated: "+status+".");
            log(c,s.userId(),"UPDATE_MILESTONE","MILESTONE",milestoneId,status+" | "+note);return null;});
    }
    public long createFundingRequest(Session s,long startupId,BigDecimal amount,String purpose) throws Exception {
        require(s,"FOUNDER");
        if(amount==null||amount.signum()<=0)throw new ValidationException("Funding amount must be positive.");
        return Db.transaction(c->{
            long requestId;
            try(PreparedStatement p=c.prepareStatement("INSERT INTO funding_requests(startup_id,amount,purpose) SELECT id,?,? FROM startups WHERE id=? AND founder_user_id=?",Statement.RETURN_GENERATED_KEYS)){
                p.setBigDecimal(1,amount);p.setString(2,purpose);p.setLong(3,startupId);p.setLong(4,s.userId());
                if(p.executeUpdate()!=1)throw new IllegalArgumentException("Startup not found or not owned by you.");
                try(ResultSet keys=p.getGeneratedKeys()){if(!keys.next())throw new SQLException("Funding request ID was not returned.");requestId=keys.getLong(1);}
            }
            try(PreparedStatement p=c.prepareStatement("UPDATE startups SET status='FUNDRAISING' WHERE id=?")){p.setLong(1,startupId);p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO notifications(user_id,message) SELECT u.id,? FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id WHERE r.role_name='INVESTOR' AND u.status='ACTIVE'")){
                p.setString(1,"A new funding opportunity is available for startup #"+startupId+".");p.executeUpdate();
            }
            log(c,s.userId(),"CREATE_FUNDING_REQUEST","STARTUP",startupId,amount.toPlainString()+" INR | request #"+requestId);
            return requestId;
        });
    }
    public List<String[]> fundingRequests(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","INVESTOR");
        List<String[]> live = switch(s.role()){
            case "FOUNDER" -> new FundingDao().findFundingRequestsForFounder(s.userId());
            case "INVESTOR" -> new FundingDao().findOpenFundingRequestsForInvestor();
            default -> new FundingDao().findFundingRequests();
        };
        live.addAll(new DatasetProjectionDao().fundingRequests());
        return live;
    }
    public void invest(Session s,long requestId,BigDecimal amount) throws Exception {
        require(s,"INVESTOR");
        if(amount==null||amount.signum()<=0)throw new ValidationException("Investment must be positive.");
        Db.transaction(c->{
            long investorId;
            try(PreparedStatement p=c.prepareStatement("SELECT id FROM investors WHERE user_id=?")){
                p.setLong(1,s.userId());try(ResultSet r=p.executeQuery()){
                    if(!r.next())throw new IllegalStateException("Investor profile is missing.");
                    investorId=r.getLong(1);
                }
            }
            BigDecimal requested;String requestStatus;
            try(PreparedStatement p=c.prepareStatement("SELECT amount,status FROM funding_requests WHERE id=? FOR UPDATE")){
                p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){
                    if(!r.next())throw new IllegalArgumentException("Funding request not found.");
                    requested=r.getBigDecimal("amount");requestStatus=r.getString("status");
                }
            }
            if(!Set.of("OPEN","IN_REVIEW").contains(requestStatus))
                throw new IllegalArgumentException("Funding request is closed.");
            BigDecimal committed;
            try(PreparedStatement p=c.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM investments WHERE funding_request_id=? AND status<>'CANCELLED'")){
                p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){r.next();committed=r.getBigDecimal(1);}
            }
            if(amount.compareTo(requested.subtract(committed))>0)
                throw new ValidationException("Commitment exceeds the remaining funding amount of "+requested.subtract(committed)+".");
            long investmentId;
            try(PreparedStatement p=c.prepareStatement("INSERT INTO investments(funding_request_id,investor_id,amount) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                p.setLong(1,requestId);p.setLong(2,investorId);p.setBigDecimal(3,amount);p.executeUpdate();
                try(ResultSet keys=p.getGeneratedKeys()){if(!keys.next())throw new SQLException("Investment ID was not returned.");investmentId=keys.getLong(1);}
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO investment_transactions(investment_id,amount,transaction_date,reference_code) VALUES(?,?,CURRENT_DATE,?)")){
                p.setLong(1,investmentId);p.setBigDecimal(2,amount);p.setString(3,"COMMIT-"+investmentId);p.executeUpdate();
            }
            String nextStatus=committed.add(amount).compareTo(requested)>=0?"FUNDED":"IN_REVIEW";
            try(PreparedStatement p=c.prepareStatement("UPDATE funding_requests SET status=? WHERE id=?")){
                p.setString(1,nextStatus);p.setLong(2,requestId);p.executeUpdate();
            }
            long founderUserId;
            try(PreparedStatement p=c.prepareStatement("SELECT st.founder_user_id FROM funding_requests fr JOIN startups st ON st.id=fr.startup_id WHERE fr.id=?")){
                p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){r.next();founderUserId=r.getLong(1);}
            }
            notify(c,founderUserId,"An investor recorded a commitment of "+amount.toPlainString()+" for funding request #"+requestId+".");
            log(c,s.userId(),"INVEST","FUNDING_REQUEST",requestId,amount.toPlainString());
            return null;
        });
    }
    public List<String[]> matches(Session s) throws Exception {
        require(s,"INVESTOR");List<String[]> out=new ArrayList<>();
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement(
            "SELECT s.id,s.name,s.sector,s.city,COUNT(DISTINCT ii.id) sector_match, " +
            "COUNT(DISTINCT isp.id) stage_match, " +
            "MAX(CASE WHEN fr.amount BETWEEN COALESCE(i.ticket_min,0) AND COALESCE(i.ticket_max,999999999999) THEN 1 ELSE 0 END) ticket_match, " +
            "(COUNT(DISTINCT ii.id)*40 + COUNT(DISTINCT isp.id)*35 + MAX(CASE WHEN fr.amount BETWEEN COALESCE(i.ticket_min,0) AND COALESCE(i.ticket_max,999999999999) THEN 25 ELSE 0 END)) match_score " +
            "FROM startups s LEFT JOIN investors i ON i.user_id=? " +
            "LEFT JOIN investor_interests ii ON ii.investor_id=i.id AND LOWER(ii.sector)=LOWER(s.sector) " +
            "LEFT JOIN investor_stage_preferences isp ON isp.investor_id=i.id AND LOWER(isp.preferred_stage)=LOWER(s.stage) " +
            "LEFT JOIN funding_requests fr ON fr.startup_id=s.id AND fr.status IN ('OPEN','IN_REVIEW') " +
            "WHERE s.status IN ('INCUBATED','FUNDRAISING') " +
            "GROUP BY s.id,s.name,s.sector,s.city ORDER BY match_score DESC,s.name")){
            p.setLong(1,s.userId());try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});}
        }return out;
    }
    public void addInterest(Session s,String sector) throws Exception {
        require(s,"INVESTOR");
        if(sector==null||sector.isBlank())throw new ValidationException("Enter a sector preference.");
        Db.transaction(c->{try(PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO investor_interests(investor_id,sector) SELECT id,? FROM investors WHERE user_id=?")){
            p.setString(1,sector.trim());p.setLong(2,s.userId());p.executeUpdate();
        }
        log(c,s.userId(),"ADD_INVESTOR_INTEREST","INVESTOR",s.userId(),sector.trim());return null;});
    }
    public void addStageInterest(Session s,String stage) throws Exception {
        require(s,"INVESTOR");
        if(stage==null||stage.isBlank())throw new ValidationException("Enter a preferred startup stage.");
        Db.transaction(c->{try(PreparedStatement p=c.prepareStatement(
                "INSERT IGNORE INTO investor_stage_preferences(investor_id,preferred_stage) SELECT id,? FROM investors WHERE user_id=?")){
            p.setString(1,stage.trim());p.setLong(2,s.userId());p.executeUpdate();
        }
        log(c,s.userId(),"ADD_INVESTOR_STAGE_PREFERENCE","INVESTOR",s.userId(),stage.trim());return null;});
    }

    public void uploadFundingDocument(Session s,long requestId,java.io.File source) throws Exception {
        require(s,"FOUNDER","ADMIN");
        if(source==null||!source.isFile()||!source.canRead())throw new ValidationException("Select a readable funding document.");
        try(Connection c=Db.get()) {
            if(s.role().equals("FOUNDER")) try(PreparedStatement q=c.prepareStatement(
                    "SELECT 1 FROM funding_requests fr JOIN startups st ON st.id=fr.startup_id WHERE fr.id=? AND st.founder_user_id=?")) {
                q.setLong(1,requestId);q.setLong(2,s.userId());try(ResultSet r=q.executeQuery()){if(!r.next())throw new AuthorizationException("You can only upload documents for your own funding requests.");}
            }
            java.nio.file.Path dir=java.nio.file.Path.of("uploads","funding-request-"+requestId);java.nio.file.Files.createDirectories(dir);
            String safe=java.util.UUID.randomUUID()+"_"+source.getName().replaceAll("[^A-Za-z0-9._-]","_");
            java.nio.file.Path target=dir.resolve(safe);java.nio.file.Files.copy(source.toPath(),target,java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            try(PreparedStatement p=c.prepareStatement("INSERT INTO funding_documents(funding_request_id,file_name,stored_path) VALUES(?,?,?)")) {
                p.setLong(1,requestId);p.setString(2,source.getName());p.setString(3,target.toString());p.executeUpdate();
            }
            log(c,s.userId(),"UPLOAD_FUNDING_DOCUMENT","FUNDING_REQUEST",requestId,source.getName());
        }
    }
    public void updateInvestorTicketRange(Session s,BigDecimal minimum,BigDecimal maximum) throws Exception {
        require(s,"INVESTOR");
        if(minimum==null||maximum==null||minimum.signum()<0||maximum.compareTo(minimum)<0)
            throw new ValidationException("Enter a valid ticket range where maximum is at least minimum.");
        Db.transaction(c->{
            try(PreparedStatement p=c.prepareStatement("UPDATE investors SET ticket_min=?,ticket_max=? WHERE user_id=?")){
                p.setBigDecimal(1,minimum);p.setBigDecimal(2,maximum);p.setLong(3,s.userId());
                if(p.executeUpdate()!=1)throw new IllegalStateException("Investor profile is missing.");
            }
            log(c,s.userId(),"UPDATE_INVESTOR_TICKET","INVESTOR",s.userId(),minimum+"–"+maximum+" INR");return null;
        });
    }
    public List<String[]> notifications(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        List<String[]> out=new ArrayList<>();
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("SELECT id,message,is_read,created_at FROM notifications WHERE user_id=? ORDER BY created_at DESC LIMIT 100")) {
            p.setLong(1,s.userId());try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)});}
        }return out;
    }
    public void markNotificationRead(Session s,long notificationId) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        try(Connection c=Db.get();PreparedStatement p=c.prepareStatement("UPDATE notifications SET is_read=TRUE WHERE id=? AND user_id=?")) {
            p.setLong(1,notificationId);p.setLong(2,s.userId());if(p.executeUpdate()!=1)throw new AuthorizationException("Notification not found for this account.");
        }
        try(Connection c=Db.get()){log(c,s.userId(),"READ_NOTIFICATION","NOTIFICATION",notificationId,"");}
    }

    public List<StartupSummary> topStartupsByReadiness(Session s,int limit) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        CrudRepository<StartupSummary,Long> summaryCache = new InMemoryRepository<>();
        Set<Long> visible=visibleStartupIds(s);
        for (StartupSummary summary : new StartupDao().findSummaries(null))
            if(visible.contains(summary.id())) summaryCache.save(summary);
        return startupAnalytics.topByReadiness(summaryCache.findAll(),limit);
    }
    public java.util.Map<String,Long> startupSectorSummary(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        Set<Long> visible=visibleStartupIds(s);
        return startupAnalytics.countBySector(new StartupDao().findSummaries(null).stream().filter(x->visible.contains(x.id())).toList());
    }
    public java.util.Optional<StartupSummary> startupSummary(Session s,long startupId) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        Set<Long> visible=visibleStartupIds(s);
        return startupAnalytics.findById(new StartupDao().findSummaries(null).stream().filter(x->visible.contains(x.id())).toList(),startupId);
    }
    public java.util.Map<String,Long> startupStatusSummary(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");
        Set<Long> visible=visibleStartupIds(s);
        List<String> states=new ArrayList<>();
        for(String[] row:new StartupDao().findStartup360ForRole(s.role(),s.userId())) states.add(row[4]);
        return CollectionExercises.countStatuses(states);
    }
    private Set<Long> visibleStartupIds(Session s) throws Exception {
        Set<Long> ids=new HashSet<>();
        for(String[] row:new StartupDao().findStartup360ForRole(s.role(),s.userId())) ids.add(Long.parseLong(row[0]));
        return ids;
    }

    public List<String[]> dashboard(Session s) throws Exception {
        require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR");

        String startupFilter;
        String applicationFilter;
        String fundingFilter;
        String investmentFilter;
        switch (s.role()) {
            case "ADMIN" -> {
                startupFilter = "1=1";
                applicationFilter = "1=1";
                fundingFilter = "1=1";
                investmentFilter = "1=1";
            }
            case "FOUNDER" -> {
                startupFilter = "st.founder_user_id=?";
                applicationFilter = "st.founder_user_id=?";
                fundingFilter = "st.founder_user_id=?";
                investmentFilter = "st.founder_user_id=?";
            }
            case "MENTOR" -> {
                startupFilter = "EXISTS (SELECT 1 FROM mentor_assignments ma JOIN mentors m ON m.id=ma.mentor_id WHERE ma.startup_id=st.id AND m.user_id=? AND m.approval_status='APPROVED')";
                applicationFilter = "EXISTS (SELECT 1 FROM mentor_assignments ma JOIN mentors m ON m.id=ma.mentor_id WHERE ma.startup_id=st.id AND m.user_id=? AND m.approval_status='APPROVED')";
                fundingFilter = "1=0";
                investmentFilter = "1=0";
            }
            case "EVALUATOR" -> {
                startupFilter = "EXISTS (SELECT 1 FROM applications a WHERE a.startup_id=st.id AND a.status IN ('SUBMITTED','UNDER_REVIEW'))";
                applicationFilter = "a.status IN ('SUBMITTED','UNDER_REVIEW')";
                fundingFilter = "1=0";
                investmentFilter = "1=0";
            }
            case "INVESTOR" -> {
                startupFilter = "st.status IN ('INCUBATED','FUNDRAISING')";
                applicationFilter = "a.status='ACCEPTED'";
                fundingFilter = "fr.status IN ('OPEN','IN_REVIEW')";
                investmentFilter = "inv.investor_id IN (SELECT id FROM investors WHERE user_id=?)";
            }
            case "INCUBATOR" -> {
                String ownProgramme = "EXISTS (SELECT 1 FROM applications a2 JOIN programs p2 ON p2.id=a2.program_id JOIN incubators i2 ON i2.id=p2.incubator_id WHERE a2.startup_id=st.id AND i2.user_id=?)";
                startupFilter = ownProgramme;
                applicationFilter = "EXISTS (SELECT 1 FROM programs p JOIN incubators i ON i.id=p.incubator_id WHERE p.id=a.program_id AND i.user_id=?)";
                fundingFilter = ownProgramme;
                investmentFilter = ownProgramme;
            }
            default -> throw new IllegalStateException("Unsupported dashboard role: " + s.role());
        }

        String pendingMentorMetric = s.role().equals("ADMIN")
                ? "UNION ALL SELECT 'Mentors awaiting approval',COUNT(*) FROM mentors WHERE approval_status='PENDING'"
                : "UNION ALL SELECT 'Mentors awaiting approval',0";
        String sql = "SELECT 'Startups',COUNT(*) FROM startups st WHERE " + startupFilter
                + " UNION ALL SELECT 'Applications',COUNT(*) FROM applications a JOIN startups st ON st.id=a.startup_id WHERE " + applicationFilter
                + " UNION ALL SELECT 'Funding requests',COUNT(*) FROM funding_requests fr JOIN startups st ON st.id=fr.startup_id WHERE " + fundingFilter
                + " UNION ALL SELECT 'Investments',COUNT(*) FROM investments inv JOIN funding_requests fr ON fr.id=inv.funding_request_id JOIN startups st ON st.id=fr.startup_id WHERE " + investmentFilter
                + " " + pendingMentorMetric;

        List<String[]> rows = new ArrayList<>();
        try (Connection c=Db.get(); PreparedStatement p=c.prepareStatement(sql)) {
            int parameter = 1;
            for (String filter : List.of(startupFilter, applicationFilter, fundingFilter, investmentFilter)) {
                int occurrences = (int) filter.chars().filter(ch -> ch == '?').count();
                for (int i = 0; i < occurrences; i++) p.setLong(parameter++, s.userId());
            }
            try (ResultSet r=p.executeQuery()) {
                while (r.next()) rows.add(new String[]{r.getString(1),r.getString(2)});
            }
        }

        List<String[]> dataset = new DatasetProjectionDao().reportTotals();
        if (!dataset.isEmpty()) {
            String[] d = dataset.get(0);
            rows.add(new String[]{"Funding records", d[0]});
            rows.add(new String[]{"Companies represented", d[1]});
            rows.add(new String[]{"Sectors represented", d[2]});
            rows.add(new String[]{"Countries represented", d[3]});
        }
        return rows;
    }
    public List<String[]> pendingMentors(Session s) throws Exception {
        require(s,"ADMIN");
        return new IncubationDao().findPendingMentors();
    }
    public List<String[]> mentorDirectory(Session s) throws Exception {
        require(s,"ADMIN","INCUBATOR");
        return new IncubationDao().findAllMentors();
    }
    /** Public, read-only market intelligence used by every authenticated workspace. */
    public List<String[]> historicalKpis(Session s) throws Exception { require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR"); return new HistoricalAnalyticsDao().kpis(); }
    public List<String[]> historicalSectors(Session s,int limit) throws Exception { require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR"); return new HistoricalAnalyticsDao().sectors(limit); }
    public List<String[]> historicalStages(Session s,int limit) throws Exception { require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR"); return new HistoricalAnalyticsDao().stages(limit); }
    public List<String[]> historicalYears(Session s) throws Exception { require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR"); return new HistoricalAnalyticsDao().yearly(); }
    public List<String[]> historicalTopCompanies(Session s,int limit) throws Exception { require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR"); return new HistoricalAnalyticsDao().topCompanies(limit); }
    public List<String[]> historicalLatest(Session s,int limit) throws Exception { require(s,"ADMIN","FOUNDER","MENTOR","EVALUATOR","INVESTOR","INCUBATOR"); return new HistoricalAnalyticsDao().latest(limit); }

    private static void require(Session s,String... roles){if(s==null||Arrays.stream(roles).noneMatch(s.role()::equals))throw new AuthorizationException("Your account role is not permitted to perform this action.");}
    private static void notify(Connection c,long userId,String message)throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO notifications(user_id,message) VALUES(?,?)")) {
            p.setLong(1,userId);p.setString(2,message);p.executeUpdate();
        }
    }
    private static void log(Connection c,long actor,String action,String type,long id,String details)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("INSERT INTO activity_logs(actor_user_id,action,entity_type,entity_id,details) VALUES(?,?,?,?,?)")){
            p.setLong(1,actor);p.setString(2,action);p.setString(3,type);p.setLong(4,id);p.setString(5,details);p.executeUpdate();}
    }
}
