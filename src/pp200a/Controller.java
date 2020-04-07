/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;


import com.opus.syssupport.Profile;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opus.fxsupport.FXFConfirmationDialog;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.FXFHeaderband;
import com.opus.fxsupport.FXFWindowManager;
import com.opus.fxsupport.StatusMessage;
import com.opus.syssupport.Config;
import com.opus.syssupport.PicnoUtils;
import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.SignalListener;
import com.opus.syssupport.VirnaPayload;
import com.opus.syssupport.VirnaServiceProvider;
import com.opus.syssupport.smstate;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Control;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


public class Controller implements SignalListener, VirnaServiceProvider, PropertyChangeListener {

    private static final Logger log = Logger.getLogger(Controller.class.getName());
    
    private Controller.SMThread service_thread;     
    private LinkedBlockingQueue<SMTraffic> smqueue;
    private LinkedHashMap<String, StateDescriptor> statesptr ;
    private final ScheduledExecutorService scheduler;

    private AnaliseDescriptor an;
    private CalibDescriptor cal;
    private CalibDescriptor lastcal;
    
    private boolean anclean = true;
    private boolean calibclean = true;
    private boolean nocalc = false;
    
    public static final Double visco_a0 = 1.8076923000E-06;
    public static final Double visco_a1 = 1.3084615380E-03;

        
    private LinkedHashMap<String,PropertyLinkDescriptor>ana_proplink_uimap;
    private LinkedHashMap<String,PropertyLinkDescriptor>ana_proplink_modelmap;
    
    private LinkedHashMap<String,PropertyLinkDescriptor>cal_proplink_uimap;
    private LinkedHashMap<String,PropertyLinkDescriptor>cal_proplink_modelmap;
    
    private FX1Controller anct;
//    private TopComponent FX1Top;
    private boolean fx1open = false;
    
//    private FX2Controller calct;
//    private TopComponent FX2Top;
//    private boolean fx2open = false;
    
    private FX3Controller dbct;
//    private TopComponent FX3Top;
    private boolean fx3open = false;
    
    private DBService dbservice;
//    private PP100TopComponent top;
    
    private boolean locked = false;
    
    public static final Color RPT_COLOR = new Color(0, 50 , 130);
    
    private static Controller instance; 
    public static Controller getInstance(){
        if (instance == null) {instance = new Controller();}
        return instance;
    }

    
    public Controller() {
        
        smqueue = new LinkedBlockingQueue<>();
        statesptr = new LinkedHashMap<>();
        
        alarms = new LinkedHashMap<>();
        scheduler = Executors.newScheduledThreadPool(5); 
        
        addSignalListener(this);
//        sys_services = Lookup.getDefault().lookup(SystemServicesProvider.class).getDefault();
        
        log.setLevel(Level.FINE);
        instance = this;
    
    }
   
    
    
    // ================================================================ALARMS  =====================================================
    private LinkedHashMap<Integer, AlarmHandle> alarms;
    private static Integer alarmid = 1;
    
    
    public static Integer getAlarmID(){
        return alarmid++;
    }
    
    public boolean hasAlarmSet(Long uid){
        
        for (AlarmHandle al : alarms.values()){
            if (al.handleid == uid) return true; 
        }
        return false;
    }
    
    public void setAlarm (final Long addr, Integer id, final SMTraffic message, long init, long period){
        
        if (alarms.containsKey(id)){
            log.warning("Trying to set an already loaded alarm");
            return;
        }
        
        log.fine(String.format("Setting alarm to %d with id = %d", addr, id));
        
        final Runnable alarm = new Runnable() {
            
            public void run() {
                Controller al_ctrl = Controller.getInstance();
                al_ctrl.notifySignalListeners(addr, message);             
                log.finest(String.format("Alarm to addr %d : %s to %d",
                        addr,
                        message.getState(),
                        message.getHandle()
                )) ; 
            }
        };
        
        final ScheduledFuture<?> alarmhandle = scheduler.scheduleAtFixedRate(alarm, init, period, TimeUnit.MILLISECONDS); 
        
        alarms.put(id, new AlarmHandle(addr, 0l, alarmhandle));
        
    }

    
    private class AlarmHandle{
      
        public Long handleid;
        public Long context;
        
        public int type = 0;
        
        public ScheduledFuture<?> handle;

        public AlarmHandle(Long handleid, Long context, ScheduledFuture<?> handle) {
            this.handleid = handleid;
            this.context = context;
            this.handle = handle;
        }
        
    };
    
    public void removeAlarm(Integer id){
        
        log.info(String.format("Removing alarm id = %d", id));
        AlarmHandle handle = alarms.get(id); 
        
        if (handle != null){
            handle.handle.cancel(true);
            alarms.remove(id);
        }
    }
    
    public void isAlarmSet (Long uid){
   
    }
    
    
    
    
    // ===========SIGNAL HANDLING ===================================================================
        
    /** Estrutura para armazenamento dos listeners do dispositivo*/ 
    //private transient LinkedHashMap<Long,SignalListener> listeners = new LinkedHashMap<>();
    
    private transient ArrayList<SignalListener> listeners = new ArrayList<>();
    
    @Override
    public Long getContext() {
        return -1L;
    }
    
    @Override
    public Long getUID() {
        return -1L;
    }
    
    @Override
    public void processSignal (SMTraffic signal){
        smqueue.offer(signal);
    }
    
     /** Método de registro do listener do dispositivo serial
     * @param l */
    public void addSignalListener (SignalListener l){
        listeners.add(l);
    }

    /** Método de remoção do registro do listener do dispositivo serial
     * @param l */
    public void removeSignalListener (SignalListener l){
        listeners.remove(l);
    }

    /** Esse método é chamado quando algo acontece no dispositivo
     * @param uid_addr
     * @param signal */
    protected void notifySignalListeners(long uid_addr, SMTraffic signal) {

        if (!listeners.isEmpty()){      
            //log.fine("Notifying "+ uid_addr);
            for (SignalListener sl : listeners){
                if (sl.getUID() == uid_addr){
                    sl.processSignal(signal);
                }
            }
        }
    }
    
    
    // ============================================Profile Management ========================================================
    
    private Profile profile;
    private String profiletag = "Default";
    //private ArrayList<String> profile_list = new ArrayList<>();
    private LinkedHashMap<String, String> profile_map = new LinkedHashMap<>();
    private Long logtime = System.currentTimeMillis();
    
    
    private void storeProfileFile(Path p) throws IOException{
        
        GsonBuilder builder = new GsonBuilder(); 
        builder.serializeSpecialFloatingPointValues();
        builder.setPrettyPrinting(); 
        Gson gson = builder.create();
        
        String sjson = gson.toJson(profile);
 
        Files.write(p, sjson.getBytes(StandardCharsets.UTF_8));
        log.info(String.format("Stored Profile on %s", p.toString()));

    }
    
    private void loadProfileList(){
        
        Profile prof;
        Path p;
        byte[] bytes;
        String json_out ; 
  
        try {
            ArrayList<String> profiles = PicnoUtils.scanDir(Config.getInstance().getProfile_dir(), "absolute");
            for (String pfile : profiles){
                p = Paths.get(pfile);
                bytes = Files.readAllBytes(p);
                json_out = new String(bytes, StandardCharsets.UTF_8);
                GsonBuilder builder = new GsonBuilder(); 
                builder.setPrettyPrinting(); 
                Gson gson = builder.create();
                prof = gson.fromJson(json_out, Profile.class);
                profile_map.put(prof.getLabel(), pfile);
            }
            
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
        }
    }
    
    
    public void loadProfile(String userprof){
   
        //loadProfileList();
        if (profile_map.isEmpty()){
            log.info(String.format("Unable to list profiles from : %s -> using default...", Config.getInstance().getProfile_dir()));
            profile = Profile.getInstance();
            profile.createAnaDefault();
            profile.createCalDefault();
            return;
        }
    
        String file = profile_map.get(userprof);
        //String file = Config.getInstance().getProfile_dir()+userprof;
        
        if (file == null){
            file = Config.getInstance().getProfile_dir()+"default";
        }
        Path p = Paths.get(file);
        log.info(String.format("Loading profile from : %s", file));
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(p);
            String json_out = new String(bytes, StandardCharsets.UTF_8);
            GsonBuilder builder = new GsonBuilder(); 
            builder.setPrettyPrinting(); 
            Gson gson = builder.create();
            profile = gson.fromJson(json_out, Profile.class);
            log.info(String.format("Profile loaded from %s", p.toFile().getAbsolutePath()));
            //StatusDisplayer.getDefault().setStatusText(String.format("Perfil foi carregado do arquivo %s", 
            //p.toFile().getAbsolutePath()));
        } catch (Exception ex) {
            log.info(String.format("Unable to load profile from : %s -> using default...", file));
            profile = Profile.getInstance();
            profile.createAnaDefault();
            profile.createCalDefault();
        }
     
        profiletag = profile.getLabel();
        
        if (anct != null){
            log.info(String.format("anct loaded"));
            
            anct.loadProfile();
            smqueue.offer(new SMTraffic(0l, 0l, 0, "NEWANALISE", 
                                   new VirnaPayload().setObject(an)));
            
        }
        else{
            log.info(String.format("anct not loaded")); 
        } 
    }
    
    public Profile getProfile() { return profile;}

    public String getProfiletag() {
        return profiletag;
    }

    public void setProfiletag(String profiletag) {
        this.profiletag = profiletag;
    }

    public ArrayList<String> getProfile_list() {
        return new ArrayList<String>(profile_map.keySet());
    }

    public Long getLogTime() { return logtime;}
  
    
    public void loadCalibration (String fpath){
        
        Path p = Paths.get(Config.getInstance().getExport_dir()+ fpath);
        File f = p.toFile();
        
        if (f.exists()){
            try {
                byte[] bytes = Files.readAllBytes(p);
                String json_out = new String(bytes, StandardCharsets.UTF_8);
                GsonBuilder builder = new GsonBuilder();
                builder.setPrettyPrinting();
                Gson gson = builder.create();
                cal = gson.fromJson(json_out, CalibDescriptor.class);
                log.info(String.format("Calibration indicated by profile was loaded from %s", p.toFile().getAbsolutePath()));
//                StatusDisplayer.getDefault().setStatusText(String.format("Calibração indicada pelo perfil foi carregada do arquivo %s",
//                        p.toFile().getAbsolutePath()));
            } catch (IOException ex) {
                log.info(String.format("Failed to load profile calibration from %s --> using default", p.toFile().getAbsolutePath()));
                cal = new CalibDescriptor();
                cal.loadDefaults();
                //storeCalibFile(p);
            }
        }
    }
    
    public void loadLastCalibration(){
        
        Long lastmod = 0L;
        Path p = Paths.get(Config.getInstance().getExport_dir()+"cal-default.json");
        //BasicFileAttributes attr;
        
        try {
            ArrayList<String> calfiles = PicnoUtils.scanDir(Config.getInstance().getExport_dir(), "cal-");
            for (String scal : calfiles){
                File f = new File(Config.getInstance().getExport_dir()+scal);
                long lmod = f.lastModified();
                //Path p1 = f.getAbsoluteFile().toPath();
                //attr = Files.readAttributes(p1, BasicFileAttributes.class);
                //FileTime attrcreated = attr.creationTime();
                if (lmod > lastmod){
                    lastmod = f.lastModified();
                    p = f.toPath();
                }
            }
            byte[] bytes = Files.readAllBytes(p);
            String json_out = new String(bytes, StandardCharsets.UTF_8);
            GsonBuilder builder = new GsonBuilder(); 
            builder.setPrettyPrinting(); 
            Gson gson = builder.create();
            lastcal = gson.fromJson(json_out, CalibDescriptor.class);
            log.info(String.format("Last Calibration loaded from %s", p.toFile().getAbsolutePath()));
//            StatusDisplayer.getDefault().setStatusText(String.format("Ultima Calibração foi carregada do arquivo %s", 
//            p.toFile().getAbsolutePath()));
            
        } catch (Exception ex) {
            log.info(String.format("Failed to load last calibration from %s --> using default", p.toFile().getAbsolutePath()));
            lastcal = new CalibDescriptor();
            lastcal.loadDefaults();
            //storeCalibFile(p);
        }
        
    }
    
    
    
    // ==================================================== Properties management =============================================
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        if (source == an) {
            //log.info(String.format("AnaliseDescriptor changed : %s", e.getPropertyName()));
            anclean = false;
        }
       
    }
    
    
    public void varCallback(Control control, String value, boolean validated, boolean analise){
        
        Method m;
        PropertyLinkDescriptor pld;
        
        if (analise){
            pld = getLinkDescriptor((FXFField)control, ana_proplink_uimap);
        }
        else{
            pld = getLinkDescriptor((FXFField)control, cal_proplink_uimap);
        }
        
        if (pld != null){
            pld.setAuxiliar("");
            if (!validated){
                //log.info(String.format("Var callback said %d is invalid", control.hashCode()));
                if (pld.isValid()){
                    pld.setValid(false);
                    String callback = pld.getCallstate();
                    if (!callback.equals("NONE") && !nocalc){
                        smqueue.offer(new SMTraffic(0l, 0l, 0, callback, 
                                   new VirnaPayload().setObject(pld)));
                    }
                }
            }
            else{
                m = pld.getMethod();
                try {
                    if (m != null){
                        Boolean ret = (Boolean)m.invoke(pld.getInstance(), value);
                    }
                    pld.setValid(true);
                    String callback = pld.getCallstate();
                    if (!callback.equals("NONE") && !nocalc){
                        smqueue.offer(new SMTraffic(0l, 0l, 0, callback, 
                                   new VirnaPayload().setObject(pld)));
                    }
                    //log.info(String.format("var callback %d approved value %s", control.hashCode(), value));
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    log.warning(String.format("Failed to update property due %s", ex.getMessage()));
                }
            }
        }
  
    }
    
    public void newAnalise(){      
        smqueue.offer(new SMTraffic(0l, 0l, 0, "NEWANALISE", 
                                   new VirnaPayload().setObject(an)));
    }
    
    public void newCalib(){      
        smqueue.offer(new SMTraffic(0l, 0l, 0, "NEWCALIB", 
                                   new VirnaPayload().setObject(cal)));
    }
    
    
    public void reportAnalise(){
//        smqueue.offer(new SMTraffic(0l, 0l, 0, "LOADREPORTDESCRIPTOR", 
//                                new VirnaPayload()
//                                .setObject(an)
//                                .setCaller(this)
//                                .setCallerstate("")
//        )); 
        
        smqueue.offer(new SMTraffic(0l, 0l, 0, "REPORTANALISE", 
                                new VirnaPayload()
                                .setObject(an)
                                .setCaller(this)
                                .setCallerstate("")
        ));  
        
    }
    
    public void reportCalibration(){
        
        smqueue.offer(new SMTraffic(0l, 0l, 0, "REPORTCALIBRATION", 
                                new VirnaPayload()
                                .setObject(cal)
                                .setCaller(this)
                                .setCallerstate("")
        )); 
        
    }
    
    public void storeCalibration(){   
        
        if (cal == null){
            cal = new CalibDescriptor();
            cal.addPropertyChangeListener(this);
        }

        smqueue.offer(new SMTraffic(0l, 0l, 0, "STORECALIBFILE", 
                                    new VirnaPayload()
                                    .setObject(cal)
                                    .setCaller(this)
                                    .setCallerstate("")
        ));  
        
    }
    
    
    public void storeAnalise(){        
        if (!locked){
            
            if (an == null){
                an = new AnaliseDescriptor();
                an.addPropertyChangeListener(this);
            }
            
            smqueue.offer(new SMTraffic(0l, 0l, 0, "STOREANALISEFILE", 
                                        new VirnaPayload()
                                        .setObject(an)
                                        .setCaller(this)
                                        .setCallerstate("")
            ));        
            smqueue.offer(new SMTraffic(0l, 0l, 0, "STOREANALISERECORD", 
                                        new VirnaPayload()
                                        .setObject(an)
                                        .setCaller(this)
                                        .setCallerstate("")
            ));
        }
    }
    
    public void loadFile(){
        smqueue.offer(new SMTraffic(0l, 0l, 0, "LOADFILE", 
                                new VirnaPayload()
                                .setObject(an)
                                .setCaller(this)
                                .setCallerstate("")
        ));    
    }
    
    public void loadRecord(AnaliseDescriptor andesc){
       
        
//        if (fx1open){
//            an.setBlaine(andesc.getBlaine());
//            an.setCalibid(andesc.getCalibid());
//            an.setDensidade(andesc.getDensidade());
//            an.setKfactor(andesc.getKfactor());
//            an.setLote(andesc.getLote());
//            an.setMassa_ensaio(andesc.getMassa_ensaio());
//            an.setMedia(andesc.getMedia());
//            an.setNotas(andesc.getNotas());
//            an.setPorosidade(andesc.getPorosidade());
//            an.setProfile(andesc.getProfile());
//            an.setRsd(andesc.getRsd());
//            an.setSid(andesc.getSid());
//            an.setTemperature(andesc.getTemperature());
//
//            an.setTimestamp(andesc.getTimestamp());
//            an.setUid(andesc.getUid());
//            an.setUser(andesc.getUser());
//            an.setVolume_camada(andesc.getVolume_camada());
//
//            ArrayList<String>tempos = an.getTempos();
//            tempos.clear();
////            for (String t : andesc.getTempos()){
////                tempos.add(t);
////            }
//            
//            updateAnaliseCanvas();
//            anclean = true;
//
//            smqueue.offer(new SMTraffic(0l, 0l, 0, "LOCKUI", 
//                                       new VirnaPayload()
//                                               .setInt1(0)
//                                               .setInt2(0)
//            ));
//            
//        }
//        else{
//            NotifyDescriptor nd = new NotifyDescriptor.Message(
//                "<html>OOPS ! A tela de Analises não está aberta, então ... : "
//                    + "<ul>"
//                    + "<li>Vá ao painel de comando (ative o tab : ACP PP100).</li>"
//                    + "<li>Ative a tela de analises via botão ANALISES. </li>"
//                    + "<li>Volte aqui e repita a requisição que voce deseja.</li>"
//                    + "</ul>"
//                + "</html>", 
//            NotifyDescriptor.ERROR_MESSAGE);
//            Object retval = DialogDisplayer.getDefault().notify(nd);   
//        }
        
   
    }
    
    
    
    
    // ===================================================== Controllers ===================================================
    
    
    public void doLogout(){
        log.info("Doing logout...");
    }
    
    
    public void setFXANController (FX1Controller controller){
        this.anct = controller;      
        ana_proplink_uimap = new LinkedHashMap<>();
        ana_proplink_modelmap = new LinkedHashMap<>();
        mapProperties(an, true);
    }
    
//    public void setFX1Top (TopComponent top) {FX1Top = top;}
//    
//    public void setFX2Top (TopComponent top) {FX2Top = top;}
//    
//    public void setPP100Top (PP100TopComponent top) {this.top = top;}
//    
//    public void setFX3Top (FX3TopComponent top) {this.FX3Top = top;}
    
    public boolean isFX1open() { return fx1open;}
    public void setFX1open (boolean open) { fx1open = open;}
//    public boolean isFX2open() { return fx2open;}
//    public void setFX2open (boolean open) { fx2open = open;}
//    public boolean isFX3open() { return fx3open;}
//    public void setFX3open (boolean open) { fx3open = open;}
//    
    
//    public void setFXCalibController (FX2Controller controller){
//        this.calct = controller;
//        cal_proplink_uimap = new LinkedHashMap<>();
//        cal_proplink_modelmap = new LinkedHashMap<>();
//        mapProperties(cal, false);
//    }
    
    
    public void setFXDBController (FX3Controller controller){
        this.dbct = controller;
//        cal_proplink_uimap = new LinkedHashMap<>();
//        cal_proplink_modelmap = new LinkedHashMap<>();
//        mapProperties(cal, false);
    }
    
    
    public LinkedHashMap<String,PropertyLinkDescriptor> getAnaUIMap() { return ana_proplink_uimap; }
    public LinkedHashMap<String,PropertyLinkDescriptor> getCalUIMap() { return cal_proplink_uimap; }
    
    public LinkedHashMap<String,PropertyLinkDescriptor> getAnaModelMap() { return ana_proplink_modelmap; }
    public LinkedHashMap<String,PropertyLinkDescriptor> getCalModelMap() { return cal_proplink_modelmap; }
    
    
    public PropertyLinkDescriptor getLinkDescriptor(FXFField field, LinkedHashMap<String,PropertyLinkDescriptor> descriptor) {
        
        //for (PropertyLinkDescriptor pld : ana_proplink_uimap.values()){
        for (PropertyLinkDescriptor pld : descriptor.values()){
            FXFField dfield = pld.getFxfield();
            if (dfield != null && dfield.equals(field)) return pld;
        } 
        
        return null;
    }
    
    public LinkedBlockingQueue<SMTraffic> getQueue(){ return smqueue;}
    
    
    public void updateUIWidget (String key, FXFField field, boolean analise){
        
        PropertyLinkDescriptor pld1, pld2;
        
        if (analise){
            pld1 = ana_proplink_uimap.get(key);
        }
        else{
            pld1 = cal_proplink_uimap.get(key);
        }
        
        if (pld1 != null){
            pld1.setFxfield(field);
            if (analise){
                pld2 = ana_proplink_modelmap.get(key);
            }
            else{
                pld2 = cal_proplink_modelmap.get(key);
            }
            
            if (pld2 != null){
                pld2.setFxfield(field);
            }
        }
    }
    
    
    
    public void mapProperties(Object bean, boolean analise){
     
        Class<?> c = bean.getClass();     
        propertylink plink;
       
        PropertyLinkDescriptor linkdesc;
       
        for (Method mt : c.getDeclaredMethods() ){
            plink = mt.getAnnotation(propertylink.class);
            if (plink != null){
   
                log.info(String.format(" %s Property %s has link = %s  -- input=%s  -- callback=%s", 
                        analise ? "Analise" : "Calibration",
                        plink.propname(),
                        plink.plink(), 
                        plink.input(),
                        plink.callstate()));
    
                linkdesc = new PropertyLinkDescriptor().setClazz(c)
                                                .setInstance(bean)
                                                .setMethod(mt)
                                                .setPropname(plink.propname())
                                                .setInput(plink.input())
                                                .setPlink(plink.plink())
                                                .setCallstate(plink.callstate())
                                                ;
                if (analise){
                    ana_proplink_uimap.put(plink.plink(), linkdesc);
                    ana_proplink_modelmap.put(plink.propname(), linkdesc);
                }
                else{
                    cal_proplink_uimap.put(plink.plink(), linkdesc);
                    cal_proplink_modelmap.put(plink.propname(), linkdesc);
                }
            }
        }
        
        //log.info("Prop map done !");
        
    }
    
    
    
    
    
    // ======================================== STATE MACHINE ======================================================================
    public void stopService(){
        //services.removeUsbServicesListener(this);
        service_thread.setDone(true);    
    }
    
    public void startService(){      
        smqueue = new LinkedBlockingQueue<>() ;
        
        if (service_thread != null && service_thread.isAlive()){
            log.info("The Controller thread is active, no need to wakeup her");
            return;
        }
        
        service_thread = new Controller.SMThread(smqueue, statesptr, this);
        
        
        Class<?> c; 
        
        smstate annot;
        StateDescriptor stdesc;
 
        c = this.getClass();     
        for (Method mt : c.getDeclaredMethods() ){
            annot = mt.getAnnotation(smstate.class);
            if (annot != null){
                stdesc =   new StateDescriptor().setClazz(c)
                                                .setInstance(this)
                                                .setMethod(mt)
                                                .setSID(annot.state());
                statesptr.put(annot.state(), stdesc);
            }
        }
        
        
        // Loading FX1
        c = FX1SMachine.getInstance().getClass();
        for (Method mt : c.getDeclaredMethods()){
            annot = mt.getAnnotation(smstate.class);
            if (annot != null){
                stdesc =   new StateDescriptor().setClazz(c)
                                                .setInstance(FX1SMachine.getInstance())
                                                .setMethod(mt)
                                                .setSID(annot.state());
                statesptr.put(annot.state(), stdesc);
            }
        }
        
        
        
        
        an = new AnaliseDescriptor();
        cal = new CalibDescriptor();
        
        an.addPropertyChangeListener(this);
        cal.addPropertyChangeListener(this);
                
        anclean = true;

        new Thread(service_thread).start();
    }
    
    
    private class SMThread extends Thread {
    
        
        private String state;
        private ArrayDeque <String>states_stack;
        private LinkedHashMap<String, StateDescriptor> statesptr;
        
        private boolean done;
        protected BlockingQueue<SMTraffic> tqueue;
        private String cmd;
        
        private Controller parent;
        private SMTraffic smm;
        private VirnaPayload payload;
        
        private Method m;
        StateDescriptor stdesc;
      
        protected long start_tick =  System.currentTimeMillis();
 

        public SMThread(BlockingQueue<SMTraffic> tqueue, LinkedHashMap<String, StateDescriptor> _statesptr, Controller parent ) {
            
            this.parent = parent;
            this.tqueue = tqueue;
            states_stack = new ArrayDeque<>();
            this.statesptr = _statesptr;
            
            states_stack.push("RESET");
            setDone(false);
        }
        
        
        public boolean idleHook (){
            
            smm = tqueue.poll();                         
            if (smm != null){
                cmd = smm.getCommand();
                if (cmd.equals("LOADSTATE")){
                    //state = smm.getState();
                    pushState (smm.getState(), smm);
                }
            }
            else{
                try {
                    //System.out.print('.');
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    //Exceptions.printStackTrace(ex);
                }
            }   
            
            return true;
        }

       
        public void pushState (String SID, SMTraffic payload){
            
            if (statesptr.get(SID) == null) return;
            
            StateDescriptor sd = statesptr.get(SID);
            if (sd != null){
                if (payload !=null){
                    sd.setContext(payload);
                }
            }
            //new SMTraffic( 0l, 0l, 0, SID, new VirnaPayload())
            states_stack.push(SID);
            
        }
        
        @Override
        public void run(){
   
            //log.log(Level.FINE, "Iniciando Thread de Serviços principal");
            states_stack.clear();
            states_stack.push("RESET");
            setDone(false);
           
            try {
                while (!done){
                    
                    if (states_stack.isEmpty()){
                       state = "IDLE"; 
                    }
                    else{
                       state = states_stack.pop();
                    }
                    
                    if (state.equals("IDLE")){
                        idleHook();
                    }
                    else{
                        stdesc = statesptr.get(state);
                        if (state != null){
                            m = stdesc.getMethod();
                            smm = stdesc.getContext();
                            //log.log(Level.INFO, String.format("Activating state %s @ %d", state, System.currentTimeMillis()-start_tick));
                            Boolean ret = (Boolean)m.invoke(stdesc.getInstance(), smm);
                            if (!ret){
                                log.log(Level.WARNING, String.format("State: %s failed @ %d", state, System.currentTimeMillis()-start_tick));
                            }
                        }
                    }
                    
                    
                }
            } catch (Exception ex) {
                log.log(Level.SEVERE,String.format("Controller State Machine failed with %s @ state %s", ex.getMessage(), state));
                
//                ImageIcon image = ImageUtilities.loadImageIcon("com/opus/pp100/db-schema-icon.png", true);
//        
//                Notification noti = NotificationDisplayer.getDefault().notify(
//                    "Kernel Controlador da Virna 3 detectou anormalidade",
//                    image,
//                    String.format("Falha na máquina de estados em : %s", state),
//                    Lookups.forPath("NotificationActions").lookup(ActionListener.class)
//                );
                System.exit(0);
                //startService();
                
            }

        }

        public void setDone(boolean done) {
            if (done) log.log(Level.FINE, "Controller is Stopping Service");
            this.done = done;
        }
            
    };
    
    
    
    // =========================================================================================================================
    // ========================================   STATES  ======================================================================
    // =========================================================================================================================
    
   
    @smstate (state = "RESET")
    public boolean st_reset(SMTraffic smm){
        
        //log.log(Level.INFO, String.format("RESET state activated with payload : %s", smm.getPayload().vstring));
        service_thread.states_stack.push("IDLE");
        service_thread.states_stack.push("CONFIG");
        service_thread.states_stack.push("INIT");
        return true;
    }
    
    @smstate (state = "INIT")
    public boolean st_init(SMTraffic smm){
        //log.log(Level.INFO, String.format("INIT state activated with payload : %s", smm.getPayload().vstring));
        return true;
    }
    
    @smstate (state = "CONFIG")
    public boolean st_config(SMTraffic smm){
      
        // Para que isso mesmo ?
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        
        dbservice = DBService.getInstance();
        dbservice.setControler(this);
        
        FXFWindowManager wm = FXFWindowManager.getInstance();
     
        SMTraffic alarm_config = new SMTraffic(0l, 0l, 0, "HOUSEKEEP", 
                        new VirnaPayload()
                );
        setAlarm (-1l, -1, alarm_config, 500l, 500l);

        smqueue.offer(new SMTraffic(0l, 0l, 0, "FX1TEST", 
                                   new VirnaPayload()
                                           .setString("Called from Controller")
        ));
        
        
//        loadProfileList();
//        loadProfile(PicnoUtils.user.getProfile());
//        logtime = System.currentTimeMillis();
//        loadLastCalibration();
        
//        top.enableTasks(PicnoUtils.user.isMaycalibrate(), 
//                        PicnoUtils.user.isMay_search());
        
        
        //loadProfile(PicnoUtils.user.getProfile());
          
        //log.log(Level.INFO, String.format("Config state activated with payload : %s", smm.getPayload().vstring));
//        VirnaPayload vpl = new VirnaPayload()
//                .setString("/Bascon/ASVP/Quantawin/sample_a (Isotherm).txt")
//                .setObject(this)
//                .setObjectType(this.getClass().getCanonicalName());
//        
//        SMTraffic importcall = new SMTraffic(0l, 0l, 0, "IMPORTISOTHERM", vpl);
        
        return true;
    }
    
    
    @smstate (state = "ENABLESU")
    public boolean st_enableSU(SMTraffic smm){
        VirnaPayload payload = smm.getPayload();
        String user = payload.vstring;
        FXFWindowManager wm = FXFWindowManager.getInstance();
        FXFHeaderband hb = wm.getHeaderBand();
        
        if (payload.getFlag2()){
            sudotimeout = 0;
            removeAlarm(sudoalarmid);
            PicnoUtils.sudo_enabled = false;
            hb.updateAvatar(false);
            log.info(String.format("Controller removed SU prev. from  %s", user));
            smqueue.offer(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", 
                    new VirnaPayload().setString(
                                "Gerente de Administração&" + "INFO&" +
                                String.format("Previlégios de Administrador foram removidos do usuário %s&", user) +
                                "O usuário deverá autenticar-se novamente"        
            )));
        }
        else{
            if (payload.getFlag1()){
                sudotimeout = 30;
                SMTraffic alarm_config = new SMTraffic(0l, 0l, 0, "SUDOTIMEOUT", 
                        new VirnaPayload().setString(user)
                );
                sudoalarmid = getAlarmID();
                setAlarm (-1l, sudoalarmid, alarm_config, 5000l, 5000l);
                log.info(String.format("Controller granted SU prev. to %s", user));
                smqueue.offer(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", 
                        new VirnaPayload().setString(
                                    "Gerente de Administração&" + "INFO&" +
                                    String.format("Previlégios de Administrador foram concedidos ao usuário %s&", user) +
                                    "O período de inatividade é de 150 seg."        
                )));
            }
            else{
                log.info(String.format("Wrong SU password on user %s", user));
                smqueue.offer(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", 
                        new VirnaPayload().setString(
                                    "Gerente de Administração&" + "SEVERE&" +
                                    String.format("Houve uma tentativa frustrada do usuário %s em acessar previlégios de administrador&", user) +
                                    "Esse episódio será informado ao administrador do sistema."        
                )));
                sudotimeout = 0;
                sudoalarmid = -1;
                PicnoUtils.sudo_enabled = false;
            } 
        }
        
        hb.updateAvatar(PicnoUtils.sudo_enabled);
        return true;
    }
    
    
    Integer sudoalarmid = -1;
    int sudotimeout = 0;
    @smstate (state = "SUDOTIMEOUT")
    public boolean st_doSUDOTIMEOUT(SMTraffic smm){
        //payload = smm.getPayload();
        sudotimeout--;
        if (sudotimeout <= 0){
            FXFWindowManager wm = FXFWindowManager.getInstance();
            FXFHeaderband hb = wm.getHeaderBand();
            PicnoUtils.sudo_enabled = false;
            hb.updateAvatar(false);
            String user = smm.getPayload().vstring;
            sudotimeout = 0;
            removeAlarm(sudoalarmid);
            log.info(String.format("Timeout on SUDO previleges to %s", user));
            smqueue.offer(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", 
                    new VirnaPayload().setString(
                                "Gerente de Administração&" + "INFO&" +
                                String.format("Previlegios de Administrador foram removidos do usuário %s devido a inatividade&", user) +
                                "O usuário deverá autenticar-se novamente"        
            )));
        }
        
        Long ts = System.currentTimeMillis();
        log.info(String.format("SUDO Timeout loop %03d @ %d", sudotimeout,  ((ts % 1000000)) ));
        return true;
    }
    
    
    
    @smstate (state = "FLAGACTIVITY")
    public boolean st_flagActivity(SMTraffic smm){

        VirnaPayload payload = smm.getPayload();
        
        //log.info(String.format("Activity @ %s", smm.getPayload().vstring ));
        if (PicnoUtils.sudo_enabled){
            sudotimeout = 30;
        }
        return true;
    }
    
    
    
    private int housekeep_loop = 0;
    private PriorityQueue<StatusMessage> statusmessages = new PriorityQueue<>();
    private StatusMessage currentstatus;
    
    @smstate (state = "HOUSEKEEP")
    public boolean st_doHousekeep(SMTraffic smm){

        //VirnaPayload payload = smm.getPayload();
        
        //log.info(String.format("Housekeep loop %03d @ %d", housekeep_loop++, ((System.currentTimeMillis() % 1000000))));
        if (currentstatus != null){
            if (System.currentTimeMillis() > currentstatus.getkeepAlive()){
                FXFHeaderband hb = FXFWindowManager.getInstance().getHeaderBand();
                hb.updateStatus("");
                currentstatus = null;
            }
        }
        if (!statusmessages.isEmpty()){
            StatusMessage sm = statusmessages.peek();
            if ((currentstatus == null) || sm.getPriority() > currentstatus.getPriority()){
                currentstatus = statusmessages.poll();
                currentstatus.setkeepAlive(System.currentTimeMillis() + (currentstatus.getPriority()));
                FXFHeaderband hb = FXFWindowManager.getInstance().getHeaderBand();
                hb.updateStatus(currentstatus.getMessage());
            }
        }
        
        return true;
        
    }
    
    
    @smstate (state = "UPDATESTATUS")
    public boolean st_updateStatus(SMTraffic smm){

        VirnaPayload payload = smm.getPayload();
        StatusMessage sm = (StatusMessage)payload.vobject;
        
        log.info(String.format("Status = [%d]%s", sm.getPriority(), sm.getMessage()));
        statusmessages.add(sm);
        
        return true;
    }
    
    
    
    
    @smstate (state = "NAV_ACTION")
    public boolean st_navAction(SMTraffic smm){
        
        VirnaPayload payload = smm.getPayload();
        String cmd = payload.vstring;
        log.info(String.format("Nav Action = %s", cmd));
        FXFWindowManager wm = FXFWindowManager.getInstance();
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
              try {
                  if (cmd.equals("Back")){
                      wm.activateWindow("FX4");
                  }
                  else if (cmd.equals("List")){
                      wm.activateWindow("FX3");
                  }
                  else{
                      wm.activateWindow("CANVAS");
                  }
              } catch (Exception ex) {
                  Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, "Failed to load window due : ", ex.getMessage());
              }
            }    
        });   
        
        return true;
        
    }
    
    @smstate (state = "REMOVE_NOTIFICATION")
    public boolean st_removeNotification(SMTraffic smm){
        
        VirnaPayload payload = smm.getPayload();
        TextFlow tf = (TextFlow)payload.vobject;
        
        if (tf != null){
            FXFWindowManager wm = FXFWindowManager.getInstance();
            ArrayList<TextFlow> notifs = wm.getNotifications();
            notifs.remove(tf);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                   //FXFWindowManager wm = FXFWindowManager.getInstance();
                   wm.getHeaderBand().updateNotificationIcon();
                }
            });
        }
        return true;
    }
    
    @smstate (state = "ADD_NOTIFICATION")
    public boolean st_addNotification(SMTraffic smm){
        
        VirnaPayload payload = smm.getPayload();
        String mes = payload.vstring;
        
        String[] tokens = mes.split("&");
        
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
               FXFWindowManager wm = FXFWindowManager.getInstance();
               wm.addNotification(tokens[0], tokens[1], tokens[2], tokens[3]);
            }
        });
        
        
        return true;
    }
    
    
    
    
    public void updateCalibCanvas(){
        
        nocalc=true;
        
        
//        calct.updateField ("it_densidade", cal.getDensidade(), true);
//        calct.updateField ("it_porosidade", cal.getPorosidade(), true);
//        calct.updateField ("it_volume", cal.getVolume_camada(), false);
//        
//        calct.updateField ("it_analiseaverage", cal.getMedia(), false);
//        calct.updateField ("it_analisersd", cal.getRsd(), false);
//        
//        
//        calct.updateField ("it_sid", cal.getSid(), false);
//        calct.updateField ("it_filtro", cal.getFiltro(), false);
//        calct.updateField ("it_notas", cal.getNotas(), false);
//        
//        
//        calct.updateField ("it_kfactor", cal.getKfactor(), false);
//        calct.updateField ("it_mass", cal.getMassa_ensaio(), true);
//        
//        calct.updateField ("it_area", cal.getArea(), false);
//        calct.updateField ("it_temperature", cal.getTemperature(), false);
//        
//        calct.updateAnaliseTime(cal.getTimestamp());
//        
//        calct.initTimeList();
//        for (String atime : cal.getTempos()){
//            calct.addTimeEntry(atime);
//        }
        
        nocalc = false;  
    }
    
    
    
    @smstate (state = "NEWCALIB")
    public boolean st_newCalib(SMTraffic smm){
        
        log.info("New Calib called ...");

        cal.loadDefaults();
        
        updateCalibCanvas();
        calibclean = true;

        smqueue.offer(new SMTraffic(0l, 0l, 0, "LOCKUI", 
                                   new VirnaPayload()
                                           .setInt1(0)
                                           .setInt2(1)
        ));

        return true; 
        
    }
    
    
    public void updateAnaliseCanvas(){
        
        nocalc=true;
        
        anct.updateField ("it_densidade", an.getDensidade(), true);
        anct.updateField ("it_porosidade", an.getPorosidade(), true);
        anct.updateField ("it_massa_calculada", an.getMassa_ensaio(), true);
        
        anct.updateField ("it_analiseaverage", an.getMedia(), false);
        anct.updateField ("it_analisersd", an.getRsd(), false);
        anct.updateField ("it_blaineresult", an.getBlaine(), false);
        
        anct.updateField ("it_sid", an.getSid(), false);
        anct.updateField ("it_lote", an.getLote(), false);
        anct.updateField ("it_notas", an.getNotas(), false);
        
        String lbc = "calib";
        if (an.getCalibid().equals("last")){
            lbc = String.format("%1$s/%2$s em %3$td-%3$tm-%3$tY %3$tH:%3$tM:%3$tS",
                    lastcal.getProfile(),
                    lastcal.getSid(),
                    lastcal.getTimestamp()
            );
        }
        else{
            lbc = String.format("%1$s/%2$s em %3$td-%3$tm-%3$tY %3$tH:%3$tM:%3$tS",
                    cal.getProfile(),
                    cal.getSid(),
                    cal.getTimestamp()
            );
        }
        anct.updateField ("it_calibfile", lbc, false);
        
        anct.updateField ("it_constantek", an.getKfactor(), false);
        anct.updateField ("it_layervolume", an.getVolume_camada(), false);
        anct.updateField ("it_temperature", an.getTemperature(), false);
        
        anct.updateAnaliseTime(an.getTimestamp());
        
        anct.initTimeList();
        for (String atime : an.getTempos()){
            anct.addTimeEntry(atime);
        }
        
        nocalc = false;  
    }
    
    
    private void loadAnaliseDefaults(){
        
        an.loadDefaults();
        
        an.setDensidade(profile.getAnaDescriptor("it_densidade").getDefault_value());
        an.setPorosidade(profile.getAnaDescriptor("it_porosidade").getDefault_value());
        an.setMassa_ensaio(profile.getAnaDescriptor("it_massa_calculada").getDefault_value());
   
        an.getTempos().clear();
        an.setMedia("");
        an.setRsd("");
        an.setBlaine("");
       
        an.setSid(profile.getAnaDescriptor("it_sid").getDefault_value());
        an.setLote(profile.getAnaDescriptor("it_lote").getDefault_value());
        an.setNotas(profile.getAnaDescriptor("it_notas").getDefault_value());

        String calibid = profile.getAnaDescriptor("it_calibfile").getDefault_value();
        
        if (calibid.equals("last")){
            cal = lastcal;
        }
        else{
            loadCalibration (Config.getInstance().getExport_dir()+calibid);
        }
        
        an.setCalibid(profile.getAnaDescriptor("it_calibfile").getDefault_value());
        an.setKfactor(cal.getKfactor());
        an.setVolume_camada(cal.getVolume_camada());
        an.setTemperature(cal.getTemperature());
      
    }
    
    @smstate (state = "NEWANALISE")
    public boolean st_newAnalise(SMTraffic smm){
        
        log.info("New Analise called ...");

        loadAnaliseDefaults();
        updateAnaliseCanvas();
        anclean = true;
 
        smqueue.offer(new SMTraffic(0l, 0l, 0, "LOCKUI", 
                                   new VirnaPayload()
                                           .setInt1(0)
                                           .setInt2(0)
        ));
   
        return true;
    }
    
    
    @smstate (state = "CALCULATETIME")
    public boolean st_calculateTime(SMTraffic smm){
        
        //log.info("Calculating new analysis time");
        ArrayList<Double> values = (ArrayList<Double>)smm.getPayload().vobject;
        
        
        
        int num = 0;
        double average = 0.0;
        double dif = 0.0;
        double rsd = 0.0;
        
        // Calculate average
        if (values.size() > 1){
            for (Double ivl : values){
                if (ivl != 0.0){
                    average += ivl;
                    num++;
                }
            }
            average = average / num;
            // Calculate RSD
            for (double vl : values){
                if (vl != 0.0){
                    dif += Math.pow(vl-average, 2);
                }    
            }
            dif = dif / num;     
            Double rawrsd = Math.sqrt(dif);
            rsd = rawrsd;
            rsd = (rsd /average) * 100;
            if (rsd == 0.0) rsd = 0.001;
            
        }
        else if (values.size() == 0){
            average = 0.0;
            rsd = 0.0;
        }
        else{
            average = values.get(0);
            rsd = 0.0;
        }
    
        if (smm.getPayload().getFlag1() == true){
//            calct.updateField ("it_analiseaverage", String.format(Locale.US, "%4.1f", average), true);
//            calct.updateField ("it_analisersd", String.format(Locale.US, "%5.3f", rsd), true);
        }
        else{
            anct.updateField ("it_analiseaverage", String.format(Locale.US, "%4.1f", average), true);
            anct.updateField ("it_analisersd", String.format(Locale.US, "%5.3f", rsd), true);
        }
        
        
        return true;
    }
    
    
    @smstate (state = "UPDATETIME")
    public boolean st_updateTime(SMTraffic smm){
        
        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
        
        if (pld != null){
            String mode = pld.getAuxiliar();
            if (mode != null && mode.equals("STOPENTER")){
                //log.info("Input Time called called with STOPENTER");
                anct.addTimeEntry(pld.getFxfield().getValue());
                an.getTempos().add(pld.getFxfield().getValue());
            }
            else{
                //log.info(String.format("ANATIME called with %s ", pld.isValid() ? "valid":"invalid"));
            }
        }
        
        return true;
    }
    
    @smstate (state = "UPDATECALTIME")
    public boolean st_updatCalTime(SMTraffic smm){
        
        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
        
        if (pld != null){
            String mode = pld.getAuxiliar();
            if (mode != null && mode.equals("STOPENTER")){
                //log.info("Input Time called called with STOPENTER");
                //calct.addTimeEntry(pld.getFxfield().getValue());
                cal.getTempos().add(pld.getFxfield().getValue());
            }
            else{
                //log.info(String.format("CALTIME called with %s ", pld.isValid() ? "valid":"invalid"));
            }
        }
        
        return true;
    }
    
    
    
    @smstate (state = "ANALISEDONE")
    public boolean st_analiseDone(SMTraffic smm){
 
        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
        
        if (pld!= null){
            if (!pld.isValid()){
                anct.updateField ("it_blaineresult", "", false);
                smqueue.offer(new SMTraffic(0l, 0l, 0, "LOCKUI", 
                                   new VirnaPayload()
                                           .setInt1(1)
                                           .setInt2(0)
                ));
                return true;
            }
        }
 
        smqueue.offer(new SMTraffic(0l, 0l, 0, "LOCKUI", 
                                   new VirnaPayload()
                                           .setInt1(0)
                                           .setInt2(0)
        
        ));
        return true;
        
    }
    
    @smstate (state = "CALCKFACTOR")
    public boolean st_calcKFactor(SMTraffic smm){
        
        //Double viscosity = 0.001348;
        
        Double viscosity = 1.349;
        
        Double kfactor;
        Double mass;
        Double temperature;
        
        Double layervol;
        
        Double porosity;
        Double density;
        Double area;
        
        Double time;
        Double rsd;
        
        double top, bottom;
        
        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
        
        //log.info("Calc kfactor called.......................");
        
        if (pld!= null){
            if (!pld.isValid()){
                //calct.updateField ("it_kfactor", "", false);
                return true;
            }
        }
        
        porosity = getDoublePValue("porosidade", cal.getPorosidade(), false);
        if (porosity.isNaN()){
            //calct.updateField ("it_kfactor", "", false);
            return true;
        }
        
        density  = getDoublePValue("densidade", cal.getDensidade(), false);
        if (density.isNaN()){
            //calct.updateField ("it_kfactor", "", false);
            return true;
        }
        
        
        layervol  = getDoublePValue("volume_camada", cal.getVolume_camada(), false);
        if (layervol.isNaN()){
            //calct.updateField ("it_kfactor", "", false);
            return true;
        }
        
        
        
        
        area  = getDoublePValue("area", cal.getArea(), false);
        if (area.isNaN()){
            //calct.updateField ("it_kfactor", "", false);
            return true;
        }
       
        temperature  = getDoublePValue("temperature", cal.getTemperature(), false);
        if (temperature.isNaN()){
            //calct.updateField ("it_kfactor", "", false);
            return true;
        }
        else{
            viscosity = getVisco(temperature);
        }
        
        
        time  = getDoublePValue("media", cal.getMedia(), false);
        if (time.isNaN()){
            //calct.updateField ("it_kfactor", "", false);
            return true;
        }
        
        rsd  = getDoublePValue("rsd", cal.getRsd(), false);
        if (rsd.isNaN()){
            //calct.updateField ("it_kfactor", "", false);
            return true;
        }
        
        
        top = (1 - porosity) * viscosity;
        bottom = Math.sqrt(porosity * porosity * porosity) * Math.sqrt(time) ;
        
        kfactor = area*density*(top/bottom);
        mass = (1 - porosity) * (density * layervol);
        
        
        Double kfactor1 = 1.414 * area * density * (viscosity/ Math.sqrt(time));
        
        String skfactor = String.format(Locale.US, "%6.4f",kfactor);
        String smass = String.format(Locale.US, "%6.3f",mass);
      
        log.info(String.format("Kfactor was calculated and is %s -- mass is %s g", skfactor, smass));
        log.info(String.format("\tequals -> (1 - epsilon) * visco -> %7.4f * %9.7f", (1 - porosity), viscosity/1000));
        log.info(String.format("\tdivided by -> sqrt(epsilon³) * sqrt(time average) -> %7.4f * %7.4f", Math.sqrt(porosity * porosity * porosity),  Math.sqrt(time)));
        log.info(String.format("\ttimes -> area * rho -> %7.4f * %7.4f", area, density));
        
        cal.setKfactor(skfactor);
        cal.setMassa_ensaio(smass);
        //calct.updateField ("it_kfactor", skfactor, false);
        //calct.updateField ("it_mass", smass, false);
        
        
        return true;
    }
    
    @smstate (state = "CALCBLAINE")
    public boolean st_calcBlaine(SMTraffic smm){
        
        
        Double viscosity = 1.349;
        Double temperature = 22.0;
        
        Double blaine;
        
        Double kfactor;
        
        Double porosity;
        Double density;
        
        Double time;
        Double rsd;
        
        
        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
        
        if (pld!= null){
            if (!pld.isValid()){
                anct.updateField ("it_blaineresult", "", false);
                return true;
            }
        }
        
        porosity = getDoublePValue("porosidade", an.getPorosidade(), true);
        if (porosity.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        
        density  = getDoublePValue("densidade", an.getDensidade(), true);
        if (density.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        
        
        time  = getDoublePValue("media", an.getMedia(), true);
        if (time.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        
        rsd  = getDoublePValue("rsd", an.getRsd(), true);
        if (rsd.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        
        kfactor  = getDoublePValue("kfactor", an.getKfactor(), true);
        if (kfactor.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        
        temperature  = getDoublePValue("temperature", an.getTemperature(), true);
        if (kfactor.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        else{
            viscosity = getVisco(temperature);
        }
        
        
        Double top = Math.sqrt(porosity * porosity * porosity) * Math.sqrt(time) ;
        Double bottom = (1 - porosity) * density * viscosity;
        
        blaine = kfactor * (top / bottom);
        
        String sblaine = String.format(Locale.US, "%5.3f", blaine);
        log.info(String.format("Blaine was calculated and is  %s", sblaine));
        log.info(String.format("\tequals -> sqrt(epsilon³) * sqrt(time average) -> %7.4f * %7.4f", Math.sqrt(porosity * porosity * porosity),  Math.sqrt(time)));
        log.info(String.format("\tdivided by -> (1 - epsilon) * rho * visco -> %7.4f * %7.4f * %9.7f", (1 - porosity), density, viscosity/1000));
        log.info(String.format("\ttimes -> kfactor -> %7.4f", kfactor));
        
        anct.updateField ("it_blaineresult", sblaine, false);
        an.setBlaine(sblaine);
        
        
        return true;
        
    }
    
    
    
    @smstate (state = "CALCMASS")
    public boolean st_calcMass(SMTraffic smm){
        
        Double porosity;
        Double density;
        Double layervol;
        Double samplemass;
        
        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
        
        if (pld!= null){
            if (!pld.isValid()){
                anct.updateField ("it_massa_calculada", "", false);
                return true;
            }
        }
        
        porosity = getDoublePValue("porosidade", an.getPorosidade(), true);
        if (porosity.isNaN()){
            anct.updateField ("it_massa_calculada", "", false);
            return true;
        }
        
        density  = getDoublePValue("densidade", an.getDensidade(), true);
        if (density.isNaN()){
            anct.updateField ("it_massa_calculada", "", false);
            return true;
        }
        
        layervol  = getDoublePValue("volume_camada", an.getVolume_camada(), true);
        if (layervol.isNaN()){
            anct.updateField ("it_massa_calculada", "", false);
            return true;
        }
        
        samplemass = (1 - porosity) * (density * layervol);
        
        
        String smass = String.format(Locale.US, "%5.3f", samplemass);
        log.info(String.format("Samplemass was calculated : %s", smass));
        anct.updateField ("it_massa_calculada", smass, false);
    
        
        return true;
    }
    
    @smstate (state = "CALCCALMASS")
    public boolean st_calcCalMass(SMTraffic smm){
        
        Double porosity;
        Double density;
        Double layervol;
        Double samplemass;
        
        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
        
        if (pld!= null){
            if (!pld.isValid()){
                //calct.updateField ("it_mass", "", true);
                return true;
            }
        }
        
        porosity = getDoublePValue("porosidade", cal.getPorosidade(), false);
        if (porosity.isNaN()){
            //calct.updateField ("it_mass", "", false);
            return true;
        }
        
        density  = getDoublePValue("densidade", cal.getDensidade(), false);
        if (density.isNaN()){
            //calct.updateField ("it_mass", "", false);
            return true;
        }
        
        layervol  = getDoublePValue("volume_camada", cal.getVolume_camada(), false);
        if (layervol.isNaN()){
            //calct.updateField ("it_mass", "", false);
            return true;
        }
        
        samplemass = (1 - porosity) * (density * layervol);
        
        
        String smass = String.format(Locale.US, "%5.3f", samplemass);
        log.info(String.format("Calib sample mass was calculated : %s", smass));
        //calct.updateField ("it_mass", smass, false);
    
        
        return true;
    }
    
    
    
    
    private Double getVisco (Double temp){
        
        Double ret;
        ret = ((temp * visco_a0) + visco_a1) * 1000;
        return ret;
    }
    
    
    private Double getDoublePValue (String key, String svalue, boolean analise) {
        
        Double d;
        PropertyLinkDescriptor pld;
        
        if (analise){
            pld = ana_proplink_modelmap.get(key);
        }
        else{
            pld = cal_proplink_modelmap.get(key);
        }
        
        if (pld != null && pld.isValid()){
            try{
                if (svalue.equals("")){
                    d = Double.NaN;
                }
                else{
                    d = Double.parseDouble(svalue);
                }
                
                //log.info(String.format(Locale.US, "getDoublepValue parsed %s to %5.3f", svalue, d));
                return d;
            }
            catch (Exception ex){
                log.warning(String.format("Failed to convert property %s due %s", key, ex.getMessage()));
            }
        }    
        return Double.NaN;
    }
    
    
    
    
    
    
    @smstate (state = "LOADCALIBRATION")
    public boolean st_loadCalibration(SMTraffic smm){
        
        
//        anct.updateField ("it_calibfile", "clb:quartzo-nist-1304:181219165830", false);
//        anct.updateField ("it_constantek", "2.13", false);
//        anct.updateField ("it_layervolume", "1.93", false);
//        anct.updateField ("it_temperature", "22.2", false);
         
        return true;
    }
    
    
    private void storeAnaliseFile(Path p) throws IOException{
        
        GsonBuilder builder = new GsonBuilder(); 
        builder.setPrettyPrinting(); 
        Gson gson = builder.create();
        String sjson = gson.toJson(an);
 
        Files.write(p, sjson.getBytes(StandardCharsets.UTF_8));
        log.info(String.format("================Stored analise on %s: \n\r", p.toString()));

    }
    
    
    
    
    
    @smstate (state = "REGISTERNOTIFICATION")
    public boolean st_registerNotification(SMTraffic smm){
        
        VirnaPayload payload = smm.getPayload();
        String[] messages = payload.vstring.split("&");
        
//        ImageIcon image = ImageUtilities.loadImageIcon("com/opus/pp100/db-schema-icon.png", true);
//        
//        Notification noti = NotificationDisplayer.getDefault().notify(
//            messages[0],
//            image,
//            messages[1],
//            Lookups.forPath("NotificationActions").lookup(ActionListener.class)
//        );
        
        return true;
    }
    
    @smstate (state = "SHOWOOPS")
    public boolean st_showOops(SMTraffic smm){
        
        VirnaPayload payload = smm.getPayload();
        VirnaServiceProvider vsp = payload.getCaller();
        
        
        if (false){
        //if (anct != null ){
            FXFConfirmationDialog cdlg = new FXFConfirmationDialog()
                .setMessage("Já há um registro no banco com esse resultado, \r\nDevo substituir ?")
                .setTitle("Banco de dados")
                .setOwner(anct.getScene().getWindow())    
            ;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert dlg = cdlg.activateAlert();
                    ((Stage) dlg.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
                    dlg.showAndWait().ifPresent(result -> {
                        //log.info("Result is " + result);
                        if (result.getButtonData() == ButtonData.CANCEL_CLOSE){
                            log.info("Dialog canceled");
                        }
                        else{
                            //storeAnaliseRecord(an);    
                        }
                    });
                }    
            });   
        }
        else{
//            NotifyDescriptor nd = new NotifyDescriptor.Message(
//                payload.vstring, 
//                NotifyDescriptor.YES_NO_CANCEL_OPTION);
//                if (payload.getAuxiliar() != null){
//                    nd.setOptions((Object[])payload.getAuxiliar());
//                }
// 
//                Object retval = DialogDisplayer.getDefault().notify(nd);
//                if (retval != null){
//                    if (retval instanceof Integer){
//                        log.info(String.format("Retval integer = %d", retval));
//                    }
//                    else if (retval instanceof String){
//                        vsp.processSignal(new SMTraffic(0l, 0l, 0, payload.getCallerstate(), 
//                                        new VirnaPayload()
//                                        .setObject(payload.vobject)
//                                        .setString((String)retval)
//                                        .setCaller(this)
//                                        .setCallerstate("SHOWOOPS")
//                        ));
//                    }
//                }
                
        }
        return true;
    }
    
    
    
    @smstate (state = "STOREANALISERECORD")
    public boolean st_storeAnaliseRecord(SMTraffic smm){
        
        AnaliseDescriptor and;
        VirnaPayload payload = smm.getPayload();
        
        Object obj = payload.vobject;
        if (obj == null){
            and = new AnaliseDescriptor();
        }
        else{
            and = (AnaliseDescriptor)obj;
        }
        
        Object caller = payload.getCaller();
        
        
        if (caller instanceof Controller){
            // Find first if record already exists
            if (payload.getCallerstate().equals("SHOWOOPS")){
                if(payload.vstring.equals("Apagar")){
//                    StatusDisplayer.getDefault().setStatusText(String.format("Gravando sobre o registro %d", and.getUid()));
                }
                else if (payload.vstring.equals("Mudar ID")){
                    Long newid = System.currentTimeMillis();
//                    StatusDisplayer.getDefault().setStatusText(String.format("Mudando registro %d para %d", and.getUid(), newid));
                    and.setUid(newid);
                    and.setTimestamp(newid);
                }
                
                dbservice.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISE", 
                                        new VirnaPayload()
                                        .setObject(and)
                                        .setCaller(this)
                                        .setCallerstate("STOREANALISERECORD")
                ));
                
            }
            else{
                dbservice.processSignal(new SMTraffic(0l, 0l, 0, "HASRECORD", 
                                        new VirnaPayload()
                                        .setLong1(and.getUid())
                                        .setObject(and)
                                        .setCaller(this)
                                        .setCallerstate("STOREANALISERECORD")
                ));
            }
        }
        else if (caller instanceof DBService){
            // First check if we had problems and proceed aprop.
            if (payload.getFlag2() != null && payload.getFlag2()){
                processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", 
                                        payload));
            }
            else{
                // If we're checking previous records...
                if (payload.getCallerstate().equals("HASRECORD")){
                    if (payload.getFlag1()){
                        String mes1 = String.format("<html>Análise com o ID:%d já existe no Banco de Dados, voce pode : "
                            + "<ul>"
                                + "<li>Apagar o registro anterior e gravar esses novos dados</li>"
                                + "<li>Gravar esses dados com outra identificação (não recomendado...)</li>"
                            + "</ul>"
                            + "</html>", 
                            payload.long1);
                        processSignal(new SMTraffic(0l, 0l, 0, "SHOWOOPS", 
                                        new VirnaPayload()
                                        .setString(mes1)
                                        .setObject(and)
                                        .setAuxiliar(new Object[] { "Apagar", "Mudar ID", "Cancelar" })
                                        .setCaller(this)
                                        .setCallerstate("STOREANALISERECORD")
                        ));
                    }
                    else{
                        // Store record then
                        dbservice.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISE", 
                                                new VirnaPayload()
                                                .setObject(and)
                                                .setCaller(this)
                                                .setCallerstate("STOREANALISERECORD")
                        ));
                    }
                }
                else if (payload.getCallerstate().equals("STOREANALISE")){                   
//                    StatusDisplayer.getDefault().setStatusText(payload.vstring);
                }
            }
        }
   
        return true;
    }
    
    
    
    
    
    private void storeCalibFile(Path p) throws IOException{
        
        GsonBuilder builder = new GsonBuilder(); 
        builder.setPrettyPrinting(); 
        Gson gson = builder.create();
        String sjson = gson.toJson(cal);
 
        Files.write(p, sjson.getBytes(StandardCharsets.UTF_8));
        log.info(String.format("Stored Calibration on %s", p.toString()));

    }
    
    
    @smstate (state = "STORECALIBFILE")
    public boolean st_storeCalibFile(SMTraffic smm){
        
        String filename;
        
        CalibDescriptor and;
        VirnaPayload payload = smm.getPayload();
        
        Object obj = payload.vobject;
        if (obj == null){
            and = new CalibDescriptor();
        }
        else{
            and = (CalibDescriptor)obj;
        }
        
        Object caller = payload.getCaller();
        
        String message = "A Calibração com o ID";
        filename = String.format("cal-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.json", and.getUid());
        Path p = Paths.get(Config.getInstance().getExport_dir()+filename);
        try {
            storeCalibFile(p);
//            StatusDisplayer.getDefault().setStatusText(String.format("%s %d foi gravada no arquivo %s", 
//                message, and.getUid(), p.toFile().getAbsolutePath()));
            calibclean = true;
        } catch (IOException ex) {
            processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", 
                                new VirnaPayload()
                                .setString("Falha na gravação do arquivo : "+ filename)
                                .setFlag2(true)
                                .setObject(payload.vobject)        
                                .setCaller(this)
                                .setCallerstate("STOREANALISEFILE")
            ));
        }
      
        return true;
    }
    
    
    
    @smstate (state = "STOREANALISEFILE")
    public boolean st_storeAnaliseFile(SMTraffic smm){
        
        String filename;
        
        AnaliseDescriptor and;
        VirnaPayload payload = smm.getPayload();
        
        Object obj = payload.vobject;
        if (obj == null){
            and = new AnaliseDescriptor();
        }
        else{
            and = (AnaliseDescriptor)obj;
        }
        
        Object caller = payload.getCaller();
        
        if (caller instanceof Controller){
            if (payload.getCallerstate().equals("")){
                filename = String.format("ana-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.json", and.getUid());
                Path p = Paths.get(Config.getInstance().getExport_dir()+filename);
                File f = p.toFile();
                if (f.exists()){
                    String mes1 = String.format("<html>Arquivo %s já existe, voce pode : "
                                + "<ul>"
                                    + "<li>Gravar esses novos dados sobre o arquivo anterior.</li>"
                                    + "<li>Providenciar nova identificação e gravar arquivo.</li>"
                                + "</ul>"
                                + "</html>", 
                                filename);
                            processSignal(new SMTraffic(0l, 0l, 0, "SHOWOOPS", 
                                    new VirnaPayload()
                                    .setString(mes1)
                                    .setObject(and)
                                    .setAuxiliar(new Object[] { "Gravar", "Mudar ID", "Cancelar" })
                                    .setCaller(this)
                                    .setCallerstate("STOREANALISEFILE")
                            )); 
                }
                else{
                    try {
                        storeAnaliseFile(p);
//                        StatusDisplayer.getDefault().setStatusText(String.format("Analise com ID %d foi gravada em %s", 
//                            and.getUid(), p.toFile().getAbsolutePath()));
                        anclean = true;
                    } catch (IOException ex) {
                        processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", 
                                            new VirnaPayload()
                                            .setString("Falha na gravação do arquivo : "+ filename)
                                            .setFlag2(true)
                                            .setObject(payload.vobject)        
                                            .setCaller(this)
                                            .setCallerstate("STOREANALISEFILE")
                        ));
                    }
                }
            }
            else if (payload.getCallerstate().equals("SHOWOOPS")){
                
                String message = "Usando analise com ";
                
                if (payload.vstring.equals("Mudar ID")){
                    Long newid = System.currentTimeMillis();
                    and.setUid(newid);
                    and.setTimestamp(newid);
                    message = "Mudando analise para  ";
                }
                
                filename = String.format("ana-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.json", and.getUid());
                Path p = Paths.get(Config.getInstance().getExport_dir()+filename);
                try {
                    storeAnaliseFile(p);
//                    StatusDisplayer.getDefault().setStatusText(String.format("%s ID %d e gravando em %s", 
//                        message, and.getUid(), p.toFile().getAbsolutePath()));
                    anclean = true;
                } catch (IOException ex) {
                    processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", 
                                        new VirnaPayload()
                                        .setString("Falha na gravação do arquivo : "+ filename)
                                        .setFlag2(true)
                                        .setObject(payload.vobject)        
                                        .setCaller(this)
                                        .setCallerstate("STOREANALISEFILE")
                    ));
                }
            }
        }
        return true;
    }
    
    
    @smstate (state = "LOADFILE")
    public boolean st_loadFile(SMTraffic smm){
        
        String file;
        AnaliseDescriptor and;
        VirnaPayload payload = smm.getPayload();
        Object caller = payload.getCaller();
        
        if (caller instanceof Controller){
            if (payload.getCallerstate().equals("")){
                if (!anclean){
                    String mes1 = String.format("<html>Há dados não salvos na analise corrente, voce pode : "
                                + "<ul>"
                                    + "<li>Gravar esses dados primeiro.</li>"
                                    + "<li>Descartar o conteúdo</li>"
                                + "</ul>"
                                + "</html>");
                            processSignal(new SMTraffic(0l, 0l, 0, "SHOWOOPS", 
                                    new VirnaPayload()
                                    .setString(mes1)
                                    .setObject(an)
                                    .setAuxiliar(new Object[] { "Gravar", "Descartar", "Cancelar" })
                                    .setCaller(this)
                                    .setCallerstate("LOADFILE")
                            )); 
                }
                else{
                    processSignal(new SMTraffic(0l, 0l, 0, "LOADFILE", 
                            new VirnaPayload()
                            .setObject(an)
                            .setCaller(this)
                            .setCallerstate("LOADFILE")
                    )); 
                }
            }
            else if (payload.getCallerstate().equals("SHOWOOPS")){
                if (payload.vstring.equals("Gravar")){
                    storeAnalise();
                }
                processSignal(new SMTraffic(0l, 0l, 0, "LOADFILE", 
                        new VirnaPayload()
                        .setObject(an)
                        .setCaller(this)
                        .setCallerstate("LOADFILE")
                )); 
                
            }
            else if (payload.getCallerstate().equals("LOADFILE")){
                file = PicnoUtils.selectFile(false, Config.getInstance().getExport_dir(), "json");
                if (file != null){
                    Path p = Paths.get(file);
                    byte[] bytes;
                    try {
                        bytes = Files.readAllBytes(p);
                        String json_out = new String(bytes, StandardCharsets.UTF_8);
                        GsonBuilder builder = new GsonBuilder(); 
                        builder.setPrettyPrinting(); 
                        Gson gson = builder.create();
                        an = gson.fromJson(json_out, AnaliseDescriptor.class);
                        log.info(String.format("Analise loaded from %s", p.toFile().getAbsolutePath()));
//                        StatusDisplayer.getDefault().setStatusText(String.format("Analise foi carregada do arquivo %s", 
//                        p.toFile().getAbsolutePath()));
                        updateAnaliseCanvas();
                        
                    } catch (Exception ex) {
                        processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", 
                                        new VirnaPayload()
                                        .setString("Falha na carga da Analise de : "+ p.toFile().getAbsolutePath())
                                        .setFlag2(true)
                                        .setObject(payload.vobject)        
                                        .setCaller(this)
                                        .setCallerstate("LOADFILE")
                        ));
                    }
                    
                }
                
            }
            
        }      
        return true;
    }
    
    
    
    
    private void addReportItem (PDPageContentStream contentStream, int xoff,  int yoff, 
            int fontsize, String item) throws IOException{
        
        
        if (item != null){
            if (fontsize != 12){
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontsize);
            }
            log.info(String.format("Addint report item %s @ %d/%d -- font=%d", item, xoff, yoff, fontsize));
            contentStream.beginText(); 
            contentStream.newLineAtOffset(xoff, yoff);
            contentStream.showText(item);
            contentStream.endText();
            if (fontsize != 12){
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            }
            
            
        }
    }
    
    
    private void emitCalibReport(String filename, String template) throws Exception{
        
        
        
        PDPage page;
        PDPageContentStream contentStream;

        File file = new File(Config.getInstance().getTemplate_dir()+template+".pdf");
        PDDocument document = PDDocument.load(file);
        
        String descfile = Config.getInstance().getTemplate_dir()+template+".json";
        ReportDescriptor rd = PicnoUtils.loadJson(descfile, ReportDescriptor.class); 

        page = document.getPage(0);
        contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

        Color c = PicnoUtils.convertFXColor(rd.getStrokecolor());
        contentStream.setNonStrokingColor(c);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
       
        ArrayList<ReportItem> ritems = rd.getItems();
        String value = "N/A";
        String format ;
        int xpos, ypos;
        int yoffset = (int) Math.round(rd.getYoffset());
        int xoffset = (int) Math.round(rd.getXoffset());
        
        
        contentStream.close();

        String pmessage = "Report Manager gravou um Relatorio de Calibração em " + filename;
//        StatusDisplayer.getDefault().setStatusText(pmessage);
        log.info("Report Manager is saving PDF Calibration report to :  " + filename);  
        document.save(filename);
        document.close();
        
        
    }
    
    
    private void emitReport(String filename, String template){
        
        PDPage page;
        PDPageContentStream contentStream;
        
        log.info(String.format("---- RE : loading template from %s", template));
        File file = new File(Config.getInstance().getTemplate_dir()+template+".pdf");
        
        PDDocument document;
        try {
            document = PDDocument.load(file);
            page = document.getPage(0);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
            log.info(String.format("---- RE : contentStream is %s", contentStream.toString()));
            
            
            String descfile = Config.getInstance().getTemplate_dir()+template+".json";
            log.info(String.format("---- RE : loading descriptor from %s", descfile));
            ReportDescriptor rd;
            rd = PicnoUtils.loadJson(descfile, ReportDescriptor.class);
            
            Color c = PicnoUtils.convertFXColor(rd.getStrokecolor());
            contentStream.setNonStrokingColor(c);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

            ArrayList<ReportItem> ritems = rd.getItems();
            log.info(String.format("---- RE : Report Descriptor has %d items", ritems.size()));

            String value = "N/A";
            String format ;
            int xpos, ypos;
            int yoffset = (int) Math.round(rd.getYoffset());
            int xoffset = (int) Math.round(rd.getXoffset());

            for (ReportItem ri : ritems){
                Double dxpos = ri.getXpos();
                if (dxpos == 0.0) continue;
                if (ri.getDatatype().equals("field")){
                    value = an.getFieldAsString(ri.getDatatag());
                    if (value == null){
                        value = "N/A";
                    }
                    else{
                        if (ri.getDatatag().equals("timestamp")){
                            format = ri.getFormat();
                            if (!format.equals("")){
                                Long l = Long.decode(value);
                                value = String.format(format, l);
                            }
                        }   
                    }
                }

                log.info(String.format("Valor do campo %s é : %s", ri.getDatatag(), value));

                xpos = (int) Math.round(ri.getXpos()) + xoffset;
                ypos = (int) Math.round(ri.getYpos()) + yoffset;

                addReportItem (contentStream, xpos, ypos, ri.getFontsize(), value);

            };

            contentStream.close();

            String pmessage = "Report Manager gravou um relatorio PDF em " + filename;
//            StatusDisplayer.getDefault().setStatusText(pmessage);
            //log.info("Report Manager is saving PDF report to  " + filename);  
            document.save(filename);
            document.close();
            
        } catch (Exception ex) {
//            Exceptions.printStackTrace(ex);
        }
     
    }
    
    @smstate (state = "REPORTCALIBRATION")
    public boolean st_reportCalibration(SMTraffic smm){
        
        String filename;
        String fullpath;
        
        CalibDescriptor and;
        VirnaPayload payload = smm.getPayload();
        
        Object obj = payload.vobject;
        if (obj == null){
            and = new CalibDescriptor();
        }
        else{
            and = (CalibDescriptor)obj;
        }
        
        Object caller = payload.getCaller(); 
        
        
        fullpath = String.format("%1$scal-%3$s:%2$td%2$tm%2$ty%2$tH%2$tM%2$tS.pdf", 
                        Config.getInstance().getReport_dir(), and.getUid(), and.getSid());
        try {
            emitCalibReport(fullpath, "calib_template");
//            StatusDisplayer.getDefault().setStatusText(String.format("Report com Calibração ID %d foi emitido no arquivo %s", 
//                and.getUid(), fullpath));
//            log.info(String.format("Report com calibração ID %d foi emitido no arquivo %s", 
//                and.getUid(), fullpath));
        } catch (Exception ex) {
            processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", 
                                new VirnaPayload()
                                .setString("Falha na gravação do relatorio : "+ fullpath)
                                .setFlag2(true)
                                .setObject(payload.vobject)        
                                .setCaller(this)
                                .setCallerstate("REPORTANALISE")
            ));
        }
        
        return true; 
    }
    
    
    @smstate (state = "REPORTANALISE")
    public boolean st_reportAnalise(SMTraffic smm){
        
        String filename;
        String fullpath;
        
        AnaliseDescriptor and;
        VirnaPayload payload = smm.getPayload();
        
        Object obj = payload.vobject;
        if (obj == null){
            and = new AnaliseDescriptor();
        }
        else{
            and = (AnaliseDescriptor)obj;
        }
        
        Object caller = payload.getCaller();
        
        
        if (caller instanceof Controller){
            if (payload.getCallerstate().equals("")){
                filename = String.format("ana-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.pdf", 
                        and.getUid());
                fullpath = String.format("%1$sana-%2$td%2$tm%2$ty%2$tH%2$tM%2$tS.pdf", 
                        Config.getInstance().getExport_dir(), and.getUid());
                Path p = Paths.get(fullpath);
                File f = p.toFile();
                if (f.exists()){
                    String mes1 = String.format("<html>Relatorio %s já existe, voce pode : "
                                + "<ul>"
                                    + "<li>Gravar esses novos dados sobre o arquivo anterior.</li>"
                                    + "<li>Providenciar nova identificação e emitir o relatorio.</li>"
                                + "</ul>"
                                + "</html>", 
                                filename);
                            processSignal(new SMTraffic(0l, 0l, 0, "SHOWOOPS", 
                                    new VirnaPayload()
                                    .setString(mes1)
                                    .setObject(and)
                                    .setAuxiliar(new Object[] { "Gravar", "Mudar ID", "Cancelar" })
                                    .setCaller(this)
                                    .setCallerstate("REPORTANALISE")
                            )); 
                }
                else{
                    processSignal(new SMTraffic(0l, 0l, 0, "REPORTANALISE", 
                        new VirnaPayload()
                        .setObject(an)
                        .setCaller(this)
                        .setCallerstate("REPORTANALISE")
                    )); 
                }
            }
            else if (payload.getCallerstate().equals("SHOWOOPS")){
                
                String message = "Usando analise com ";
                
                if (payload.vstring.equals("Mudar ID")){
                    Long newid = System.currentTimeMillis();
                    and.setUid(newid);
                    and.setTimestamp(newid);
                    message = "Mudando id do report para  ";
                }
                
                processSignal(new SMTraffic(0l, 0l, 0, "REPORTANALISE", 
                    new VirnaPayload()
                    .setObject(an)
                    .setCaller(this)
                    .setCallerstate("REPORTANALISE")
                ));
                
            }
            else if (payload.getCallerstate().equals("REPORTANALISE")){
                
                fullpath = String.format("%1$sana-%2$td%2$tm%2$ty%2$tH%2$tM%2$tS.pdf", 
                        Config.getInstance().getReport_dir(), and.getUid());
                try {
                    
                    //if (profile.getReport_template())
//                    ChoiceDialog<String> dlg = new ChoiceDialog<>("Jonathan",
//                                                          "Matthew", "Jonathan", "Ian", "Sue", "Hannah");
//                    dlg.setTitle("Name Guess");
//                    String optionalMasthead = "Name Guess";
//                    dlg.getDialogPane().setContentText("Pick a name?");
//                    configureSampleDialog(dlg, optionalMasthead);
//                    showDialog(dlg);
                        
                        
                        
                    emitReport(fullpath, "pdf1");
//                    StatusDisplayer.getDefault().setStatusText(String.format("Report com analise ID %d foi emitido no arquivo %s", 
//                        and.getUid(), fullpath));
//                    log.info(String.format("Report com analise ID %d foi emitido no arquivo %s", 
//                        and.getUid(), fullpath));
                } catch (Exception ex) {
                    processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", 
                                        new VirnaPayload()
                                        .setString("Falha na gravação do relatorio : "+ fullpath)
                                        .setFlag2(true)
                                        .setObject(payload.vobject)        
                                        .setCaller(this)
                                        .setCallerstate("REPORTANALISE")
                    ));
                }
            }
        }
   
        return true;     
    }  
    
    
    
    @smstate (state = "LOCKUI")
    public boolean st_lockUI(SMTraffic smm){
        
        locked = (smm.getPayload().int1 == 1);
        
        if (smm.getPayload().int2 == 0){
            anct.lockUI(locked);
        }
        else{
            //calct.lockUI(locked);
        }
        
        return true;
    }
    
    
    
    @smstate (state = "LOADGUI")
    public boolean st_loadGUI(SMTraffic smm){
        
        //service_thread.pushState ("LOADCONFIG", null);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
//                Mode m = WindowManager.getDefault().findMode("explorer");              
//                ExplorerTopComponent extc = ExplorerTopComponent.findInstance();
//                extc.setName("NAVTREE");
//                m.dockInto(extc);
//                extc.open();
//                extc.requestActive();
//                log.info(String.format("Explorer %d was created", extc.hashCode()));
            }    
        });
        
        return true;
    }
    
    
}


//CREATE TABLE ANALISE
//(
//   ID integer PRIMARY KEY NOT NULL,
//   TIMESTP integer DEFAULT CURRENT_TIMESTAMP NOT NULL,
//   SID varchar(2000000000) DEFAULT 'SID' NOT NULL,
//   LOTE varchar(2000000000) DEFAULT 'LOTE' NOT NULL,
//   NOTAS varchar(2000000000) DEFAULT 'NOTAS' NOT NULL,
//   OWNER varchar(2000000000) DEFAULT 'Default' NOT NULL,
//   PROFILE varchar(2000000000) DEFAULT 'Default' NOT NULL,
//   BLAINE varchar(2000000000) DEFAULT '0.0' NOT NULL,
//   POROSIDADE varchar(2000000000) DEFAULT '0.0' NOT NULL,
//   DENSIDADE varchar(2000000000) DEFAULT '0.0' NOT NULL,
//   MASSA varchar(2000000000) DEFAULT '0.0' NOT NULL,
//   KFACTOR varchar(2000000000) DEFAULT '0.0' NOT NULL,
//   VOLUME varchar(2000000000) DEFAULT '0.0' NOT NULL,
//   TEMPERATURA varchar(2000000000) DEFAULT '0.0' NOT NULL,
//   CALIBFILE varchar(2000000000) DEFAULT '' NOT NULL,
//   FLAG integer DEFAULT 0 NOT NULL,
//   MEDIA varchar(2000000000) DEFAULT '0.0' NOT NULL,
//   RSD varchar(2000000000) DEFAULT '0.0' NOT NULL,
//   RUNS varchar(2000000000) DEFAULT '' NOT NULL
//)
//;


        
//        GsonBuilder builder = new GsonBuilder(); 
//        builder.setPrettyPrinting(); 
//        Gson gson = builder.create();
//        String sjson = gson.toJson(an);
//
//        log.info("========== JSON : ==================\n\r");
//        log.info(sjson);
//        log.info(String.format("Json parser loaded %d chars", sjson.length()));




//ReportDescriptor rd = new ReportDescriptor();
//        
//        rd.addItem(new ReportItem("it_densidade"))
//                .setXoffset(200.0)
//                .setYoffset(200.0);
//        rd.addItem(new ReportItem("it_porosidade"));
//        rd.addItem(new ReportItem("it_massa_calculada"));
//        rd.addItem(new ReportItem("it_analiseaverage"));
//        rd.addItem(new ReportItem("it_analisersd"));
//        rd.addItem(new ReportItem("it_blaineresult"));
//        rd.addItem(new ReportItem("it_sid"));
//        rd.addItem(new ReportItem("it_lote"));
//        rd.addItem(new ReportItem("it_notas"));
//        rd.addItem(new ReportItem("it_calibfile"));
//        rd.addItem(new ReportItem("it_constantek"));
//        rd.addItem(new ReportItem("it_layervolume"));
//        rd.addItem(new ReportItem("it_temperature"));
//       
//        
//        GsonBuilder builder = new GsonBuilder(); 
//        builder.setPrettyPrinting(); 
//        Gson gson = builder.create();
//        String sjson = gson.toJson(rd);
// 
//        String filename = String.format("pdfroot.json");
//        Path p = Paths.get(Config.getInstance().getTemplate_dir()+filename);
//        try {
//            Files.write(p, sjson.getBytes(StandardCharsets.UTF_8));
//        } catch (IOException ex) {
//            log.severe(String.format("Failed to store ReportDescriptor @ %s", p.toFile().getAbsolutePath()));
//        }
//        log.info(String.format("================Stored report descriptor %s: \n\r%s", "file", sjson));
//        


   
    
    //                FXFFieldDescriptor fd = prof.getDescriptor(ri.getField());
//                if (fd != null) {
//                    fd.setXpos(ri.getXpos());
//                    fd.setYpos(ri.getYpos());
//                    fd.setReport_font(ri.getFont());
//                    fd.setReport_format(ri.getFormat());
//                    fd.setReport_fontsize(ri.getFontsize());
//                    log.info(String.format("Report Descriptor %s was updated to %5.3f / %5.3f  - fontsize = %d", 
//                            ri.getField(), ri.getXpos(), ri.getYpos(), ri.getFontsize()));
//                }
    
    
    
    
    
    
    
//    
//    @smstate (state = "LOADREPORTDESCRIPTOR")
//    public boolean st_loadReportDescriptor(SMTraffic smm){
//   
//        VirnaPayload payload = smm.getPayload();
//        AnaliseDescriptor and = (AnaliseDescriptor)payload.vobject;
//        String descfile = Config.getInstance().getTemplate_dir()+"pdfroot.json";
//        
//        try {
//            ReportDescriptor rd = PicnoUtils.loadAuxJson(descfile, ReportDescriptor.class); 
//            ArrayList<ReportItem> ritems = rd.getItems();
//            ritems.forEach((ri) -> {
//                String value = and.getFieldAsString(ri.getDatatype());
//                if (value == null) value = "indetermindado";
//                log.info(String.format("Valor do campo %s é : %s", ri.getDatatype(), value));
//                
//
//            });
//            
//        } catch (IOException ex) {
//            log.severe(String.format("Unable to load descriptor from %s", descfile));
//        }
//        
//        return true;
//    }
//


//@smstate (state = "CALCLAYERVOL")
//    public boolean st_calcLayerVol(SMTraffic smm){
//        
//        Double porosity;
//        Double density;
//        Double layervol;
//        Double samplemass;
//        
//        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
//        
//        if (pld!= null){
//            if (!pld.isValid()){
//                anct.updateField ("it_volume", "", false);
//                return true;
//            }
//        }
//        
//        porosity = getDoublePValue("porosidade", cal.getPorosidade());
//        if (porosity.isNaN()){
//            calct.updateField ("it_volume", "", false);
//            return true;
//        }
//        
//        density  = getDoublePValue("densidade", cal.getDensidade());
//        if (density.isNaN()){
//            calct.updateField ("it_volume", "", false);
//            return true;
//        }
//        
//        layervol  = getDoublePValue("volume_camada", an.getVolume_camada());
//        if (layervol.isNaN()){
//            calct.updateField ("it_volume", "", false);
//            return true;
//        }
//        
//        samplemass = (1 - porosity) * (density * layervol);
//        
//        
//        String smass = String.format(Locale.US, "%5.3f", samplemass);
//        log.info(String.format("Layer volume was calculated : %s", smass));
//        calct.updateField ("it_volume", smass, false);
//        
//        
//        return true;
//        
//    }
//    