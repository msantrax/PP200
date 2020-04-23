/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.syssupport.ActivityMachine;
import com.opus.syssupport.Config;
import com.opus.syssupport.PicnoUtils;
import com.opus.syssupport.Profile;
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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import com.opus.fxsupport.FXFControllerInterface;



public class FX1SMachine extends ActivityMachine {

    private static final Logger log = Logger.getLogger(FX1SMachine.class.getName());
    
    private Controller ctrl = Controller.getInstance();
    private FX1Model current_model;
    private FX1Controller anct;
    
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
        
        pld = getLinkDescriptor((FXFField)control, current_model.getAna_proplink_uimap());
        
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
                current_model.setCal(gson.fromJson(json_out, CalibDescriptor.class));
                log.info(String.format("Calibration indicated by profile was loaded from %s", p.toFile().getAbsolutePath()));
//                StatusDisplayer.getDefault().setStatusText(String.format("Calibração indicada pelo perfil foi carregada do arquivo %s",
//                        p.toFile().getAbsolutePath()));
            } catch (IOException ex) {
                log.info(String.format("Failed to load profile calibration from %s --> using default", p.toFile().getAbsolutePath()));
                current_model.setCal(new CalibDescriptor());
                current_model.getCal().loadDefaults();
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
            current_model.setLastcal(gson.fromJson(json_out, CalibDescriptor.class));
            log.info(String.format("Last Calibration loaded from %s", p.toFile().getAbsolutePath()));
//            StatusDisplayer.getDefault().setStatusText(String.format("Ultima Calibração foi carregada do arquivo %s", 
//            p.toFile().getAbsolutePath()));
            
        } catch (Exception ex) {
            log.info(String.format("Failed to load last calibration from %s --> using default", p.toFile().getAbsolutePath()));
            current_model.setLastcal(new CalibDescriptor());
            current_model.getLastcal().loadDefaults();
            //storeCalibFile(p);
        }
        
    }
    
    public void newAnalise(){      
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "NEWANALISE", this.getClass(),
                                   new VirnaPayload().setObject(current_model.getAn())
        ));
    }
    
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
    
    
    public void storeAnalise(){
        
        //if (!locked){
            
            if (current_model.getAn() == null){
                current_model.setAn(new AnaliseDescriptor());
                //current_model.getAn().addPropertyChangeListener(ctrl);
            }
            
            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISEFILE", this.getClass(),
                                        new VirnaPayload()
                                        .setObject(current_model.getAn())
                                        .setCaller(ctrl)
                                        .setCallerstate("")
            ));        
            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISERECORD", this.getClass(),
                                        new VirnaPayload()
                                        .setObject(current_model.getAn())
                                        .setCaller(ctrl)
                                        .setCallerstate("")
            ));
        //}
    }
    
    public void loadFile(){
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "LOADFILE", this.getClass(),
                                new VirnaPayload()
                                .setObject(current_model.getAn())
                                .setCaller(ctrl)
                                .setCallerstate("")
        ));    
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
    
    
    public void updateAnaliseCanvas(){
        
        nocalc=true;
        
        anct.updateField ("it_densidade", current_model.getAn().getDensidade(), true);
        anct.updateField ("it_porosidade", current_model.getAn().getPorosidade(), true);
        anct.updateField ("it_massa_calculada", current_model.getAn().getMassa_ensaio(), true);
        
        anct.updateField ("it_analiseaverage", current_model.getAn().getMedia(), false);
        anct.updateField ("it_analisersd", current_model.getAn().getRsd(), false);
        anct.updateField ("it_blaineresult", current_model.getAn().getBlaine(), false);
        
        anct.updateField ("it_sid", current_model.getAn().getSid(), false);
        anct.updateField ("it_lote", current_model.getAn().getLote(), false);
        anct.updateField ("it_notas", current_model.getAn().getNotas(), false);
        
        String lbc = "calib";
//        if (current_model.getAn().getCalibid().equals("last")){
//            lbc = String.format("%1$s/%2$s em %3$td-%3$tm-%3$tY %3$tH:%3$tM:%3$tS",
//                    current_model.getLastcal().getProfile(),
//                    current_model.getLastcal().getSid(),
//                    current_model.getLastcal().getTimestamp()
//            );
//        }
//        else{
//            lbc = String.format("%1$s/%2$s em %3$td-%3$tm-%3$tY %3$tH:%3$tM:%3$tS",
//                    current_model.getCal().getProfile(),
//                    current_model.getCal().getSid(),
//                    current_model.getCal().getTimestamp()
//            );
//        }
        anct.updateField ("it_calibfile", lbc, false);
        
        anct.updateField ("it_constantek", current_model.getAn().getKfactor(), false);
        anct.updateField ("it_layervolume", current_model.getAn().getVolume_camada(), false);
        anct.updateField ("it_temperature", current_model.getAn().getTemperature(), false);
        
        anct.updateAnaliseTime(current_model.getAn().getTimestamp());
        
        anct.initTimeList();
        for (String atime : current_model.getAn().getTempos()){
            anct.addTimeEntry(atime);
        }
        
        nocalc = false;  
    }
    
    
    private void loadAnaliseDefaults(){
        
        current_model.getAn().loadDefaults();
        
        current_model.getAn().setDensidade(current_model.getProfile().getDescriptor("it_densidade").getDefault_value());
        current_model.getAn().setPorosidade(current_model.getProfile().getDescriptor("it_porosidade").getDefault_value());
        current_model.getAn().setMassa_ensaio(current_model.getProfile().getDescriptor("it_massa_calculada").getDefault_value());
   
        current_model.getAn().getTempos().clear();
        current_model.getAn().setMedia("");
        current_model.getAn().setRsd("");
        current_model.getAn().setBlaine("");
       
        current_model.getAn().setSid(current_model.getProfile().getDescriptor("it_sid").getDefault_value());
        current_model.getAn().setLote(current_model.getProfile().getDescriptor("it_lote").getDefault_value());
        current_model.getAn().setNotas(current_model.getProfile().getDescriptor("it_notas").getDefault_value());

        String calibid = current_model.getProfile().getDescriptor("it_calibfile").getDefault_value();
        
        if (calibid.equals("last")){
            current_model.setCal(current_model.getLastcal());
        }
        else{
            loadCalibration (Config.getInstance().getExport_dir()+calibid);
        }
        
        current_model.getAn().setCalibid(current_model.getProfile().getDescriptor("it_calibfile").getDefault_value());
//        current_model.getAn().setKfactor(current_model.getCal().getKfactor());
//        current_model.getAn().setVolume_camada(current_model.getCal().getVolume_camada());
//        current_model.getAn().setTemperature(current_model.getCal().getTemperature());
        
    }
    
    
    private Double getVisco (Double temp){
        
        Double ret;
        ret = ((temp * visco_a0) + visco_a1) * 1000;
        return ret;
    }
    
    
    private Double getDoublePValue (String key, String svalue, boolean analise) {
        
        Double d;
        PropertyLinkDescriptor pld;
        
        pld = current_model.getAna_proplink_modelmap().get(key);
  
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
    
    
    
    
    // ================================================= STATES =================================================================
    
    @smstate (state = "FX1TEST")
    public boolean st_fx1test(SMTraffic smm){
        log.info(String.format("FX1TEST was called from : %s", smm.getPayload().vstring));
        return true;
    }
    
    
    @smstate (state = "NEWANALISE")
    public boolean st_newAnalise(SMTraffic smm){
        
        log.info("New Analise called ...");

        loadAnaliseDefaults();
        updateAnaliseCanvas();
        current_model.setAnclean(true);
 
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "LOCKUI", this.getClass(),
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
                current_model.getAn().getTempos().add(pld.getFxfield().getValue());
            }
            else{
                //log.info(String.format("ANATIME called with %s ", pld.isValid() ? "valid":"invalid"));
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
                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "LOCKUI", this.getClass(),
                                   new VirnaPayload()
                                           .setInt1(1)
                                           .setInt2(0)
                ));
                return true;
            }
        }
 
        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "LOCKUI", this.getClass(),
                                   new VirnaPayload()
                                           .setInt1(0)
                                           .setInt2(0)      
        ));
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
    
    
     
    @smstate (state = "LOADCALIBRATION")
    public boolean st_loadCalibration(SMTraffic smm){

//        anct.updateField ("it_calibfile", "clb:quartzo-nist-1304:181219165830", false);
//        anct.updateField ("it_constantek", "2.13", false);
//        anct.updateField ("it_layervolume", "1.93", false);
//        anct.updateField ("it_temperature", "22.2", false);
         
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
                
//                dbservice.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISE", 
//                                        new VirnaPayload()
//                                        .setObject(and)
//                                        .setCaller(this)
//                                        .setCallerstate("STOREANALISERECORD")
//                ));
                
            }
            else{
//                dbservice.processSignal(new SMTraffic(0l, 0l, 0, "HASRECORD", 
//                                        new VirnaPayload()
//                                        .setLong1(and.getUid())
//                                        .setObject(and)
//                                        .setCaller(this)
//                                        .setCallerstate("STOREANALISERECORD")
//                ));
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
                        String mes1 = String.format("<html>Análise com o ID:%d já existe no Banco de Dados, voce pode : "
                            + "<ul>"
                                + "<li>Apagar o registro anterior e gravar esses novos dados</li>"
                                + "<li>Gravar esses dados com outra identificação (não recomendado...)</li>"
                            + "</ul>"
                            + "</html>", 
                            payload.long1);
                        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "SHOWOOPS", this.getClass(),
                                        new VirnaPayload()
                                        .setString(mes1)
                                        .setObject(and)
                                        .setAuxiliar(new Object[] { "Apagar", "Mudar ID", "Cancelar" })
                                        .setCaller(ctrl)
                                        .setCallerstate("STOREANALISERECORD")
                        ));
                    }
                    else{
                        // Store record then
//                        dbservice.processSignal(new SMTraffic(0l, 0l, 0, "STOREANALISE", 
//                                                new VirnaPayload()
//                                                .setObject(and)
//                                                .setCaller(this)
//                                                .setCallerstate("STOREANALISERECORD")
//                        ));
                    }
                }
                else if (payload.getCallerstate().equals("STOREANALISE")){                   
//                    StatusDisplayer.getDefault().setStatusText(payload.vstring);
                }
            }
        }
   
        return true;
    }
    
    
    
    private void storeAnaliseFile(Path p) throws IOException{
        
        GsonBuilder builder = new GsonBuilder(); 
        builder.setPrettyPrinting(); 
        Gson gson = builder.create();
        String sjson = gson.toJson(current_model.getAn());
 
        Files.write(p, sjson.getBytes(StandardCharsets.UTF_8));
        log.info(String.format("================Stored analise on %s: \n\r", p.toString()));

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
                            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "SHOWOOPS", this.getClass(),
                                    new VirnaPayload()
                                    .setString(mes1)
                                    .setObject(and)
                                    .setAuxiliar(new Object[] { "Gravar", "Mudar ID", "Cancelar" })
                                    .setCaller(ctrl)
                                    .setCallerstate("STOREANALISEFILE")
                            )); 
                }
                else{
                    try {
                        storeAnaliseFile(p);
//                        StatusDisplayer.getDefault().setStatusText(String.format("Analise com ID %d foi gravada em %s", 
//                            and.getUid(), p.toFile().getAbsolutePath()));
                        current_model.setAnclean(true);
                    } catch (IOException ex) {
                        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", this.getClass(),
                                            new VirnaPayload()
                                            .setString("Falha na gravação do arquivo : "+ filename)
                                            .setFlag2(true)
                                            .setObject(payload.vobject)        
                                            .setCaller(ctrl)
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
                    current_model.setAnclean(true);
                } catch (IOException ex) {
                    ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", this.getClass(),
                                        new VirnaPayload()
                                        .setString("Falha na gravação do arquivo : "+ filename)
                                        .setFlag2(true)
                                        .setObject(payload.vobject)        
                                        .setCaller(ctrl)
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
                if (!current_model.isAnclean()){
                    String mes1 = String.format("<html>Há dados não salvos na analise corrente, voce pode : "
                                + "<ul>"
                                    + "<li>Gravar esses dados primeiro.</li>"
                                    + "<li>Descartar o conteúdo</li>"
                                + "</ul>"
                                + "</html>");
                            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "SHOWOOPS", this.getClass(),
                                    new VirnaPayload()
                                    .setString(mes1)
                                    .setObject(current_model.getAn())
                                    .setAuxiliar(new Object[] { "Gravar", "Descartar", "Cancelar" })
                                    .setCaller(ctrl)
                                    .setCallerstate("LOADFILE")
                            )); 
                }
                else{
                    ctrl.processSignal(new SMTraffic(0l, 0l, 0, "LOADFILE", this.getClass(),
                            new VirnaPayload()
                            .setObject(current_model.getAn())
                            .setCaller(ctrl)
                            .setCallerstate("LOADFILE")
                    )); 
                }
            }
            else if (payload.getCallerstate().equals("SHOWOOPS")){
                if (payload.vstring.equals("Gravar")){
                    storeAnalise();
                }
                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "LOADFILE", this.getClass(), 
                        new VirnaPayload()
                        .setObject(current_model.getAn())
                        .setCaller(ctrl)
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
                        current_model.setAn(gson.fromJson(json_out, AnaliseDescriptor.class));
                        log.info(String.format("Analise loaded from %s", p.toFile().getAbsolutePath()));
//                        StatusDisplayer.getDefault().setStatusText(String.format("Analise foi carregada do arquivo %s", 
//                        p.toFile().getAbsolutePath()));
                        updateAnaliseCanvas();
                        
                    } catch (Exception ex) {
                        ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", this.getClass(),
                                        new VirnaPayload()
                                        .setString("Falha na carga da Analise de : "+ p.toFile().getAbsolutePath())
                                        .setFlag2(true)
                                        .setObject(payload.vobject)        
                                        .setCaller(ctrl)
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

            String pmessage = "Report Manager gravou um relatorio PDF em " + filename;
//            StatusDisplayer.getDefault().setStatusText(pmessage);
            //log.info("Report Manager is saving PDF report to  " + filename);  
            document.save(filename);
            document.close();
            
        } catch (Exception ex) {
//            Exceptions.printStackTrace(ex);
        }
     
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
                            ctrl.processSignal(new SMTraffic(0l, 0l, 0, "SHOWOOPS", this.getClass(),
                                    new VirnaPayload()
                                    .setString(mes1)
                                    .setObject(and)
                                    .setAuxiliar(new Object[] { "Gravar", "Mudar ID", "Cancelar" })
                                    .setCaller(ctrl)
                                    .setCallerstate("REPORTANALISE")
                            )); 
                }
                else{
                    ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REPORTANALISE", this.getClass(),
                        new VirnaPayload()
                        .setObject(current_model.getAn())
                        .setCaller(ctrl)
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
                
                ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REPORTANALISE", this.getClass(),
                    new VirnaPayload()
                    .setObject(current_model.getAn())
                    .setCaller(ctrl)
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
                    ctrl.processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", this.getClass(),
                                        new VirnaPayload()
                                        .setString("Falha na gravação do relatorio : "+ fullpath)
                                        .setFlag2(true)
                                        .setObject(payload.vobject)        
                                        .setCaller(ctrl)
                                        .setCallerstate("REPORTANALISE")
                    ));
                }
            }
        }
   
        return true;     
    }  
    
    
}
