/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opus.fxsupport.DialogMessageBuilder;
import com.opus.fxsupport.FXFAnaliseListItem;
import com.opus.fxsupport.FXFCheckListViewNumber;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.FXFHeaderband;
import com.opus.fxsupport.FXFListDialogBuilder;
import com.opus.fxsupport.FXFWindowManager;
import com.opus.fxsupport.LauncherItem;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.fxsupport.StatusMessage;
import com.opus.syssupport.ActivityMachine;
import com.opus.syssupport.Config;
import com.opus.syssupport.PicnoUtils;
import com.opus.syssupport.ProfileResources;
import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.VirnaPayload;
import com.opus.syssupport.VirnaServiceProvider;
import com.opus.syssupport.smstate;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 *
 * @author opus
 */
public class FX2SMachine extends ActivityMachine{

    private static final Logger log = Logger.getLogger(FX2SMachine.class.getName());
  
    
    private Controller ctrl = Controller.getInstance();
    private FX2Model current_model;
    private FX2Controller anct;
    private DBService dbservice;
    
    private boolean nocalc = false;
    
    
    public static final Double visco_a0 = 1.8076923000E-06;
    public static final Double visco_a1 = 1.3084615380E-03;
    
    private static FX2SMachine instance; 
    public static FX2SMachine getInstance(){
        if (instance == null) {instance = new FX2SMachine();}
        return instance;
    }

    
    public FX2SMachine() {
        models = new LinkedHashMap<>();
        ctrl.loadStates(this.getClass(), this); 
    }
    
    // Application controller link 
    private VirnaServiceProvider absctrl;
    public void setAppController (VirnaServiceProvider ctrl){
        this.absctrl = ctrl;
        this.dbservice = Controller.getInstance().getDbservice();
    }
    
 
    @Override
    public void activateModel (String id){
        log.info(String.format("Activating model %s ", id));
        FX2Model fx2m = (FX2Model)models.get(id);
        if (fx2m != null){
            current_model = fx2m;
            anct = (FX2Controller)fx2m.getFXCtrl();
        }
        else{
            log.severe(String.format("Failed to activate model %s", id));
        }
        
    }
    
     public void varCallback(Control control, String value, boolean validated, boolean analise){
        
        Method m;
        PropertyLinkDescriptor pld;
        
        pld = getLinkDescriptor((FXFField)control, current_model.getProplink_uimap());
        
        if (pld != null){
            pld.setAuxiliar("");
            if (!validated){
                //log.info(String.format("Var callback said %d is invalid", control.hashCode()));
                if (pld.isValid()){
                    pld.setValid(false);
                    String callback = pld.getCallstate();
                    if (!callback.equals("NONE") && !nocalc){
                        ctrl.processSignal(new SMTraffic(0l, 0l, 0, callback, this.getClass(),
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
                        ctrl.processSignal(new SMTraffic(0l, 0l, 0, callback, this.getClass(),
                                   new VirnaPayload().setObject(pld)));
                    }
                    //log.info(String.format("var callback %d approved value %s", control.hashCode(), value));
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    log.warning(String.format("Failed to update property due %s", ex.getMessage()));
                }
            }
        }
    }
  
 
    public void updateCalibCanvas(){
        
//        nocalc=true;
        
        
//        anct.updateField ("it_densidade", cal.getDensidade(), true);
//        anct.updateField ("it_porosidade", cal.getPorosidade(), true);
//        anct.updateField ("it_volume", cal.getVolume_camada(), false);
//        
//        anct.updateField ("it_analiseaverage", cal.getMedia(), false);
//        anct.updateField ("it_analisersd", cal.getRsd(), false);
//        
//        
//        anct.updateField ("it_sid", cal.getSid(), false);
//        anct.updateField ("it_filtro", cal.getFiltro(), false);
//        anct.updateField ("it_notas", cal.getNotas(), false);
//        
//        
//        anct.updateField ("it_kfactor", cal.getKfactor(), false);
//        anct.updateField ("it_mass", cal.getMassa_ensaio(), true);
//        
//        anct.updateField ("it_area", cal.getArea(), false);
//        anct.updateField ("it_temperature", cal.getTemperature(), false);
//        
//        anct.updateAnaliseTime(cal.getTimestamp());
//        
//        anct.initTimeList();
//        for (String atime : cal.getTempos()){
//            anct.addTimeEntry(atime);
//        }
        
//        nocalc = false;  
    }
    
    
    // ============================================= PROFILE & LOAD ACTIONS ==================================================
    
    
    @smstate (state = "DELETECALPROFILE")
    public boolean st_deleteCalProfile(SMTraffic smm){
        
        
        ProfileCal pbl = (ProfileCal)current_model.getProfile();
        FXFHeaderband hb = FXFWindowManager.getInstance().getHeaderBand();
        ProfileResources prs = PicnoUtils.profile_resources;
        
        hb.hideMenubox();
        
        if (pbl.getArgument().contains("default")){
            hb.showSnack("Ooops !! - Esse perfil é o default para a atividade, não pode ser removido");
            return true;
        }
        
        ArrayList<LauncherItem> umap = prs.getUseMap(pbl);
        if(umap == null || umap.isEmpty()){
            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                    new VirnaPayload().setString(
                                "Gerente de Administração&" + "SEVERE&" +
                                String.format("Houve um erro na varredura de uso do perfil : %s&", pbl.getLabel()) +
                                "Verifique se vc tem permissões para essa operação."        
            )));
        }
        else if (umap.size() > 1){
            hb.showSnack(String.format("Ooops !! - Esse perfil tambem é usado pelo menos pelo lançador %s", umap.get(1).getIconlabel()));
            return true;
        }
        else{
            LauncherItem li = umap.get(0);
            
            String removed = pbl.getLabel();
            ProfileCal nprof = (ProfileCal)prs.removeResource(pbl);
            if (nprof != null){
                li.setArgument(nprof.getArgument());
                PicnoUtils.saveLauncher(li);
                
                
                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                                new VirnaPayload().setObject(
                                new StatusMessage(String.format("O perfil %s foi removido com sucesso", removed), 3000))
                ));
            }
            else{
                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                    new VirnaPayload().setString(
                                "Gerente de Administração&" + "SEVERE&" +
                                String.format("Houve um erro na remoção do Perfil : %s&", pbl.getLabel()) +
                                "Verifique se vc tem permissões para essa operação."        
                )));
            }
        }
        
        return true;
    }
    
    
    
    @smstate (state = "CLONECALPROFILE")
    public boolean st_cloneCalProfile(SMTraffic smm){

        ProfileCal pbl = (ProfileCal)current_model.getProfile();        
        FXFHeaderband hb = FXFWindowManager.getInstance().getHeaderBand();
        hb.hideMenubox();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                SimpleStringProperty result = hb.showInputDialog("Informe o nome do novo perfil", pbl.getLabel());
                if (result != null){
                    result.addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue <? extends String> prop, String ov, String nv) {
                            //log.info(String.format("Valor : %s ", nv));
                            hb.hideInputDialog();
                            if (nv != null && !nv.isEmpty() && !nv.equals("cancel")){
                                ProfileResources pr = PicnoUtils.profile_resources;
                                if (pr.getByLabel(nv) != null){
                                    hb.showSnack("Ooops !! - O nome do perfil já existe.");
                                }
                                else{
                                    pbl.updateArgument(nv);
                                    pbl.setLabel(nv);
                                    String fout = pr.cloneResource(pbl);
                                    if (fout == null){
                                        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                                                new VirnaPayload().setString(
                                                            "Gerente de Administração&" + "SEVERE&" +
                                                            String.format("Não foi possivel criar novo perfil como : %s&", nv) +
                                                            "Verifique se vc tem permissões para essa operação."        
                                        )));
                                    }
                                    else{
                                        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                                                            new VirnaPayload().setObject(
                                                            new StatusMessage("Criado novo Perfil : "+ nv, 2000))
                                        ));
                                    }
                                }
                            }
                        }
                    });
                } 
            }
        });       
        
        return true;
    }
    
    
    // ====================================== ANALYSIS UPDATE ==============================================================
    
    public void storeCalibrationAction(){
            
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "STORECALIBRATIONFILE", this.getClass(),
                                    new VirnaPayload()                         
        )); 
    }
    
    private boolean storeCalibrationFile(Path p) {
        
        try {
            GsonBuilder builder = new GsonBuilder(); 
            builder.setPrettyPrinting(); 
            Gson gson = builder.create();
            String sjson = gson.toJson(current_model.getCal());

            Files.write(p, sjson.getBytes(StandardCharsets.UTF_8));
            log.info(String.format("================Stored calibration on %s: \n\r", p.toString()));

            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                    new VirnaPayload().setObject(
                    new StatusMessage(String.format("Calibração foi gravada no arquivo %s", p.getFileName().toString()), 3000))
            ));

            current_model.setCalibclean(true);

        } catch (IOException ex) {
            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                new VirnaPayload().setString(
                            "Gerente de Administração&" + "SEVERE&" +
                            String.format("Houve um erro na gravação da arquivo de calibração %s&", p.toFile().getAbsolutePath()) +
                            "Verifique se vc tem permissões para essa operação."        
            )));
            return false;
        }
        return true;
    }
    
    
    private File getCalblFile(Long timestamp){
       
        String filename = String.format("calbl-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.json", timestamp);
        Path p = Paths.get(Config.getInstance().getExport_dir()+filename);
        return p.toFile();
    }
    
    @smstate (state = "STORECALIBRATIONFILE")
    public boolean st_storeCalibrationFile(SMTraffic smm){
        
//        String filename = String.format("anabl-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.json", current_model.getAn().getUid());
//        Path p = Paths.get(Config.getInstance().getExport_dir()+filename);
        File f = getCalblFile(current_model.getCal().getUid()); //p.toFile();
        
        if (f.exists()){
            FXFWindowManager wm = FXFWindowManager.getInstance();
            FXFHeaderband hb = wm.getHeaderBand();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    SimpleStringProperty result = hb.showQuestionDialog("Controle de Calibrações", 
                            new DialogMessageBuilder()
                                    //.setHeight(400.0)
                                    .enableButton("ok", "Sobrescrever", "overwrite", true)
                                    .enableButton("cancel", "Cancelar", "cancel", true)
                                    .enableButton("aux", "Novo Arquivo", "new", true)
                                    .add("Essa Calibração já tem um arquivo no diretório de exportação.\n", "-fx-font-size: 12px;")
                                    .add("Você poderá tomar as seguintes atitudes.\n", "")
                                    .addSpacer(0)
                                    .add("\t\u2022 Descartar resultados antigos e sobrescrever (irrevogavel!!)\n", "")
                                    .add("\t\u2022 Gravar como outra calibração.\n", "")
                                    .add("\t\u2022 Cancelar a operação.", "")
                    );
                    if (result != null){
                        
                        result.addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue <? extends String> prop, String ov, String nv) {
                                //log.info(String.format("Valor : %s ", nv));
                                hb.hideInputDialog();

                                if (nv.equals("new")){
                                    Long nuid = System.currentTimeMillis();
                                    current_model.getCal().setUid(nuid);
                                    current_model.getCal().setTimestamp(nuid);
                                    File f = getCalblFile(current_model.getCal().getUid()); 
                                    if (!storeCalibrationFile(f.toPath())) return ;                               }    
                                else if (nv.equals("cancel")){
                                    return;
                                }
                                else{
                                    if (!storeCalibrationFile(f.toPath())) return;
                                    ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                                        new VirnaPayload().setString(
                                                    "Gerente de Administração&" + "INFO&" +
                                                    String.format("O arquivo de analise %s foi sobrescrito&", f.getName()) +
                                                    "Pode ter havido edição de resultados, sua flag de 'editado' foi marcada"        
                                    )));
                                }
//                                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "STORECALIBRATIONRECORD", this.getClass(),
//                                                        new VirnaPayload()
//                                                        .setObject(current_model.getCal())
//                                                        .setCaller(FX2SMachine.instance)
//                                                        .setCallerstate("CHECKRECORD")
//                                ));
                            }
                        });
                    }
                }
            });  
        }
        else{
            if (storeCalibrationFile(f.toPath())){
//                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISERECORD", this.getClass(),
//                                        new VirnaPayload()
//                                        .setObject(current_model.getAn())
//                                        .setCaller(FX1SMachine.instance)
//                                        .setCallerstate("CHECKRECORD")
//                ));
            }
        }
         
        return true;
    }
 
    
    
    
    // ===================================================  LOAD ANALYSIS SERVICES ============================================
    
    public void loadFileAction(){
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "LOADCALFILE", this.getClass(),
                                new VirnaPayload()
                                .setObject(current_model.getCal())
                                .setCaller(ctrl)
                                .setCallerstate("")
        ));    
    }
    
    
    public void loadFile (String path){
        
        Path p = Paths.get(path);
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(p);
            String json_out = new String(bytes, StandardCharsets.UTF_8);
            GsonBuilder builder = new GsonBuilder(); 
            builder.setPrettyPrinting(); 
            Gson gson = builder.create();
            CalibDescriptor adesc = gson.fromJson(json_out, CalibDescriptor.class);
            current_model.setCal(adesc);
            
            log.info(String.format("Calibration loaded from %s", p.toFile().getAbsolutePath()));
            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                    new VirnaPayload().setObject(
                    new StatusMessage(String.format("Calibração foi carregada"), 3000))
            ));
            //updateCalCanvas();

        } catch (Exception ex) {
            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                            new VirnaPayload().setString(
                                 "Interface com Sistema&" + "SEVERE&" +
                                 String.format("Ocorreu uma falha na carga do arquivo %s&", path) +
                                 "Verifique se vc tem permissões para essa ação"        
                            )
            ));   
        }
    }
    
    
    @smstate (state = "LOADCALFILE")
    public boolean st_loadCalFile(SMTraffic smm){
        
        String file = "";
        CalibDescriptor and;
        VirnaPayload payload = smm.getPayload();
        Object caller = payload.getCaller();
       
        FXFWindowManager wm = FXFWindowManager.getInstance();
        FXFHeaderband hb = wm.getHeaderBand();
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
        
                SimpleStringProperty result = hb.showListDialog("Escolha o arquivo a carregar", 
                    new FXFListDialogBuilder()
                            .enableButton("cancel", "Cancelar", "cancel", true)
                            .addFiles("/home/acp/PP200/Export", "formated_time", "calbl-")
                );

                if (result != null){
                    result.addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue <? extends String> prop, String ov, String nv) {
                            log.info(String.format("File Selection = %s ", nv));
                            hb.hideInputDialog();
                            if (nv.equals("cancel") || nv.equals("ok")){
                                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                                        new VirnaPayload().setObject(
                                        new StatusMessage(String.format("A carga de arquivo de calibração foi abortada"), 3000))
                                ));
                            }
                            else{
                                loadFile(nv);
                            }
                        }
                    });
                }
            }
        });
        
        return true;
    }
 
    
    // ================================== ANALISE CYCLES MANAGEMENT =========================================================
    
    public void newCalibration(){      
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "NEWCALIBRATION", this.getClass(),
                                   new VirnaPayload().setObject(current_model.getCal())
        ));
    }
    
    public void updateCalCanvas(){
        
        nocalc=true;
        
        anct.updateField ("it_densidade", current_model.getCal().getDensidade(), true);
        anct.updateField ("it_porosidade", current_model.getCal().getPorosidade(), true);
        anct.updateField ("it_volume", current_model.getCal().getVolume_camada(), false);
        anct.updateField ("it_temperature", current_model.getCal().getTemperature(), false);
        anct.updateField ("it_area", current_model.getCal().getArea(), false);
        
        
        anct.updateField ("it_analiseaverage", current_model.getCal().getMedia(), false);
        anct.updateField ("it_analisersd", current_model.getCal().getRsd(), false);
        
        
        anct.updateField ("it_sid", current_model.getCal().getSid(), false);
        anct.updateField ("it_filtro", current_model.getCal().getFiltro(), false);
        anct.updateField ("it_notas", current_model.getCal().getNotas(), false);
        
        anct.updateField ("it_kfactor", current_model.getCal().getKfactor(), false);
        anct.updateField ("it_mass", current_model.getCal().getMassa_ensaio(), true);
        
        anct.updateAnaliseTime(current_model.getCal().getTimestamp());
        
        ArrayList<String> runs = current_model.getCal().getTempos();
        if (!runs.isEmpty()){
            FXFCheckListViewNumber<String> rc = anct.getBlaineDevice().getRunControl();
            //log.info("Loading runs...");
            for (String s : runs){
                rc.addEntry(s);
            }
            anct.setUIState("LOADED");
        }
        else{
            anct.resetDevices();
            anct.setUIState("FRESH_ANALISYS");
        }
       
        nocalc = false;  
    }
    
    
    private void loadCalDefaults(){
        
        current_model.getCal().loadDefaults();
        
        current_model.getCal().setUser(PicnoUtils.user_name);
        current_model.getCal().setProfile(current_model.getProfile().getArgument());
        
        current_model.getCal().setDensidade(current_model.getProfile().getDescriptor("it_densidade").getDefault_value());
        current_model.getCal().setPorosidade(current_model.getProfile().getDescriptor("it_porosidade").getDefault_value());
        current_model.getCal().setArea(current_model.getProfile().getDescriptor("it_area").getDefault_value());
        current_model.getCal().setVolume_camada(current_model.getProfile().getDescriptor("it_volume").getDefault_value());
        
   
        current_model.getCal().getTempos().clear();
        current_model.getCal().setMedia("");
        current_model.getCal().setRsd("");
       
        
       
        current_model.getCal().setSid(current_model.getProfile().getDescriptor("it_sid").getDefault_value());
        current_model.getCal().setFiltro(current_model.getProfile().getDescriptor("it_filtro").getDefault_value());
        current_model.getCal().setNotas(current_model.getProfile().getDescriptor("it_notas").getDefault_value());

        
        current_model.getCal().setKfactor(current_model.getCal().getKfactor());
        current_model.getCal().setVolume_camada(current_model.getCal().getVolume_camada());
       

        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "CALCCALMASS", this.getClass(),
                        new VirnaPayload()
        ));
        
    }
    
    
    @smstate (state = "NEWCALIBRATION")
    public boolean st_newCalibration(SMTraffic smm){
        
        log.info("New CALIBRATION called ...");

        loadCalDefaults();
        updateCalCanvas();
        current_model.setCalibclean(true);
 
        return true;
    }
    
    
    @smstate (state = "CALIBRATIONDONE")
    public boolean st_calibrationDone(SMTraffic smm){
 
        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
        
        if (pld!= null){
            if (!pld.isValid()){
                anct.updateField ("it_constantek", "", false);
                anct.setUIState("DONE_INVALID");
                return true;
            }
        }
               
        //FXFBlaineDeviceController bdc = anct.getBlaineDevice();
        //FXFCheckListViewNumber<String> runs = anct.getBlaineDevice().getRunControl();
        ArrayList<FXFAnaliseListItem> ritens = anct.getBlaineDevice().getRunControl().getCheckedItems();
        ArrayList<String> an_runs = current_model.getCal().getTempos();
        
        if (!ritens.isEmpty()){
            for (FXFAnaliseListItem s : ritens){
                an_runs.add(String.format("%5.2f", s.getValue()));
            }        
            anct.setUIState("DONE_VALID");
        }
        else{
            anct.setUIState("DONE_INVALID");
        }
        return true; 
    }
    
    
    
    
    
    
    // ========================================== CALCULATION SERVICES ===============================================
    
    private Double getVisco (Double temp){
        
        Double ret;
        ret = ((temp * visco_a0) + visco_a1) * 1000;
        return ret;
    }
    
    private Double getDoublePValue (String key, String svalue, boolean analise) {
        
        Double d;
        PropertyLinkDescriptor pld;
       
        pld = current_model.getProplink_modelmap().get(key);
       
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
    
    
    @smstate (state = "UPDATECALTIME")
    public boolean st_updateCalTime(SMTraffic smm){
        
        if (smm.getPayload().getFlag1()){
            Double average = (Double)smm.getPayload().vobject;
            Double rsd = (Double)smm.getPayload().getAuxiliar();
            
            anct.updateField ("it_analiseaverage", String.format(Locale.US, "%4.1f", average), true);
            anct.updateField ("it_analisersd", String.format(Locale.US, "%5.3f", rsd), true);
        }
        else{
            anct.updateField ("it_analiseaverage","", true);
            anct.updateField ("it_analisersd", "", true);
        }
        
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
        
        log.info("Calc kfactor called.......................");
        
        if (pld!= null){
            if (!pld.isValid()){
                //anct.updateField ("it_kfactor", "", false);
                return true;
            }
        }
        
        porosity = getDoublePValue("porosidade", current_model.getCal().getPorosidade(), false);
        if (porosity.isNaN()){
            anct.updateField ("it_kfactor", "", false);
            return true;
        }
        
        density  = getDoublePValue("densidade", current_model.getCal().getDensidade(), false);
        if (density.isNaN()){
            anct.updateField ("it_kfactor", "", false);
            return true;
        }
        
        
        layervol  = getDoublePValue("volume_camada", current_model.getCal().getVolume_camada(), false);
        if (layervol.isNaN()){
            anct.updateField ("it_kfactor", "", false);
            return true;
        }
        
        
        
        
        area  = getDoublePValue("area", current_model.getCal().getArea(), false);
        if (area.isNaN()){
            anct.updateField ("it_kfactor", "", false);
            return true;
        }
       
        temperature  = getDoublePValue("temperature", current_model.getCal().getTemperature(), false);
        if (temperature.isNaN()){
            anct.updateField ("it_kfactor", "", false);
            return true;
        }
        else{
            viscosity = getVisco(temperature);
        }
        
        
        time  = getDoublePValue("media", current_model.getCal().getMedia(), false);
        if (time.isNaN()){
            anct.updateField ("it_kfactor", "", false);
            return true;
        }
        
        rsd  = getDoublePValue("rsd", current_model.getCal().getRsd(), false);
        if (rsd.isNaN()){
            anct.updateField ("it_kfactor", "", false);
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
        
        current_model.getCal().setKfactor(skfactor);
        current_model.getCal().setMassa_ensaio(smass);
        
        anct.updateField ("it_kfactor", skfactor, false);
        anct.updateField ("it_mass", smass, false);
        
        
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
                anct.updateField ("it_mass", "", true);
                return true;
            }
        }
        
        porosity = getDoublePValue("porosidade", current_model.getCal().getPorosidade(), false);
        if (porosity.isNaN()){
            anct.updateField ("it_mass", "", false);
            return true;
        }
        
        density  = getDoublePValue("densidade", current_model.getCal().getDensidade(), false);
        if (density.isNaN()){
            anct.updateField ("it_mass", "", false);
            return true;
        }
        
        layervol  = getDoublePValue("volume_camada", current_model.getCal().getVolume_camada(), false);
        if (layervol.isNaN()){
            anct.updateField ("it_mass", "", false);
            return true;
        }
        
        samplemass = (1 - porosity) * (density * layervol);
        
        
        String smass = String.format(Locale.US, "%5.3f", samplemass);
        log.info(String.format("Calib sample mass was calculated : %s", smass));
        anct.updateField ("it_mass", smass, false);
    
        
        return true;
    }
    
    
    // ============================================= REPORT SERVICES ====================================================
    
    public void reportCalibration(){

        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REPORTCALIBRATION", this.getClass(),
                                new VirnaPayload()
                                .setObject(current_model.getCal())
                                .setCaller(ctrl)
                                .setCallerstate("")
        ));  
        
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
       
       
    private boolean emitReport(String filename, String template) {
        
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
            contentStream.setFont(PDType1Font.COURIER, 12);

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
                    value = current_model.getCal().getFieldAsString(ri.getDatatag());
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
            document.save(filename);
            document.close();
            return true;
            
        } catch (Exception ex) {
            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                                    new VirnaPayload().setString(
                                        "Gerente de Administração&" + "SEVERE&" +
                                        String.format("Ocorreu uma falha na montagem do relatorio de impressão dessa calibração %d&", 
                                                current_model.getCal().getUid()) +
                                        String.format("O sistema informa codigo : %s", ex.getMessage())
                        )));
            return false;
        }
     
    }
       
       
       
       
    @smstate (state = "REPORTCALIBRATION")
    public boolean st_reportCalibration(SMTraffic smm){
        
        
        String fullpath;
 
        VirnaPayload payload = smm.getPayload();
        Object caller = payload.getCaller();
        
        
        if (caller instanceof Controller){
            if (payload.getCallerstate().equals("")){
                
                //filename = String.format("anabl-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.pdf", current_model.getAn().getUid());
                fullpath = String.format("%1$scalbl-%2$td%2$tm%2$ty%2$tH%2$tM%2$tS.pdf", 
                                Config.getInstance().getReport_dir(), current_model.getCal().getUid());
                Path p = Paths.get(fullpath);
                File f = p.toFile();
                
                if (f.exists()){
                    FXFWindowManager wm = FXFWindowManager.getInstance();
                    FXFHeaderband hb = wm.getHeaderBand();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            SimpleStringProperty result = hb.showQuestionDialog("Emissão de Relatórios", 
                                    new DialogMessageBuilder()
                                            //.setHeight(400.0)
                                            .enableButton("ok", "Sobrescrever", "overwrite", true)
                                            .enableButton("cancel", "Cancelar", "cancel", true)
                                            .enableButton("aux", "Novo Arquivo", "new", true)
                                            .add("Já existe um relatório descrevendo essa calibração.\n", "-fx-font-size: 16px;")
                                            .add("Você poderá tomar as seguintes atitudes.\n", "")
                                            .addSpacer(0)
                                            .add("\t\u2022 Descartar resultados antigos e sobrescrever (irrevogavel!!)\n", "")
                                            .add("\t\u2022 Gravar como outra calibração.\n", "")
                                            .add("\t\u2022 Cancelar a operação.", "")
                            );
                            if (result != null){
                                result.addListener(new ChangeListener<String>() {
                                    @Override
                                    public void changed(ObservableValue <? extends String> prop, String ov, String nv) {
                                        //log.info(String.format("Valor : %s ", nv));
                                        hb.hideInputDialog();

                                        if (nv.equals("new")){
                                            Long nuid = System.currentTimeMillis();
                                            current_model.getCal().setUid(nuid);
                                            current_model.getCal().setTimestamp(nuid);
                                            String fullpath = String.format("%1$scalbl-%2$td%2$tm%2$ty%2$tH%2$tM%2$tS.pdf", 
                                                Config.getInstance().getReport_dir(), current_model.getCal().getUid());
                                            if (emitReport(fullpath, "calib")){
                                                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                                                        new VirnaPayload().setObject(
                                                        new StatusMessage(String.format("Relatorio %s foi exportado.", 
                                                                fullpath), 3000))
                                                ));
                                            }
                                        }    
                                        else if (nv.equals("cancel")){
                                            return;
                                        }
                                        else{
                                            if (!emitReport(fullpath, "pdf1")) return;
                                            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                                                new VirnaPayload().setString(
                                                            "Gerente de Administração&" + "INFO&" +
                                                            String.format("O arquivo de calibração %s foi sobrescrito&", f.getName()) +
                                                            "Pode ter havido edição de resultados, sua flag de 'editado' foi marcada"        
                                            )));
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (emitReport(fullpath, "calib")){
                                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                                        new VirnaPayload().setObject(
                                        new StatusMessage(String.format("Relatorio %s foi exportado.", 
                                                fullpath), 3000))
                                ));
                            }
                        }
                    });
                }
            }
        }        
        
        return true; 
    }
    
    
    
}



//
//
//    @smstate (state = "CALCULATETIME")
//    public boolean st_calculateTime(SMTraffic smm){
//        
////        //log.info("Calculating new analysis time");
////        ArrayList<Double> values = (ArrayList<Double>)smm.getPayload().vobject;
////        
////        
////        
////        int num = 0;
////        double average = 0.0;
////        double dif = 0.0;
////        double rsd = 0.0;
////        
////        // Calculate average
////        if (values.size() > 1){
////            for (Double ivl : values){
////                if (ivl != 0.0){
////                    average += ivl;
////                    num++;
////                }
////            }
////            average = average / num;
////            // Calculate RSD
////            for (double vl : values){
////                if (vl != 0.0){
////                    dif += Math.pow(vl-average, 2);
////                }    
////            }
////            dif = dif / num;     
////            Double rawrsd = Math.sqrt(dif);
////            rsd = rawrsd;
////            rsd = (rsd /average) * 100;
////            if (rsd == 0.0) rsd = 0.001;
////            
////        }
////        else if (values.size() == 0){
////            average = 0.0;
////            rsd = 0.0;
////        }
////        else{
////            average = values.get(0);
////            rsd = 0.0;
////        }
////    
////        if (smm.getPayload().getFlag1() == true){
//////            anct.updateField ("it_analiseaverage", String.format(Locale.US, "%4.1f", average), true);
//////            anct.updateField ("it_analisersd", String.format(Locale.US, "%5.3f", rsd), true);
////        }
////        else{
////            anct.updateField ("it_analiseaverage", String.format(Locale.US, "%4.1f", average), true);
////            anct.updateField ("it_analisersd", String.format(Locale.US, "%5.3f", rsd), true);
////        }
////        
////        
//        return true;
//    }
//    
//   
//    @smstate (state = "UPDATECALTIME")
//    public boolean st_updatCalTime(SMTraffic smm){
//        
////        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
////        
////        if (pld != null){
////            String mode = pld.getAuxiliar();
////            if (mode != null && mode.equals("STOPENTER")){
////                //log.info("Input Time called called with STOPENTER");
////                //anct.addTimeEntry(pld.getFxfield().getValue());
////                cal.getTempos().add(pld.getFxfield().getValue());
////            }
////            else{
////                //log.info(String.format("CALTIME called with %s ", pld.isValid() ? "valid":"invalid"));
////            }
////        }
////        
//        return true;
//    }
//    

//private Double getVisco (Double temp){
//        
//        Double ret;
//        ret = ((temp * visco_a0) + visco_a1) * 1000;
//        return ret;
//    }
//    
//    
//    private Double getDoublePValue (String key, String svalue, boolean analise) {
//        
////        Double d;
////        PropertyLinkDescriptor pld;
////        
////        if (analise){
////            pld = ana_proplink_modelmap.get(key);
////        }
////        else{
////            pld = cal_proplink_modelmap.get(key);
////        }
////        
////        if (pld != null && pld.isValid()){
////            try{
////                if (svalue.equals("")){
////                    d = Double.NaN;
////                }
////                else{
////                    d = Double.parseDouble(svalue);
////                }
////                
////                //log.info(String.format(Locale.US, "getDoublepValue parsed %s to %5.3f", svalue, d));
////                return d;
////            }
////            catch (Exception ex){
////                log.warning(String.format("Failed to convert property %s due %s", key, ex.getMessage()));
////            }
////        }    
//        return Double.NaN;
//    }
//    


