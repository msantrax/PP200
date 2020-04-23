/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;


import com.opus.syssupport.propertylink;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.fxsupport.FXFConfirmationDialog;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.FXFHeaderband;
import com.opus.fxsupport.FXFWindowManager;
import com.opus.fxsupport.StatusMessage;
import com.opus.syssupport.PicnoUtils;
import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.SignalListener;
import com.opus.syssupport.StateDescriptor;
import com.opus.syssupport.VirnaPayload;
import com.opus.syssupport.VirnaServiceProvider;
import com.opus.syssupport.smstate;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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


public class Controller implements SignalListener, VirnaServiceProvider, PropertyChangeListener {

    private static final Logger log = Logger.getLogger(Controller.class.getName());
    
    private Controller.SMThread service_thread;     
    private LinkedBlockingQueue<SMTraffic> smqueue;
    private LinkedHashMap<String, StateDescriptor> statesptr ;
    private final ScheduledExecutorService scheduler;
 
    private FX1Controller anct;
    private boolean fx1open = false;
    
    private FX3Controller dbct;
    private boolean fx3open = false;
    
    private DBService dbservice;

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
    
    
    // ==================================================== Properties management =============================================
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        log.info(String.format("Controller Property change called with : %s", e.getPropertyName()));
//        if (source == an) {
//            
//            anclean = false;
//        }
       
    }
    
    public void varCallback(Control control, String value, boolean validated, boolean analise){
        
        Method m;
        PropertyLinkDescriptor pld;
        
//        if (analise){
//            pld = getLinkDescriptor((FXFField)control, ana_proplink_uimap);
//        }
//        else{
//            pld = getLinkDescriptor((FXFField)control, cal_proplink_uimap);
//        }
//        
//        if (pld != null){
//            pld.setAuxiliar("");
//            if (!validated){
//                //log.info(String.format("Var callback said %d is invalid", control.hashCode()));
//                if (pld.isValid()){
//                    pld.setValid(false);
//                    String callback = pld.getCallstate();
//                    if (!callback.equals("NONE") && !nocalc){
//                        processSignal(new SMTraffic(0l, 0l, 0, callback, 
//                                   new VirnaPayload().setObject(pld)));
//                    }
//                }
//            }
//            else{
//                m = pld.getMethod();
//                try {
//                    if (m != null){
//                        Boolean ret = (Boolean)m.invoke(pld.getInstance(), value);
//                    }
//                    pld.setValid(true);
//                    String callback = pld.getCallstate();
//                    if (!callback.equals("NONE") && !nocalc){
//                        processSignal(new SMTraffic(0l, 0l, 0, callback, 
//                                   new VirnaPayload().setObject(pld)));
//                    }
//                    //log.info(String.format("var callback %d approved value %s", control.hashCode(), value));
//                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                    log.warning(String.format("Failed to update property due %s", ex.getMessage()));
//                }
//            }
//        }
    }
    
    
 
    // ===================================================== Controllers ===================================================
 
    public void setFXANController (FX1Controller controller){
        this.anct = controller;   
        //mapProperties(an, true);
    }
    
    public boolean isFX1open() { return fx1open;}
    public void setFX1open (boolean open) { fx1open = open;}

    public void setFXDBController (FX3Controller controller){
        this.dbct = controller;
    }
    
    
    public LinkedBlockingQueue<SMTraffic> getQueue(){ return smqueue;}
    
    
    
    public void updateUIWidget (String key, FXFField field, boolean analise){
        
        PropertyLinkDescriptor pld1, pld2;
        
//        if (analise){
//            pld1 = ana_proplink_uimap.get(key);
//        }
//        else{
//            pld1 = cal_proplink_uimap.get(key);
//        }
//        
//        if (pld1 != null){
//            pld1.setFxfield(field);
//            if (analise){
//                pld2 = ana_proplink_modelmap.get(key);
//            }
//            else{
//                pld2 = cal_proplink_modelmap.get(key);
//            }
//            
//            if (pld2 != null){
//                pld2.setFxfield(field);
//            }
//        }
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
                if (statesptr.get(annot.state()) != null){
                    log.info(String.format("State name colision :  %s @ %s", stdesc.getSID(), c.getName()));
                    System.exit(1);
                }
                else{
                    statesptr.put(annot.state(), stdesc);
                    //log.info(String.format("Registering state %s @ %s", stdesc.getSID(), stdesc.getClazz().getName()));
                }
            }
        }
        new Thread(service_thread).start();
    }
    
    
    
    public void loadStates(Class c, Object instance){
        
        smstate annot;
        StateDescriptor stdesc;
        
        
        // Loading FX1
        //c = FX1SMachine.getInstance().getClass();
        
        for (Method mt : c.getDeclaredMethods()){
            annot = mt.getAnnotation(smstate.class);
            if (annot != null){
                stdesc =   new StateDescriptor().setClazz(c)
                                                .setInstance(instance)
                                                .setMethod(mt)
                                                .setSID(annot.state());
                if (statesptr.get(annot.state()) != null){
                    log.info(String.format("State name colision :  %s @ %s", stdesc.getSID(), c.getName()));
                    System.exit(1);
                }
                else{
                    statesptr.put(annot.state(), stdesc);
                    //log.info(String.format("Registering state %s @ %s", stdesc.getSID(), stdesc.getClazz().getName()));
                }
            }
        }
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
                    pushState(smm.getState(), smm);
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
                            if (!state.equals("HOUSEKEEP")){
                                log.log(Level.INFO, String.format("Activating state %s @ %d", state, System.currentTimeMillis()-start_tick));
                            }
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
     
        SMTraffic alarm_config = new SMTraffic(0l, 0l, 0, "HOUSEKEEP", this.getClass(),
                        new VirnaPayload()
                );
        setAlarm (-1l, -1, alarm_config, 500l, 500l);

//        smqueue.offer(new SMTraffic(0l, 0l, 0, "FX1TEST", this.getClass(),
//                                   new VirnaPayload()
//                                           .setString("Called from Controller")
//        ));
        
        
//        loadProfileList();
//        loadProfile(PicnoUtils.user.getProfile());
//        logtime = System.currentTimeMillis();
//        loadLastCalibration();
        
//        top.enableTasks(PicnoUtils.user.isMaycalibrate(), 
//                        PicnoUtils.user.isMay_search());
        
        
        //loadProfile(PicnoUtils.user.getProfile());
          
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
            smqueue.offer(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                    new VirnaPayload().setString(
                                "Gerente de Administração&" + "INFO&" +
                                String.format("Previlégios de Administrador foram removidos do usuário %s&", user) +
                                "O usuário deverá autenticar-se novamente"        
            )));
        }
        else{
            if (payload.getFlag1()){
                sudotimeout = 30;
                SMTraffic alarm_config = new SMTraffic(0l, 0l, 0, "SUDOTIMEOUT", this.getClass(),
                        new VirnaPayload().setString(user)
                );
                sudoalarmid = getAlarmID();
                setAlarm (-1l, sudoalarmid, alarm_config, 5000l, 5000l);
                log.info(String.format("Controller granted SU prev. to %s", user));
                smqueue.offer(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                        new VirnaPayload().setString(
                                    "Gerente de Administração&" + "INFO&" +
                                    String.format("Previlégios de Administrador foram concedidos ao usuário %s&", user) +
                                    "O período de inatividade é de 150 seg."        
                )));
            }
            else{
                log.info(String.format("Wrong SU password on user %s", user));
                smqueue.offer(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
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
            smqueue.offer(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
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
        //log.info(String.format("Nav Action = %s", cmd));
        FXFWindowManager wm = FXFWindowManager.getInstance();
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
              try {
                  if (cmd.equals("Back")){
                      wm.activateWindow("FX4");
                  }
                  else if (cmd.equals("List")){
                      System.exit(0);
                      //wm.activateWindow("FX3");
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
    
    
    
    
    
//    
//    
//    
//    @smstate (state = "LOCKUI")
//    public boolean st_lockUI(SMTraffic smm){
//        
//        locked = (smm.getPayload().int1 == 1);
//        
//        if (smm.getPayload().int2 == 0){
//            anct.lockUI(locked);
//        }
//        else{
//            //calct.lockUI(locked);
//        }
//        
//        return true;
//    }
//    
//    
//    
//    @smstate (state = "LOADGUI")
//    public boolean st_loadGUI(SMTraffic smm){
//        
//        //service_thread.pushState ("LOADCONFIG", null);
//        
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
////                Mode m = WindowManager.getDefault().findMode("explorer");              
////                ExplorerTopComponent extc = ExplorerTopComponent.findInstance();
////                extc.setName("NAVTREE");
////                m.dockInto(extc);
////                extc.open();
////                extc.requestActive();
////                log.info(String.format("Explorer %d was created", extc.hashCode()));
//            }    
//        });
//        
//        return true;
//    }
//    
    
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