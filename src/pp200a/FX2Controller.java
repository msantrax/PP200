/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.fxsupport.BlaineDevice;
import com.opus.fxsupport.DateWidget;
import com.opus.fxsupport.EmptyValidator;
import com.opus.fxsupport.FXFBlaineDeviceController;
import com.opus.fxsupport.FXFCheckListViewNumber;
import com.opus.fxsupport.FXFController;
import com.opus.fxsupport.FXFCountdownTimer;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.FXFFieldDescriptor;
import com.opus.fxsupport.FXFTextField;
import com.opus.fxsupport.FXFValidator;
import com.opus.fxsupport.FXFWidgetManager;
import com.opus.fxsupport.LauncherItem;
import com.opus.fxsupport.NumberValidator;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.fxsupport.SystemMenu;
import com.opus.fxsupport.VoidValidator;
import com.opus.fxsupport.WidgetDescriptor;
import com.opus.glyphs.FontAwesomeIcon;
import com.opus.glyphs.GlyphsBuilder;
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
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
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
    
   
    private String profilepath;
    private ProfileCal profile;
    
    private Controller appctrl = Controller.getInstance();
    private FX2SMachine machine;
    private FX2Model model;
    
    
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
    private FXFTextField it_porosidade;

    @FXML
    private FXFTextField it_densidade;

    @FXML
    private FXFTextField it_volume;

    @FXML
    private FXFTextField it_kfactor;


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
    private FXFTextField it_filtro;

    @FXML
    private FXFTextField it_mass;

    
    @FXML
    private FXFTextField it_area;

    @FXML
    private FXFTextField it_temperature;

    @FXML
    private DateWidget date;
   
    @FXML
    private FXFBlaineDeviceController blainedevice;


    
    public FX2Controller(FXMLLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader; 
    }
    
    
    public FX2Controller(FXMLLoader fxmlLoader, String profilepath) {
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
        
        LOG.info(String.format("FX1Controller initializing with profile : %s", profilepath));
        profile = PicnoUtils.profile_resources.getProfile(profilepath, ProfileCal.class);
        profileid = profile.getArgument();
        machine = FX2SMachine.getInstance();
        
        model = new FX2Model();
        model.setProfile(profile);
        model.setMachine(machine);
        model.setAppCtrl(appctrl);
        model.setFXCtrl(this);
        
            
        machine.setAppController(appctrl);
        machine.addModel(profile.getArgument(), model);
        machine.mapProperties(profile.getArgument(), model.getCal());
        machine.activateModel(profile.getArgument());
        
        blainedevice.setAppController(appctrl);
        
        addContext(fxmlLoader.getNamespace());
        
        //machine.newCalibration();
 
      
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
                    appctrl.processSignal(new SMTraffic(0l, 0l, 0, "CLONEPROFILE", this.getClass(),
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
                appctrl.processSignal(new SMTraffic(0l, 0l, 0, "DELETEPROFILE", this.getClass(),
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
                    
                    LOG.info(String.format("FX1 registering Key: %s of type=%s", name, obj.toString()));
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
        
        appctrl.setFXCALController(this);
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
 
        sidebar_btcycle.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.REFRESH, "black", 4));
        sidebar_btstore.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.DATABASE, "black", 4));
        sidebar_btreport.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.FILE_PDF_ALT, "black", 4));
       
        sidebar_btloadfile.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.ARCHIVE, "black", 4));
             
    }
    
    public void updateAnaliseTime (long timestamp) {
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                date.updateValue(String.valueOf(timestamp), true);
            }
        });
    }
    
    
    
    public void setUIState(String verb){
        
        
        
        switch (verb){
            case "FRESH_ANALISYS":
                sidebar_btcycle.setDisable(false);
                sidebar_btstore.setDisable(true);
                sidebar_btreport.setDisable(true);
                
                blainedevice.enableRun(true);
                break;
            case "LOADED":
                sidebar_btcycle.setDisable(false);
                sidebar_btstore.setDisable(true);
                sidebar_btreport.setDisable(false);
                
                blainedevice.enableRun(false);
                break;    
            case "RUNNING":
                sidebar_btcycle.setDisable(true);
                sidebar_btstore.setDisable(true);
                sidebar_btreport.setDisable(true);
                
                blainedevice.enableRun(true);
                break;
            case "DONE_VALID":
                sidebar_btcycle.setDisable(false);
                sidebar_btstore.setDisable(false);
                sidebar_btreport.setDisable(false);
                
                blainedevice.enableRun(false);
                break;
            case "DONE_INVALID":
                sidebar_btcycle.setDisable(false);
                sidebar_btstore.setDisable(true);
                sidebar_btreport.setDisable(true);
                
                blainedevice.enableRun(true);
                break;    
            default:
                sidebar_btcycle.setDisable(false);
                sidebar_btstore.setDisable(true);
                sidebar_btreport.setDisable(true);
                
                blainedevice.enableRun(true);
                break;
        }
        
    }
    
    
    
    
    
    // ========================================= DISPATCH SECTION =======================================================
    
    
    @FXML
    void canvas_clicked(MouseEvent event) {
        //LOG.info("Canvas clicked ...");
        //clearCanvas();
    }
    
    
    @FXML
    void btcycle_action(MouseEvent event) {
        machine.newCalibration();
    }

    
    @FXML
    void btreport_action(MouseEvent event) {
        machine.reportCalibration();
    }

    @FXML
    void btstore_action(MouseEvent event) {
        machine.storeCalibrationAction();
    }
    
    @FXML
    void btloadfile_action(MouseEvent event) {
        machine.loadFileAction();
    }
    
    @Override
    public void clearCanvas(){
        
    }
    
    
//    public void yieldFocus(FXFField field, boolean fwd){
//        wdgmanager.yieldFocus(this, field, fwd);
//    }
    
    
   public Scene getScene() { return getScene();}

    private static final String UID = "FX2";
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