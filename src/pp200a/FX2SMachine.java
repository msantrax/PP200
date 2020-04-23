/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.syssupport.Config;
import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.VirnaPayload;
import com.opus.syssupport.smstate;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

/**
 *
 * @author opus
 */
public class FX2SMachine {

    private static final Logger log = Logger.getLogger(FX2SMachine.class.getName());
  
    
    private LinkedHashMap<String, String> profile_map = new LinkedHashMap<>();
    private LinkedHashMap<String, FX2Model> models;
    private FX2Model current_model;
    
    public static final Double visco_a0 = 1.8076923000E-06;
    public static final Double visco_a1 = 1.3084615380E-03;
    
    private static FX2SMachine instance; 
    public static FX2SMachine getInstance(){
        if (instance == null) {instance = new FX2SMachine();}
        return instance;
    }

    
    public FX2SMachine() {
        
    }
    
    
    public void newCalib(){      
//        smqueue.offer(new SMTraffic(0l, 0l, 0, "NEWCALIB", 
//                                   new VirnaPayload().setObject(cal)));
    }
    
    public void reportCalibration(){
        
//        smqueue.offer(new SMTraffic(0l, 0l, 0, "REPORTCALIBRATION", 
//                                new VirnaPayload()
//                                .setObject(cal)
//                                .setCaller(this)
//                                .setCallerstate("")
//        )); 
        
    }
    
    public void storeCalibration(){   
        
//        if (cal == null){
//            cal = new CalibDescriptor();
//            cal.addPropertyChangeListener(this);
//        }
//
//        smqueue.offer(new SMTraffic(0l, 0l, 0, "STORECALIBFILE", 
//                                    new VirnaPayload()
//                                    .setObject(cal)
//                                    .setCaller(this)
//                                    .setCallerstate("")
//        ));  
        
    }
    
    public void updateCalibCanvas(){
        
//        nocalc=true;
        
        
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
        
//        nocalc = false;  
    }
    
    
    
    private Double getVisco (Double temp){
        
        Double ret;
        ret = ((temp * visco_a0) + visco_a1) * 1000;
        return ret;
    }
    
    
    private Double getDoublePValue (String key, String svalue, boolean analise) {
        
//        Double d;
//        PropertyLinkDescriptor pld;
//        
//        if (analise){
//            pld = ana_proplink_modelmap.get(key);
//        }
//        else{
//            pld = cal_proplink_modelmap.get(key);
//        }
//        
//        if (pld != null && pld.isValid()){
//            try{
//                if (svalue.equals("")){
//                    d = Double.NaN;
//                }
//                else{
//                    d = Double.parseDouble(svalue);
//                }
//                
//                //log.info(String.format(Locale.US, "getDoublepValue parsed %s to %5.3f", svalue, d));
//                return d;
//            }
//            catch (Exception ex){
//                log.warning(String.format("Failed to convert property %s due %s", key, ex.getMessage()));
//            }
//        }    
        return Double.NaN;
    }
    
    
    
    
    
    // =================================================== STATES =======================================================
    
    @smstate (state = "NEWCALIB")
    public boolean st_newCalib(SMTraffic smm){
        
//        log.info("New Calib called ...");
//
//        cal.loadDefaults();
//        
//        updateCalibCanvas();
//        calibclean = true;
//
//        smqueue.offer(new SMTraffic(0l, 0l, 0, "LOCKUI", 
//                                   new VirnaPayload()
//                                           .setInt1(0)
//                                           .setInt2(1)
//        ));

        return true; 
        
    }
    
    
    @smstate (state = "CALCULATETIME")
    public boolean st_calculateTime(SMTraffic smm){
        
//        //log.info("Calculating new analysis time");
//        ArrayList<Double> values = (ArrayList<Double>)smm.getPayload().vobject;
//        
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
        return true;
    }
    
   
    @smstate (state = "UPDATECALTIME")
    public boolean st_updatCalTime(SMTraffic smm){
        
//        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
//        
//        if (pld != null){
//            String mode = pld.getAuxiliar();
//            if (mode != null && mode.equals("STOPENTER")){
//                //log.info("Input Time called called with STOPENTER");
//                //calct.addTimeEntry(pld.getFxfield().getValue());
//                cal.getTempos().add(pld.getFxfield().getValue());
//            }
//            else{
//                //log.info(String.format("CALTIME called with %s ", pld.isValid() ? "valid":"invalid"));
//            }
//        }
//        
        return true;
    }
    
    
    @smstate (state = "CALCKFACTOR")
    public boolean st_calcKFactor(SMTraffic smm){
        
//        //Double viscosity = 0.001348;
//        
//        Double viscosity = 1.349;
//        
//        Double kfactor;
//        Double mass;
//        Double temperature;
//        
//        Double layervol;
//        
//        Double porosity;
//        Double density;
//        Double area;
//        
//        Double time;
//        Double rsd;
//        
//        double top, bottom;
//        
//        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
//        
//        //log.info("Calc kfactor called.......................");
//        
//        if (pld!= null){
//            if (!pld.isValid()){
//                //calct.updateField ("it_kfactor", "", false);
//                return true;
//            }
//        }
//        
//        porosity = getDoublePValue("porosidade", cal.getPorosidade(), false);
//        if (porosity.isNaN()){
//            //calct.updateField ("it_kfactor", "", false);
//            return true;
//        }
//        
//        density  = getDoublePValue("densidade", cal.getDensidade(), false);
//        if (density.isNaN()){
//            //calct.updateField ("it_kfactor", "", false);
//            return true;
//        }
//        
//        
//        layervol  = getDoublePValue("volume_camada", cal.getVolume_camada(), false);
//        if (layervol.isNaN()){
//            //calct.updateField ("it_kfactor", "", false);
//            return true;
//        }
//        
//        
//        
//        
//        area  = getDoublePValue("area", cal.getArea(), false);
//        if (area.isNaN()){
//            //calct.updateField ("it_kfactor", "", false);
//            return true;
//        }
//       
//        temperature  = getDoublePValue("temperature", cal.getTemperature(), false);
//        if (temperature.isNaN()){
//            //calct.updateField ("it_kfactor", "", false);
//            return true;
//        }
//        else{
//            viscosity = getVisco(temperature);
//        }
//        
//        
//        time  = getDoublePValue("media", cal.getMedia(), false);
//        if (time.isNaN()){
//            //calct.updateField ("it_kfactor", "", false);
//            return true;
//        }
//        
//        rsd  = getDoublePValue("rsd", cal.getRsd(), false);
//        if (rsd.isNaN()){
//            //calct.updateField ("it_kfactor", "", false);
//            return true;
//        }
//        
//        
//        top = (1 - porosity) * viscosity;
//        bottom = Math.sqrt(porosity * porosity * porosity) * Math.sqrt(time) ;
//        
//        kfactor = area*density*(top/bottom);
//        mass = (1 - porosity) * (density * layervol);
//        
//        
//        Double kfactor1 = 1.414 * area * density * (viscosity/ Math.sqrt(time));
//        
//        String skfactor = String.format(Locale.US, "%6.4f",kfactor);
//        String smass = String.format(Locale.US, "%6.3f",mass);
//      
//        log.info(String.format("Kfactor was calculated and is %s -- mass is %s g", skfactor, smass));
//        log.info(String.format("\tequals -> (1 - epsilon) * visco -> %7.4f * %9.7f", (1 - porosity), viscosity/1000));
//        log.info(String.format("\tdivided by -> sqrt(epsilon³) * sqrt(time average) -> %7.4f * %7.4f", Math.sqrt(porosity * porosity * porosity),  Math.sqrt(time)));
//        log.info(String.format("\ttimes -> area * rho -> %7.4f * %7.4f", area, density));
//        
//        cal.setKfactor(skfactor);
//        cal.setMassa_ensaio(smass);
//        //calct.updateField ("it_kfactor", skfactor, false);
//        //calct.updateField ("it_mass", smass, false);
//        
//        
        return true;
    }
    
    @smstate (state = "CALCCALMASS")
    public boolean st_calcCalMass(SMTraffic smm){
        
//        Double porosity;
//        Double density;
//        Double layervol;
//        Double samplemass;
//        
//        PropertyLinkDescriptor pld = (PropertyLinkDescriptor)smm.getPayload().vobject;
//        
//        if (pld!= null){
//            if (!pld.isValid()){
//                //calct.updateField ("it_mass", "", true);
//                return true;
//            }
//        }
//        
//        porosity = getDoublePValue("porosidade", cal.getPorosidade(), false);
//        if (porosity.isNaN()){
//            //calct.updateField ("it_mass", "", false);
//            return true;
//        }
//        
//        density  = getDoublePValue("densidade", cal.getDensidade(), false);
//        if (density.isNaN()){
//            //calct.updateField ("it_mass", "", false);
//            return true;
//        }
//        
//        layervol  = getDoublePValue("volume_camada", cal.getVolume_camada(), false);
//        if (layervol.isNaN()){
//            //calct.updateField ("it_mass", "", false);
//            return true;
//        }
//        
//        samplemass = (1 - porosity) * (density * layervol);
//        
//        
//        String smass = String.format(Locale.US, "%5.3f", samplemass);
//        log.info(String.format("Calib sample mass was calculated : %s", smass));
//        //calct.updateField ("it_mass", smass, false);
//    
        
        return true;
    }
    
    
    private void storeCalibFile(Path p) throws IOException{
        
//        GsonBuilder builder = new GsonBuilder(); 
//        builder.setPrettyPrinting(); 
//        Gson gson = builder.create();
//        String sjson = gson.toJson(cal);
// 
//        Files.write(p, sjson.getBytes(StandardCharsets.UTF_8));
//        log.info(String.format("Stored Calibration on %s", p.toString()));

    }
    
    
    @smstate (state = "STORECALIBFILE")
    public boolean st_storeCalibFile(SMTraffic smm){
        
//        String filename;
//        
//        CalibDescriptor and;
//        VirnaPayload payload = smm.getPayload();
//        
//        Object obj = payload.vobject;
//        if (obj == null){
//            and = new CalibDescriptor();
//        }
//        else{
//            and = (CalibDescriptor)obj;
//        }
//        
//        Object caller = payload.getCaller();
//        
//        String message = "A Calibração com o ID";
//        filename = String.format("cal-%1$td%1$tm%1$ty%1$tH%1$tM%1$tS.json", and.getUid());
//        Path p = Paths.get(Config.getInstance().getExport_dir()+filename);
//        try {
//            storeCalibFile(p);
////            StatusDisplayer.getDefault().setStatusText(String.format("%s %d foi gravada no arquivo %s", 
////                message, and.getUid(), p.toFile().getAbsolutePath()));
//            calibclean = true;
//        } catch (IOException ex) {
//            processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", 
//                                new VirnaPayload()
//                                .setString("Falha na gravação do arquivo : "+ filename)
//                                .setFlag2(true)
//                                .setObject(payload.vobject)        
//                                .setCaller(this)
//                                .setCallerstate("STOREANALISEFILE")
//            ));
//        }
      
        return true;
    }
    
    
    
    
    @smstate (state = "REPORTCALIBRATION")
    public boolean st_reportCalibration(SMTraffic smm){
        
//        String filename;
//        String fullpath;
//        
//        CalibDescriptor and;
//        VirnaPayload payload = smm.getPayload();
//        
//        Object obj = payload.vobject;
//        if (obj == null){
//            and = new CalibDescriptor();
//        }
//        else{
//            and = (CalibDescriptor)obj;
//        }
//        
//        Object caller = payload.getCaller(); 
//        
//        
//        fullpath = String.format("%1$scal-%3$s:%2$td%2$tm%2$ty%2$tH%2$tM%2$tS.pdf", 
//                        Config.getInstance().getReport_dir(), and.getUid(), and.getSid());
//        try {
//            emitCalibReport(fullpath, "calib_template");
////            StatusDisplayer.getDefault().setStatusText(String.format("Report com Calibração ID %d foi emitido no arquivo %s", 
////                and.getUid(), fullpath));
////            log.info(String.format("Report com calibração ID %d foi emitido no arquivo %s", 
////                and.getUid(), fullpath));
//        } catch (Exception ex) {
//            processSignal(new SMTraffic(0l, 0l, 0, "REGISTERNOTIFICATION", 
//                                new VirnaPayload()
//                                .setString("Falha na gravação do relatorio : "+ fullpath)
//                                .setFlag2(true)
//                                .setObject(payload.vobject)        
//                                .setCaller(this)
//                                .setCallerstate("REPORTANALISE")
//            ));
//        }
        
        return true; 
    }
    
    
    
}
