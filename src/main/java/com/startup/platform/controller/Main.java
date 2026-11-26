package com.startup.platform.controller;

import com.startup.platform.service.*;
import com.startup.platform.model.*;
import com.startup.platform.util.*;

import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.stage.Stage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.nio.file.Path;
import java.util.*;
import com.startup.platform.util.DatasetFileService;

public class Main extends Application {
    private final PlatformService service=new PlatformService();
    private Session session;
    private Stage stage;
    private final Label status=new Label("Not signed in");
    @Override public void init(){AppLog.info("Foundry application initializing");}
    @Override public void start(Stage primaryStage){stage=primaryStage;stage.setTitle("Foundry | Startup Incubation & Investment Management");stage.setMinWidth(900);stage.setMinHeight(650);status.getStyleClass().add("signed-in");showLogin();stage.show();AppLog.info("Foundry application started");}
    @Override public void stop(){AppLog.info("Foundry application stopped");}
    private void showLogin(){
        status.setText("Not signed in");
        Label eyebrow=new Label("FOUNDRY  /  STARTUP ECOSYSTEM");eyebrow.getStyleClass().add("eyebrow");
        Label headline=new Label("Build what\'s next.");headline.getStyleClass().add("hero-title");
        Label copy=new Label("One connected workspace for founders, incubators, mentors, evaluators and investors.");copy.getStyleClass().add("hero-copy");copy.setWrapText(true);
        TextField email=new TextField();email.setPromptText("Email address");
        PasswordField pass=new PasswordField();pass.setPromptText("Password");
        ComboBox<String> role=new ComboBox<>(FXCollections.observableArrayList("ADMIN","FOUNDER","EVALUATOR","MENTOR","INVESTOR","INCUBATOR"));role.setPromptText("Choose your workspace role");role.setMaxWidth(Double.MAX_VALUE);
        Button login=new Button("Continue to workspace");login.getStyleClass().add("primary-button");login.setMaxWidth(Double.MAX_VALUE);
        Button register=new Button("Create an account");register.getStyleClass().add("secondary-button");
        Button setupAdmin=new Button("First-time administrator setup");setupAdmin.getStyleClass().add("quiet-button");
        login.setOnAction(e->{try{
            if(role.getValue()==null)throw new IllegalArgumentException("Choose the role you want to access before signing in.");
            Session authenticated=service.login(email.getText().trim(),pass.getText());
            if(authenticated==null)throw new SecurityException("The email/password combination is invalid or this account is awaiting approval.");
            if(!authenticated.role().equals(role.getValue()))throw new SecurityException("Role mismatch: this account is registered as "+authenticated.role()+", not "+role.getValue()+". Choose the registered role.");
            session=authenticated;showHome();
        }catch(Exception ex){error(ex);}});
        register.setOnAction(e->showRegister());setupAdmin.setOnAction(e->showAdminSetup());
        VBox form=new VBox(12,new Label("Welcome back"),new Label("Sign in to your workspace"),email,pass,role,login,new HBox(8,register,setupAdmin));form.getStyleClass().add("auth-card");
        VBox intro=new VBox(15,eyebrow,headline,copy,new HBox(8,chip("Startup journeys"),chip("Smart matching"),chip("Milestone health")));intro.getStyleClass().add("auth-intro");
        GridPane layout=new GridPane();layout.setHgap(36);layout.setAlignment(javafx.geometry.Pos.CENTER);layout.add(intro,0,0);layout.add(form,1,0);GridPane.setHgrow(form,Priority.ALWAYS);
        StackPane canvas=new StackPane(layout);canvas.getStyleClass().add("auth-shell");canvas.setPadding(new Insets(48));setScene(canvas,1180,760);
    }
    private Label chip(String text){Label l=new Label(text);l.getStyleClass().add("chip");return l;}
    private void showRegister(){
        Label title=new Label("Create your Foundry account");title.getStyleClass().add("section-title");
        TextField name=new TextField(),email=new TextField(),profile=new TextField(),experience=new TextField("0");PasswordField pw=new PasswordField();
        name.setPromptText("Full name");email.setPromptText("Work email");pw.setPromptText("Password (8+ characters)");profile.setPromptText("Expertise / organization / founder bio");experience.setPromptText("Mentor experience in years");
        ComboBox<String> role=new ComboBox<>(FXCollections.observableArrayList("FOUNDER","MENTOR","INVESTOR","INCUBATOR"));role.setValue("FOUNDER");role.setMaxWidth(Double.MAX_VALUE);
        Label experienceLabel=new Label("Professional experience");experience.setVisible(false);experience.setManaged(false);experienceLabel.setVisible(false);experienceLabel.setManaged(false);
        role.setOnAction(e->{boolean mentor="MENTOR".equals(role.getValue());experience.setVisible(mentor);experience.setManaged(mentor);experienceLabel.setVisible(mentor);experienceLabel.setManaged(mentor);});
        Button create=new Button("Create account");create.getStyleClass().add("primary-button");Button back=new Button("Back to sign in");back.getStyleClass().add("secondary-button");
        create.setOnAction(e->{try{int years="MENTOR".equals(role.getValue())?Integer.parseInt(experience.getText()):0;service.register(name.getText(),email.getText(),pw.getText(),role.getValue(),profile.getText(),years);msg("Registration complete",role.getValue().equals("MENTOR")?"Mentor account created and submitted for approval.":"Your account is ready. Sign in to continue.");showLogin();}catch(Exception ex){error(ex);}});
        back.setOnAction(e->showLogin());VBox v=new VBox(13,title,new Label("Join the startup ecosystem"),name,email,pw,role,profile,experienceLabel,experience,new HBox(10,create,back));v.getStyleClass().add("auth-card");v.setMaxWidth(470);StackPane root=new StackPane(v);root.getStyleClass().add("auth-shell");root.setPadding(new Insets(36));setScene(root,900,700);
    }
    private void showHome(){
        try { String result=DatasetBootstrapper.loadIfNeeded(); AppLog.info(result); }
        catch(Exception ex) { AppLog.error("Funding intelligence bootstrap was not completed",ex); }
        status.setText("● Signed in as "+session.name()+"  /  "+session.role());
        BorderPane root=new BorderPane();root.getStyleClass().add("app-shell");
        VBox sidebar=new VBox(14);sidebar.getStyleClass().add("sidebar");sidebar.setPrefWidth(238);
        Label logo=new Label("foundry.");logo.getStyleClass().add("brand-mark");
        Label sub=new Label("STARTUP ECOSYSTEM");sub.getStyleClass().add("eyebrow");
        VBox brand=new VBox(3,logo,sub);brand.setPadding(new Insets(10,8,22,8));
        Label profile=new Label(session.name()+"\n"+session.role()+" WORKSPACE");profile.getStyleClass().add("profile-chip");profile.setMaxWidth(Double.MAX_VALUE);
        VBox nav=new VBox(6);nav.getStyleClass().add("nav-list");
        StackPane content=new StackPane();content.getStyleClass().add("content-stage");
        Label pageTitle=new Label("Overview");pageTitle.getStyleClass().add("page-title");
        Label pageSub=new Label("Your startup ecosystem, in one place");pageSub.getStyleClass().add("page-subtitle");
        VBox headerText=new VBox(3,pageTitle,pageSub);
        Button logout=new Button("Sign out");logout.getStyleClass().add("secondary-button");logout.setOnAction(e->{session=null;showLogin();});
        HBox top=new HBox(headerText,new Region(),logout);top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);HBox.setHgrow(top.getChildren().get(1),Priority.ALWAYS);top.getStyleClass().add("workspace-header");
        BorderPane main=new BorderPane();main.setTop(top);main.setCenter(content);main.setBottom(status);status.getStyleClass().add("session-status");
        root.setLeft(sidebar);root.setCenter(main);
        LinkedHashMap<String,javafx.scene.Node> pages=new LinkedHashMap<>();
        pages.put("Overview",dashboard());
        switch(session.role()){
            case "ADMIN" -> {pages.put("Startup directory",startupPanel());pages.put("Incubation",incubationPanel());pages.put("Mentor network",mentorPanel());pages.put("Funding & investors",fundingPanel());pages.put("Market intelligence",marketPanel());pages.put("Startup 360°",startup360Panel());pages.put("Reports & activity",reportsPanel());pages.put("Administration",adminPanel());}
            case "FOUNDER" -> {pages.put("My startup",startupPanel());pages.put("Incubation journey",incubationPanel());pages.put("Milestones",mentorPanel());pages.put("Funding",fundingPanel());pages.put("Startup intelligence",marketPanel());pages.put("Startup 360°",startup360Panel());pages.put("My activity",reportsPanel());}
            case "MENTOR" -> {pages.put("Assigned milestones",mentorPanel());pages.put("Startup intelligence",marketPanel());pages.put("Startup 360°",startup360Panel());pages.put("Activity",reportsPanel());}
            case "EVALUATOR" -> {pages.put("Application reviews",incubationPanel());pages.put("Startup intelligence",marketPanel());pages.put("Startup 360°",startup360Panel());pages.put("Activity",reportsPanel());}
            case "INVESTOR" -> {pages.put("Discover & invest",fundingPanel());pages.put("Market intelligence",marketPanel());pages.put("Startup 360°",startup360Panel());pages.put("Activity",reportsPanel());}
            case "INCUBATOR" -> {pages.put("Incubation programmes",incubationPanel());pages.put("Mentor network",mentorPanel());pages.put("Startup intelligence",marketPanel());pages.put("Activity",reportsPanel());}
            default -> throw new IllegalStateException("No workspace is configured for role "+session.role());
        }
        sidebar.getChildren().addAll(brand,profile,new Label("WORKSPACE"),nav,new Region());VBox.setVgrow(sidebar.getChildren().get(4),Priority.ALWAYS);
        String landingPage="Overview";
        final String[] active={landingPage};
        pages.forEach((name,node)->{Button item=new Button(name);item.getStyleClass().add("nav-button");item.setMaxWidth(Double.MAX_VALUE);item.setOnAction(e->{active[0]=name;pageTitle.setText(name);pageSub.setText("Live workspace · "+session.role().toLowerCase(Locale.ROOT));content.getChildren().setAll(wrapPage(node));nav.getChildren().forEach(n->n.getStyleClass().remove("nav-active"));item.getStyleClass().add("nav-active");});nav.getChildren().add(item);if(name.equals(landingPage)){item.getStyleClass().add("nav-active");pageTitle.setText(name);pageSub.setText("Funding intelligence · "+session.role().toLowerCase(Locale.ROOT));content.getChildren().setAll(wrapPage(node));}});
        main.setPadding(new Insets(0,22,14,22));BorderPane.setMargin(top,new Insets(20,4,18,4));setScene(root,1320,850);
    }
    private javafx.scene.Node wrapPage(javafx.scene.Node node){ScrollPane scroll=new ScrollPane(node);scroll.setFitToWidth(true);scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);scroll.getStyleClass().add("page-scroll");return scroll;}
    private void setScene(javafx.scene.Parent root,double width,double height){
        Scene scene=new Scene(root,width,height);
        var css=getClass().getResource("/static/css/app.css");
        if(css!=null)scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
    }
    private Tab tab(String title,javafx.scene.Node node){node.getStyleClass().add("page-content");ScrollPane scroll=new ScrollPane(node);scroll.setFitToWidth(true);scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);Tab t=new Tab(title,scroll);t.setClosable(false);return t;}
    private javafx.scene.Node dashboard(){
        VBox page=new VBox(18); page.getStyleClass().add("dashboard-page");
        HBox hero=new HBox(12); hero.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox heading=new VBox(4,new Label("Your ecosystem, in motion"),new Label("Funding intelligence and your connected Foundry workspace"));
        heading.getChildren().get(0).getStyleClass().add("dashboard-heading"); heading.getChildren().get(1).getStyleClass().add("muted-copy");
        Region push=new Region(); HBox.setHgrow(push,Priority.ALWAYS); Button refresh=new Button("Refresh analytics"); refresh.getStyleClass().add("primary-button"); hero.getChildren().addAll(heading,push,refresh);
        GridPane kpis=new GridPane(); kpis.setHgap(12); kpis.setVgap(12); for(int c=0;c<4;c++){ColumnConstraints cc=new ColumnConstraints();cc.setPercentWidth(25);cc.setHgrow(Priority.ALWAYS);kpis.getColumnConstraints().add(cc);}
        Label recordsValue=new Label("—"),companiesValue=new Label("—"),sectorsValue=new Label("—"),countriesValue=new Label("—");
        kpis.add(metricCard("FUNDING ROUNDS",recordsValue,"Total publisher funding records","kpi-olive"),0,0);
        kpis.add(metricCard("COMPANIES REPRESENTED",companiesValue,"Distinct company names","kpi-lilac"),1,0);
        kpis.add(metricCard("SECTORS COVERED",sectorsValue,"Market categories represented","kpi-orange"),2,0);
        kpis.add(metricCard("COUNTRIES",countriesValue,"Geographic market footprint","kpi-sage"),3,0);
        NumberAxis sectorX=new NumberAxis(); sectorX.setLabel("Funding rounds (count)"); sectorX.setForceZeroInRange(true); sectorX.setMinorTickVisible(false); sectorX.setTickLabelFill(javafx.scene.paint.Color.web("#48232E")); sectorX.setTickLabelFont(javafx.scene.text.Font.font(10));
        CategoryAxis sectorY=new CategoryAxis(); sectorY.setLabel("Sector"); sectorY.setTickLabelGap(8); sectorY.setTickLabelFill(javafx.scene.paint.Color.web("#48232E")); sectorY.setTickLabelFont(javafx.scene.text.Font.font(10));
        BarChart<Number,String> sectorChart=new BarChart<>(sectorX,sectorY); sectorChart.setTitle("Funding rounds by sector"); sectorChart.setLegendVisible(false); sectorChart.setAnimated(true); sectorChart.setPrefHeight(350); sectorChart.setCategoryGap(12); sectorChart.setHorizontalGridLinesVisible(true); sectorChart.setVerticalGridLinesVisible(false);
        CategoryAxis yearX=new CategoryAxis(); yearX.setLabel("Year"); yearX.setTickLabelRotation(0); yearX.setTickLabelFill(javafx.scene.paint.Color.web("#48232E")); yearX.setTickLabelFont(javafx.scene.text.Font.font(10));
        NumberAxis yearY=new NumberAxis(); yearY.setLabel("Funding rounds (count)"); yearY.setForceZeroInRange(true); yearY.setMinorTickVisible(false); yearY.setTickLabelFill(javafx.scene.paint.Color.web("#48232E")); yearY.setTickLabelFont(javafx.scene.text.Font.font(10)); yearY.setTickLabelFormatter(new javafx.util.converter.NumberStringConverter("#,##0"));
        LineChart<String,Number> yearChart=new LineChart<>(yearX,yearY); yearChart.setTitle("Funding rounds by year"); yearChart.setLegendVisible(false); yearChart.setCreateSymbols(true); yearChart.setPrefHeight(350); yearChart.setVerticalGridLinesVisible(true); yearChart.setHorizontalGridLinesVisible(true);
        PieChart stageChart=new PieChart(); stageChart.setTitle("Funding stage mix"); stageChart.setLabelsVisible(true); stageChart.setPrefHeight(290);
        TableView<String[]> topTable=dashboardTable(new String[]{"Company","Sector","City","Rounds","Total USD","Latest"},new int[]{0,1,2,3,4,5}); topTable.setPrefHeight(310);
        TableView<String[]> latestTable=dashboardTable(new String[]{"Startup","Country","City","Sector","Stage","Amount USD","Lead investor","Announced"},new int[]{0,1,2,3,4,5,6,7}); latestTable.setPrefHeight(330);
        TableView<String[]> operations=dashboardTable(new String[]{"Metric","Value"},new int[]{0,1}); operations.setPrefHeight(190);
        TableView<String[]> activity=dashboardTable(new String[]{"Source","Event","Type","Description","Date"},new int[]{0,1,2,3,4}); activity.setPrefHeight(190);
        GridPane charts=new GridPane(); charts.setHgap(14); charts.setVgap(14); for(int c=0;c<2;c++){ColumnConstraints cc=new ColumnConstraints();cc.setPercentWidth(50);cc.setHgrow(Priority.ALWAYS);charts.getColumnConstraints().add(cc);} charts.add(section("Sector landscape",sectorChart),0,0); charts.add(section("Yearly momentum",yearChart),1,0); charts.add(section("Funding stages",stageChart),0,1); charts.add(section("Most funded companies in the dataset",topTable),1,1);
        GridPane lower=new GridPane(); lower.setHgap(14); lower.setVgap(14); for(int c=0;c<2;c++){ColumnConstraints cc=new ColumnConstraints();cc.setPercentWidth(50);cc.setHgrow(Priority.ALWAYS);lower.getColumnConstraints().add(cc);} lower.add(section("Recent funding rounds",latestTable),0,0,2,1); lower.add(section("Your live workspace",operations),0,1); lower.add(section("Recent activity",activity),1,1);
        Label provenance=new Label("Funding intelligence and operational Foundry records are brought together across the role-based workspaces."); provenance.getStyleClass().add("data-provenance"); provenance.setWrapText(true);
        Runnable load=()->{try{
            List<String[]> k=service.historicalKpis(session); if(!k.isEmpty()){String[] a=k.get(0);recordsValue.setText(String.format(Locale.ROOT,"%,d",Long.parseLong(a[0])));companiesValue.setText(String.format(Locale.ROOT,"%,d",Long.parseLong(a[1])));sectorsValue.setText(a[2]);countriesValue.setText(a[3]);}
            XYChart.Series<Number,String> ss=new XYChart.Series<>(); for(String[] r:service.historicalSectors(session,8)){long count=Long.parseLong(r[1]);XYChart.Data<Number,String> point=new XYChart.Data<>(count,r[0]);point.nodeProperty().addListener((obs,oldNode,node)->{if(node!=null)Tooltip.install(node,new Tooltip("Sector: "+r[0]+"\\nFunding rounds: "+String.format(Locale.ROOT,"%,d",count)));});ss.getData().add(point);} sectorChart.getData().setAll(ss);
            XYChart.Series<String,Number> ys=new XYChart.Series<>(); for(String[] r:service.historicalYears(session)){long count=Long.parseLong(r[1]);XYChart.Data<String,Number> point=new XYChart.Data<>(r[0],count);javafx.scene.control.Label valueLabel=new javafx.scene.control.Label(String.format(Locale.ROOT,"%,d",count));valueLabel.getStyleClass().add("chart-point-label");valueLabel.setTranslateY(-16);javafx.scene.layout.StackPane marker=new javafx.scene.layout.StackPane(new javafx.scene.shape.Circle(4,javafx.scene.paint.Color.web("#48232E")),valueLabel);point.setNode(marker);Tooltip.install(marker,new Tooltip("Year: "+r[0]+"\\nFunding rounds: "+String.format(Locale.ROOT,"%,d",count)));ys.getData().add(point);} yearChart.getData().setAll(ys);
            stageChart.getData().clear(); for(String[] r:service.historicalStages(session,7))stageChart.getData().add(new PieChart.Data(r[0],Long.parseLong(r[1])));
            topTable.getItems().setAll(service.historicalTopCompanies(session,12)); latestTable.getItems().setAll(service.historicalLatest(session,18));
            operations.getItems().setAll(service.dashboard(session));
            activity.getItems().setAll(service.activityTimeline(session).stream().limit(12).toList());
        }catch(Exception ex){error(ex);}};
        refresh.setOnAction(e->load.run()); page.getChildren().addAll(hero,kpis,charts,lower,provenance); load.run(); return page;
    }
    private TableView<String[]> dashboardTable(String[] headers,int[] fields){
        TableView<String[]> table=new TableView<>(); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        for(int i=0;i<headers.length;i++){final int f=fields[i];TableColumn<String[],String> col=new TableColumn<>(headers[i]);col.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(f<d.getValue().length&&d.getValue()[f]!=null?d.getValue()[f]:"—"));table.getColumns().add(col);} return table;
    }
    private VBox metricCard(String title,Label value,String note,String tone){
        Label t=new Label(title);t.getStyleClass().add("kpi-label");value.getStyleClass().add("kpi-value");Label n=new Label(note);n.getStyleClass().add("kpi-note");VBox card=new VBox(8,t,value,n);card.getStyleClass().addAll("kpi-card",tone);card.setPadding(new Insets(17));card.setMinWidth(150);GridPane.setHgrow(card,Priority.ALWAYS);return card;
    }
    private VBox section(String title,javafx.scene.Node content){
        Label label=new Label(title); label.getStyleClass().add("panel-title"); VBox box=new VBox(10,label,content); box.getStyleClass().add("dashboard-panel"); return box;
    }
    private javafx.scene.Node historicalPreview(String title,String subtitle,int limit){
        VBox box=new VBox(8); Label heading=new Label(title);heading.getStyleClass().add("section-title");Label note=new Label(subtitle);note.getStyleClass().add("muted-copy");note.setWrapText(true);
        TableView<String[]> table=dashboardTable(new String[]{"Startup","Sector","City","Stage","Country","Funding USD"},new int[]{0,3,2,5,1,6});table.setPrefHeight(300);
        try{table.getItems().setAll(service.externalFundingRounds(session,"","","",0,limit));}catch(Exception ex){error(ex);}
        box.getChildren().addAll(heading,note,table);return box;
    }
    private javafx.scene.Node startupPanel(){
        VBox v=new VBox(8);
        TextField n=new TextField(),sector=new TextField(),city=new TextField(),year=new TextField(),stageField=new TextField(),desc=new TextField();
        n.setPromptText("Startup name");sector.setPromptText("Sector");city.setPromptText("City");year.setPromptText("Founded year");stageField.setPromptText("Stage");desc.setPromptText("Description");
        Button add=new Button("Create startup");
        TableView<String[]> table=buildStringTable(new String[]{"No.","Startup","Sector","City","Stage","Founded year"},new int[]{0,1,2,3,4,5},380);
        Label count=new Label("Loading startup records…");
        Button refresh=new Button("Refresh list");
        refresh.setOnAction(e->{try{List<String[]> rows=service.startups(session);table.getItems().setAll(rows);count.setText(String.format(Locale.ROOT,"Showing %,d startup records",rows.size()));}catch(Exception ex){error(ex);}});
        add.setOnAction(e->{try{long createdId=service.createStartup(session,n.getText(),sector.getText(),city.getText(),desc.getText(),Integer.parseInt(year.getText()),stageField.getText());msg("Saved","Startup #"+createdId+" created in MySQL. Use this ID for applications and documents.");refresh.fire();}catch(Exception ex){error(ex);}});
        TextField docStartup=new TextField();docStartup.setPromptText("Startup ID for document upload");Button upload=new Button("Upload startup document");
        upload.setOnAction(e->{try{javafx.stage.FileChooser fc=new javafx.stage.FileChooser();fc.setTitle("Choose startup document");File f=fc.showOpenDialog(stage);if(f!=null){service.uploadStartupDocument(session,Long.parseLong(docStartup.getText()),f,"SUPPORTING_DOCUMENT");msg("Uploaded","Document stored and linked to startup.");}}catch(Exception ex){error(ex);}});
        TextField teamStartup=new TextField(),memberName=new TextField(),memberRole=new TextField(),memberEmail=new TextField();teamStartup.setPromptText("Startup ID");memberName.setPromptText("Team member name");memberRole.setPromptText("Role / designation");memberEmail.setPromptText("Email (optional)");
        Button addMember=new Button("Add team member");addMember.setOnAction(e->{try{service.addTeamMember(session,Long.parseLong(teamStartup.getText()),memberName.getText(),memberRole.getText(),memberEmail.getText());msg("Saved","Team member added.");refresh.fire();}catch(Exception ex){error(ex);}});
        TextField coFounderStartup=new TextField(),coFounderEmail=new TextField(),coFounderRole=new TextField("Co-founder");
        coFounderStartup.setPromptText("Startup ID");coFounderEmail.setPromptText("Registered co-founder email");coFounderRole.setPromptText("Founder role");
        Button linkCoFounder=new Button("Link registered co-founder");
        linkCoFounder.setOnAction(e->{try{service.linkCoFounder(session,Long.parseLong(coFounderStartup.getText()),coFounderEmail.getText(),coFounderRole.getText());msg("Linked","Existing founder account linked to this startup.");refresh.fire();}catch(Exception ex){error(ex);}});
        TextField progressStartup=new TextField(),progressPercent=new TextField(),progressSummary=new TextField();progressStartup.setPromptText("Startup ID");progressPercent.setPromptText("Progress percentage (0–100)");progressSummary.setPromptText("Progress summary");
        Button saveProgress=new Button("Save progress update");saveProgress.setOnAction(e->{try{service.updateStartupProgress(session,Long.parseLong(progressStartup.getText()),Double.parseDouble(progressPercent.getText()),progressSummary.getText());msg("Saved","Startup progress recorded.");refresh.fire();}catch(Exception ex){error(ex);}});
        v.getChildren().add(new Label("Startup directory"));
        if(session.role().equals("FOUNDER"))v.getChildren().addAll(new Label("Register a startup"),n,sector,city,year,stageField,desc,add);
        v.getChildren().addAll(refresh,count,table);
        if(session.role().equals("FOUNDER")||session.role().equals("ADMIN"))v.getChildren().addAll(new Separator(),new Label("Startup document management"),docStartup,upload,new Separator(),new Label("Startup team"),teamStartup,memberName,memberRole,memberEmail,addMember,new Separator(),new Label("Link an existing co-founder account"),coFounderStartup,coFounderEmail,coFounderRole,linkCoFounder,new Separator(),new Label("Progress tracking"),progressStartup,progressPercent,progressSummary,saveProgress);
        refresh.fire();
        return v;
    }
    private javafx.scene.Node incubationPanel(){
        VBox v=new VBox(8);
        TextField sid=new TextField(),pid=new TextField(),aid=new TextField(),i=new TextField("7"),m=new TextField("7"),f=new TextField("7"),t=new TextField("7"),comments=new TextField();
        sid.setPromptText("Startup ID");pid.setPromptText("Program ID");
        Button apply=new Button("Submit incubation application");
        apply.setOnAction(e->{try{service.submitApplication(session,Long.parseLong(sid.getText()),Long.parseLong(pid.getText()));msg("Submitted","Application submitted.");}catch(Exception ex){error(ex);}});
        aid.setPromptText("Application ID");comments.setPromptText("Evaluation comments");ComboBox<String> rec=new ComboBox<>(FXCollections.observableArrayList("ACCEPT","REJECT","REVIEW"));rec.setValue("REVIEW");
        Button evaluate=new Button("Save evaluation");evaluate.setOnAction(e->{try{service.evaluate(session,Long.parseLong(aid.getText()),Double.parseDouble(i.getText()),Double.parseDouble(m.getText()),Double.parseDouble(f.getText()),Double.parseDouble(t.getText()),comments.getText(),rec.getValue());msg("Saved","Evaluation recorded.");}catch(Exception ex){error(ex);}});
        TableView<String[]> programTable=buildStringTable(new String[]{"Program ID","Programme","Incubator","Duration (months)"},new int[]{0,1,2,3},245);
        TableView<String[]> applicationTable=buildStringTable(new String[]{"Application ID","Startup","Programme","Application date","Status"},new int[]{0,1,2,3,4},340);
        Button refreshPrograms=new Button("Refresh programmes"),refreshApps=new Button("Refresh applications");
        refreshPrograms.setOnAction(e->{try{programTable.getItems().setAll(service.programs(session));}catch(Exception ex){error(ex);}});
        refreshApps.setOnAction(e->{try{applicationTable.getItems().setAll(service.applications(session));}catch(Exception ex){error(ex);}});
        v.getChildren().addAll(new Label("Available incubation programmes"),refreshPrograms,programTable,new Label("Incubation applications"),refreshApps,applicationTable);
        if(session.role().equals("FOUNDER"))v.getChildren().addAll(new Label("Submit an application"),sid,pid,apply);
        if(session.role().equals("INCUBATOR")){
            TextField ownProgramName=new TextField(),ownProgramDuration=new TextField("6");ownProgramName.setPromptText("Your programme name");ownProgramDuration.setPromptText("Duration in months");Button createOwnProgram=new Button("Create programme for my incubator");
            createOwnProgram.setOnAction(e->{try{service.createProgram(session,service.ownIncubatorId(session),ownProgramName.getText(),Integer.parseInt(ownProgramDuration.getText()));msg("Programme created","Your incubator programme has been saved.");refreshPrograms.fire();}catch(Exception ex){error(ex);}});
            v.getChildren().addAll(new Separator(),new Label("Manage your incubator programme"),ownProgramName,ownProgramDuration,createOwnProgram);
        }
        if(session.role().equals("EVALUATOR")||session.role().equals("ADMIN"))v.getChildren().addAll(new Separator(),new Label("Evaluation scorecard (0–10)"),aid,new HBox(6,i,m,f,t),comments,rec,evaluate);
        refreshPrograms.fire();refreshApps.fire();
        return v;
    }
    private javafx.scene.Node mentorPanel(){
        VBox v=new VBox(8);
        if(session.role().equals("ADMIN")||session.role().equals("INCUBATOR")){
            TableView<String[]> mentors=buildStringTable(new String[]{"Mentor ID","Mentor","Expertise","Experience (years)","Approval status","Assigned startups"},new int[]{0,1,2,3,4,5},340);
            Button refresh=new Button("Refresh mentor network");
            refresh.setOnAction(e->{try{mentors.getItems().setAll(service.mentorDirectory(session));}catch(Exception ex){error(ex);}});
            v.getChildren().addAll(new Label("Mentor network"),refresh,mentors);
            if(session.role().equals("ADMIN")){
                TextField mid=new TextField(),startup=new TextField();mid.setPromptText("Mentor ID");startup.setPromptText("Startup ID");
                Button approve=new Button("Approve mentor ID"),reject=new Button("Reject mentor ID"),assign=new Button("Assign approved mentor to startup");
                approve.setOnAction(e->{mentorDecision(mid,true);refresh.fire();});reject.setOnAction(e->{mentorDecision(mid,false);refresh.fire();});
                assign.setOnAction(e->{try{service.assignMentor(session,Long.parseLong(mid.getText()),Long.parseLong(startup.getText()));msg("Assigned","Mentor assigned.");refresh.fire();}catch(Exception ex){error(ex);}});
                v.getChildren().addAll(new Label("Mentor approvals and assignments"),mid,new HBox(8,approve,reject),startup,assign);
            }
            refresh.fire();
        }
        if(session.role().equals("MENTOR")) {
            TableView<String[]> assigned=buildStringTable(new String[]{"Milestone ID","Startup","Milestone","Due date","Status"},new int[]{0,1,2,3,4},330);
            Button refreshAssigned=new Button("Refresh my assigned milestones");
            refreshAssigned.setOnAction(e->{try{assigned.getItems().setAll(service.assignedMilestones(session));}catch(Exception ex){error(ex);}});
            v.getChildren().addAll(new Label("My assigned startup milestones"),refreshAssigned,assigned);refreshAssigned.fire();
        }
        v.getChildren().addAll(milestoneControls());return v;
    }
    private javafx.scene.Node milestoneControls(){
        VBox v=new VBox(6);TextField sid=new TextField(),title=new TextField(),due=new TextField(),mid=new TextField(),note=new TextField();
        sid.setPromptText("Startup ID");title.setPromptText("Milestone title");due.setPromptText("Due date YYYY-MM-DD");
        Button add=new Button("Add milestone");add.setOnAction(e->{try{service.addMilestone(session,Long.parseLong(sid.getText()),title.getText(),LocalDate.parse(due.getText()));msg("Saved","Milestone created.");}catch(Exception ex){error(ex);}});
        mid.setPromptText("Milestone ID");note.setPromptText("Progress note");ComboBox<String> st=new ComboBox<>(FXCollections.observableArrayList("NOT_STARTED","IN_PROGRESS","COMPLETED","BLOCKED"));st.setValue("IN_PROGRESS");
        Button update=new Button("Update milestone");update.setOnAction(e->{try{service.updateMilestone(session,Long.parseLong(mid.getText()),st.getValue(),note.getText());msg("Updated","Milestone status and history saved.");}catch(Exception ex){error(ex);}});
        v.getChildren().add(new Label("Milestone tracking"));
        if(session.role().equals("ADMIN")||session.role().equals("FOUNDER"))v.getChildren().addAll(new Label("Create milestone"),sid,title,due,add);
        if(session.role().equals("ADMIN")||session.role().equals("MENTOR"))v.getChildren().addAll(new Separator(),new Label("Update milestone progress"),mid,st,note,update);
        return v;
    }
    private javafx.scene.Node fundingPanel(){
        VBox v=new VBox(8);TextField startup=new TextField(),amount=new TextField(),purpose=new TextField(),request=new TextField(),investment=new TextField(),interest=new TextField(),stageInterest=new TextField(),ticketMin=new TextField(),ticketMax=new TextField(),documentRequest=new TextField();
        startup.setPromptText("Startup ID");amount.setPromptText("Funding amount (INR)");purpose.setPromptText("Funding purpose");Button create=new Button("Create funding request");
        create.setOnAction(e->{try{long requestId=service.createFundingRequest(session,Long.parseLong(startup.getText()),new BigDecimal(amount.getText()),purpose.getText());documentRequest.setText(Long.toString(requestId));msg("Created","Funding request #"+requestId+" saved. Its ID is ready for document upload.");}catch(Exception ex){error(ex);}});
        request.setPromptText("Funding request ID");investment.setPromptText("Investment commitment (INR)");Button invest=new Button("Commit investment");
        invest.setOnAction(e->{try{service.invest(session,Long.parseLong(request.getText()),new BigDecimal(investment.getText()));msg("Recorded","Investment commitment recorded.");}catch(Exception ex){error(ex);}});
        interest.setPromptText("Investor sector preference");Button addInterest=new Button("Add sector preference");addInterest.setOnAction(e->{try{service.addInterest(session,interest.getText());msg("Saved","Sector preference saved.");}catch(Exception ex){error(ex);}});
        stageInterest.setPromptText("Preferred startup stage (Seed, Series A...)");Button addStageInterest=new Button("Add stage preference");addStageInterest.setOnAction(e->{try{service.addStageInterest(session,stageInterest.getText());msg("Saved","Stage preference saved.");}catch(Exception ex){error(ex);}});
        ticketMin.setPromptText("Minimum ticket (INR)");ticketMax.setPromptText("Maximum ticket (INR)");
        Button saveTicket=new Button("Save investment ticket range");saveTicket.setOnAction(e->{try{service.updateInvestorTicketRange(session,new BigDecimal(ticketMin.getText()),new BigDecimal(ticketMax.getText()));msg("Saved","Investor ticket range updated.");}catch(Exception ex){error(ex);}});
        documentRequest.setPromptText("Funding request ID for document upload");Button uploadFundingDoc=new Button("Upload funding supporting document");
        uploadFundingDoc.setOnAction(e->{try{javafx.stage.FileChooser fc=new javafx.stage.FileChooser();fc.setTitle("Choose funding document");File f=fc.showOpenDialog(stage);if(f!=null){service.uploadFundingDocument(session,Long.parseLong(documentRequest.getText()),f);msg("Uploaded","Funding document stored and linked to request.");}}catch(Exception ex){error(ex);}});
        TableView<String[]> matches=buildStringTable(new String[]{"Startup ID","Startup","Sector","City","Sector matches","Stage matches"},new int[]{0,1,2,3,4,5},300);
        Button match=new Button("Show startup matches");match.setOnAction(e->{try{matches.getItems().setAll(service.matches(session));}catch(Exception ex){error(ex);}});
        TableView<String[]> requests=buildStringTable(new String[]{"No.","Startup","Amount","Currency","Purpose / lead investor","Status / stage"},new int[]{0,1,2,3,4,5},390);
        Button reload=new Button("Refresh funding requests");reload.setOnAction(e->{try{requests.getItems().setAll(service.fundingRequests(session));}catch(Exception ex){error(ex);}});
        if(session.role().equals("FOUNDER"))v.getChildren().addAll(new Label("Create a funding request (INR)"),startup,amount,purpose,create,new Label("My funding requests"),reload,requests,new Label("Attach supporting document"),documentRequest,uploadFundingDoc);
        if(session.role().equals("INVESTOR"))v.getChildren().addAll(new Label("Investor preferences and matching"),interest,addInterest,stageInterest,addStageInterest,new Label("Investment ticket range"),ticketMin,ticketMax,saveTicket,match,matches,new Separator(),new Label("Funding opportunities"),reload,requests,request,investment,invest);
        if(session.role().equals("ADMIN"))v.getChildren().addAll(new Label("Funding request oversight"),reload,requests);
        if(session.role().equals("FOUNDER")||session.role().equals("INVESTOR")||session.role().equals("ADMIN")) reload.fire();
        if(session.role().equals("INVESTOR")) match.fire();
        v.getChildren().add(new Label("Funding requests and funding rounds"));
        return v;
    }
    private javafx.scene.Node marketPanel(){
        VBox v=new VBox(12); Label intro=new Label("Startup intelligence"); intro.getStyleClass().add("section-title");
        Label note=new Label("Browse the complete funding-round records in pages. Filters search the full database, not just the visible page."); note.setWrapText(true); note.getStyleClass().add("muted-copy");
        TextField search=new TextField(), sector=new TextField(), stageFilter=new TextField(); search.setPromptText("Search startup, city, country or lead investor"); sector.setPromptText("Sector contains…"); stageFilter.setPromptText("Funding stage contains…");
        HBox filters=new HBox(9,search,sector,stageFilter); HBox.setHgrow(search,Priority.ALWAYS); HBox.setHgrow(sector,Priority.ALWAYS); HBox.setHgrow(stageFilter,Priority.ALWAYS);
        TableView<String[]> liveTable=buildStringTable(
                new String[]{"No.","Startup","Sector","City","Stage","Applications","Mentors","Milestones","Completed","Funding value","Committed","Readiness","Team","Progress"},
                new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13},300);
        Label liveCount=new Label("Loading current startup activity…");
        Button refreshLive=new Button("Refresh startup activity");
        refreshLive.setOnAction(e->{try{
            List<String[]> rows=service.liveStartupIntelligence(session);
            liveTable.getItems().setAll(rows);
            liveCount.setText(String.format(Locale.ROOT,"Showing %,d current startup records",rows.size()));
        }catch(Exception ex){error(ex);liveCount.setText("Unable to load current startup activity: "+ex.getMessage());}});
        javafx.scene.control.TableView<String[]> table=new javafx.scene.control.TableView<>(); table.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN); table.setPrefHeight(500);
        String role=session.role();
        int[] fields; String[] headers;
        switch(role){
            case "INVESTOR" -> { fields=new int[]{0,1,2,3,4,5,6,7,8,9}; headers=new String[]{"Startup","Country","City","Sector","Announced","Stage","Amount USD","Lead investor","Investors","News"}; }
            case "EVALUATOR" -> { fields=new int[]{0,1,2,3,4,5,8,9}; headers=new String[]{"Startup","Country","City","Sector","Announced","Stage","Investors","News"}; }
            case "MENTOR" -> { fields=new int[]{0,1,2,3,4,5}; headers=new String[]{"Startup","Country","City","Sector","Announced","Stage"}; }
            case "FOUNDER" -> { fields=new int[]{0,1,2,3,4,5,6,8,9}; headers=new String[]{"Startup","Country","City","Sector","Announced","Stage","Amount USD","Investors","News"}; }
            default -> { fields=new int[]{0,1,2,3,4,5,6,7,8,9,10}; headers=new String[]{"Startup","Country","City","Sector","Announced","Stage","Amount USD","Lead investor","Investors","News","Source URL"}; }
        }
        for(int c=0;c<fields.length;c++){ final int field=fields[c]; javafx.scene.control.TableColumn<String[],String> col=new javafx.scene.control.TableColumn<>(headers[c]); col.setCellValueFactory(data->new javafx.beans.property.SimpleStringProperty(field<data.getValue().length&&data.getValue()[field]!=null?data.getValue()[field]:"—")); col.setPrefWidth(c==0?190:120); table.getColumns().add(col); }
        int pageSize=100; final int[] page={0}; final long[] total={0}; Label count=new Label("Loading funding records…"); Button previous=new Button("Previous 100"), next=new Button("Next 100"), apply=new Button("Search / filter"), reset=new Button("Clear filters"); apply.getStyleClass().add("primary-button");
        Runnable load=()->{try{total[0]=service.externalFundingRoundCount(session,search.getText(),sector.getText(),stageFilter.getText()); int lastPage=total[0]==0?0:(int)((total[0]-1)/pageSize); if(page[0]>lastPage)page[0]=lastPage; table.getItems().setAll(service.externalFundingRounds(session,search.getText(),sector.getText(),stageFilter.getText(),page[0],pageSize)); long from=total[0]==0?0:(long)page[0]*pageSize+1; long to=Math.min(total[0],(long)(page[0]+1)*pageSize); count.setText(String.format(Locale.ROOT,"Showing %,d–%,d of %,d funding records · Page %d of %d",from,to,total[0],page[0]+1,Math.max(1,lastPage+1))); previous.setDisable(page[0]==0); next.setDisable((long)(page[0]+1)*pageSize>=total[0]); if(total[0]==0)table.setPlaceholder(new Label("No records match these filters. Clear filters to view the full dataset."));}catch(Exception ex){error(ex);count.setText("Dataset unavailable: "+ex.getClass().getSimpleName()+" — "+ex.getMessage());}};
        apply.setOnAction(e->{page[0]=0;load.run();}); search.setOnAction(e->{page[0]=0;load.run();}); sector.setOnAction(e->{page[0]=0;load.run();}); stageFilter.setOnAction(e->{page[0]=0;load.run();}); reset.setOnAction(e->{search.clear();sector.clear();stageFilter.clear();page[0]=0;load.run();}); previous.setOnAction(e->{if(page[0]>0){page[0]--;load.run();}}); next.setOnAction(e->{page[0]++;load.run();});
        HBox paging=new HBox(10,previous,next,count); paging.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label liveTitle=new Label("Current startup activity");liveTitle.getStyleClass().add("section-title");
        Label roundsTitle=new Label("Funding intelligence");roundsTitle.getStyleClass().add("section-title");
        v.getChildren().addAll(intro,note,liveTitle,refreshLive,liveCount,liveTable,roundsTitle,filters,new HBox(9,apply,reset),table,paging);
        refreshLive.fire();load.run();return v;
    }
    private TableView<String[]> buildStringTable(String[] headers, int[] fields, double prefHeight) {
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(prefHeight);
        table.setMinHeight(220);
        table.setPlaceholder(new Label("No records available."));
        for (int c = 0; c < headers.length; c++) {
            final int field = fields[c];
            TableColumn<String[], String> col = new TableColumn<>(headers[c]);
            col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                    field < data.getValue().length && data.getValue()[field] != null ? data.getValue()[field] : "—"));
            col.setPrefWidth(c == 0 ? 190 : 135);
            col.setMinWidth(c == 0 ? 170 : 110);
            table.getColumns().add(col);
        }
        return table;
    }

    private javafx.scene.Node startup360Panel(){
        VBox v=new VBox(10);
        Label title=new Label("Startup 360° — consolidated startup profiles");
        title.getStyleClass().add("section-title");
        Button refresh=new Button("Refresh consolidated profile");
        String[] headers={"No.","Startup","Sector","City","Stage","Applications","Mentors","Milestones","Completed","Funding value","Committed","Readiness","Team","Progress"};
        int[] fields={0,1,2,3,4,5,6,7,8,9,10,11,12,13};
        TableView<String[]> table=buildStringTable(headers,fields,520);
        Label count=new Label("Loading consolidated records…");
        refresh.setOnAction(e->{try{
            List<String[]> rows=service.startup360(session);
            table.getItems().setAll(rows);
            count.setText(String.format(Locale.ROOT,"Showing %,d consolidated startup records",rows.size()));
        }catch(Exception ex){error(ex);count.setText("Unable to load Startup 360°: "+ex.getMessage());}});
        v.getChildren().addAll(title,refresh,count,table);
        refresh.fire();
        return v;
    }

    private javafx.scene.Node reportsPanel(){
        VBox v=new VBox(10);
        Label title=new Label("Reports & activity");
        title.getStyleClass().add("section-title");

        String[] summaryHeaders={"Metric","Value"};
        int[] summaryFields={0,1};
        TableView<String[]> summary=buildStringTable(summaryHeaders,summaryFields,230);

        String[] healthHeaders={"ID","Startup","Milestone / reference","Due date","Status","Health"};
        int[] healthFields={0,1,2,3,4,5};
        TableView<String[]> health=buildStringTable(healthHeaders,healthFields,360);

        String[] activityHeaders={"Source","Event","Type","Description","Date"};
        int[] activityFields={0,1,2,3,4};
        TableView<String[]> timeline=buildStringTable(activityHeaders,activityFields,420);

        Button refresh=new Button("Refresh reports and activity");
        Label totalsLabel=new Label("Platform totals");
        Label healthLabel=new Label("Milestone health");
        Label activityLabel=new Label("Recent activity timeline");
        refresh.setOnAction(e->{try{
            summary.getItems().setAll(service.dashboard(session));
            health.getItems().setAll(service.milestoneHealth(session));
            timeline.getItems().setAll(service.activityTimeline(session));
        }catch(Exception ex){error(ex);}});

        Button snapshot=new Button("Export serializable platform snapshot");
        snapshot.setOnAction(e->{try{var file=SnapshotService.export(service,session);msg("Snapshot exported",file.toString());}catch(Exception ex){error(ex);}});
        Button readSnapshot=new Button("Read and verify a saved snapshot");
        readSnapshot.setOnAction(e->{try{javafx.stage.FileChooser fc=new javafx.stage.FileChooser();fc.setTitle("Open Foundry snapshot");File f=fc.showOpenDialog(stage);if(f!=null){var saved=SnapshotService.read(f.toPath());msg("Snapshot verified","Generated: "+saved.generatedAt()+"\nStartups: "+saved.startupSummaries().size()+"\nMilestones: "+saved.milestoneSummaries().size());}}catch(Exception ex){error(ex);}});

        v.getChildren().addAll(title,totalsLabel,summary,healthLabel,health,activityLabel,timeline,refresh,new HBox(8,snapshot,readSnapshot));
        refresh.fire();
        return v;
    }
    private void showAdminSetup(){
        TextField name=new TextField(),email=new TextField();PasswordField password=new PasswordField();name.setPromptText("Administrator name");email.setPromptText("Administrator email");password.setPromptText("Password (10+ characters)");
        Button create=new Button("Create first administrator"),back=new Button("Back");
        create.setOnAction(e->{try{service.bootstrapAdmin(name.getText().trim(),email.getText().trim(),password.getText());msg("Administrator created","Sign in with the administrator account.");showLogin();}catch(Exception ex){error(ex);}});
        back.setOnAction(e->showLogin());VBox v=new VBox(12,new Label("First-time secure setup"),new Label("This works only while no administrator account exists."),name,email,password,new HBox(8,create,back));v.setPadding(new Insets(25));setScene(v,520,360);
    }

    private javafx.scene.Node adminPanel(){
        VBox v=new VBox(9);TextField name=new TextField(),email=new TextField(),expertise=new TextField();PasswordField password=new PasswordField();
        name.setPromptText("Evaluator full name");email.setPromptText("Evaluator email");password.setPromptText("Temporary password (8+ characters)");expertise.setPromptText("Evaluator expertise");
        Button create=new Button("Create evaluator account");create.setOnAction(e->{try{service.createEvaluator(session,name.getText().trim(),email.getText().trim(),password.getText(),expertise.getText());msg("Created","Evaluator account created. Share credentials securely.");}catch(Exception ex){error(ex);}});
        TextField incubatorName=new TextField(),incubatorCity=new TextField(),incubatorDesc=new TextField();incubatorName.setPromptText("Incubator name");incubatorCity.setPromptText("City");incubatorDesc.setPromptText("Description");
        Button addIncubator=new Button("Create incubator");addIncubator.setOnAction(e->{try{service.createIncubator(session,incubatorName.getText(),incubatorCity.getText(),incubatorDesc.getText());msg("Created","Incubator added.");}catch(Exception ex){error(ex);}});
        TextField incubatorId=new TextField(),programName=new TextField(),duration=new TextField("6");incubatorId.setPromptText("Incubator ID");programName.setPromptText("Programme name");duration.setPromptText("Duration in months");
        Button addProgram=new Button("Create programme");addProgram.setOnAction(e->{try{service.createProgram(session,Long.parseLong(incubatorId.getText()),programName.getText(),Integer.parseInt(duration.getText()));msg("Created","Incubation programme added.");}catch(Exception ex){error(ex);}});
        TableView<String[]> incubatorList=buildStringTable(new String[]{"Incubator ID","Incubator","City"},new int[]{0,1,2},220);Button showIncubators=new Button("Show incubators and IDs");showIncubators.setOnAction(e->{try{incubatorList.getItems().setAll(service.incubators(session));}catch(Exception ex){error(ex);}});
        v.getChildren().addAll(new Label("User administration"),new Label("Create an evaluator account"),name,email,password,expertise,create,new Separator(),new Label("Incubator and programme setup"),incubatorName,incubatorCity,incubatorDesc,addIncubator,showIncubators,incubatorList,incubatorId,programName,duration,addProgram,new Label("Mentor approvals are available in the Mentor network."));return v;
    }

    private void mentorDecision(TextField id,boolean yes){try{service.approveMentor(session,Long.parseLong(id.getText()),yes);msg("Updated","Mentor status updated.");}catch(Exception ex){error(ex);}}
    private void msg(String title,String text){status.setText(session==null?"Ready · "+text:"Signed in · "+session.name()+" · "+session.role()+" · "+text);Alert a=new Alert(Alert.AlertType.INFORMATION,text,ButtonType.OK);a.setHeaderText(title);a.showAndWait();status.setText(session==null?"Ready":("● Signed in as "+session.name()+" / "+session.role()));}
    private void error(Exception ex){
        AppLog.error("UI operation failed",ex);
        String type=ex.getClass().getSimpleName();
        String message=ex.getMessage()==null||ex.getMessage().isBlank()?"No additional message was provided.":ex.getMessage();
        Alert a=new Alert(Alert.AlertType.ERROR);
        a.setTitle("Foundry · Error");
        a.setHeaderText(type+" raised");
        a.setContentText(message);
        java.io.StringWriter trace=new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(trace));
        TextArea details=new TextArea(trace.toString());
        details.setEditable(false);details.setWrapText(false);details.setPrefSize(720,220);
        a.getDialogPane().setExpandableContent(new VBox(6,new Label("Technical details (also written to logs)"),details));
        a.showAndWait();
        status.setText("Last operation failed · "+type+": "+message);
    }
    public static void main(String[] args){launch(args);}
}
