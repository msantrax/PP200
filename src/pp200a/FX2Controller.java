/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.fxsupport.DateWidget;
import com.opus.fxsupport.EmptyValidator;
import com.opus.fxsupport.FXFController;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.FXFFieldDescriptor;
import com.opus.fxsupport.FXFTextField;
import com.opus.fxsupport.FXFValidator;
import com.opus.fxsupport.FXFWidgetManager;
import com.opus.fxsupport.NumberValidator;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.fxsupport.SystemMenu;
import com.opus.fxsupport.VoidValidator;
import com.opus.fxsupport.WidgetDescriptor;
import com.opus.syssupport.PicnoUtils;
import com.opus.syssupport.Profile;
import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.VirnaPayload;
import com.opus.syssupport.VirnaServiceProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import javafx.scene.paint.Color;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;




public class FX2Controller extends FXFController implements com.opus.fxsupport.FXFControllerInterface {

    private static final Logger LOG = Logger.getLogger(FX2Controller.class.getName());
    
   
    private FXFWidgetManager wdgmanager;
    
    private Scene scene;
    private ValidationSupport vs;
    private LinkedHashMap<FXFField, FXFValidator>validators;
   
    private String profilepath;
    private Profile profile;
    private Controller pp100ctrl = Controller.getInstance();
    
    private ObservableList<String> time_entries = FXCollections.observableArrayList();
    private ArrayList<Double> time_values = new ArrayList<>();
    
    
    
    @FXML
    private Label sidebar_btcycle;

    @FXML
    private Label sidebar_btstore;

    @FXML
    private Label sidebar_btreport;

    @FXML
    private FXFTextField it_porosidade;

    @FXML
    private FXFTextField it_densidade;

    @FXML
    private FXFTextField it_volume;

    @FXML
    private FXFTextField it_kfactor;

    @FXML
    private ImageView logoacp;

    @FXML
    private FXFTextField it_analisetime;

    @FXML
    private Button bt_timeplus;

    @FXML
    private Button bt_timedrop;

    @FXML
    private FXFTextField it_analiseaverage;

    @FXML
    private FXFTextField it_analisersd;

    @FXML
    private FXFTextField it_sid;

    @FXML
    private FXFTextField it_notas;

    @FXML
    private FXFTextField it_filtro;

    @FXML
    private FXFTextField it_mass;

    @FXML
    private CheckListView<String> checklist1;

    @FXML
    private FXFTextField it_area;

    @FXML
    private FXFTextField it_temperature;

    @FXML
    private DateWidget date;
   
    
    public FX2Controller(FXMLLoader fxmlLoader) {
        
        wdgmanager = FXFWidgetManager.getInstance();
        this.fxmlLoader = fxmlLoader; 
    }
    
    
    public FX2Controller(FXMLLoader fxmlLoader, String profilepath) {
        
        wdgmanager = FXFWidgetManager.getInstance();
        this.fxmlLoader = fxmlLoader; 
        this.profilepath = profilepath;
    }
    
    
    public void activateModel(){
        //machine.activateModel(profile.getArgument());
    }
    
    
    
    
    @FXML
    void initialize() {

        LOG.info("FX2Controller initializing ...");
        
        wdgmanager.addContext(this, fxmlLoader.getNamespace());
        wdgmanager.initFocus(this);
      
    }
    
    
    private void loadProfile(){   
        profile = Profile.getInstance();
    }
    
    public Profile getProfile() { return profile;}
    
    
    public void updateAnaliseTime (long timestamp) {
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                date.updateValue(String.valueOf(timestamp), true);
            }
        });
    }
    
    
    public void updateField (String fieldname, String value, boolean required){
        
        FXFField field = wdgmanager.getWidget(this, fieldname);
        WidgetDescriptor wd = wdgmanager.context.get(this).findByName(fieldname);
        if (field != null){
            field.updateValue(value, wd.required);
            //LOG.info(String.format("Updating %s with %s / required = %s", fieldname, value, required));
        }
        else{
            LOG.warning(String.format("Widgetmanager failed to locate widget : %s", fieldname));
        } 
    }
    
    
    
    
    private void initField(FXFField field, FXFFieldDescriptor fxfd){
        
        
        fxfd.setField(field);
        Control ctrl = (Control)fxfd.getField();
        
        //LinkedHashMap<String,PropertyLinkDescriptor>proplink_uimap = pp100ctrl.getCalUIMap();
        
        ctrl.setTooltip(new Tooltip(fxfd.getTooltip_message()));

        pp100ctrl.updateUIWidget(fxfd.getName(), fxfd.getField(), false);
        field.setSid(fxfd.getName());

        // Create Number Validator if needed
        if (fxfd.getValidator_type().equals("number")){
            NumberValidator nv = new NumberValidator();
            validators.put(fxfd.getField(), nv);
            
            nv.setMaybenull(fxfd.isMaybenull());
            ValidationSupport.setRequired(ctrl, fxfd.isRequired());
            
            if (fxfd.isUse_range()){
                if (fxfd.isUse_windowrange()){
                    nv.setRangeWindows(fxfd.getRanges()[0], fxfd.getRanges()[1], fxfd.getRanges()[2]);
                }
                else{
                    nv.setRanges(fxfd.getRanges());
                }
            } 
            
            validators.get(fxfd.getField()).initTooltip(ctrl.getTooltip());
           
            
            vs.registerValidator(ctrl, new Validator<String>(){
                @Override
                public ValidationResult apply( Control control, String value ){
                    NumberValidator validator = (NumberValidator)validators.get(control);
                    validator.getResult(value);
                    
                    boolean validated = !validator.isFailed() || validator.isWarning(); 
                    if (!value.equals("")){
                        pp100ctrl.varCallback(control, value, validated, false);
                    }
                    return ValidationResult.fromMessageIf(control, 
                            validator.getMessage(), 
                            validator.isWarning() ? Severity.WARNING : Severity.ERROR, 
                            validator.isFailed() ? true : false);
                };
            });
        } // Number Validator
        
        else if (fxfd.getValidator_type().equals("notempty")){
            EmptyValidator ev = new EmptyValidator();
            validators.put(fxfd.getField(), ev);
            validators.get(fxfd.getField()).initTooltip(ctrl.getTooltip());
            //ValidationSupport.setRequired(ctrl, fxfd.isRequired());
            
            vs.registerValidator(ctrl, new Validator<String>(){
                @Override
                public ValidationResult apply( Control control, String value ){
                    EmptyValidator validator = (EmptyValidator)validators.get(control);
                    validator.getResult(value);
                    
                    boolean validated = !validator.isFailed() || validator.isWarning(); 
                    pp100ctrl.varCallback(control, value, validated, false);
   
                    return ValidationResult.fromMessageIf(control, 
                            validator.getMessage(), 
                            validator.isWarning() ? Severity.WARNING : Severity.ERROR, 
                            validator.isFailed() ? true : false);
                    
                };
            });
        } // Empty Validator
        
        else if (fxfd.getValidator_type().equals("void")){
            VoidValidator vv = new VoidValidator();
            validators.put(fxfd.getField(), vv);
            validators.get(fxfd.getField()).initTooltip(ctrl.getTooltip());
            //ValidationSupport.setRequired(ctrl, fxfd.isRequired());
            
            vs.registerValidator(ctrl, new Validator<String>(){
                @Override
                public ValidationResult apply( Control control, String value ){
                    VoidValidator validator = (VoidValidator)validators.get(control);
                    validator.getResult(value);
                    
                    boolean validated = !validator.isFailed() || validator.isWarning(); 
                    pp100ctrl.varCallback(control, value, validated, false);
   
                    return ValidationResult.fromMessageIf(control, 
                            validator.getMessage(), 
                            validator.isWarning() ? Severity.WARNING : Severity.ERROR, 
                            validator.isFailed() ? true : false);
                    
                };
            });
        } // Void Validator
        
        
        if (fxfd.isUse_autocomplete()){
            String acfile = fxfd.getAutocomplete_file();
            ArrayList<String> acl = null;
            if(acfile != null){
                try {
                    acl = PicnoUtils.loadAuxJson(acfile, ArrayList.class);
                } catch (IOException ex) {
                    LOG.warning(String.format("AuxJson unable to load from %s due %s - using default list instead", acfile, ex.getMessage()));
                }
            }
            
            if (acl == null){
                try {
                    acl = getDefaultACList();
                    PicnoUtils.saveAuxJson(acfile, acl, true);
                } catch (IOException ex) {
                    LOG.warning(String.format("AuxJson unable to save default list to %s due %s", acfile, ex.getMessage()));
                }
            }
            
            TextFields.bindAutoCompletion((TextField)fxfd.getField(), acl);
        }
        
        String localcallback = fxfd.getLocal_callback();
        if (!localcallback.equals("")){
            PropertyLinkDescriptor linkdesc;
            linkdesc = new PropertyLinkDescriptor()
                                                .setFxfield(field)
                                                .setInput(true)
                                                .setStopfocus(true)
                                                .setPlink(fxfd.getName())
                                                .setCallstate(localcallback);
//            proplink_uimap.put(fxfd.getName(), linkdesc);
  
            LOG.info(String.format("Added local field %s", fxfd.getName()));
        }
        
        WidgetDescriptor wd = getWidgetDescriptor(fxfd.getName());
        wd.required = fxfd.isRequired();
//        PropertyLinkDescriptor pld = proplink_uimap.get(fxfd.getName());
//        if (wd != null && pld != null){
//            wd.linkdescriptor = pld;
//        }
     
   
    }
    
    public ArrayList<String> getDefaultACList(){
        
        ArrayList<String> ac = new ArrayList<>();        
        ac.addAll(Arrays.asList(
        "Amostra de Teste 1", "Amostra de Teste 2", "Amostra Secundária 1", "Padrão de Validação", 
                "Amostra sem compromisso", "Teste operacional", "Jack Daniels", "Coca Cola"
        ));
 
        return ac;
    }
    
    
    
    
    @Override
    public void sendSignal (PropertyLinkDescriptor pld, String sigtype){
        
        pp100ctrl.getQueue().offer(new SMTraffic(0l, 0l, 0, pld.getCallstate(), this.getClass(),
                                   new VirnaPayload().setObject(pld)));
        
    }
    
    
    public void update(Scene scene){
        
        FXFField field;
        
        this.scene = scene;
        vs = new ValidationSupport(); 
        validators = new LinkedHashMap<>();
        
        //pp100ctrl.setFXCalibController(this);
        
        loadProfile();
        ArrayList<FXFFieldDescriptor> descriptors = profile.getDescriptors();
        
        
        for (FXFFieldDescriptor fxfd : descriptors){
            
            
            String fieldname = fxfd.getName();
            field = wdgmanager.getWidget(this, fieldname);
            if (field !=null){
                initField( field, fxfd);
                //LOG.warning(String.format("Profile load said field %s was updated", fxfd.getName()));
            }
            else{
                LOG.warning(String.format("Profile load said Failed do locate field %s on wdgmanager...", fxfd.getName()));
            }
        }
  
        
        initTimeList();
        checklist1.setItems(time_entries);
  
        checklist1.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) change -> {
            
            boolean changed = false;
            
            while (change.next()) {
                
                int index = change.getFrom();
                Double v = getTime_values().get(index);
                    
                if (!change.wasAdded()){
                    LOG.info(String.format("Change removed item %d", index));    
                    getTime_values().remove(index);
                    getTime_values().add(index, 0.0);
                    changed = true;
                }
                else{
                    if (v == 0.0){
                        LOG.info(String.format("Change added item %d", index));
                        String s = change.getAddedSubList().get(0);
                        try{
                            Double value = Double.valueOf(s);
                            getTime_values().remove(index);
                            getTime_values().add(index, value);
                            changed = true;
                        }
                        catch (Exception ex){
                            LOG.warning(String.format("Failed to convert Checked list item on %d", index));   
                            return;
                        }
                    }
                }
                
                if (changed){
                    pp100ctrl.getQueue().offer(new SMTraffic(0l, 0l, 0, "CALCULATETIME", this.getClass(),
                                   new VirnaPayload().setObject(time_values)));
                }
                
                checklist1.setOpacity(0.0);
                
 
            }
        });
        
        
        
        sidebar_btcycle.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.REFRESH).getText())
                .sizeFactor(5).color(Color.LIGHTGREEN)
                .useGradientEffect()
        );
        
        sidebar_btstore.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.DATABASE).getText())
                .sizeFactor(5).color(Color.LIGHTGREEN)
                .useGradientEffect()
        );
        
        sidebar_btreport.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.FILE_PDF_ALT).getText())
                .sizeFactor(5).color(Color.LIGHTGREEN)
                .useGradientEffect()
        );
        
        
        pp100ctrl.getQueue().offer(new SMTraffic(0l, 0l, 0, "NEWCALIB", this.getClass(),
                                   new VirnaPayload()));
        
    }
    
    public void initTimeList() { 
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                time_entries.clear();
                getTime_values().clear();
            }
        });
    }
    
    public List<String> getCheckedTimeEntries() { 
        return checklist1.getCheckModel().getCheckedItems();
    }
    
    public void addTimeEntry (String item) { 
        
        try{
            Double value = Double.valueOf(item);
            getTime_values().add(value);
        }
        catch (Exception ex){
            return;
        }
        
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                time_entries.add(item);
                
                for (int i = 0; i < getTime_values().size(); i++) {
                    Double d = getTime_values().get(i);
                    if (d != 0){
                        checklist1.getCheckModel().check(i);
                    }
                }             
                it_analisetime.setText("");
            }
        });
        
        pp100ctrl.getQueue().offer(new SMTraffic(0l, 0l, 0, "CALCULATETIME", this.getClass(),
                                   new VirnaPayload()
                                           .setObject(time_values)
                                           .setFlag1(Boolean.TRUE)
                                    ));
    
    }
 
    public ArrayList<Double> getTime_values() {
        return time_values;
    }

    public void setTime_values(ArrayList<Double> time_values) {
        this.time_values = time_values;
    }
    
    
    public void lockUI (boolean lock){
      
        if (lock){
            sidebar_btstore.setOpacity(0.5);
            sidebar_btreport.setOpacity(0.5);
            //sidebar_btbroadcast.setOpacity(0.5);
        }
        else{
            sidebar_btstore.setOpacity(1.0);
            sidebar_btreport.setOpacity(1.0);
            //sidebar_btbroadcast.setOpacity(1.0);
        }
        
    }
    
    
    // ========================================= DISPATCH SECTION =======================================================
    
    
    @FXML
    void canvas_clicked(MouseEvent event) {
        //LOG.info("Canvas clicked ...");
        clearCanvas();
    }
    
    
    @FXML
    void btcycle_action(MouseEvent event) {
        //pp100ctrl.newCalib();
    }

    
    @FXML
    void btreport_action(MouseEvent event) {
        //pp100ctrl.reportCalibration();
    }

    @FXML
    void btstore_action(MouseEvent event) {
        //pp100ctrl.storeCalibration();
    }
    
    
    @FXML
    void timedrop_action(ActionEvent event) {
        //LOG.info("Timedrop clicked ...");
        if (checklist1.getOpacity() == 100.0){
            checklist1.setOpacity(0.0);
        }
        else{
            checklist1.setOpacity(100.0);
        }
        
    }
    
    @Override
    public void clearCanvas(){
        checklist1.setOpacity(0.0);
    }
    
    
    public void yieldFocus(FXFField field, boolean fwd){
        wdgmanager.yieldFocus(this, field, fwd);
    }
    
    
    public Scene getScene() { return scene;}

    @Override
    public String getUID() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAppController(VirnaServiceProvider ctrl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SystemMenu getMenu(boolean isadm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

























       
        
        
//        FX001_campo1.setTooltip(new Tooltip("Mensagem de teste numero 2"));
//        validators.put(FX001_campo1, new NumberValidator()
//                .setRangeWindows(10.0, 10.0, 15.0)
//        );
//    
//        validators.get(FX001_campo1).initTooltip(FX001_campo1.getTooltip());
//        
//        vs.registerValidator(FX001_campo1, new Validator<String>(){
//            @Override
//            public ValidationResult apply( Control control, String value ){
//                NumberValidator validator = (NumberValidator)validators.get(control);
//                validator.getResult(value);
// 
//                return ValidationResult.fromMessageIf(control, 
//                        validator.getMessage(), 
//                        validator.isWarning() ? Severity.WARNING : Severity.ERROR, 
//                        validator.isFailed() ? true : false);
//            };
//        });
        

//        FX002_campo2.setTooltip(new Tooltip("Mensagem de teste numero 2"));
//        validators.put(FX002_campo2, new NumberValidator()
//                .setRangeWindows(50.0, 10.0, 15.0)
//        );
//    
//        validators.get(FX002_campo2).initTooltip(FX002_campo2.getTooltip());
//        
//        vs.registerValidator(FX002_campo2, new Validator<String>(){
//            @Override
//            public ValidationResult apply( Control control, String value ){
//                NumberValidator validator = (NumberValidator)validators.get(control);
//                validator.getResult(value);
// 
//                return ValidationResult.fromMessageIf(control, 
//                        validator.getMessage(), 
//                        validator.isWarning() ? Severity.WARNING : Severity.ERROR, 
//                        validator.isFailed() ? true : false);
//            };
//        });



        //Color randomColor = new Color( Math.random(), Math.random(), Math.random(), 1);
        
        //Glyph glyph = new Glyph("FontAwesome", FontAwesome.Glyph.REFRESH);
        //Glyph graphic = Glyph.create( "FontAwesome|" + glyph.getText()).sizeFactor(5).color(Color.LIGHTBLUE).useGradientEffect();