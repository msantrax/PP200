/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;


import com.opus.fxsupport.BlaineDevice;
import com.opus.fxsupport.SystemMenu;
import com.opus.fxsupport.DateWidget;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.FXFFieldDescriptor;
import com.opus.fxsupport.FXFTextField;
import com.opus.fxsupport.NumberValidator;
import com.opus.fxsupport.EmptyValidator;
import com.opus.fxsupport.FXFBlaineDeviceController;
import com.opus.fxsupport.FXFCheckListViewNumber;
import com.opus.fxsupport.FXFController;
import com.opus.fxsupport.FXFCountdownTimer;
import com.opus.fxsupport.LauncherItem;
import com.opus.fxsupport.WidgetDescriptor;
import com.opus.fxsupport.VoidValidator;
import com.opus.glyphs.FontAwesomeIcon;
import com.opus.glyphs.GlyphsBuilder;
import com.opus.syssupport.FormulaResources;
import com.opus.syssupport.PicnoUtils;
import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.VirnaPayload;
import com.opus.syssupport.VirnaServiceProvider;


import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;


import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;



public class FX5Controller extends FXFController implements com.opus.fxsupport.FXFControllerInterface {

    private static final Logger LOG = Logger.getLogger(FX5Controller.class.getName());
    
    private String profilepath;
    private ProfileYara profile;
    
    private Controller appctrl = Controller.getInstance();
    private FX5SMachine machine;
    private FX5Model model;
    
    
    @FXML
    private Label lb_profile;

    @FXML
    private Label sidebar_btcycle;

    @FXML
    private Label sidebar_btstore;

    @FXML
    private Label sidebar_btreport;

    @FXML
    private Label sidebar_btloadfile;

    @FXML
    private FXFTextField it_altura;

    @FXML
    private FXFTextField it_densidade;

    @FXML
    private FXFTextField it_poros;

    
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
    private FXFTextField it_peso;

    @FXML
    private FXFTextField it_temperature;

    @FXML
    private DateWidget date;

    @FXML
    private FXFBlaineDeviceController blainedevice;

    @FXML
    private FXFTextField it_ssa;

    @FXML
    private FXFTextField it_perm;
    
    
    
    public FX5Controller(FXMLLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader; 
    }
    
    
    public FX5Controller(FXMLLoader fxmlLoader, String profilepath) {
        this.fxmlLoader = fxmlLoader; 
        this.profilepath = profilepath;
    }
    
    
    // Application controller link 
    private VirnaServiceProvider ctrl;
    @Override
    public void setAppController (VirnaServiceProvider ctrl){
        this.ctrl = ctrl;
        FXFController.sctrl = ctrl;
        model.setAppCtrl(ctrl);
        machine.setAppController(ctrl);
        blainedevice.setAppController(ctrl);
        
    }
    
    public void activateModel(){
        machine.activateModel(profile.getArgument());
        BlaineDevice.getInstance().setFXController(this);
    }
    
    
    @FXML
    void initialize() {
        
        
        LOG.info(String.format("FX5Controller initializing with profile : %s", profilepath));
        profile = PicnoUtils.profile_resources.getProfile(profilepath, ProfileYara.class);
        profileid = profile.getArgument();
        machine = FX5SMachine.getInstance();
        
        model = new FX5Model();
        model.setProfile(profile);
        model.setMachine(machine);
        model.setAppCtrl(appctrl);
        model.setFXCtrl(this);
        
            
        machine.setAppController(appctrl);
        machine.addModel(profile.getArgument(), model);
        machine.mapProperties(profile.getArgument(), model.getAn());
        machine.activateModel(profile.getArgument());
        
        blainedevice.setAppController(appctrl);
        
        addContext(fxmlLoader.getNamespace());
        
        //machine.newAnalise();
 
    }
    
    @Override
    public void setLauncher(LauncherItem li){
        profile.setLauncher(li);
        launcher = li;
    }
    
    @Override
    public LauncherItem getLauncher(){
        return launcher;
    }
    
    @Override
    public FXFCountdownTimer getCDT() {
        return blainedevice.getCDT();
    }
    
    @Override
    public FXFCheckListViewNumber<String> getRunControl() {
        return blainedevice.getRunControl();
    }
    
    @Override
    public FXFBlaineDeviceController getBlaineDevice(){
        return blainedevice;
    }
    
    
    @Override
    public void resetDevices() {
        blainedevice.initAnalises();
    }
    
    
    // =============================================== MENU SERVICES =======================================================
    
    private void drawSystemMenuSeparator(SystemMenu menupane, Double ypos){
        Line sep = new Line();
        sep.setEndX(100.0);
        sep.setStartX(-100.0);
        menupane.setTopAnchor(sep, ypos);
        menupane.setLeftAnchor(sep, 0.0);
        menupane.getChildren().add(sep);
        
    }
    

    @Override
    public SystemMenu getMenu(boolean isadm){
        
        SystemMenu menupane = new SystemMenu();
        
        drawSystemMenuSeparator (menupane, 0.0);
        
        
        Label bt_clone = new Label("Clonar esse perfil ...");
//        bt_logout.setGraphicTextGap(15.0);
//        bt_logout.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.POWER_OFF, "black", 2));
        bt_clone.getStyleClass().add("fxf-shutdownbutton");
        
        bt_clone.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override 
            public void handle(MouseEvent event) {
                    appctrl.processSignal(new SMTraffic(0l, 0l, 0, "CLONEYARAPROFILE", this.getClass(),
                                   new VirnaPayload().setFlag1(true)
                    ));
            } 
        });
        menupane.setTopAnchor(bt_clone, 15.0);
        menupane.setLeftAnchor(bt_clone, 25.0);
        menupane.getChildren().add(bt_clone);
        
        
        
        
        Label bt_delete= new Label("Apagar esse perfil");
//        bt_logout.setGraphicTextGap(15.0);
//        bt_logout.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.POWER_OFF, "black", 2));
        bt_delete.getStyleClass().add("fxf-shutdownbutton");
        
        bt_delete.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override 
            public void handle(MouseEvent event) {
                appctrl.processSignal(new SMTraffic(0l, 0l, 0, "DELETEYARAPROFILE", this.getClass(),
                                   new VirnaPayload().setFlag1(true)
                    ));
            } 
        });
        
        menupane.setTopAnchor(bt_delete, 45.0);
        menupane.setLeftAnchor(bt_delete, 25.0);
        menupane.getChildren().add(bt_delete);

        return menupane;
    }
    
    
    
    
    // =========================================================
    
    public void updateAnaliseTime (long timestamp) {
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                date.updateValue(String.valueOf(timestamp), true);
            }
        });
    }
    
    
    @Override
    public void addContext (ObservableMap<String, Object> namespace){
        
        super.addContext(namespace);
        
        namespace.forEach((name, obj) -> {
            if (obj != null){
                if (obj instanceof FXFBlaineDeviceController){   
                    WidgetDescriptor wd;
                    int focus = 0;
                    focus = getNextOutFocusCounter();
                    ((Node) obj).setFocusTraversable(false);
                    FXFBlaineDeviceController field = (FXFBlaineDeviceController)obj;
                    wd = new WidgetDescriptor(focus, field);
                    wd.enter_focusable = false;
                    wd.name = name;
                    String fname = name;
                    field.setManagement(this, focus, wctx);
                    wctx.getWidgetList().put(focus, wd);
                    
                    LOG.info(String.format("FX5 registering Key: %s of type=%s", name, obj.toString()));
                }
           }
        });
    }
    
    
    
    public void updateField (String fieldname, String value, boolean required){
        
        FXFField field = getWidget(fieldname, FXFField.class);
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
        
        pld1 = model.getProplink_uimap().get(key);
        if (pld1 != null){
            pld1.setFxfield(field);
            pld2 = model.getProplink_modelmap().get(key);
            if (pld2 != null){
                pld2.setFxfield(field);
            }
        }
    }
    
    public void initValidators (FXFField field, FXFFieldDescriptor fxfd){
        
        
        Control fxctrl = (Control)fxfd.getField(FXFField.class);
        fxctrl.setTooltip(new Tooltip(fxfd.getTooltip_message()));
        
        // Create Number Validator if needed
        if (fxfd.getValidator_type().equals("number")){
            NumberValidator nv = new NumberValidator();
            validators.put(fxfd.getField(FXFField.class), nv);
            
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
            validators.get(fxfd.getField(FXFField.class)).initTooltip(fxctrl.getTooltip());
           
            
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
            validators.put(fxfd.getField(FXFField.class), ev);
            validators.get(fxfd.getField(FXFField.class)).initTooltip(fxctrl.getTooltip());
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
            validators.put(fxfd.getField(FXFField.class), vv);
            validators.get(fxfd.getField(FXFField.class)).initTooltip(fxctrl.getTooltip());
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
        Control fxctrl = fxfd.getField(Control.class);
        
        fxctrl.setTooltip(new Tooltip(fxfd.getTooltip_message()));

        updateUIWidget(fxfd.getName(), fxfd.getField(FXFField.class));
        
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
        
        
        initValidators(field, fxfd);

        FXFController.updateAutocomplete(fxfd, null);
        
        FXFController.updateFormulas(fxfd, null);
        
        
        String localcallback = fxfd.getLocal_callback();
        if (!localcallback.equals("")){
            PropertyLinkDescriptor linkdesc;
            linkdesc = new PropertyLinkDescriptor()
                                                .setFxfield(field)
                                                .setInput(true)
                                                .setStopfocus(true)
                                                .setPlink(fxfd.getName())
                                                .setCallstate(localcallback);
            model.getProplink_uimap().put(fxfd.getName(), linkdesc);
            LOG.info(String.format("Added local field %s", fxfd.getName()));
        }

        WidgetDescriptor wd = getWidgetDescriptor(fxfd.getName());
        wd.required = fxfd.isRequired();
        PropertyLinkDescriptor pld3 = model.getProplink_uimap().get(fxfd.getName());
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
        update(getScene());
    }
    
    @Override
    public void update(Scene scene){
      
        this.setScene(scene);
        vs = new ValidationSupport(); 
        validators = new LinkedHashMap<>();
        
        appctrl.setFXYARAController(this);
//        appctrl.loadLastCalibration();

        lb_profile.setText(profile.getLabel());

        ArrayList<FXFFieldDescriptor> descriptors = profile.getDescriptors();
         
        for (FXFFieldDescriptor fxfd : descriptors){
            if (fxfd.getName().equals("blainedevice")){
                FXFBlaineDeviceController field = getWidget(fxfd.getName(), FXFBlaineDeviceController.class);
                if (field !=null){
                    field.initProfile(fxfd, profile.getDescriptor("it_analiseaverage"), profile.getDescriptor("it_analisersd"));
                }
                else{
                    LOG.warning(String.format("Profile load said it failed to find a BlaineDescriptor on controller wdglist..."));
                } 
            }
            else{
                FXFField field = getWidget(fxfd.getName(), FXFField.class);
                if (field !=null){
                    //LOG.info(String.format("Profile load said field %s was updated", fxfd.getName()));
                    initField(field, fxfd);
                }
                else{
                    LOG.warning(String.format("Profile load said it failed do locate field %s on controller wdglist...", 
                            fxfd.getName()));
                } 
            }
        }
 
        FormulaResources fr = FormulaResources.getInstance();
        fr.setProfile(profile);
        fr.updateFormulas();
        
        
        sidebar_btcycle.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.REFRESH, "black", 4));
        sidebar_btstore.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.DATABASE, "black", 4));
        sidebar_btreport.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.FILE_PDF_ALT, "black", 4));
        //sidebar_btbroadcast.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.SHARE_ALT, "black", 4));
        sidebar_btloadfile.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.ARCHIVE, "black", 4));
             
    }
    
    public void setUIState(String verb){
        
        
        
        switch (verb){
            case "FRESH_ANALISYS":
                sidebar_btcycle.setDisable(false);
                sidebar_btstore.setDisable(true);
                sidebar_btreport.setDisable(true);
                //sidebar_btbroadcast.setDisable(true);
                sidebar_btloadfile.setDisable(false);
                blainedevice.enableRun(true);
                break;
            case "LOADED":
                sidebar_btcycle.setDisable(false);
                sidebar_btstore.setDisable(true);
                sidebar_btreport.setDisable(false);
                //sidebar_btbroadcast.setDisable(false);
                sidebar_btloadfile.setDisable(true);
                blainedevice.enableRun(false);
                break;    
            case "RUNNING":
                sidebar_btcycle.setDisable(true);
                sidebar_btstore.setDisable(true);
                sidebar_btreport.setDisable(true);
                //sidebar_btbroadcast.setDisable(true);
                sidebar_btloadfile.setDisable(true);
                blainedevice.enableRun(true);
                break;
            case "DONE_VALID":
                sidebar_btcycle.setDisable(false);
                sidebar_btstore.setDisable(false);
                sidebar_btreport.setDisable(false);
                //sidebar_btbroadcast.setDisable(false);
                sidebar_btloadfile.setDisable(true);
                blainedevice.enableRun(false);
                break;
            case "DONE_INVALID":
                sidebar_btcycle.setDisable(false);
                sidebar_btstore.setDisable(true);
                sidebar_btreport.setDisable(true);
                //sidebar_btbroadcast.setDisable(true);
                sidebar_btloadfile.setDisable(true);
                blainedevice.enableRun(true);
                break;    
            default:
                sidebar_btcycle.setDisable(false);
                sidebar_btstore.setDisable(true);
                sidebar_btreport.setDisable(true);
                //sidebar_btbroadcast.setDisable(true);
                sidebar_btloadfile.setDisable(false);
                blainedevice.enableRun(true);
                break;
        }
        
    }
    
    
    
    // ========================================= DISPATCH SECTION =======================================================
    
    @FXML
    void btcycle_action(MouseEvent event) {
        machine.newAnalise();
    }

//    @FXML
//    void btexport_action(MouseEvent event) {
//        LOG.info("BT export clicked ...");
//    }

    @FXML
    void btreport_action(MouseEvent event) {
        machine.reportAnalise();
    }

    @FXML
    void btstore_action(MouseEvent event) {
        machine.storeAnaliseAction();
    }
    
    @FXML
    void btloadfile_action(MouseEvent event) {
        machine.loadFileAction();
    }
    
    @FXML
    void btrun_action(MouseEvent event) {
        //Random rand = new Random();
        //addTimeEntry (String.format(Locale.US, "%5.2f", 125.0 + (rand.nextDouble()-0.5)*2));
        //checklist1.addEntry(160 + ((rand.nextDouble()-0.5)*2), "Normal");
        appctrl.processSignal(new SMTraffic(0l, 0l, 0, "INITYARARUNS", this.getClass(),
                                   new VirnaPayload()));
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

    }
  
    public Scene getScene() { return getScene();}

    private static final String UID = "FX5";
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