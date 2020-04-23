/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;


import com.opus.fxsupport.SystemMenu;
import com.opus.fxsupport.DateWidget;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.FXFFieldDescriptor;
import com.opus.fxsupport.FXFTextField;
import com.opus.fxsupport.NumberValidator;
import com.opus.fxsupport.EmptyValidator;
import com.opus.fxsupport.FXFCheckListViewNumber;
import com.opus.fxsupport.FXFController;
import com.opus.fxsupport.IndexedCheckModel;
import com.opus.fxsupport.WidgetDescriptor;
import com.opus.fxsupport.VoidValidator;
import com.opus.glyphs.FontAwesomeIcon;
import com.opus.glyphs.GlyphsBuilder;
import com.opus.syssupport.PicnoUtils;
import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.VirnaPayload;
import com.opus.syssupport.VirnaServiceProvider;


import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;


import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;



public class FX1Controller extends FXFController implements com.opus.fxsupport.FXFControllerInterface {

    private static final Logger LOG = Logger.getLogger(FX1Controller.class.getName());
    
    private String profilepath;
    private ProfileBlaine profile;
    
    private Controller appctrl = Controller.getInstance();
    private FX1SMachine machine;
    private FX1Model model;
    
    
    
    
    
    
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

   
    @FXML
    private Label lb_profile;
    
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
    private Label sidebar_btrun;
    
    
    @FXML
    private FXFTextField it_porosidade;

    @FXML
    private FXFTextField it_densidade;

    @FXML
    private FXFTextField it_massa_calculada;

    @FXML
    private FXFTextField it_blaineresult;

    @FXML
    private FXFCheckListViewNumber<String> checklist1;

    
    @FXML
    private FXFTextField it_analisetime;

    

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

    
    
    
    public FX1Controller(FXMLLoader fxmlLoader) {
        //wdgmanager = FXFWidgetManager.getInstance();
        this.fxmlLoader = fxmlLoader; 
    }
    
    
    public FX1Controller(FXMLLoader fxmlLoader, String profilepath) {
        //wdgmanager = FXFWidgetManager.getInstance();
        this.fxmlLoader = fxmlLoader; 
        this.profilepath = profilepath;
    }
    
    
    // Application controller link 
    private VirnaServiceProvider ctrl;
    @Override
    public void setAppController (VirnaServiceProvider ctrl){
        this.ctrl = ctrl;
    }
    
    public void activateModel(){
        machine.activateModel(profile.getArgument());
    }
    
    
    
    @FXML
    void initialize() {
        
        
        LOG.info(String.format("FX1Controller initializing with profile : %s", profilepath));
        profile = PicnoUtils.profile_resources.getProfile(profilepath, ProfileBlaine.class);
        profileid = profile.getArgument();
        machine = FX1SMachine.getInstance();
        
        model = new FX1Model();
        model.setProfile(profile);
        model.setMachine(machine);
        model.setAppCtrl(ctrl);
        model.setFXCtrl(this);
            
        machine.setAppController(ctrl);
        machine.addModel(profile.getArgument(), model);
        machine.mapProperties(profile.getArgument(), model.getAn());
        machine.activateModel(profile.getArgument());
        
        addContext(fxmlLoader.getNamespace());
        
        appctrl.processSignal(new SMTraffic(0l, 0l, 0, "NEWANALISE", this.getClass(),
                                   new VirnaPayload()
        ));
        
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
        
        FXFField field = getWidget(fieldname);
        WidgetDescriptor wd = wctx.findByName(fieldname);
        if (field != null){
            field.updateValue(value, wd.required);
            //LOG.info(String.format("Updating %s with %s / required = %s", fieldname, value, required));
        }
        else{
            LOG.warning(String.format("Widgetmanager failed to locate widget : %s", fieldname));
        } 
    }
    
    public void updateUIWidget (String key, FXFField field){
        
        PropertyLinkDescriptor pld1, pld2;
        
        pld1 = model.getAna_proplink_uimap().get(key);
        if (pld1 != null){
            pld1.setFxfield(field);
            pld2 = model.getAna_proplink_modelmap().get(key);
            if (pld2 != null){
                pld2.setFxfield(field);
            }
        }
    }
    
    public void initValidators (FXFField field, FXFFieldDescriptor fxfd){
        
        
        Control fxctrl = (Control)fxfd.getField();
        fxctrl.setTooltip(new Tooltip(fxfd.getTooltip_message()));
        
        // Create Number Validator if needed
        if (fxfd.getValidator_type().equals("number")){
            NumberValidator nv = new NumberValidator();
            validators.put(fxfd.getField(), nv);
            
            nv.setMaybenull(fxfd.isMaybenull());
            ValidationSupport.setRequired(fxctrl, fxfd.isRequired());
            
            if (fxfd.isUse_range()){
                if (fxfd.isUse_windowrange()){
                    nv.setRangeWindows(fxfd.getRanges()[0], fxfd.getRanges()[1], fxfd.getRanges()[2]);
                }
                else{
                    nv.setRanges(fxfd.getRanges());
                }
            } 
            fxfd.setValidator(nv);
            validators.get(fxfd.getField()).initTooltip(fxctrl.getTooltip());
           
            
            vs.registerValidator(fxctrl, new Validator<String>(){
                @Override
                public ValidationResult apply( Control control, String value ){
                    NumberValidator validator = (NumberValidator)validators.get(control);
                    validator.getResult(value);
                    
                    boolean validated = !validator.isFailed() || validator.isWarning(); 
                    if (!value.equals("")){
                        machine.varCallback(control, value, validated, true);
                    }
                    return ValidationResult.fromMessageIf(control, 
                            validator.getMessage(), 
                            validator.isWarning() ? Severity.WARNING : Severity.ERROR, 
                            validator.isFailed() ? true : false);
                };
            });
        } // Number Validator
        
        
        else if (fxfd.getValidator_type().contains("notempty")){
            EmptyValidator ev = new EmptyValidator();
            validators.put(fxfd.getField(), ev);
            validators.get(fxfd.getField()).initTooltip(fxctrl.getTooltip());
            ValidationSupport.setRequired(fxctrl, fxfd.isRequired());
            
            vs.registerValidator(fxctrl, new Validator<String>(){
                @Override
                public ValidationResult apply( Control control, String value ){
                    EmptyValidator validator = (EmptyValidator)validators.get(control);
                    validator.getResult(value);
                    
                    boolean validated = !validator.isFailed() || validator.isWarning(); 
                    machine.varCallback(control, value, validated, true);
   
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
            validators.get(fxfd.getField()).initTooltip(fxctrl.getTooltip());
            ValidationSupport.setRequired(fxctrl, fxfd.isRequired());
            
            vs.registerValidator(fxctrl, new Validator<String>(){
                @Override
                public ValidationResult apply( Control control, String value ){
                    VoidValidator validator = (VoidValidator)validators.get(control);
                    validator.getResult(value);
                    
                    boolean validated = !validator.isFailed() || validator.isWarning(); 
                    machine.varCallback(control, value, validated, true);
   
                    return ValidationResult.fromMessageIf(control, 
                            validator.getMessage(), 
                            validator.isWarning() ? Severity.WARNING : Severity.ERROR, 
                            validator.isFailed() ? true : false);
                };
            });
        } // Void Validator
     
        
        
        
    }
    
    
    private void initField(FXFField field, FXFFieldDescriptor fxfd){
      
        
        fxfd.setField(field);
        Control fxctrl = (Control)fxfd.getField();
        
        fxctrl.setTooltip(new Tooltip(fxfd.getTooltip_message()));

        updateUIWidget(fxfd.getName(), fxfd.getField());
        
        field.setSid(fxfd.getName());
        
        fxctrl.setContextMenu(new ContextMenu());      
        fxctrl.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (e.getButton() == MouseButton.SECONDARY) {
                    //LOG.info(String.format("Widget Context requested @ %f/%f",  e.getScreenX(), e.getScreenY()));
                    ContextMenu ctxm = field.getConfigurationMenu(fxctrl, fxfd);
                    ctxm.show(fxctrl, e.getScreenX(), e.getScreenY());
                    e.consume();
                }
            }
        });  
        
        initValidators (field, fxfd);
        
        FXFController.updateAutocomplete(fxfd, null);
        
        String localcallback = fxfd.getLocal_callback();
        if (!localcallback.equals("")){
            PropertyLinkDescriptor linkdesc;
            linkdesc = new PropertyLinkDescriptor()
                                                .setFxfield(field)
                                                .setInput(true)
                                                .setStopfocus(true)
                                                .setPlink(fxfd.getName())
                                                .setCallstate(localcallback);
            model.getAna_proplink_uimap().put(fxfd.getName(), linkdesc);
            LOG.info(String.format("Added local field %s", fxfd.getName()));
        }
        
        WidgetDescriptor wd = getWidgetDescriptor(fxfd.getName());
        wd.required = fxfd.isRequired();
        PropertyLinkDescriptor pld3 = model.getAna_proplink_uimap().get(fxfd.getName());
        if (wd != null && pld3 != null){
            wd.linkdescriptor = pld3;
        }
              
    }
    
    
    @Override
    public void sendSignal (PropertyLinkDescriptor pld, String sigtype){
        
        appctrl.processSignal(new SMTraffic(0l, 0l, 0, pld.getCallstate(), this.getClass(),
                                   new VirnaPayload().setObject(pld)));
        
    }

    
    
    @Override
    public void update() {
        update(scene);
    }
    
    @Override
    public void update(Scene scene){
        
        FXFField field;
        
        this.scene = scene;
        vs = new ValidationSupport(); 
        validators = new LinkedHashMap<>();
        
        appctrl.setFXANController(this);
//        appctrl.loadLastCalibration();

        lb_profile.setText(profile.getLabel());

        ArrayList<FXFFieldDescriptor> descriptors = profile.getDescriptors();
         
        for (FXFFieldDescriptor fxfd : descriptors){
            field = getWidget(fxfd.getName());
            if (field !=null){
                //LOG.info(String.format("Profile load said field %s was updated", fxfd.getName()));
                initField(field, fxfd);
            }
            else{
                LOG.warning(String.format("Profile load said Failed do locate field %s on controller wdglist...", fxfd.getName()));
            }
        }
        
        
        //initTimeList();
//        checklist1.setItems(time_entries);
//        checklist1.getCheckModel().getCheckedItems().addListener(this::aclistChanged);
        
        
        //checklist1.getCheckModel().getCheckedIndices().
        
//        checklist1.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) change -> {
//            
//            boolean changed = false;
//            LOG.info(String.format("Checklist addListener called ..."));   
//            
//            while (change.next()) {
//                LOG.info(String.format("Checklist changed : %s", change.toString()));   
//                int index = change.getFrom();
//                Double v = getTime_values().get(index);
//                    
//                if (!change.wasAdded()){
//                    LOG.info(String.format("Change removed item %d", index));    
//                    getTime_values().remove(index);
//                    getTime_values().add(index, 0.0);
//                    changed = true;
//                }
//                else{
//                    if (v == 0.0){
//                        LOG.info(String.format("Change added item %d", index));
//                        String s = change.getAddedSubList().get(0);
//                        try{
//                            Double value = Double.valueOf(s);
//                            getTime_values().remove(index);
//                            getTime_values().add(index, value);
//                            changed = true;
//                        }
//                        catch (Exception ex){
//                            LOG.warning(String.format("Failed to convert Checked list item on %d", index));   
//                            return;
//                        }
//                    }
//                }
//                
//                if (changed){
//                    appctrl.processSignal(new SMTraffic(0l, 0l, 0, "CALCULATETIME", this.getClass(),
//                                   new VirnaPayload().setObject(time_values)));
//                }
//            }
//        });
        
        sidebar_btcycle.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.REFRESH, "black", 4));
        sidebar_btstore.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.DATABASE, "black", 4));
        sidebar_btreport.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.FILE_PDF_ALT, "black", 4));
        sidebar_btbroadcast.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.SHARE_ALT, "black", 4));
        sidebar_btloadfile.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.ARCHIVE, "black", 4));
        sidebar_btrun.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.PLAY, "black", 4));

    }
    
    
    
    
    // ============================================ Timelist services ===================================================
    
   
    public ObservableList<String> time_entries = FXCollections.observableArrayList();
    private ArrayList<Double> time_values = new ArrayList<>();
    
    public void aclistChanged(Observable change){
        
        //LOG.info(String.format("aclist changed - touched = %s", touched));
        
        boolean changed = false;
        
        IndexedCheckModel<String> im = checklist1.getCheckModel();
        
        LOG.info(String.format("AclistChanged = %s", checklist1.getCheckModel().getCheckedItems()));   
        
        
//        while (change.next()) {
//            LOG.info(String.format("Checklist changed : %s", change.toString()));   
//            int index = change.getFrom();
//            Double v = getTime_values().get(index);
//
//            if (!change.wasAdded()){
//                LOG.info(String.format("Change removed item %d", index));    
//                getTime_values().remove(index);
//                getTime_values().add(index, 0.0);
//                changed = true;
//            }
//            else{
//                if (v == 0.0){
//                    LOG.info(String.format("Change added item %d", index));
//                    String s = change.getAddedSubList().get(0);
//                    try{
//                        Double value = Double.valueOf(s);
//                        getTime_values().remove(index);
//                        getTime_values().add(index, value);
//                        changed = true;
//                    }
//                    catch (Exception ex){
//                        LOG.warning(String.format("Failed to convert Checked list item on %d", index));   
//                        return;
//                    }
//                }
//            }
//
//            if (changed){
//                appctrl.processSignal(new SMTraffic(0l, 0l, 0, "CALCULATETIME", this.getClass(),
//                               new VirnaPayload().setObject(time_values)));
//            }
//        }
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
//                for (int i = 0; i < getTime_values().size(); i++) {
//                    Double d = getTime_values().get(i);
//                    if (d != 0){
//                        checklist1.getCheckModel().check(i);
//                    }
//                }             
                //it_analisetime.setText("");
                
//                appctrl.processSignal(new SMTraffic(0l, 0l, 0, "CALCULATETIME", this.getClass(),
//                                   new VirnaPayload()
//                                           .setObject(time_values)
//                                           .setFlag1(Boolean.FALSE)
//                                    ));     
            }
        });
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
        checklist1.initTimeList();
        machine.newAnalise();
    }

    @FXML
    void btexport_action(MouseEvent event) {
        LOG.info("BT export clicked ...");
    }

    @FXML
    void btreport_action(MouseEvent event) {
        //pp100ctrl.reportAnalise();
    }

    @FXML
    void btstore_action(MouseEvent event) {
        //pp100ctrl.storeAnalise();
    }
    
    @FXML
    void btloadfile_action(MouseEvent event) {
        //pp100ctrl.loadFile();
    }
    
    @FXML
    void btrun_action(MouseEvent event) {
        Random rand = new Random();
        //addTimeEntry (String.format(Locale.US, "%5.2f", 125.0 + (rand.nextDouble()-0.5)*2));
        checklist1.addEntry(125 + ((rand.nextDouble()-0.5)*2), "Normal");
    }
    
   
    @Override
    public void clearCanvas(){ 
//        pnl_user.setVisible(false);
//        checklist1.setOpacity(0.0);
    }
    
    @FXML
    void canvas_clicked(MouseEvent event) {
        //LOG.info("Canvas clicked ...");
        clearCanvas();
    }
    
    
    @FXML
    void user_action(MouseEvent event) {
        //LOG.info("User clicked ...");
//        if (pnl_user.isVisible()){
//            pnl_user.setVisible(false);
//        }
//        else{
//            pnl_user.setVisible(true);
//        }
    }
    
    
    
    
    
    public Scene getScene() { return scene;}

    private static final String UID = "FX1";
    @Override
    public String getUID() { return UID;}

    
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
        
       

    
    
//    public void loadProfile(){
//        
//        profile = appctrl.getProfile();
//        lb_profile.setText(profile.getLabel());
//        
//    }
//    
//    public Profile getProfile() { return profile;}
//    
//    
//    public void updateUserPanel(){
//        
//        cb_profile.setItems(FXCollections.observableArrayList(appctrl.getProfile_list()));
//        cb_profile.setValue(profile.getLabel());
////        if (PicnoUtils.user.isMay_changeprofile()){
////            cb_profile.setDisable(false);
////        }
////        else{
////            cb_profile.setDisable(true);
////        }
//        lb_avatar.setText("User");
//        String log1 = String.format(PicnoUtils.timestamp_format, appctrl.getLogTime());
//        lb_logged.setText(log1);
//        
//        String savatar = "";
//        if (!savatar.isEmpty()){
//            File file = new File(Config.getInstance().getAux_dir()+savatar);
//            Image image = new Image(file.toURI().toString());
//            avatar.setImage(image);          
//            ImageView iv = new ImageView(image);
//            iv.setFitHeight(50);
//            iv.setFitWidth(50);
//            lb_user.setGraphic(iv);
//        }
//        else{
//            lb_user.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.USER).getText())
//                .sizeFactor(3).color(Color.WHITE)
//            );
//        }
//    }
//    



        
//        sidebar_btcycle.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.REFRESH).getText())
//                .sizeFactor(5).color(Color.LIGHTBLUE)
//                .useGradientEffect()
//        );



//
//        // Create the CheckListView with the data 
////        checklist1.setItems(strings);
//        //initTimeList();
//        
//        Random rand = new Random();
//        for (int i = 0; i <= 10; i++) {
//            time_entries.add(String.format("%5.2f", 125.0 + (rand.nextDouble()-0.5)*2));
//        }
//        
////        Platform.runLater(new Runnable() {
////            @Override
////            public void run() {
//                checklist1.setItems(time_entries);
////            }
////        });
////        