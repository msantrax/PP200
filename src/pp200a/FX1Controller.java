/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;


import com.opus.syssupport.Profile;
import com.opus.fxsupport.SystemMenu;
import com.opus.fxsupport.DateWidget;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.FXFFieldDescriptor;
import com.opus.fxsupport.FXFTextField;
import com.opus.fxsupport.FXFValidator;
import com.opus.fxsupport.FXFWidgetManager;
import com.opus.fxsupport.NumberValidator;
import com.opus.fxsupport.EmptyValidator;
import com.opus.fxsupport.FXFWidgetManager.WidgetDescriptor;
import com.opus.fxsupport.VoidValidator;
import com.opus.syssupport.Config;
import com.opus.syssupport.PicnoUtils;
import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.VirnaPayload;
import com.opus.syssupport.VirnaServiceProvider;
import java.io.File;
import java.io.IOException;


import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import javafx.scene.paint.Color;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;



public class FX1Controller implements com.opus.fxsupport.FXFController {

    private static final Logger LOG = Logger.getLogger(FX1Controller.class.getName());
    
    private FXMLLoader fxmlLoader;
    private FXFWidgetManager wdgmanager;
    private final String id = "scene1";
    private Scene scene;
    private ValidationSupport vs;
    private LinkedHashMap<FXFField, FXFValidator>validators;
    
    private Profile profile;
    private Controller pp100ctrl = Controller.getInstance();
    
    private ObservableList<String> time_entries = FXCollections.observableArrayList();
    private ArrayList<Double> time_values = new ArrayList<>();
    
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    
    @FXML
    private ImageView logoacp;


     @FXML
    private Label sidebar_btcycle;

    @FXML
    private Label sidebar_btstore;

    @FXML
    private Label sidebar_btreport;

    @FXML
    private Label sidebar_btbroadcast;
    
    @FXML
    private Label sidebar_btloadfile;
    
    
    
    
    @FXML
    private FXFTextField it_porosidade;

    @FXML
    private FXFTextField it_densidade;

    @FXML
    private FXFTextField it_massa_calculada;

    @FXML
    private FXFTextField it_blaineresult;

    @FXML
    private CheckListView<String> checklist1;

    
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
    private FXFTextField it_lote;
    
    
    @FXML
    private FXFTextField it_calibfile;

    @FXML
    private FXFTextField it_constantek;

    @FXML
    private FXFTextField it_layervolume;

    @FXML
    private FXFTextField it_temperature;

    @FXML
    private DateWidget date;
    
    @FXML
    private Label lb_user;
    
    @FXML
    private Label lb_profile;
    
    @FXML
    private ComboBox<String> cb_profile;
    
    
    @FXML
    private AnchorPane pnl_user;

    @FXML
    private Button bt_logout;
    
    
    @FXML
    private Label lb_avatar;
    
    @FXML
    private Label lb_logged;

    @FXML
    private ImageView avatar;
    
    
    public FX1Controller(FXMLLoader fxmlLoader) {
        
        wdgmanager = FXFWidgetManager.getInstance();
        this.fxmlLoader = fxmlLoader; 
    }
    
    
    // Application controller link 
    private VirnaServiceProvider ctrl;
    @Override
    public void setAppController (VirnaServiceProvider ctrl){
        this.ctrl = ctrl;
    }
    
    
    @FXML
    void initialize() {
        LOG.info("FX1Controller initializing ...");        
        wdgmanager.addContext(this, fxmlLoader.getNamespace());
        wdgmanager.initFocus(this);
      
    }
    
    
    @Override
    public SystemMenu getMenu(boolean isadm){
        
        SystemMenu menupane = new SystemMenu();
        
        Label shutdown = new Label("Shutdown");

        menupane.setTopAnchor(shutdown, 20.0);
        menupane.setRightAnchor(shutdown, 20.0);
        menupane.getChildren().add(shutdown);
        
        return menupane;

    }
    
    
    
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
        
        LinkedHashMap<String,PropertyLinkDescriptor>proplink_uimap = pp100ctrl.getAnaUIMap();
        
        ctrl.setTooltip(new Tooltip(fxfd.getTooltip_message()));

        pp100ctrl.updateUIWidget(fxfd.getName(), fxfd.getField(), true);
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
                        pp100ctrl.varCallback(control, value, validated, true);
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
                    pp100ctrl.varCallback(control, value, validated, true);
   
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
                    pp100ctrl.varCallback(control, value, validated, true);
   
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
            proplink_uimap.put(fxfd.getName(), linkdesc);
  
            LOG.info(String.format("Added local field %s", fxfd.getName()));
        }
        
        WidgetDescriptor wd = wdgmanager.getWidgetDescriptor(this, fxfd.getName());
        wd.required = fxfd.isRequired();
        PropertyLinkDescriptor pld = proplink_uimap.get(fxfd.getName());
        if (wd != null && pld != null){
            wd.linkdescriptor = pld;
        }
     
    }
    
    
    @Override
    public void sendSignal (PropertyLinkDescriptor pld, String sigtype){
        
        pp100ctrl.getQueue().offer(new SMTraffic(0l, 0l, 0, pld.getCallstate(), 
                                   new VirnaPayload().setObject(pld)));
        
    }
    
    
    public ArrayList<String> getDefaultACList(){
        
        ArrayList<String> ac = new ArrayList<>();        
        ac.addAll(Arrays.asList(
        "Amostra de Teste 1", "Amostra de Teste 2", "Amostra Secundária 1", "Padrão de Validação", 
                "Amostra sem compromisso", "Teste operacional", "Jack Daniels", "Coca Cola"
        ));
 
        return ac;
    }
    
    
    public void loadProfile(){
        
        profile = pp100ctrl.getProfile();
        lb_profile.setText(profile.getLabel());
        
    }
    
    public Profile getProfile() { return profile;}
    
    
    public void updateUserPanel(){
        
        cb_profile.setItems(FXCollections.observableArrayList(pp100ctrl.getProfile_list()));
        cb_profile.setValue(profile.getLabel());
//        if (PicnoUtils.user.isMay_changeprofile()){
//            cb_profile.setDisable(false);
//        }
//        else{
//            cb_profile.setDisable(true);
//        }
        lb_avatar.setText("User");
        String log1 = String.format(PicnoUtils.timestamp_format, pp100ctrl.getLogTime());
        lb_logged.setText(log1);
        
        String savatar = "";
        if (!savatar.isEmpty()){
            File file = new File(Config.getInstance().getAux_dir()+savatar);
            Image image = new Image(file.toURI().toString());
            avatar.setImage(image);          
            ImageView iv = new ImageView(image);
            iv.setFitHeight(50);
            iv.setFitWidth(50);
            lb_user.setGraphic(iv);
        }
        else{
            lb_user.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.USER).getText())
                .sizeFactor(3).color(Color.WHITE)
            );
        }
    }
    
    
    public void update(Scene scene){
        
        FXFField field;
        
        this.scene = scene;
        vs = new ValidationSupport(); 
        validators = new LinkedHashMap<>();
        
        pp100ctrl.setFXANController(this);
        pp100ctrl.loadLastCalibration();
        
        loadProfile();
        updateUserPanel();
        
        ArrayList<FXFFieldDescriptor> descriptors = profile.getAnadescriptors();
        
        for (FXFFieldDescriptor fxfd : descriptors){
            
            field = wdgmanager.getWidget(this, fxfd.getName());
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
                    pp100ctrl.getQueue().offer(new SMTraffic(0l, 0l, 0, "CALCULATETIME", 
                                   new VirnaPayload().setObject(time_values)));
                }
                
                checklist1.setOpacity(0.0);
                
 
            }
        });
        
        
        sidebar_btcycle.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.REFRESH).getText())
                .sizeFactor(5).color(Color.LIGHTBLUE)
                .useGradientEffect()
        );
        
        sidebar_btstore.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.DATABASE).getText())
                .sizeFactor(5).color(Color.LIGHTBLUE)
                .useGradientEffect()
        );
        
        sidebar_btreport.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.FILE_PDF_ALT).getText())
                .sizeFactor(5).color(Color.LIGHTBLUE)
                .useGradientEffect()
        );
        
        sidebar_btbroadcast.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.SHARE_ALT).getText())
                .sizeFactor(5).color(Color.LIGHTBLUE)
                .useGradientEffect()
        );
        
        sidebar_btloadfile.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.ARCHIVE).getText())
                .sizeFactor(5).color(Color.LIGHTBLUE)
                .useGradientEffect()
        );
        
        bt_logout.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.POWER_OFF).getText())
                .sizeFactor(2).color(Color.LIGHTBLUE)
                .useGradientEffect()
        );
        
        
        
        
        
        pp100ctrl.getQueue().offer(new SMTraffic(0l, 0l, 0, "NEWANALISE", 
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
                
                pp100ctrl.getQueue().offer(new SMTraffic(0l, 0l, 0, "CALCULATETIME", 
                                   new VirnaPayload()
                                           .setObject(time_values)
                                           .setFlag1(Boolean.FALSE)
                                    ));
                
                
            }
        });
        
//        pp100ctrl.getQueue().offer(new SMTraffic(0l, 0l, 0, "CALCULATETIME", 
//                                   new VirnaPayload()
//                                           .setObject(time_values)
//                                           .setFlag1(Boolean.FALSE)
//                                    ));
    
    }
 
    
    public void lockUI (boolean lock){
      
        if (lock){
            sidebar_btstore.setOpacity(0.5);
            sidebar_btreport.setOpacity(0.5);
            sidebar_btbroadcast.setOpacity(0.5);
        }
        else{
            sidebar_btstore.setOpacity(1.0);
            sidebar_btreport.setOpacity(1.0);
            sidebar_btbroadcast.setOpacity(1.0);
        }
        
    }
    
    
    // ========================================= DISPATCH SECTION =======================================================
    
    @FXML
    void btcycle_action(MouseEvent event) {
        pp100ctrl.newAnalise();
    }

    @FXML
    void btexport_action(MouseEvent event) {
        LOG.info("BT export clicked ...");
    }

    @FXML
    void btreport_action(MouseEvent event) {
        pp100ctrl.reportAnalise();
    }

    @FXML
    void btstore_action(MouseEvent event) {
        pp100ctrl.storeAnalise();
    }
    
    @FXML
    void btloadfile_action(MouseEvent event) {
        pp100ctrl.loadFile();
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
    
    @FXML
    void timeadd_action(ActionEvent event) {
        LOG.info("Timeadd clicked ...");  
    }
    
    
    
    @FXML
    void profile_action(ActionEvent event) {
        //LOG.info("Profile action called ...");
        
        pp100ctrl.loadProfile(cb_profile.getValue());
        pnl_user.setVisible(false);    
    }

    
    @FXML
    void logout_action(MouseEvent event) {
        pp100ctrl.doLogout();
        //pnl_user.setVisible(false);
    }
   
    @Override
    public void clearCanvas(){ 
        pnl_user.setVisible(false);
        checklist1.setOpacity(0.0);
    }
    
    @FXML
    void canvas_clicked(MouseEvent event) {
        //LOG.info("Canvas clicked ...");
        clearCanvas();
    }
    
    
    @FXML
    void user_action(MouseEvent event) {
        //LOG.info("User clicked ...");
        if (pnl_user.isVisible()){
            pnl_user.setVisible(false);
        }
        else{
            pnl_user.setVisible(true);
        }
    }
    
    
    @Override
    public void yieldFocus(FXFField field, boolean fwd){
        wdgmanager.yieldFocus(this, field, fwd);
    }
    
    public ArrayList<Double> getTime_values() {
        return time_values;
    }

    public void setTime_values(ArrayList<Double> time_values) {
        this.time_values = time_values;
    }
    
    
    public Scene getScene() { return scene;}

    private static final String UID = "FX1";
    @Override
    public String getUID() { return UID;}

    @Override
    public void update() {
        
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


//final ObservableList<String> calibs = FXCollections.observableArrayList();
//        for (int i = 0; i <= 15; i++) {
//            calibs.add("Padrão HF-207 calib:" + i);
//        }
//        
//        cb_calibration.setItems(calibs);
//        cb_calibration.setValue("Escolha uma calibração");


               
                
//                System.out.println("============================================");
//                System.out.println("Change: " + change);
//                System.out.println("Added sublist " + change.getAddedSubList());
//                System.out.println("Removed sublist " + change.getRemoved());
//                System.out.println("From " + change.getFrom());
//                System.out.println("To " + change.getTo());
//                System.out.println("Next " + change.next());
//                System.out.println("Added Size " + change.getAddedSize());
//                System.out.println("Removed Size " + change.getRemovedSize());
//                System.out.println("List " + change.getList());
//                System.out.println("Added " + change.wasAdded() + " Permutated " + change.wasPermutated() + " Removed " + change.wasRemoved() + " Replaced "
//                        + change.wasReplaced() + " Updated " + change.wasUpdated());
//                System.out.println("============================================");
                
                
                
        
//        TextInputControl tic = (TextInputControl)field;
//        sp_tic.addListener((ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
//            LOG.info(String.format("Field @ %d changed to %s", tic.hashCode(),  newVal));
//        });
        
       