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
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.syssupport.ActivityMachine;
import com.opus.syssupport.Config;
import com.opus.syssupport.PicnoUtils;
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
import javafx.scene.control.Control;

import com.opus.fxsupport.FXFHeaderband;
import com.opus.fxsupport.FXFListDialogBuilder;
import com.opus.fxsupport.FXFWindowManager;
import com.opus.fxsupport.LauncherItem;
import com.opus.fxsupport.StatusMessage;
import com.opus.syssupport.ProfileResources;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;



public class FX1SMachine extends ActivityMachine {

    private static final Logger log = Logger.getLogger(FX1SMachine.class.getName());
    
    private Controller ctrl = Controller.getInstance();
    private FX1Model current_model;
    private FX1Controller anct;
    private DBService dbservice;
    
    private boolean nocalc = false;
    
    public static final Double visco_a0 = 1.8076923000E-06;
    public static final Double visco_a1 = 1.3084615380E-03;
   
    
    private static FX1SMachine instance; 
    public static FX1SMachine getInstance(){
        if (instance == null) {instance = new FX1SMachine();}
        return instance;
    }

    
    public FX1SMachine() {
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
        FX1Model fx1m = (FX1Model)models.get(id);
        if (fx1m != null){
            current_model = fx1m;
            anct = (FX1Controller)fx1m.getFXCtrl();
        }
        else{
            log.severe(String.format("Failed to activate model %s", id));
        }
        
    }
    
    
    
    // ============================================Profile Management ========================================================
    
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
  
    
    // ============================================= PROFILE & LOAD ACTIONS ==================================================
    
    
    @smstate (state = "DELETEPROFILE")
    public boolean st_deleteProfile(SMTraffic smm){
        
        
        ProfileBlaine pbl = (ProfileBlaine)current_model.getProfile();
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
            ProfileBlaine nprof = (ProfileBlaine)prs.removeResource(pbl);
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
    
    
    
    @smstate (state = "CLONEPROFILE")
    public boolean st_cloneProfile(SMTraffic smm){

        ProfileBlaine pbl = (ProfileBlaine)current_model.getProfile();        
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
    
    
    public String loadCalibration (String fpath){
        
        String caltag = fpath.equals("calbl-000000000000.json") ? fpath : "Ultima";
        Path p = Paths.get(Config.getInstance().getExport_dir()+ fpath);
        File f = p.toFile();
        
        if (f.exists()){
            try {
                byte[] bytes = Files.readAllBytes(p);
                String json_out = new String(bytes, StandardCharsets.UTF_8);
                GsonBuilder builder = new GsonBuilder();
                builder.setPrettyPrinting();
                Gson gson = builder.create();
                current_model.setCal(gson.fromJson(json_out, CalibDescriptor.class));
                log.info(String.format("Calibration indicated by profile was loaded from %s", p.toFile().getAbsolutePath()));
                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                        new VirnaPayload().setObject(
                        new StatusMessage(String.format("Usando calibração :  %s", caltag), 3000))
                ));
            } catch (IOException ex) {
                log.info(String.format("Failed to load profile calibration from %s --> using default", p.toFile().getAbsolutePath()));
                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                            new VirnaPayload().setString(
                                 "Interface com Sistema&" + "SEVERE&" +
                                 String.format("Ocorreu uma falha na carga da calibração %s&", fpath) +
                                 "Verifique se vc tem permissões para essa ação"        
                            )
                )); 
                current_model.setCal(new CalibDescriptor());
                current_model.getCal().loadDefaults();
                //storeCalibFile(p);
            }
        }
        else{
            log.info(String.format("Calibration file %s doesnt exists --> using default", p.toFile().getAbsolutePath()));
            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                            new VirnaPayload().setString(
                                 "Interface com Sistema&" + "SEVERE&" +
                                 String.format("O arquivo de calibração %s, indicado pelo perfil não existe&", fpath) +
                                 "Usando default, corrija esse parametro editando o campo correspondente"        
                            )
            )); 
            current_model.setCal(new CalibDescriptor());
            current_model.getCal().loadDefaults();
        }
        return "";
    }
    
   
    
 
    
 
    
    // ================================== ANALISE CYCLES MANAGEMENT =========================================================
    
    public void newAnalise(){      
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "NEWANALISE", this.getClass(),
                                   new VirnaPayload().setObject(current_model.getAn())
        ));
    }
    
    public void updateAnaliseCanvas(){
        
        nocalc=true;
        
        anct.updateField ("it_densidade", current_model.getAn().getDensidade(), true);
        anct.updateField ("it_porosidade", current_model.getAn().getPorosidade(), true);
        anct.updateField ("it_massa_calculada", current_model.getAn().getMassa_ensaio(), true);
        anct.updateField ("it_temperature", current_model.getAn().getTemperature(), false);
        
        anct.updateField ("it_analiseaverage", current_model.getAn().getMedia(), false);
        anct.updateField ("it_analisersd", current_model.getAn().getRsd(), false);
        anct.updateField ("it_blaineresult", current_model.getAn().getBlaine(), false);
        
        anct.updateField ("it_sid", current_model.getAn().getSid(), false);
        anct.updateField ("it_lote", current_model.getAn().getLote(), false);
        anct.updateField ("it_notas", current_model.getAn().getNotas(), false);
        
        String lbc = current_model.getProfile().getDescriptor("it_calibfile").getDefault_value();
        if (lbc.equals("calbl-000000000000.json")){
            anct.updateField ("it_calibfile", "Ultima Calibração", false);
        }
        else{
            anct.updateField ("it_calibfile", current_model.getCal().getSid() , false);
        }
 
        anct.updateField ("it_constantek", current_model.getAn().getKfactor(), false);
        anct.updateField ("it_layervolume", current_model.getAn().getVolume_camada(), false);
        anct.updateField ("it_caltemp", current_model.getAn().getCaltemp(), false);
        
        anct.updateAnaliseTime(current_model.getAn().getTimestamp());
        
        ArrayList<String> runs = current_model.getAn().getTempos();
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
    
    
    private void loadAnaliseDefaults(){
        
        current_model.getAn().loadDefaults();
        
        current_model.getAn().setUser(PicnoUtils.user_name);
        current_model.getAn().setProfile(current_model.getProfile().getArgument());
        
        current_model.getAn().setDensidade(current_model.getProfile().getDescriptor("it_densidade").getDefault_value());
        current_model.getAn().setPorosidade(current_model.getProfile().getDescriptor("it_porosidade").getDefault_value());
        //current_model.getAn().setMassa_ensaio(current_model.getProfile().getDescriptor("it_massa_calculada").getDefault_value());
        
   
        current_model.getAn().getTempos().clear();
        current_model.getAn().setMedia("");
        current_model.getAn().setRsd("");
        current_model.getAn().setBlaine("");
        
       
        current_model.getAn().setSid(current_model.getProfile().getDescriptor("it_sid").getDefault_value());
        current_model.getAn().setLote(current_model.getProfile().getDescriptor("it_lote").getDefault_value());
        current_model.getAn().setNotas(current_model.getProfile().getDescriptor("it_notas").getDefault_value());

        
        if (current_model.getCal() == null){
            String calibid = current_model.getProfile().getDescriptor("it_calibfile").getDefault_value();
            String ret = loadCalibration (calibid);
        }
        
        //current_model.getAn().setCalibid(current_model.getProfile().getDescriptor("it_calibfile").getDefault_value());
        current_model.getAn().setKfactor(current_model.getCal().getKfactor());
        current_model.getAn().setVolume_camada(current_model.getCal().getVolume_camada());
        current_model.getAn().setCaltemp(current_model.getCal().getTemperature());

        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "CALCMASS", this.getClass(),
                        new VirnaPayload()
        ));
        
    }
    
    
    @smstate (state = "NEWANALISE")
    public boolean st_newAnalise(SMTraffic smm){
        
        log.info("New Analise called ...");

        loadAnaliseDefaults();
        updateAnaliseCanvas();
        current_model.setAnclean(true);
 
        return true;
    }
    
    
    @smstate (state = "ANALISEDONE")
    public boolean st_analiseDone(SMTraffic smm){
 
        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
        
        if (pld!= null){
            if (!pld.isValid()){
                anct.updateField ("it_blaineresult", "", false);
                anct.setUIState("DONE_INVALID");
                return true;
            }
        }
               
        //FXFBlaineDeviceController bdc = anct.getBlaineDevice();
        //FXFCheckListViewNumber<String> runs = anct.getBlaineDevice().getRunControl();
        ArrayList<FXFAnaliseListItem> ritens = anct.getBlaineDevice().getRunControl().getCheckedItems();
        ArrayList<String> an_runs = current_model.getAn().getTempos();
        
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
    
    
    
    
    
    // ================================================
    
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
    
    
    
    
    // ================================================= CALCULATION SERVICES =================================================================
  
    @smstate (state = "UPDATEANTIME")
    public boolean st_updateTime(SMTraffic smm){
        
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
        
        porosity = getDoublePValue("porosidade", current_model.getAn().getPorosidade(), true);
        if (porosity.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        
        density  = getDoublePValue("densidade", current_model.getAn().getDensidade(), true);
        if (density.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        
        
        time  = getDoublePValue("media", current_model.getAn().getMedia(), true);
        if (time.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        
        rsd  = getDoublePValue("rsd", current_model.getAn().getRsd(), true);
        if (rsd.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        
        kfactor  = getDoublePValue("kfactor", current_model.getAn().getKfactor(), true);
        if (kfactor.isNaN()){
            anct.updateField ("it_blaineresult", "", false);
            return true;
        }
        
        temperature  = getDoublePValue("temperature", current_model.getAn().getTemperature(), true);
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
        current_model.getAn().setBlaine(sblaine);
        
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
        
        porosity = getDoublePValue("porosidade", current_model.getAn().getPorosidade(), true);
        if (porosity.isNaN()){
            anct.updateField ("it_massa_calculada", "", false);
            return true;
        }
        
        density  = getDoublePValue("densidade", current_model.getAn().getDensidade(), true);
        if (density.isNaN()){
            anct.updateField ("it_massa_calculada", "", false);
            return true;
        }
        
        layervol  = getDoublePValue("volume_camada", current_model.getAn().getVolume_camada(), true);
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
    
  
    // ====================================== ANALYSIS UPDATE ==============================================================
    
    public void storeAnaliseAction(){
            
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISEFILE", this.getClass(),
                                    new VirnaPayload()                         
        )); 
    }
    
    private boolean storeAnaliseFile(Path p) {
        
        try {
            GsonBuilder builder = new GsonBuilder(); 
            builder.setPrettyPrinting(); 
            Gson gson = builder.create();
            String sjson = gson.toJson(current_model.getAn());

            Files.write(p, sjson.getBytes(StandardCharsets.UTF_8));
            log.info(String.format("================Stored analise on %s: \n\r", p.toString()));

            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                    new VirnaPayload().setObject(
                    new StatusMessage(String.format("Analise foi gravada no arquivo %s", p.getFileName().toString()), 3000))
            ));

            current_model.setAnclean(true);

        } catch (IOException ex) {
            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                new VirnaPayload().setString(
                            "Gerente de Administração&" + "SEVERE&" +
                            String.format("Houve um erro na gravação da arquivo de analise %s&", p.toFile().getAbsolutePath()) +
                            "Verifique se vc tem permissões para essa operação."        
            )));
            return false;
        }
        return true;
    }
    
    
    private File getAnablFile(Long timestamp){
       
        String filename = String.format("anabl-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.json", timestamp);
        Path p = Paths.get(Config.getInstance().getExport_dir()+filename);
        return p.toFile();
    }
    
    @smstate (state = "STOREANALISEFILE")
    public boolean st_storeAnaliseFile(SMTraffic smm){
        
//        String filename = String.format("anabl-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.json", current_model.getAn().getUid());
//        Path p = Paths.get(Config.getInstance().getExport_dir()+filename);
        File f = getAnablFile(current_model.getAn().getUid()); //p.toFile();
        
        if (f.exists()){
            FXFWindowManager wm = FXFWindowManager.getInstance();
            FXFHeaderband hb = wm.getHeaderBand();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    SimpleStringProperty result = hb.showQuestionDialog("Controle das Análises", 
                            new DialogMessageBuilder()
                                    //.setHeight(400.0)
                                    .enableButton("ok", "Sobrescrever", "overwrite", true)
                                    .enableButton("cancel", "Cancelar", "cancel", true)
                                    .enableButton("aux", "Novo Arquivo", "new", true)
                                    .add("Essa análise já tem um arquivo no diretório de exportação.\n", "-fx-font-size: 14px;")
                                    .add("Você poderá tomar as seguintes atitudes.\n", "")
                                    .addSpacer(0)
                                    .add("\t\u2022 Descartar resultados antigos e sobrescrever (irrevogavel!!)\n", "")
                                    .add("\t\u2022 Gravar como outra analise.\n", "")
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
                                    current_model.getAn().setUid(nuid);
                                    current_model.getAn().setTimestamp(nuid);
                                    File f = getAnablFile(current_model.getAn().getUid()); 
                                    if (!storeAnaliseFile(f.toPath())) return ;                               }    
                                else if (nv.equals("cancel")){
                                    return;
                                }
                                else{
                                    if (!storeAnaliseFile(f.toPath())) return;
                                    ctrl.processSignal(new SMTraffic(0l, 0l, 0, "ADD_NOTIFICATION", this.getClass(),
                                        new VirnaPayload().setString(
                                                    "Gerente de Administração&" + "INFO&" +
                                                    String.format("O arquivo de analise %s foi sobrescrito&", f.getName()) +
                                                    "Pode ter havido edição de resultados, sua flag de 'editado' foi marcada"        
                                    )));
                                }
                                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISERECORD", this.getClass(),
                                                        new VirnaPayload()
                                                        .setObject(current_model.getAn())
                                                        .setCaller(FX1SMachine.instance)
                                                        .setCallerstate("CHECKRECORD")
                                ));
                            }
                        });
                    }
                }
            });  
        }
        else{
            if (storeAnaliseFile(f.toPath())){
                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISERECORD", this.getClass(),
                                        new VirnaPayload()
                                        .setObject(current_model.getAn())
                                        .setCaller(FX1SMachine.instance)
                                        .setCallerstate("CHECKRECORD")
                ));
            }
        }
         
        return true;
    }
 
    
    @smstate (state = "STOREANALISERECORD")
    public boolean st_storeAnaliseRecord(SMTraffic smm){
        
        AnaliseDescriptor and;
        VirnaPayload payload = smm.getPayload();
       
        Object caller = payload.getCaller();
        
        if (caller instanceof FX1SMachine){           
            if (payload.getCallerstate().equals("CHECKRECORD")){
                dbservice.processSignal(new SMTraffic(0l, 0l, 0, "HASRECORD", FX1Controller.class,
                                        new VirnaPayload()
                                        .setLong1(current_model.getAn().getUid())
                                        .setObject(current_model.getAn())
                                        .setCaller(ctrl)
                                        .setCallerstate("STOREANALISERECORD")
                ));
            }
 
        }
        else if (caller instanceof DBService){
            // First check if we had problems and proceed aprop.
            if (payload.getFlag2() != null && payload.getFlag2()){
                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", this.getClass(),
                                        payload));
            }
            else{
                // If we're checking previous records...
                if (payload.getCallerstate().equals("HASRECORD")){
                    if (payload.getFlag1()){
                        // Record já existe
                        FXFWindowManager wm = FXFWindowManager.getInstance();
                        FXFHeaderband hb = wm.getHeaderBand();
                        
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                SimpleStringProperty result = hb.showQuestionDialog("Controle das Análises", 
                                        new DialogMessageBuilder()
                                                //.setHeight(400.0)
                                                .enableButton("ok", "Sobrescrever", "overwrite", true)
                                                .enableButton("cancel", "Cancelar", "cancel", true)
                                                .enableButton("aux", "Novo Registro", "new", true)
                                                .add("Essa análise já está registrada no banco de dados.\n", "-fx-font-size: 16px;")
                                                .add("Você poderá tomar as seguintes atitudes.\n", "")
                                                .addSpacer(0)
                                                .add("\t\u2022 Descartar resultados antigos e sobrescrever (irrevogavel!!)\n", "")
                                                .add("\t\u2022 Gravar como nova analise (\n", "")
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
                                                current_model.getAn().setUid(nuid);
                                                current_model.getAn().setTimestamp(nuid);
                                            }
                                            else if (nv.equals("cancel")){
                                                return;
                                            }
                                            dbservice.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISE", this.getClass(),
                                                                    new VirnaPayload()
                                                                    .setObject(current_model.getAn())
                                                                    .setCaller(ctrl)
                                                                    .setCallerstate("STOREANALISERECORD")
                                            ));
                                        }
                                    });
                                }
                            }
                        });    
                    }
                    else{
                        // Store record then
                        dbservice.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISE", this.getClass(),
                                                new VirnaPayload()
                                                .setObject(current_model.getAn())
                                                .setCaller(ctrl)
                                                .setCallerstate("STOREANALISERECORD")
                        ));
                    }
                }
                else if (payload.getCallerstate().equals("STOREANALISE")){                   
                    ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                            new VirnaPayload().setObject(
                            new StatusMessage(String.format("Análise %d foi registrada no banco", 
                                    current_model.getAn().getUid()), 3000))
                    ));
                }
            }
        }

        return true;
    }
    

    // ===================================================  LOAD ANALYSIS SERVICES ============================================
    
    public void loadFileAction(){
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "LOADFILE", this.getClass(),
                                new VirnaPayload()
                                .setObject(current_model.getAn())
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
            AnaliseDescriptor adesc = gson.fromJson(json_out, AnaliseDescriptor.class);
            current_model.setAn(adesc);
            
            log.info(String.format("Analise loaded from %s", p.toFile().getAbsolutePath()));
            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "UPDATESTATUS", this.getClass(),
                    new VirnaPayload().setObject(
                    new StatusMessage(String.format("Análise foi carregada"), 3000))
            ));
            updateAnaliseCanvas();

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
    
    
    @smstate (state = "LOADFILE")
    public boolean st_loadFile(SMTraffic smm){
        
        String file = "";
        AnaliseDescriptor and;
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
                            .addFiles("/home/acp/PP200/Export", "formated_time", "anabl-")
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
                                        new StatusMessage(String.format("A carga de arquivo de análise foi abortada"), 3000))
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
 
    
    public void loadRecord(AnaliseDescriptor andesc){
       
//        if (fx1open){
//            current_model.getAn().setBlaine(andesc.getBlaine());
//            current_model.getAn().setCalibid(andesc.getCalibid());
//            current_model.getAn().setDensidade(andesc.getDensidade());
//            current_model.getAn().setKfactor(andesc.getKfactor());
//            current_model.getAn().setLote(andesc.getLote());
//            current_model.getAn().setMassa_ensaio(andesc.getMassa_ensaio());
//            current_model.getAn().setMedia(andesc.getMedia());
//            current_model.getAn().setNotas(andesc.getNotas());
//            current_model.getAn().setPorosidade(andesc.getPorosidade());
//            current_model.getAn().setProfile(andesc.getProfile());
//            current_model.getAn().setRsd(andesc.getRsd());
//            current_model.getAn().setSid(andesc.getSid());
//            current_model.getAn().setTemperature(andesc.getTemperature());
//
//            current_model.getAn().setTimestamp(andesc.getTimestamp());
//            current_model.getAn().setUid(andesc.getUid());
//            current_model.getAn().setUser(andesc.getUser());
//            current_model.getAn().setVolume_camada(andesc.getVolume_camada());
//
//            ArrayList<String>tempos = current_model.getAn().getTempos();
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
    
    

    // ============================================== REPORT SERVICES ================================================
    
       public void reportAnalise(){
//        smqueue.offer(new SMTraffic(0l, 0l, 0, "LOADREPORTDESCRIPTOR", 
//                                new VirnaPayload()
//                                .setObject(an)
//                                .setCaller(this)
//                                .setCallerstate("")
//        )); 
        
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REPORTANALISE", this.getClass(),
                                new VirnaPayload()
                                .setObject(current_model.getAn())
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
                    value = current_model.getAn().getFieldAsString(ri.getDatatag());
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
                                        String.format("Ocorreu uma falha na montagem do relatorio de impressão dessa analise %d&", 
                                                current_model.getAn().getUid()) +
                                        String.format("O sistema informa codigo : %s", ex.getMessage())
                        )));
            return false;
        }
     
    }
    
    
    @smstate (state = "REPORTANALISE")
    public boolean st_reportAnalise(SMTraffic smm){
        
        //String filename;
        String fullpath;
 
        VirnaPayload payload = smm.getPayload();
        Object caller = payload.getCaller();
        
        
        if (caller instanceof Controller){
            if (payload.getCallerstate().equals("")){
                
                //filename = String.format("anabl-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.pdf", current_model.getAn().getUid());
                fullpath = String.format("%1$sanabl-%2$td%2$tm%2$ty%2$tH%2$tM%2$tS.pdf", 
                                Config.getInstance().getReport_dir(), current_model.getAn().getUid());
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
                                            .add("Já existe um relatório descrevendo essa análise.\n", "-fx-font-size: 16px;")
                                            .add("Você poderá tomar as seguintes atitudes.\n", "")
                                            .addSpacer(0)
                                            .add("\t\u2022 Descartar resultados antigos e sobrescrever (irrevogavel!!)\n", "")
                                            .add("\t\u2022 Gravar como outra analise.\n", "")
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
                                            current_model.getAn().setUid(nuid);
                                            current_model.getAn().setTimestamp(nuid);
                                            String fullpath = String.format("%1$sanabl-%2$td%2$tm%2$ty%2$tH%2$tM%2$tS.pdf", 
                                                Config.getInstance().getReport_dir(), current_model.getAn().getUid());
                                            if (emitReport(fullpath, "anabl")){
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
                                                            String.format("O arquivo de analise %s foi sobrescrito&", f.getName()) +
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
                            if (emitReport(fullpath, "anabl")){
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



















//@smstate (state = "CALCULATETIME")
//    public boolean st_calculateTime(SMTraffic smm){
//        
//        //log.info("Calculating new analysis time");
//        ArrayList<Double> values = (ArrayList<Double>)smm.getPayload().vobject;
//        
//        
//        int num = 0;
//        double average = 0.0;
//        double dif = 0.0;
//        double rsd = 0.0;
//        
//        // Calculate average
//        if (values.size() > 1){
//            for (Double ivl : values){
//                if (ivl != 0.0){
//                    average += ivl;
//                    num++;
//                }
//            }
//            average = average / num;
//            // Calculate RSD
//            for (double vl : values){
//                if (vl != 0.0){
//                    dif += Math.pow(vl-average, 2);
//                }    
//            }
//            dif = dif / num;     
//            Double rawrsd = Math.sqrt(dif);
//            rsd = rawrsd;
//            rsd = (rsd /average) * 100;
//            if (rsd == 0.0) rsd = 0.001;
//            
//        }
//        else if (values.size() == 0){
//            average = 0.0;
//            rsd = 0.0;
//        }
//        else{
//            average = values.get(0);
//            rsd = 0.0;
//        }
//    
//        if (smm.getPayload().getFlag1() == true){
////            calct.updateField ("it_analiseaverage", String.format(Locale.US, "%4.1f", average), true);
////            calct.updateField ("it_analisersd", String.format(Locale.US, "%5.3f", rsd), true);
//        }
//        else{
//            anct.updateField ("it_analiseaverage", String.format(Locale.US, "%4.1f", average), true);
//            anct.updateField ("it_analisersd", String.format(Locale.US, "%5.3f", rsd), true);
//        }
//        
//        
//        return true;
//    }



// 
//    public void loadLastCalibration(){
//        
//        Long lastmod = 0L;
//        Path p = Paths.get(Config.getInstance().getExport_dir()+"cal-default.json");
//        //BasicFileAttributes attr;
//        
//        try {
//            ArrayList<String> calfiles = PicnoUtils.scanDir(Config.getInstance().getExport_dir(), "cal-");
//            for (String scal : calfiles){
//                File f = new File(Config.getInstance().getExport_dir()+scal);
//                long lmod = f.lastModified();
//                //Path p1 = f.getAbsoluteFile().toPath();
//                //attr = Files.readAttributes(p1, BasicFileAttributes.class);
//                //FileTime attrcreated = attr.creationTime();
//                if (lmod > lastmod){
//                    lastmod = f.lastModified();
//                    p = f.toPath();
//                }
//            }
//            byte[] bytes = Files.readAllBytes(p);
//            String json_out = new String(bytes, StandardCharsets.UTF_8);
//            GsonBuilder builder = new GsonBuilder(); 
//            builder.setPrettyPrinting(); 
//            Gson gson = builder.create();
//            current_model.setLastcal(gson.fromJson(json_out, CalibDescriptor.class));
//            log.info(String.format("Last Calibration loaded from %s", p.toFile().getAbsolutePath()));
////            StatusDisplayer.getDefault().setStatusText(String.format("Ultima Calibração foi carregada do arquivo %s", 
////            p.toFile().getAbsolutePath()));
//            
//        } catch (Exception ex) {
//            log.info(String.format("Failed to load last calibration from %s --> using default", p.toFile().getAbsolutePath()));
//            current_model.setLastcal(new CalibDescriptor());
//            current_model.getLastcal().loadDefaults();
//            //storeCalibFile(p);
//        }
//       
//    }
//    


   
    
//    Object obj = payload.vobject;
//        if (obj == null){
//            and = new AnaliseDescriptor();
//        }
//        else{
//            and = (AnaliseDescriptor)obj;
//        }
//        
//        Object caller = payload.getCaller();
//        
//        if (caller instanceof Controller){
//            if (payload.getCallerstate().equals("")){
//                filename = String.format("ana-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.json", and.getUid());
//                Path p = Paths.get(Config.getInstance().getExport_dir()+filename);
//                File f = p.toFile();
//                if (f.exists()){
//                    String mes1 = String.format("<html>Arquivo %s já existe, voce pode : "
//                                + "<ul>"
//                                    + "<li>Gravar esses novos dados sobre o arquivo anterior.</li>"
//                                    + "<li>Providenciar nova identificação e gravar arquivo.</li>"
//                                + "</ul>"
//                                + "</html>", 
//                                filename);
//                            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "SHOWOOPS", this.getClass(),
//                                    new VirnaPayload()
//                                    .setString(mes1)
//                                    .setObject(and)
//                                    .setAuxiliar(new Object[] { "Gravar", "Mudar ID", "Cancelar" })
//                                    .setCaller(ctrl)
//                                    .setCallerstate("STOREANALISEFILE")
//                            )); 
//                }
//                else{
//                    try {
//                        storeAnaliseFile(p);
////                        StatusDisplayer.getDefault().setStatusText(String.format("Analise com ID %d foi gravada em %s", 
////                            and.getUid(), p.toFile().getAbsolutePath()));
//                        current_model.setAnclean(true);
//                    } catch (IOException ex) {
//                        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", this.getClass(),
//                                            new VirnaPayload()
//                                            .setString("Falha na gravação do arquivo : "+ filename)
//                                            .setFlag2(true)
//                                            .setObject(payload.vobject)        
//                                            .setCaller(ctrl)
//                                            .setCallerstate("STOREANALISEFILE")
//                        ));
//                    }
//                }
//            }
//            else if (payload.getCallerstate().equals("SHOWOOPS")){
//                
//                String message = "Usando analise com ";
//                
//                if (payload.vstring.equals("Mudar ID")){
//                    Long newid = System.currentTimeMillis();
//                    and.setUid(newid);
//                    and.setTimestamp(newid);
//                    message = "Mudando analise para  ";
//                }
//                
//                filename = String.format("ana-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.json", and.getUid());
//                Path p = Paths.get(Config.getInstance().getExport_dir()+filename);
//                try {
//                    storeAnaliseFile(p);
////                    StatusDisplayer.getDefault().setStatusText(String.format("%s ID %d e gravando em %s", 
////                        message, and.getUid(), p.toFile().getAbsolutePath()));
//                    current_model.setAnclean(true);
//                } catch (IOException ex) {
//                    ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", this.getClass(),
//                                        new VirnaPayload()
//                                        .setString("Falha na gravação do arquivo : "+ filename)
//                                        .setFlag2(true)
//                                        .setObject(payload.vobject)        
//                                        .setCaller(ctrl)
//                                        .setCallerstate("STOREANALISEFILE")
//                    ));
//                }
//            }
//        }
//    
    


//private void emitCalibReport(String filename, String template) throws Exception{
//        
//        
//        PDPage page;
//        PDPageContentStream contentStream;
//
//        File file = new File(Config.getInstance().getTemplate_dir()+template+".pdf");
//        PDDocument document = PDDocument.load(file);
//        
//        String descfile = Config.getInstance().getTemplate_dir()+template+".json";
//        ReportDescriptor rd = PicnoUtils.loadJson(descfile, ReportDescriptor.class); 
//
//        page = document.getPage(0);
//        contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
//
//        Color c = PicnoUtils.convertFXColor(rd.getStrokecolor());
//        contentStream.setNonStrokingColor(c);
//        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
//       
//        ArrayList<ReportItem> ritems = rd.getItems();
//        String value = "N/A";
//        String format ;
//        int xpos, ypos;
//        int yoffset = (int) Math.round(rd.getYoffset());
//        int xoffset = (int) Math.round(rd.getXoffset());
//        
//        
//        contentStream.close();
//
//        String pmessage = "Report Manager gravou um Relatorio de Calibração em " + filename;
////        StatusDisplayer.getDefault().setStatusText(pmessage);
//        log.info("Report Manager is saving PDF Calibration report to :  " + filename);  
//        document.save(filename);
//        document.close();
//        
//        
//    }
//    