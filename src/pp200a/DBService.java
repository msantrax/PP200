/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.syssupport.Config;
import com.opus.syssupport.PicnoUtils;
import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.StateDescriptor;
import com.opus.syssupport.VirnaPayload;
import com.opus.syssupport.VirnaServiceProvider;
import com.opus.syssupport.smstate;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;



public class DBService implements VirnaServiceProvider {

    private static final Logger log = Logger.getLogger(DBService.class.getName());
    
    private static DBService instance;    
    private DBService.SMThread service_thread;
    protected LinkedBlockingQueue<SMTraffic> smqueue;
    private LinkedHashMap<String, StateDescriptor> statesptr ;
 
    protected Controller ctrl;
    protected FX3Controller dbtc;
    protected Connection conn;
    
    public LocalDateTime init_timestamp;
    public LocalDateTime end_timestamp;
    public String filter_period = "Até";
    
   
    public static DBService getInstance(){
        if (instance == null) {instance = new DBService();}
        return instance;
    }

    
    public DBService() {
  
        log.setLevel(Level.FINE);      
        instance = this;
    
        smqueue = new LinkedBlockingQueue<>();
        statesptr = new LinkedHashMap<>();
        
        this.init_timestamp = LocalDateTime.now();
        this.end_timestamp = LocalDateTime.now();
        
    }

    public DBService getDefault() { return instance; }
  
    public void setControler (Controller ctrl) { this.ctrl = ctrl;}
    
    public void setView (FX3Controller dbtc) { this.dbtc = dbtc;}
    
    public void processSignal (SMTraffic signal){ 
        smqueue.add(signal);
        //log.info(String.format("File writer registered %s to %d @ %s ", signal.getState().toString(), signal.getHandle(), this.toString()));  
    }
    
    // ===================================== DBASE TASKS =============================================================
   
    
    public boolean locateRecord(long sid) throws SQLException{
       
        //throw new SQLException("Fake exception");
        
        String sql = String.format("SELECT ID, SID FROM ANALISE WHERE ID=%d", sid);
             
        Statement stmt  = conn.createStatement();  
        ResultSet rs    = stmt.executeQuery(sql);
        log.info("Applying filter : "+ sql);

        if (rs.next()) {
           return true; 
        }
            
        return false;  
    }
    
    
    public void loadDBRecords(String sql){
        
        smqueue.offer(new SMTraffic(0l, 0l, 0, "LOADANALISES", 
                                        new VirnaPayload()
                                        .setString(sql)
                                        .setCaller(this)
                                        .setCallerstate("")
            )); 
    }
             
    public void loadDBRecord(Long id){
        
        smqueue.offer(new SMTraffic(0l, 0l, 0, "LOADANALISE", 
                                        new VirnaPayload()
                                        .setLong1(id)
                                        .setCaller(this)
                                        .setCallerstate("")
            )); 
    }
    
    
    
    public ArrayList<DBRecord> loadRecords(String sql) throws SQLException{
        
        int recordsloaded = 0;
        ArrayList<DBRecord> records = new ArrayList<>();
        int auxinit = sql.indexOf("|");
        int auxend = sql.lastIndexOf("|");
        Boolean hasaux = (auxend-auxinit)>1;
        String auxfield = hasaux ? sql.substring(auxinit+2, auxend).trim(): "";
        
        sql = sql.replace("|", "");
        
        Statement stmt  = conn.createStatement();  
        ResultSet rs    = stmt.executeQuery(sql);
        log.info("loadrecord is applying filter : "+ sql);

        while (rs.next()) {
            DBRecord dbr = new DBRecord().
                    setId(String.valueOf(rs.getLong("ID"))).
                    setBlaine(rs.getString("BLAINE")).
                    setSid(rs.getString("SID")).
                    setLote(rs.getString("LOTE"));
            if (hasaux){        
                dbr.setAux(rs.getString(auxfield));
            }
            else{
                dbr.setAux("");
            }
            recordsloaded++;
            records.add(dbr);
        }

        return records;
    }
    
    
    
    
    private String getStringTimestamp(int timestamp){
        String s = PicnoUtils.df.format(LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.ofHours(-3)));
        return s;                
    }
    
    private Double convertDouble(String value) {    
        if (value != null){
            return Double.valueOf(value);
        }
        else {
            return 0.0;
        }
    }
    
    private String convertString (String value){
        if (value != null){
            return value;
        }
        else {
            return "";
        }
    }
    
    private Integer convertInteger(String value) {
        if (value != null){
            if (value.length() > 10){
                value = value.substring(0, 10);
            }
            return Integer.parseInt(value);
        }
        else {
            return 0;
        }
    }
    

    
    
    public void deleteAnalise(AnaliseDescriptor andesc) throws SQLException{
        
        String stm1 = "DELETE FROM ANALISE WHERE ID=" + andesc.getUid();
        PreparedStatement pstmt = conn.prepareStatement(stm1);
        pstmt.executeUpdate();
        log.info(String.format("Run %d was deleted from DB", andesc.getUid()));
        
    }
    
    
    public AnaliseDescriptor loadAnalise (Long id) throws SQLException{
        
        AnaliseDescriptor ldesc = new AnaliseDescriptor(); 

        String stm1 = "SELECT "+
         "ID,TIMESTP,SID,LOTE,NOTAS,OWNER,PROFILE,BLAINE,POROSIDADE,DENSIDADE,MASSA,KFACTOR,VOLUME,TEMPERATURA,CALIBFILE,MEDIA,RSD,RUNS "+
    //        1     2    3    4    5     6      7      8        9         10      11     12      13        14        15      16    17   18  19        
        "FROM ANALISE WHERE (ID=" + String.valueOf(id)+")";
       
        Statement stmt  = conn.createStatement();  
        ResultSet rs    = stmt.executeQuery(stm1);
        log.info(String.format("Loading record %d", id));

        while (rs.next()) {
            ldesc.setUid(rs.getLong("ID"));
            ldesc.setTimestamp(rs.getLong("TIMESTP"));
            ldesc.setSid(rs.getString("SID"));
            ldesc.setLote(rs.getString("LOTE"));
            ldesc.setNotas(rs.getString("NOTAS"));
            ldesc.setUser(rs.getString("OWNER"));
            ldesc.setProfile(rs.getString("PROFILE"));
            ldesc.setBlaine(rs.getString("BLAINE"));
            ldesc.setPorosidade(rs.getString("POROSIDADE"));
            ldesc.setDensidade(rs.getString("DENSIDADE"));
            ldesc.setMassa_ensaio(rs.getString("MASSA"));
            ldesc.setKfactor(rs.getString("KFACTOR"));
            ldesc.setVolume_camada(rs.getString("VOLUME"));
            ldesc.setTemperature(rs.getString("TEMPERATURA"));
            ldesc.setCalibid(rs.getString("CALIBFILE"));
            ldesc.setMedia(rs.getString("MEDIA"));
            ldesc.setRsd(rs.getString("RSD"));
            
            String values = rs.getString("RUNS");
            if (values != null && !values.isEmpty()){
                String [] avalues = values.split(":");
                ArrayList<String> descvalues = ldesc.getTempos();
                for (String svalue : avalues){
                    descvalues.add(svalue);
                }
            } 
        }
       
        return ldesc;
    }
    
    
    public void storeAnalise (AnaliseDescriptor andesc) throws SQLException{
        
        String stm1 = "INSERT INTO ANALISE ("+
        
        "ID,TIMESTP,SID,LOTE,NOTAS,OWNER,PROFILE,BLAINE,POROSIDADE,DENSIDADE,MASSA,KFACTOR,VOLUME,TEMPERATURA,CALIBFILE,FLAG,MEDIA,RSD,RUNS) "+
//        1     2    3    4    5     6      7      8        9         10      11     12      13        14        15      16    17   18  19        
"VALUES ( ?,    ?,   ?,   ?,   ?,    ?,     ?,     ?,       ?,         ?,      ?,     ?,      ?,        ?,        ?,      ?,    ?,   ?,  ?  )";
  
        PreparedStatement pstmt = conn.prepareStatement(stm1);       

        pstmt.setLong(1, andesc.getUid());
        pstmt.setLong(2, System.currentTimeMillis());
        pstmt.setString(3, convertString(andesc.getSid()));
        pstmt.setString(4, convertString(andesc.getLote()));
        pstmt.setString(5, convertString(andesc.getNotas()));
        pstmt.setString(6, convertString(andesc.getUser()));
        pstmt.setString(7, convertString(andesc.getProfile()));
        pstmt.setString(8, convertString(andesc.getBlaine()));
        pstmt.setString(9, convertString(andesc.getPorosidade()));
        pstmt.setString(10, convertString(andesc.getDensidade()));
        pstmt.setString(11, convertString(andesc.getMassa_ensaio()));
        pstmt.setString(12, convertString(andesc.getKfactor()));
        pstmt.setString(13, convertString(andesc.getVolume_camada()));
        pstmt.setString(14, convertString(andesc.getTemperature()));
        pstmt.setString(15, convertString(andesc.getCalibid()));
        pstmt.setString(16, convertString("0"));
        pstmt.setString(17, convertString(andesc.getMedia()));
        pstmt.setString(18, convertString(andesc.getRsd()));
        
        StringBuilder sb = new StringBuilder();
        for (String st : andesc.getTempos()){
            sb.append(st);
            sb.append(":");
        }
        String time = sb.toString();
        
        if (time.isEmpty()){
            pstmt.setString(19, time);
        }
        else{
            pstmt.setString(19, time.substring(0, time.length()-1));
        }
   
        pstmt.executeUpdate();
        log.info(String.format("Run %d was stored to DB", andesc.getUid()));  
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
        
        service_thread = new DBService.SMThread(smqueue, statesptr, this);
        
        Class<?> c = this.getClass();     
        smstate annot;
        StateDescriptor stdesc;
 
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
        
//        sys_services.addSignalListener(this);
//        addSignalListener(sys_services);
        
        new Thread(service_thread).start();
    }
    
    
    
    
    private class SMThread extends Thread {
    
        
        private String state;
        private ArrayDeque <String>states_stack;
        private LinkedHashMap<String, StateDescriptor> statesptr;
        
        private boolean done;
        protected BlockingQueue<SMTraffic> tqueue;
        private String cmd;
        
        private DBService parent;
        private SMTraffic smm;
        private VirnaPayload payload;
        
        private Method m;
        StateDescriptor stdesc;
      
        protected long start_tick =  System.currentTimeMillis();
 

        public SMThread(BlockingQueue<SMTraffic> tqueue, LinkedHashMap<String, StateDescriptor> _statesptr, DBService parent ) {
            
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
                log.log(Level.SEVERE,String.format("Database State Machine failed with %s @ state %s", ex.toString(), state));
                
//                ImageIcon image = ImageUtilities.loadImageIcon("com/opus/pp100/db-schema-icon.png", true);
//        
//                Notification noti = NotificationDisplayer.getDefault().notify(
//                    "Kernel Database da Virna 3 detectou anormalidade",
//                    image,
//                    String.format("Falha na máquina de estados em : %s", state),
//                    Lookups.forPath("NotificationActions").lookup(ActionListener.class)
//                );
                
                startService();
            }

        }

        public void setDone(boolean done) {
            if (done) log.log(Level.FINE, "Controller is Stopping Service");
            this.done = done;
        }
            
    };
    
    
    
    // =========================================================================================================================
    // ======================================== STATES    ======================================================================
    // =========================================================================================================================
    
   
    @smstate (state = "RESET")
    public boolean st_reset(SMTraffic smm){
        
        //log.log(Level.INFO, String.format("DDBService RESET state activated with payload : %s", smm.getPayload().vstring));
        service_thread.states_stack.push("IDLE");
        service_thread.states_stack.push("CONFIG");
        service_thread.states_stack.push("INIT");
        return true;
    }
    
    @smstate (state = "INIT")
    public boolean st_init(SMTraffic smm){
        //log.log(Level.INFO, String.format("DBService INIT state activated with payload : %s", smm.getPayload().vstring));
        return true;
    }
    
    @smstate (state = "CONFIG")
    public boolean st_config(SMTraffic smm){
        
        //log.log(Level.INFO, String.format("DBService Config state activated with payload : %s", smm.getPayload().vstring));
        
        String dbfile = Config.getInstance().getDatabase_file();
  
        Path p = Paths.get(dbfile);
        File f = p.toFile();
        
        try {
            if (!f.exists()){
                dbfile = Config.getInstance().getDatabase_dir()+"acp1" ;
                PicnoUtils.exportResource ("com/opus/pp100/acp1", dbfile);
            }
            String url = "jdbc:sqlite:" + dbfile;
            conn = DriverManager.getConnection(url);
            if (!conn.isClosed()){
                log.info(String.format("Connection to Database has been established -> is using now the file @ %s and the flavor is SQLite", dbfile));
            }
            
        } catch (Exception ex) {
            log.warning(String.format("Failed do connet to database due : ", ex.getMessage()));
        }
        
        return true;
    }
   
    @smstate (state = "HASRECORD")
    public boolean st_hasRecord(SMTraffic smm){
        
        Long uid;
        VirnaPayload payload = smm.getPayload();
        
        uid = payload.long1;
        if (uid == null){
            uid = Long.parseLong(payload.vstring);    
        }
        
        VirnaServiceProvider vsp = payload.getCaller();
        try {
            Boolean hasrecord = locateRecord(uid);
            if (vsp != null){
                vsp.processSignal(new SMTraffic(0l, 0l, 0, payload.getCallerstate(), 
                                        new VirnaPayload()
                                        .setFlag1(hasrecord)
                                        .setLong1(uid)
                                        .setObject(payload.vobject)        
                                        .setCaller(this)
                                        .setCallerstate("HASRECORD")
                ));
            }    
        } catch (SQLException ex) {
            log.warning(String.format("Failed to search for record"));
            if (vsp != null){
                String message = "<html>OOPS ! Não foram encontradas <b>analises</b> conf. o critério, provavelmente :"
                        + "<ul>"
                        + "<li>Pode haver um erro de digitação na especificação do critério</li>"
                        + "<li>Os qualificadores de tempo, identificação e tipo podem ser inter excludentes</li>"
                        + "<li>Realmente não há tais registros, tente um filtro mais abrangente</li>"
                        + "</ul>"
                    + "</html>"; 
                vsp.processSignal(new SMTraffic(0l, 0l, 0, payload.getCallerstate(), 
                                        new VirnaPayload()
                                        .setString("Falha no acesso ao banco de dados&"+ message)
                                        .setFlag2(true)
                                        .setObject(payload.vobject)        
                                        .setCaller(this)
                                        .setCallerstate("HASRECORD")
                ));
            }
        }
        
        
        return true;
    }
    
    @smstate (state = "LOADANALISE")
    public boolean st_loadAnalise(SMTraffic smm){
        
        VirnaPayload payload = smm.getPayload();
        Long id = payload.long1;
        
        try {
            AnaliseDescriptor ad = loadAnalise(id);
            if (ctrl != null){
                ctrl.loadRecord(ad);
            }
            else{
                log.severe("unable to talk to controller");
            }
            
        } catch (SQLException ex) {
//            ImageIcon image = ImageUtilities.loadImageIcon("com/opus/pp100/db-schema-icon.png", true);
//            Notification noti = NotificationDisplayer.getDefault().notify(
//                String.format("Falha na carga da analise com id=%d do banco de dados", id),
//                image,
//                ex.getMessage(),
//                Lookups.forPath("NotificationActions").lookup(ActionListener.class)
//            );
        }
        
        return true;
    }
    
    
    
    @smstate (state = "STOREANALISE")
    public boolean st_storeAnalise(SMTraffic smm){
         
        VirnaPayload payload = smm.getPayload();
        AnaliseDescriptor ad = (AnaliseDescriptor)payload.vobject;
        VirnaServiceProvider vsp = payload.getCaller();
        
        try {
            deleteAnalise(ad);
            storeAnalise(ad);
            log.warning(String.format("Analise %d/%s was registered on database", ad.getUid(), ad.getSid()));
            if (vsp != null){
                String message = String.format("A analise \'%s\' foi registrada no banco de dados com a chave %d", ad.getSid(), ad.getUid());
                vsp.processSignal(new SMTraffic(0l, 0l, 0, payload.getCallerstate(), 
                                        new VirnaPayload()
                                        .setString(message)
                                        .setFlag1(true)
                                        .setCaller(this)
                                        .setCallerstate("STOREANALISE")
                ));
            }
        } 
        catch (Exception ex) {
            log.warning(String.format("Failed to store record on Database"));
            if (vsp != null){
                vsp.processSignal(new SMTraffic(0l, 0l, 0, payload.getCallerstate(), 
                                        new VirnaPayload()
                                        .setString("Falha no acesso ao banco de dados&Erro ao registrar analise no banco")
                                        .setFlag2(true)
                                        .setCaller(this)
                                        .setCallerstate("STOREANALISE")
                ));
            }
        }
        return true;
    }
    
    
    
    
    
    @smstate (state = "LOADANALISES")
    public boolean st_loadAnalises(SMTraffic smm){
        
        VirnaPayload payload = smm.getPayload();
        Object caller = payload.getCaller();
        
        if (caller instanceof DBService){
            String sql = payload.vstring;
            try {
                ArrayList<DBRecord> records = loadRecords(sql);
                if (records.isEmpty()){
//                    NotifyDescriptor nd = new NotifyDescriptor.Message(
//                        "<html>OOPS ! Não foram encontradas analises conf. o critério, provavelmente : "
//                            + "<ul>"
//                            + "<li>Pode haver um erro de digitação na especificação do critério</li>"
//                            + "<li>Os qualificadores de tempo, identificação e lote podem ser inter excludentes</li>"
//                            + "<li>Realmente não há tais registros, tente um filtro mais abrangente</li>"
//                            + "</ul>"
//                        + "</html>", 
//                    NotifyDescriptor.ERROR_MESSAGE);
//                    Object retval = DialogDisplayer.getDefault().notify(nd);   
                }
//                else{
//                    dbtc.updateTable(records);
//                }
                dbtc.updateTable(records);
            } catch (SQLException ex) {
//                ImageIcon image = ImageUtilities.loadImageIcon("com/opus/pp100/db-schema-icon.png", true);
//                Notification noti = NotificationDisplayer.getDefault().notify(
//                    "Falha na consulta de registros do banco de dados",
//                    image,
//                    ex.getMessage(),
//                    Lookups.forPath("NotificationActions").lookup(ActionListener.class)
//                );
            }
        }
      
        return true;
    }
    
    
    
    
    
    
}























//Notification noti = NotificationDisplayer.getDefault().notify(
//                "My first notification...",
//                ImageUtilities.loadImageIcon("com/galileo/netbeans/module/info16.png", true),
//                "... which disappears in a few seconds",
//                Lookups.forPath("NotificationActions").lookup(ActionListener.class)
//            );
//            noti.notify();