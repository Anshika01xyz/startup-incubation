import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.time.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Foundry standalone backend demonstration.
 * Run with: java BackendOnly.java
 * Demo run:  java BackendOnly.java --demo
 *
 * This one-file runner uses Java serialization for portable terminal testing.
 * The full JavaFX project uses MySQL/JDBC as its production/coursework data layer.
 */
public class BackendOnly {
    private static final Path STORE = Path.of("backend-data", "foundry-store.bin");
    private static final Scanner IN = new Scanner(System.in);
    private static State state;
    private static final Path HISTORICAL_CSV = Path.of("backend-data", "india_startup_funding_sample.csv");
    private static List<String[]> historicalRows = new ArrayList<>();
    private static String historicalStatus = "Historical dataset has not been checked yet.";

    enum Role { ADMIN, FOUNDER, MENTOR, EVALUATOR, INVESTOR }

    record Session(long userId, String name, String email, Role role) implements Serializable {}

    static final class User implements Serializable {
        long id; String name, email, passwordHash; Role role; boolean active;
        User(long id,String name,String email,String passwordHash,Role role,boolean active){
            this.id=id;this.name=name;this.email=email;this.passwordHash=passwordHash;this.role=role;this.active=active;
        }
    }
    static final class Startup implements Serializable {
        long id, founderId; String name,sector,city,stage,status,description;
        int foundedYear; double progress;
        Startup(long id,long founderId,String name,String sector,String city,String stage,int year,String description){
            this.id=id;this.founderId=founderId;this.name=name;this.sector=sector;this.city=city;
            this.stage=stage;this.foundedYear=year;this.description=description;this.status="DRAFT";
        }
    }
    static final class ApplicationRecord implements Serializable {
        long id,startupId,programId,evaluatorId; String status,comments,recommendation;
        double innovation,market,feasibility,team,readiness;
        ApplicationRecord(long id,long startupId,long programId){this.id=id;this.startupId=startupId;this.programId=programId;this.status="SUBMITTED";}
    }
    static final class MentorProfile implements Serializable {
        long userId; String expertise; int years; String approval;
        MentorProfile(long id,String expertise,int years){userId=id;this.expertise=expertise;this.years=years;approval="PENDING";}
    }
    static final class Milestone implements Serializable {
        long id,startupId; String title,status,note; LocalDate due;
        Milestone(long id,long startupId,String title,LocalDate due){this.id=id;this.startupId=startupId;this.title=title;this.due=due;status="NOT_STARTED";note="";}
        String health(){if(status.equals("COMPLETED"))return "COMPLETED";if(due.isBefore(LocalDate.now()))return "OVERDUE";if(!due.isAfter(LocalDate.now().plusDays(7)))return "DUE_SOON";return "ON_TRACK";}
    }
    static final class FundingRequest implements Serializable {
        long id,startupId; double amount; String purpose,status;
        FundingRequest(long id,long startupId,double amount,String purpose){this.id=id;this.startupId=startupId;this.amount=amount;this.purpose=purpose;status="OPEN";}
    }
    static final class Investment implements Serializable {
        long id,requestId,investorId; double amount; String status;
        Investment(long id,long requestId,long investorId,double amount){this.id=id;this.requestId=requestId;this.investorId=investorId;this.amount=amount;status="COMMITTED";}
    }
    static final class Activity implements Serializable {
        LocalDateTime at; long actor; String action,entity,detail;
        Activity(long actor,String action,String entity,String detail){this.at=LocalDateTime.now();this.actor=actor;this.action=action;this.entity=entity;this.detail=detail;}
        public String toString(){return at+" | user#"+actor+" | "+action+" | "+entity+" | "+detail;}
    }
    static final class InvestorPreference implements Serializable {
        long investorId; Set<String> sectors=new HashSet<>(),stages=new HashSet<>(); double min=0,max=1_000_000_000;
    }
    static final class State implements Serializable {
        long nextUser=1,nextStartup=1,nextApplication=1,nextMilestone=1,nextRequest=1,nextInvestment=1;
        Map<Long,User> users=new LinkedHashMap<>();
        Map<Long,Startup> startups=new LinkedHashMap<>();
        Map<Long,ApplicationRecord> applications=new LinkedHashMap<>();
        Map<Long,MentorProfile> mentors=new LinkedHashMap<>();
        Map<Long,Milestone> milestones=new LinkedHashMap<>();
        Map<Long,FundingRequest> requests=new LinkedHashMap<>();
        Map<Long,Investment> investments=new LinkedHashMap<>();
        Map<Long,InvestorPreference> preferences=new LinkedHashMap<>();
        List<Activity> activity=new LinkedList<>();
        Set<String> documents=new HashSet<>();
    }

    public static void main(String[] args) {
        load();
        if(args.length>0 && args[0].equalsIgnoreCase("--demo")) { runDemo(); return; }
        System.out.println("\nFOUNDRY | STARTUP INCUBATION & INVESTMENT BACKEND");
        System.out.println("Local standalone Java backend • persisted to "+STORE);
        System.out.println("Demo accounts: admin@foundry.demo / Admin@12345");
        System.out.println("               founder@foundry.demo / Founder@12345");
        System.out.println("               evaluator@foundry.demo / Evaluator@12345");
        System.out.println("               mentor@foundry.demo / Mentor@12345");
        System.out.println("               investor@foundry.demo / Investor@12345");
        System.out.println("Historical startup dataset: automatically checked after each successful sign-in.");
        Session session=null;
        while(true){
            try{
                if(session==null){
                    System.out.println("\n1. Sign in   2. Self-register   0. Exit");
                    int choice=number("Choice: ");
                    if(choice==0){save();System.out.println("Saved. Goodbye.");return;}
                    if(choice==1){session=login();if(session!=null){try{ensureHistoricalDataset();System.out.println(historicalStatus);}catch(Exception ex){showError(ex);}}}
                    else if(choice==2)register();
                    else System.out.println("Choose 0, 1 or 2.");
                }else{
                    menu(session);
                    int choice=number("Action: ");
                    if(choice==0){session=null;continue;}
                    execute(session,choice);
                }
            }catch(Exception ex){showError(ex);}
        }
    }

    private static void load(){
        try{
            if(Files.exists(STORE)){
                try(ObjectInputStream in=new ObjectInputStream(Files.newInputStream(STORE))){state=(State)in.readObject();}
            }else{
                state=new State();seed();save();
            }
        }catch(Exception ex){System.out.println("Store could not be read: "+ex.getClass().getSimpleName()+" — "+ex.getMessage());state=new State();seed();save();}
    }
    private static void save(){
        try{
            Files.createDirectories(STORE.getParent());
            try(ObjectOutputStream out=new ObjectOutputStream(Files.newOutputStream(STORE))){out.writeObject(state);}
        }catch(IOException ex){throw new IllegalStateException("PersistenceException: "+ex.getMessage(),ex);}
    }
    private static void seed(){
        long admin=addUser("Foundry Administrator","admin@foundry.demo","Admin@12345",Role.ADMIN,true);
        long founder=addUser("Demo Founder","founder@foundry.demo","Founder@12345",Role.FOUNDER,true);
        long evaluator=addUser("Demo Evaluator","evaluator@foundry.demo","Evaluator@12345",Role.EVALUATOR,true);
        long mentor=addUser("Demo Mentor","mentor@foundry.demo","Mentor@12345",Role.MENTOR,true);
        long investor=addUser("Demo Investor","investor@foundry.demo","Investor@12345",Role.INVESTOR,true);
        state.mentors.put(mentor,new MentorProfile(mentor,"Product strategy",8));
        state.mentors.get(mentor).approval="APPROVED";
        InvestorPreference pref=new InvestorPreference();pref.investorId=investor;pref.sectors.add("ClimateTech");pref.stages.add("Seed");pref.min=10000;pref.max=250000;state.preferences.put(investor,pref);
        long startup=state.nextStartup++;
        Startup st=new Startup(startup,founder,"GreenRoute Mobility","ClimateTech","Pune","Seed",2022,"Electric fleet optimization for local logistics.");
        st.status="INCUBATED";st.progress=45;state.startups.put(startup,st);
        long app=state.nextApplication++;ApplicationRecord a=new ApplicationRecord(app,startup,1);a.status="ACCEPTED";a.evaluatorId=evaluator;a.innovation=8;a.market=7.5;a.feasibility=8;a.team=8.5;a.readiness=80;a.recommendation="ACCEPT";state.applications.put(app,a);
        long ms=state.nextMilestone++;Milestone m=new Milestone(ms,startup,"Pilot deployment",LocalDate.now().plusDays(12));m.status="IN_PROGRESS";state.milestones.put(ms,m);
        long req=state.nextRequest++;state.requests.put(req,new FundingRequest(req,startup,180000,"Expand the pilot fleet"));
        log(admin,"DEMO_DATA_CREATED","PLATFORM","Starter operational records loaded");
        save();
    }

    private static long addUser(String name,String email,String password,Role role,boolean active){
        if(findEmail(email)!=null)throw new IllegalArgumentException("DuplicateUserException: email already registered.");
        long id=state.nextUser++;state.users.put(id,new User(id,name,email,hash(password),role,active));
        if(role==Role.INVESTOR)state.preferences.put(id,new InvestorPreference());
        return id;
    }
    private static User findEmail(String email){return state.users.values().stream().filter(u->u.email.equalsIgnoreCase(email)).findFirst().orElse(null);}
    private static Session login(){
        String email=ask("Email: "),password=ask("Password: ");
        User u=findEmail(email);
        if(u==null||!u.active||!verify(password,u.passwordHash)){System.out.println("AuthenticationException: invalid credentials or inactive account.");return null;}
        System.out.println("Signed in as "+u.name+" · "+u.role);
        return new Session(u.id,u.name,u.email,u.role);
    }
    private static void register(){
        String name=ask("Full name: "),email=ask("Email: "),password=ask("Password (10+ characters): ");
        System.out.println("1 FOUNDER  2 MENTOR  3 INVESTOR");
        int n=number("Role: ");Role role=n==1?Role.FOUNDER:n==2?Role.MENTOR:n==3?Role.INVESTOR:null;
        if(role==null)throw new IllegalArgumentException("ValidationException: choose a permitted self-registration role.");
        if(password.length()<10)throw new IllegalArgumentException("ValidationException: password must contain at least 10 characters.");
        long id=addUser(name,email,password,role,role!=Role.MENTOR);
        if(role==Role.MENTOR)state.mentors.put(id,new MentorProfile(id,ask("Expertise: "),number("Years of experience: ")));
        log(id,"SELF_REGISTER",role.name(),"Account created"+(role==Role.MENTOR?" · pending admin approval":""));
        save();System.out.println("Registration saved. Mentor accounts require administrator approval.");
    }
    private static String hash(String password){
        try{byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);return "pbkdf2$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(derive(password,salt));}
        catch(Exception ex){throw new IllegalStateException("PasswordHashException",ex);}
    }
    private static boolean verify(String password,String encoded){
        try{String[] p=encoded.split("\\$");return p.length==3&&MessageDigest.isEqual(Base64.getDecoder().decode(p[2]),derive(password,Base64.getDecoder().decode(p[1])));}
        catch(Exception ex){return false;}
    }
    private static byte[] derive(String password,byte[] salt)throws Exception{
        PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,150000,256);
        try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}
        finally{spec.clearPassword();}
    }

    private static void menu(Session s){
        System.out.println("\n"+s.name()+" · "+s.role()+"  |  0 Sign out");
        switch(s.role()){
            case FOUNDER -> System.out.println("1 Create startup  2 My startups  3 Submit application  4 Add milestone  5 Funding request  6 Startup 360  7 Activity  8 Attach document  9 Historical startup dataset");
            case ADMIN -> System.out.println("1 All startups  2 Approve mentor  3 Assign mentor  4 Add milestone  5 Startup 360  6 Activity  7 Dashboard  9 Historical startup dataset");
            case EVALUATOR -> System.out.println("1 Applications  2 Evaluate application  3 Startup 360  4 Activity  9 Historical startup dataset");
            case MENTOR -> System.out.println("1 Assigned milestones  2 Update milestone  3 Startup 360  4 Activity  9 Historical startup dataset");
            case INVESTOR -> System.out.println("1 Investor matches  2 Commit investment  3 Startup 360  4 Activity  5 Set preferences  9 Historical startup dataset");
        }
    }
    private static void execute(Session s,int choice)throws Exception{
        if(choice==9){showHistoricalDataset(s);return;}
        switch(s.role()){
            case FOUNDER -> { switch(choice){
                case 1 -> createStartup(s);case 2 -> listStartups(s);case 3 -> submit(s);case 4 -> addMilestone(s);
                case 5 -> funding(s);case 6 -> startup360(s);case 7 -> activity(s);case 8 -> attachDocument(s);
                default -> throw new IllegalArgumentException("ValidationException: option is not available in the founder workspace.");
            } }
            case ADMIN -> { switch(choice){
                case 1 -> listStartups(s);case 2 -> approveMentor(s);case 3 -> assignMentor(s);case 4 -> addMilestone(s);
                case 5 -> startup360(s);case 6 -> activity(s);case 7 -> dashboard(s);
                default -> throw new IllegalArgumentException("ValidationException: option is not available in the administrator workspace.");
            } }
            case EVALUATOR -> { switch(choice){
                case 1 -> listApplications(s);case 2 -> evaluate(s);case 3 -> startup360(s);case 4 -> activity(s);
                default -> throw new IllegalArgumentException("ValidationException: option is not available in the evaluator workspace.");
            } }
            case MENTOR -> { switch(choice){
                case 1 -> listMilestones(s);case 2 -> updateMilestone(s);case 3 -> startup360(s);case 4 -> activity(s);
                default -> throw new IllegalArgumentException("ValidationException: option is not available in the mentor workspace.");
            } }
            case INVESTOR -> { switch(choice){
                case 1 -> matches(s);case 2 -> invest(s);case 3 -> startup360(s);case 4 -> activity(s);case 5 -> preferences(s);
                default -> throw new IllegalArgumentException("ValidationException: option is not available in the investor workspace.");
            } }
        }
    }

    private static void ensureHistoricalDataset() throws Exception {
        Files.createDirectories(HISTORICAL_CSV.getParent());
        if(!Files.isRegularFile(HISTORICAL_CSV)||Files.size(HISTORICAL_CSV)<1024){
            URI source=URI.create("https://fliarbi.com/datasets/files/india-startup-funding/india_startup_funding_sample.csv");
            HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest request=HttpRequest.newBuilder(source).timeout(Duration.ofSeconds(90)).header("User-Agent","Foundry-Standalone-Backend/1.0").GET().build();
            HttpResponse<Path> response=client.send(request,HttpResponse.BodyHandlers.ofFile(HISTORICAL_CSV));
            if(response.statusCode()!=200||Files.size(HISTORICAL_CSV)<1024){Files.deleteIfExists(HISTORICAL_CSV);throw new IOException("DatasetDownloadException: publisher returned HTTP "+response.statusCode()+".");}
        }
        List<String[]> parsed;
        try(Reader reader=Files.newBufferedReader(HISTORICAL_CSV)){parsed=parseCsv(reader);}
        if(parsed.size()<2)throw new IOException("DatasetFormatException: CSV has no data rows.");
        int count=parsed.size()-1;
        if(count!=5006)throw new IOException("DatasetCompletenessException: expected 5,006 historical rows but found "+count+". No rows have been fabricated.");
        historicalRows=parsed;
        historicalStatus="Historical dataset ready: "+count+" / 5,006 publisher rows loaded from "+HISTORICAL_CSV+".";
    }

    private static List<String[]> parseCsv(Reader source)throws IOException{
        List<String[]> rows=new ArrayList<>();try(PushbackReader reader=new PushbackReader(source,1)){
            List<String> fields=new ArrayList<>();StringBuilder field=new StringBuilder();boolean quoted=false;int ch;
            while((ch=reader.read())!=-1){char c=(char)ch;if(quoted){if(c=='"'){int next=reader.read();if(next=='"')field.append('"');else{quoted=false;if(next!=-1)reader.unread(next);}}else field.append(c);}else if(c=='"'&&field.isEmpty())quoted=true;else if(c==','){fields.add(field.toString());field.setLength(0);}else if(c=='\n'||c=='\r'){if(c=='\r'){int next=reader.read();if(next!='\n'&&next!=-1)reader.unread(next);}fields.add(field.toString());field.setLength(0);rows.add(fields.toArray(String[]::new));fields.clear();}else field.append(c);}
            if(quoted)throw new IOException("DatasetFormatException: unclosed quoted CSV field.");if(!fields.isEmpty()||!field.isEmpty()){fields.add(field.toString());rows.add(fields.toArray(String[]::new));}
        }if(!rows.isEmpty()&&rows.get(0).length>0)rows.get(0)[0]=rows.get(0)[0].replace("\uFEFF","").trim();return rows;
    }

    private static void showHistoricalDataset(Session session)throws Exception{
        if(historicalRows.size()!=5007)ensureHistoricalDataset();
        String[] header=historicalRows.get(0);Map<String,Integer> columns=new HashMap<>();for(int i=0;i<header.length;i++)columns.put(header[i].toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]",""),i);
        int name=column(columns,"companyname","startupname","startup","name"),country=column(columns,"country"),city=column(columns,"city","location"),sector=column(columns,"sector","industry"),stage=column(columns,"stage","fundingstage"),date=column(columns,"announceddate","date","year"),amount=column(columns,"amountusd","amount","fundingamount"),investor=column(columns,"leadinvestor");
        String query=ask("Optional search (startup / city / country / sector / stage, blank for all): ").toLowerCase(Locale.ROOT);List<String[]> matches=new ArrayList<>();
        for(int i=1;i<historicalRows.size();i++){String[] row=historicalRows.get(i);String joined=String.join(" ",row).toLowerCase(Locale.ROOT);if(query.isBlank()||joined.contains(query))matches.add(row);}
        int page=0;while(true){int from=page*100,to=Math.min(matches.size(),from+100);if(from>=matches.size()){System.out.println("No records on this page.");return;}System.out.println("\nHISTORICAL STARTUP DATASET · "+session.role()+" VIEW · rows "+(from+1)+"–"+to+" of "+matches.size()+" filtered / 5,006 total");
            for(int i=from;i<to;i++){String[] row=matches.get(i);String line=value(row,name)+" | "+value(row,country)+" | "+value(row,city)+" | "+value(row,sector)+" | "+value(row,date)+" | "+value(row,stage);
                if(session.role()==Role.ADMIN||session.role()==Role.INVESTOR||session.role()==Role.FOUNDER)line+=" | USD "+value(row,amount);
                if(session.role()==Role.ADMIN||session.role()==Role.INVESTOR)line+=" | Lead: "+value(row,investor);System.out.println((i+1)+". "+line);}
            if(to>=matches.size())return;String action=ask("Enter N for next 100, P for previous 100, or Q to return: ");if(action.equalsIgnoreCase("N"))page++;else if(action.equalsIgnoreCase("P"))page=Math.max(0,page-1);else return;
        }
    }
    private static int column(Map<String,Integer> map,String...keys){for(String key:keys){Integer index=map.get(key);if(index!=null)return index;}return -1;}
    private static String value(String[] row,int index){return index<0||index>=row.length||row[index].isBlank()?"—":row[index].trim();}

    private static void createStartup(Session s){
        require(s,Role.FOUNDER);
        String name=ask("Startup name: "),sector=ask("Sector: "),city=ask("City: "),stage=ask("Stage (Idea/Pre-seed/Seed/Series A): ");
        int year=number("Founded year: ");String desc=ask("Short description: ");
        if(name.isBlank()||sector.isBlank()||stage.isBlank()||year<1900||year>LocalDate.now().getYear())throw new IllegalArgumentException("ValidationException: enter valid startup details.");
        long id=state.nextStartup++;state.startups.put(id,new Startup(id,s.userId(),name,sector,city,stage,year,desc));
        log(s.userId(),"CREATE_STARTUP","STARTUP#"+id,name);save();System.out.println("Created startup #"+id);
    }
    private static List<Startup> visible(Session s){
        List<Startup> list=new ArrayList<>();
        for(Startup st:state.startups.values()){
            boolean allowed=s.role()==Role.ADMIN
                ||(s.role()==Role.FOUNDER&&st.founderId==s.userId())
                ||(s.role()==Role.INVESTOR&&Set.of("INCUBATED","FUNDRAISING").contains(st.status))
                ||(s.role()==Role.MENTOR&&state.mentors.get(s.userId())!=null&&state.mentors.get(s.userId()).approval.equals("APPROVED")&&assigned(s.userId(),st.id))
                ||(s.role()==Role.EVALUATOR&&state.applications.values().stream().anyMatch(a->a.startupId==st.id&&Set.of("SUBMITTED","UNDER_REVIEW").contains(a.status)));
            if(allowed)list.add(st);
        }return list;
    }
    private static void listStartups(Session s){for(Startup st:visible(s))System.out.println("#"+st.id+" | "+st.name+" | "+st.sector+" | "+st.stage+" | "+st.status);}
    private static void submit(Session s){
        require(s,Role.FOUNDER);long sid=number("Your startup ID: ");Startup st=owned(s,sid);
        if(state.applications.values().stream().anyMatch(a->a.startupId==sid&&a.programId==1))throw new IllegalArgumentException("DuplicateApplicationException: this startup already applied to the demo programme.");
        long id=state.nextApplication++;state.applications.put(id,new ApplicationRecord(id,sid,1));st.status="SUBMITTED";
        log(s.userId(),"SUBMIT_APPLICATION","APPLICATION#"+id,"Startup "+sid);save();System.out.println("Application #"+id+" submitted.");
    }
    private static void listApplications(Session s){
        require(s,Role.EVALUATOR,Role.ADMIN);
        for(ApplicationRecord a:state.applications.values())if(Set.of("SUBMITTED","UNDER_REVIEW").contains(a.status)){
            Startup st=state.startups.get(a.startupId);System.out.println("#"+a.id+" | "+st.name+" | "+a.status);
        }
    }
    private static void evaluate(Session s){
        require(s,Role.EVALUATOR,Role.ADMIN);listApplications(s);
        long id=number("Application ID: ");ApplicationRecord a=state.applications.get(id);
        if(a==null||!Set.of("SUBMITTED","UNDER_REVIEW").contains(a.status))throw new IllegalArgumentException("WorkflowException: application is missing or already decided.");
        a.innovation=score("Innovation");a.market=score("Market");a.feasibility=score("Feasibility");a.team=score("Team");
        a.readiness=Math.round(((a.innovation+a.market+a.feasibility+a.team)/4.0)*10*10)/10.0;
        a.comments=ask("Feedback: ");a.recommendation=ask("Decision (ACCEPT/REJECT/REVIEW): ").toUpperCase(Locale.ROOT);
        if(!Set.of("ACCEPT","REJECT","REVIEW").contains(a.recommendation))throw new IllegalArgumentException("ValidationException: decision must be ACCEPT, REJECT or REVIEW.");
        a.evaluatorId=s.userId();a.status=a.recommendation.equals("ACCEPT")?"ACCEPTED":a.recommendation.equals("REJECT")?"REJECTED":"UNDER_REVIEW";
        Startup st=state.startups.get(a.startupId);st.status=a.recommendation.equals("ACCEPT")?"INCUBATED":a.recommendation.equals("REJECT")?"DRAFT":"SUBMITTED";
        log(s.userId(),"EVALUATE_APPLICATION","APPLICATION#"+id,"Readiness "+a.readiness+"/100 · "+a.recommendation);save();
        System.out.println("Saved scorecard: "+a.readiness+"/100 (equal weight: innovation, market, feasibility, team).");
    }
    private static double score(String label){double x=decimal(label+" score (0–10): ");if(x<0||x>10)throw new IllegalArgumentException("ValidationException: "+label+" score must be between 0 and 10.");return x;}
    private static void approveMentor(Session s){
        require(s,Role.ADMIN);state.mentors.forEach((id,m)->{if(m.approval.equals("PENDING"))System.out.println("Mentor #"+id+" | "+state.users.get(id).name+" | "+m.expertise);});
        long id=number("Mentor user ID: ");MentorProfile m=state.mentors.get(id);if(m==null)throw new IllegalArgumentException("NotFoundException: mentor does not exist.");
        m.approval=ask("Approve? (y/n): ").equalsIgnoreCase("y")?"APPROVED":"REJECTED";state.users.get(id).active=m.approval.equals("APPROVED");
        log(s.userId(),"MENTOR_"+m.approval,"MENTOR#"+id,"Approval decision");save();
    }
    private static void assignMentor(Session s){
        require(s,Role.ADMIN);long mentor=number("Approved mentor user ID: "),startup=number("Startup ID: ");
        MentorProfile m=state.mentors.get(mentor);if(m==null||!m.approval.equals("APPROVED"))throw new IllegalArgumentException("WorkflowException: only approved mentors can be assigned.");
        ownedAny(startup);state.documents.add("ASSIGN:"+mentor+":"+startup);log(s.userId(),"ASSIGN_MENTOR","STARTUP#"+startup,"Mentor user #"+mentor);save();
    }
    private static boolean assigned(long mentor,long startup){return state.documents.contains("ASSIGN:"+mentor+":"+startup);}
    private static void addMilestone(Session s){
        require(s,Role.FOUNDER,Role.ADMIN);long sid=number("Startup ID: ");
        if(s.role()==Role.FOUNDER)owned(s,sid);else ownedAny(sid);
        String title=ask("Milestone title: ");LocalDate due=LocalDate.parse(ask("Due date YYYY-MM-DD: "));
        if(title.isBlank())throw new IllegalArgumentException("ValidationException: milestone title is required.");
        long id=state.nextMilestone++;state.milestones.put(id,new Milestone(id,sid,title,due));log(s.userId(),"CREATE_MILESTONE","MILESTONE#"+id,title);save();
    }
    private static void listMilestones(Session s){
        require(s,Role.MENTOR);
        for(Milestone m:state.milestones.values())if(assigned(s.userId(),m.startupId))System.out.println("#"+m.id+" | "+m.title+" | "+m.due+" | "+m.status+" | "+m.health());
    }
    private static void updateMilestone(Session s){
        require(s,Role.MENTOR,Role.ADMIN);listMilestonesIfMentor(s);
        long id=number("Milestone ID: ");Milestone m=state.milestones.get(id);if(m==null)throw new IllegalArgumentException("NotFoundException: milestone does not exist.");
        if(s.role()==Role.MENTOR&&!assigned(s.userId(),m.startupId))throw new SecurityException("AuthorizationException: mentor is not assigned to this startup.");
        m.status=ask("Status (NOT_STARTED/IN_PROGRESS/COMPLETED/BLOCKED): ").toUpperCase(Locale.ROOT);
        if(!Set.of("NOT_STARTED","IN_PROGRESS","COMPLETED","BLOCKED").contains(m.status))throw new IllegalArgumentException("ValidationException: invalid milestone status.");
        m.note=ask("Progress note: ");log(s.userId(),"UPDATE_MILESTONE","MILESTONE#"+id,m.status+" · "+m.note);save();
    }
    private static void listMilestonesIfMentor(Session s){if(s.role()==Role.MENTOR)listMilestones(s);}
    private static void funding(Session s){
        require(s,Role.FOUNDER);long sid=number("Startup ID: ");owned(s,sid);double amount=decimal("Funding amount (INR): ");
        if(amount<=0)throw new IllegalArgumentException("ValidationException: funding amount must be positive.");
        String purpose=ask("Purpose: ");long id=state.nextRequest++;state.requests.put(id,new FundingRequest(id,sid,amount,purpose));
        state.startups.get(sid).status="FUNDRAISING";log(s.userId(),"CREATE_FUNDING_REQUEST","FUNDING#"+id,purpose);save();System.out.println("Funding request #"+id+" created.");
    }
    private static void preferences(Session s){
        require(s,Role.INVESTOR);InvestorPreference p=state.preferences.computeIfAbsent(s.userId(),k->new InvestorPreference());
        p.sectors.add(ask("Preferred sector: "));p.stages.add(ask("Preferred stage: "));p.min=decimal("Minimum ticket INR: ");p.max=decimal("Maximum ticket INR: ");
        if(p.min<0||p.max<p.min)throw new IllegalArgumentException("ValidationException: invalid investment ticket range.");
        save();System.out.println("Investor preferences saved.");
    }
    private static void matches(Session s){
        require(s,Role.INVESTOR);InvestorPreference p=state.preferences.computeIfAbsent(s.userId(),k->new InvestorPreference());
        for(Startup st:visible(s)){
            int sector=p.sectors.stream().anyMatch(x->x.equalsIgnoreCase(st.sector))?1:0;
            int stage=p.stages.stream().anyMatch(x->x.equalsIgnoreCase(st.stage))?1:0;
            double best=state.requests.values().stream().filter(r->r.startupId==st.id&&r.status.equals("OPEN")).mapToDouble(r->r.amount).max().orElse(0);
            int range=best>=p.min&&best<=p.max&&best>0?1:0;
            int score=sector*40+stage*35+range*25;
            System.out.println(st.name+" | match "+score+"/100 | sector "+(sector==1?"✓":"—")+" | stage "+(stage==1?"✓":"—")+" | ticket "+(range==1?"✓":"—"));
        }
    }
    private static void invest(Session s){
        require(s,Role.INVESTOR);matches(s);long id=number("Funding request ID: ");FundingRequest r=state.requests.get(id);
        if(r==null||!r.status.equals("OPEN"))throw new IllegalArgumentException("WorkflowException: funding request is not open.");
        Startup st=state.startups.get(r.startupId);if(!Set.of("INCUBATED","FUNDRAISING").contains(st.status))throw new SecurityException("AuthorizationException: startup is not approved for investor discovery.");
        double committed=state.investments.values().stream().filter(i->i.requestId==id&&i.status.equals("COMMITTED")).mapToDouble(i->i.amount).sum();
        double amount=decimal("Commitment amount INR (remaining "+(r.amount-committed)+"): ");
        if(amount<=0||amount>r.amount-committed)throw new IllegalArgumentException("ValidationException: commitment exceeds the remaining funding request.");
        long iid=state.nextInvestment++;state.investments.put(iid,new Investment(iid,id,s.userId(),amount));log(s.userId(),"COMMIT_INVESTMENT","INVESTMENT#"+iid,"INR "+amount);save();
    }
    private static void startup360(Session s){
        System.out.println("\nSTARTUP 360° | Connected lifecycle records");
        for(Startup st:visible(s)){
            long apps=state.applications.values().stream().filter(a->a.startupId==st.id).count();
            long mentors=state.documents.stream().filter(x->x.startsWith("ASSIGN:")&&x.endsWith(":"+st.id)).count();
            long milestones=state.milestones.values().stream().filter(m->m.startupId==st.id).count();
            long done=state.milestones.values().stream().filter(m->m.startupId==st.id&&m.status.equals("COMPLETED")).count();
            double requested=state.requests.values().stream().filter(r->r.startupId==st.id).mapToDouble(r->r.amount).sum();
            double invested=state.investments.values().stream().filter(i->state.requests.get(i.requestId).startupId==st.id&&i.status.equals("COMMITTED")).mapToDouble(i->i.amount).sum();
            double readiness=state.applications.values().stream().filter(a->a.startupId==st.id).mapToDouble(a->a.readiness).max().orElse(0);
            System.out.printf(Locale.US,"#%d %s | %s | %s | %s | applications %d | mentors %d | milestones %d/%d | readiness %.1f | requested INR %.2f | committed INR %.2f | progress %.0f%%%n",
                st.id,st.name,st.sector,st.stage,st.status,apps,mentors,done,milestones,readiness,requested,invested,st.progress);
            for(Milestone m:state.milestones.values())if(m.startupId==st.id)System.out.println("   ↳ "+m.title+" · "+m.status+" · "+m.health()+" · due "+m.due);
        }
    }
    private static void dashboard(Session s){
        require(s,Role.ADMIN);
        System.out.println("Startups: "+state.startups.size()+" | Applications: "+state.applications.size()+" | Mentors pending: "+state.mentors.values().stream().filter(m->m.approval.equals("PENDING")).count()+" | Funding requests: "+state.requests.size()+" | Investments: "+state.investments.size());
    }
    private static void activity(Session s){
        for(Activity a:state.activity)System.out.println(a);
    }
    private static void attachDocument(Session s)throws IOException{
        require(s,Role.FOUNDER);long id=number("Startup ID: ");owned(s,id);
        Path source=Path.of(ask("File path: ")).toAbsolutePath().normalize();
        if(!Files.isRegularFile(source)||!Files.isReadable(source))throw new IOException("FileAccessException: selected document is missing or unreadable.");
        Path dir=Path.of("backend-data","documents","startup-"+id);Files.createDirectories(dir);
        String filename=UUID.randomUUID()+"_"+source.getFileName().toString().replaceAll("[^A-Za-z0-9._-]","_");
        Files.copy(source,dir.resolve(filename),StandardCopyOption.REPLACE_EXISTING);
        state.documents.add("DOC:"+id+":"+filename);log(s.userId(),"UPLOAD_DOCUMENT","STARTUP#"+id,filename);save();
        System.out.println("Document stored: "+dir.resolve(filename));
    }
    private static Startup owned(Session s,long id){
        Startup st=state.startups.get(id);if(st==null)throw new IllegalArgumentException("NotFoundException: startup does not exist.");
        if(s.role()==Role.FOUNDER&&st.founderId!=s.userId())throw new SecurityException("AuthorizationException: you do not own this startup.");
        return st;
    }
    private static Startup ownedAny(long id){Startup st=state.startups.get(id);if(st==null)throw new IllegalArgumentException("NotFoundException: startup does not exist.");return st;}
    private static void require(Session s,Role... roles){if(Arrays.stream(roles).noneMatch(r->r==s.role()))throw new SecurityException("AuthorizationException: "+s.role()+" cannot perform this operation.");}
    private static void log(long actor,String action,String entity,String detail){state.activity.add(new Activity(actor,action,entity,detail));}
    private static void runDemo(){
        System.out.println("\n=== FOUNDRY BACKEND DEMONSTRATION ===");
        Session founder=session("founder@foundry.demo"),admin=session("admin@foundry.demo"),evaluator=session("evaluator@foundry.demo"),investor=session("investor@foundry.demo");
        try{
            long sid=state.nextStartup++;Startup st=new Startup(sid,founder.userId(),"AquaLoop Analytics","ClimateTech","Jaipur","Seed",2023,"Water efficiency analytics.");state.startups.put(sid,st);log(founder.userId(),"CREATE_STARTUP","STARTUP#"+sid,st.name);
            long aid=state.nextApplication++;ApplicationRecord a=new ApplicationRecord(aid,sid,1);state.applications.put(aid,a);st.status="SUBMITTED";
            a.innovation=9;a.market=8;a.feasibility=8.5;a.team=9;a.readiness=Math.round((a.innovation+a.market+a.feasibility+a.team)/4*100)/10.0;a.status="ACCEPTED";a.recommendation="ACCEPT";a.evaluatorId=evaluator.userId();st.status="INCUBATED";log(evaluator.userId(),"EVALUATE","APPLICATION#"+aid,"Readiness "+a.readiness);
            long mentorId=state.mentors.keySet().stream().findFirst().orElseThrow();state.documents.add("ASSIGN:"+mentorId+":"+sid);log(admin.userId(),"ASSIGN_MENTOR","STARTUP#"+sid,"Mentor "+mentorId);
            long mid=state.nextMilestone++;Milestone m=new Milestone(mid,sid,"Customer pilot",LocalDate.now().plusDays(5));m.status="IN_PROGRESS";state.milestones.put(mid,m);
            long rid=state.nextRequest++;state.requests.put(rid,new FundingRequest(rid,sid,150000,"Pilot rollout"));st.status="FUNDRAISING";
            long iid=state.nextInvestment++;state.investments.put(iid,new Investment(iid,rid,investor.userId(),50000));log(investor.userId(),"COMMIT_INVESTMENT","INVESTMENT#"+iid,"INR 50000");
            System.out.println("Created startup → submitted application → evaluated → accepted → mentor assigned → milestone created → funding requested → investment committed.");
            System.out.println("Readiness formula: (innovation + market + feasibility + team) / 4 × 10 = "+a.readiness+"/100.");
            System.out.println("\nRole authorization check:");
            try{require(session("investor@foundry.demo"),Role.ADMIN);throw new AssertionError("Investor incorrectly passed admin check.");}
            catch(SecurityException expected){System.out.println("PASS · Investor cannot call administrator-only operations ("+expected.getMessage()+")");}
            System.out.println("\nConsolidated Startup 360°:");
            startup360(admin);
            save();System.out.println("\nPASS · Persistent store saved to "+STORE);
        }catch(Exception ex){showError(ex);}
    }
    private static Session session(String email){User u=findEmail(email);return new Session(u.id,u.name,u.email,u.role);}
    private static String ask(String prompt){System.out.print(prompt);return IN.nextLine().trim();}
    private static int number(String prompt){return Integer.parseInt(ask(prompt));}
    private static double decimal(String prompt){return Double.parseDouble(ask(prompt));}
    private static void showError(Exception ex){
        String kind=ex.getClass().getSimpleName();
        String message=ex.getMessage()==null?"No message":ex.getMessage();
        System.out.println("\n[ERROR] "+kind+" raised");
        System.out.println(message);
        System.out.println("Details logged to terminal. Your last successful changes remain saved.\n");
    }
}
