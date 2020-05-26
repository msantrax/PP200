/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;


import com.opus.syssupport.Profile;
import com.opus.fxsupport.SystemMenu;
import com.opus.fxsupport.EmptyValidator;
import com.opus.fxsupport.FXFController;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.FXFFieldDescriptor;
import com.opus.fxsupport.FXFTextField;
import com.opus.fxsupport.FXFValidator;
import com.opus.fxsupport.FXFWidgetManager;
import com.opus.fxsupport.NumberValidator;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.fxsupport.VoidValidator;
import com.opus.fxsupport.WidgetDescriptor;
import com.opus.glyphs.FontAwesomeIcon;
import com.opus.glyphs.GlyphsBuilder;
import com.opus.syssupport.Config;
import com.opus.syssupport.PicnoUtils;
import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.VirnaPayload;
import com.opus.syssupport.VirnaServiceProvider;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;


import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;




public class FX3Controller extends FXFController implements com.opus.fxsupport.FXFControllerInterface {

    private static final Logger LOG = Logger.getLogger(FX3Controller.class.getName());
    
   
    private FXMLLoader fxmlLoader;
    private FXFWidgetManager wdgmanager;
    
    private Scene scene;
    private ValidationSupport vs;
    private LinkedHashMap<FXFField, FXFValidator>validators;
   
    private String profilepath;
    private Profile profile;
    private Controller pp100ctrl = Controller.getInstance();
    private DBService dbsv = DBService.getInstance();
    
    private ObservableList<String> time_entries = FXCollections.observableArrayList();
    private ArrayList<Double> time_values = new ArrayList<>();
    private LinkedHashMap<String, String> auxfields = new LinkedHashMap<>();
    
    private LinkedHashMap<String, TableColumn<Map, ?>> columns = new LinkedHashMap<>();
    private LinkedHashMap<String, LinkedHashMap<Double,String>> columntype_maps = new LinkedHashMap<>();
    private ObservableList<Map<String, Object>> items;
    
    
    @FXML private ImageView logoacp;
    @FXML private Label sidebar_newsearch;
    @FXML private Label sidebar_visualize;
    @FXML private Label sidebar_remove;
    @FXML private Label sidebar_csv;
    @FXML private FXFTextField it_sid;
    @FXML private FXFTextField it_lote;
    @FXML private ToggleSwitch sw_usedate;
    @FXML private ToggleSwitch sw_useid;
    @FXML private ToggleSwitch sw_lote;   
    @FXML private ChoiceBox<String> cb_period;
    @FXML private DatePicker dp_datefrom;
    @FXML private Spinner<String> sp_timefrom;
    @FXML private DatePicker dp_dateto;
    @FXML private Spinner<String> sp_timeto;    
    @FXML private TableView<Map> tb_table;    
    @FXML private ChoiceBox<String> cb_sid;
    @FXML private ChoiceBox<String> cb_auxiliar;    
  
    @FXML
    void canvas_clicked(MouseEvent event) {
        
    }

    @FXML
    void sw_usedate_action(MouseEvent event) {
        
        Boolean is_selected = sw_usedate.isSelected();
        
//        String selected = is_selected ? "Selected" : "Cleared";
//        LOG.info("UseDate = " + selected);
        
        dp_datefrom.setDisable(!is_selected);
        dp_dateto.setDisable(!is_selected);
        sp_timefrom.setDisable(!is_selected);
        sp_timeto.setDisable(!is_selected);
        cb_period.setDisable(!is_selected);
        
    }
    
    @FXML
    void sw_useid_action(MouseEvent event) {
        
        Boolean is_selected = sw_useid.isSelected();
        
        cb_sid.setDisable(!is_selected);
        it_sid.setDisable(!is_selected);
        
    }    
    
    @FXML
    void sw_lote_action(MouseEvent event) {
        
        Boolean is_selected = sw_lote.isSelected();
        it_lote.setDisable(!is_selected);
        
    } 

    
    public FX3Controller() {
    }
    
    
    public FX3Controller(FXMLLoader fxmlLoader) {
        wdgmanager = FXFWidgetManager.getInstance();
        this.fxmlLoader = fxmlLoader; 
    }
    
    
    public FX3Controller(FXMLLoader fxmlLoader, String profilepath) {
        
        wdgmanager = FXFWidgetManager.getInstance();
        this.fxmlLoader = fxmlLoader; 
        this.profilepath = profilepath;
    }
    
    private static final String UID = "FX3";
    @Override
    public String getUID() { return UID;}
    
    
    public FXMLLoader getFxmlLoader() {
        return fxmlLoader;
    }

    public void setFxmlLoader(FXMLLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
    }
    
    public FX3Controller getFXController() {
        return this;
    }
    
    // Application controller link 
    private VirnaServiceProvider ctrl;
    public void setAppController (VirnaServiceProvider ctrl){
        this.ctrl = ctrl;
    }
    
    public void activateModel(){
        //machine.activateModel(profile.getArgument());
    }
    
    
    
    @FXML
    void initialize() {

        LOG.info("FX3Controller initializing ...");
        
        //wdgmanager = FXFWidgetManager.getInstance();
        wdgmanager.addContext(this, fxmlLoader.getNamespace());
        
        wdgmanager.initFocus(this);
        dbsv.setView(this);

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
    
    
    
    private void loadProfile(){profile = Profile.getInstance();}
    
    public Profile getProfile() { return profile;}
   
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
        Control ctrl = (Control)fxfd.getField(Control.class);
        
        //LinkedHashMap<String,PropertyLinkDescriptor>proplink_uimap = pp100ctrl.getCalUIMap();
        
        
        ctrl.setTooltip(new Tooltip(fxfd.getTooltip_message()));

        pp100ctrl.updateUIWidget(fxfd.getName(), fxfd.getField(FXFField.class), false);
        field.setSid(fxfd.getName());

        // Create Number Validator if needed
        if (fxfd.getValidator_type().equals("number")){
            NumberValidator nv = new NumberValidator();
            validators.put(fxfd.getField(FXFField.class), nv);
            
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
            
            validators.get(fxfd.getField(FXFField.class)).initTooltip(ctrl.getTooltip());
           
            
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
            validators.put(fxfd.getField(FXFField.class), ev);
            validators.get(fxfd.getField(FXFField.class)).initTooltip(ctrl.getTooltip());
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
            validators.put(fxfd.getField(FXFField.class), vv);
            validators.get(fxfd.getField(FXFField.class)).initTooltip(ctrl.getTooltip());
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
            
            TextFields.bindAutoCompletion((TextField)fxfd.getField(FXFField.class), acl);
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
            //proplink_uimap.put(fxfd.getName(), linkdesc);
  
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
    
    
    public String buildQuery() {
        
        StringBuilder sb = new StringBuilder();
        
        String auxfield = auxfields.get(cb_auxiliar.getValue());
        auxfield = "|"+auxfield + "|";
        
        sb.append(String.format("SELECT ID, BLAINE, SID, LOTE %s FROM ANALISE ", auxfield));
        
        if (sw_usedate.isSelected()){
            if (milis_startime < milis_endtime){
                sb.append(String.format("WHERE (ID>%d AND ID<%d) ", milis_startime, milis_endtime));
            }
            else if (milis_startime > milis_endtime){
                sb.append(String.format("WHERE (ID>%d AND ID<%d) ", milis_endtime, milis_startime)); 
            }
            else{
                sb.append(String.format("WHERE ID=%d ", milis_startime)); 
            }
        }
        
        if (sw_useid.isSelected()){
            
            String sid_value = it_sid.getValue();
            if (sid_value != null && !sid_value.isEmpty()){
                sb.append(sw_usedate.isSelected() ? "AND (SID " : "WHERE (SID ");

                String ftyp = cb_sid.getValue();
                if (ftyp.equals("Parece com")){
                    sb.append(String.format("LIKE '%%%s%%') ",it_sid.getValue()));
                }
                else if (ftyp.equals("Não contém")){
                    sb.append(String.format("NOT LIKE '%s') ",it_sid.getValue()));
                }        
                else if (ftyp.equals("Contém um dos itens")){
                    String itens = it_sid.getValue();
                    itens = itens.replace(" ", "','");
                    itens = "'" + itens + "'";
                    sb.append(String.format("IN (%s) ", itens));
                }
                else{
                    sb.append(String.format("='%s') ",it_sid.getValue()));
                }
            }    
        }
        
        if (sw_lote.isSelected()){
            sb.append((sw_usedate.isSelected() || sw_useid.isSelected()) ? "AND (LOTE " : "WHERE (LOTE ");
            sb.append(String.format("='%s') ",it_lote.getValue()));
        }   
        //LOG.info(sb.toString());
        return sb.toString();
    }
    
    
    
    @Override
    public void update(Scene scene){
        
        FXFField field;
        
        this.scene = scene;
        vs = new ValidationSupport(); 
        validators = new LinkedHashMap<>();
        
        pp100ctrl.setFXDBController(this);
        
        it_lote.setDisable(true);
        
        
        // ====================================== TIME FILTER ===========================================================
        
        dp_datefrom.setValue(LocalDate.now());
        timezone = Config.getInstance().getTimezone();
        dayfrom = dp_datefrom.getValue().atStartOfDay(ZoneId.of(timezone));
        milis_dayfrom = 0L;
        
        ObservableList<String> periodlist = FXCollections.<String>observableArrayList(
        "Até", "1 hora", "8 horas", "1 dia", "3 dias", "1 semana", "2 semanas", "1 mes", "3 meses", "1 ano",
               "-1 hora", "-8 horas", "-1 dia", "-3 dias", "-1 semana", "-2 semanas", "-1 mes", "-3 meses", "-1 ano");
              
        cb_period.getItems().addAll(periodlist);
        cb_period.setValue("Até");
   
       
        ObservableList<String> spinnerlist = FXCollections.<String>observableArrayList(
            "00:00", "00:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30",
            "04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00", "07:30",
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30",
            "20:00", "20:30", "21:00", "21:30", "22:00", "22:30", "23:00", "23:30"
        );
        
        SpinnerValueFactory<String> fromvalueFactory =  new SpinnerValueFactory.ListSpinnerValueFactory(spinnerlist);
        SpinnerValueFactory<String> tovalueFactory =  new SpinnerValueFactory.ListSpinnerValueFactory(spinnerlist);
        
        sp_timefrom.setValueFactory(fromvalueFactory);
        sp_timeto.setValueFactory(tovalueFactory);
        
        timesteps = new LinkedHashMap<>();
        minutesteps = new LinkedHashMap<>();
        
        Long spinnervalue = -1800000L;
        Long minutestep = -30L;
        
        for (String s1 : spinnerlist){
            timesteps.put(s1, spinnervalue += 1800000L);
            minutesteps.put(minutestep += 30, s1);
        }
        milis_dayfrom = 0l;
        
        updateTimefilter();
        
        sp_timefrom.valueProperty().addListener(new ChangeListener<String>() { 
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                milis_dayfrom = timesteps.get(newValue);
                updateTimefilter();
            }
        });
        

        dp_datefrom.valueProperty().addListener(new ChangeListener<LocalDate>() {      
            @Override
            public void changed(ObservableValue<? extends LocalDate> ov, LocalDate t, LocalDate t1) {        
                dayfrom = t1.atStartOfDay(ZoneId.of(timezone));
                //dayfrom = sphr.toInstant();
                //Long lmilis_from = dayfrom.toEpochMilli();
                //LOG.info(String.format("DatePicker changed to : %s - milis=%d - date=%tc", dayfrom.toString(), lmilis_from, lmilis_from));
                updateTimefilter();
            }
        });
        
        cb_period.valueProperty().addListener(new ChangeListener<String>() { 
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //milis_dayfrom = timesteps.get(newValue);
                updateTimefilter();
            }
        });
        
       
        ObservableList<String> sidfilterlist = FXCollections.<String>observableArrayList(
        "Exatamente igual", "Parece com", "Contém um dos itens", "Não contém" );
        cb_sid.getItems().addAll(sidfilterlist);
        cb_sid.setValue("Parece com");
        
        
        String [] auxlist_keys = {
            "Nenhum" , "Usuário", "Perfil"  , "Notas" , "Média dos tempos", "Densidade" , "Massa" , "Calibração" , "Temperatura" };
        String [] auxlist_fields = {
            "",", OWNER" ,", PROFILE",", NOTAS", ", MEDIA"         ,", DENSIDADE",", MASSA",", CALIBFILE" ,", TEMPERATURA"};
        
        for (int i = 0; i < auxlist_keys.length; i++) {
            auxfields.put(auxlist_keys[i], auxlist_fields[i]);
        }
        ObservableList<String> auxiliarlist = FXCollections.<String>observableArrayList(auxfields.keySet());
        cb_auxiliar.getItems().addAll(auxiliarlist);
        cb_auxiliar.setValue("Nenhum");
        
    
        
        // Table adjust =====================================================================================================

        columns.put("id_key", generateIntegerColumn("ID", "id_key", true, 0.0, null));
        columns.put("selected_key", generateCheckColumn("Manter", "selected_key"));
        columns.put("timestamp_key", generateDateColumn("Data", "timestamp_key"));
        columns.put("blaine_key", generateDoubleColumn("Blaine", "blaine_key"));     
        columns.put("sid_key", generateTextColumn("Identificação", "sid_key", true, 350.0));
        columns.put("lote_key", generateTextColumn("Lote", "lote_key", true, 200.0));
        columns.put("aux_key", generateTextColumn("Campo Auxiliar", "aux_key", true, 200.0));
        
        tb_table.getColumns().addAll(columns.values());
        
        // Turn on multiple-selection mode for the TableView
        TableView.TableViewSelectionModel<Map> tsm = tb_table.getSelectionModel();
        tsm.setSelectionMode(SelectionMode.MULTIPLE);
        ObservableList<Integer> list = tsm.getSelectedIndices();
        
        enableVisualize(false);
        enableCSV(false);
        enableRemove(false);
 
        // Add a ListChangeListener
        list.addListener((ListChangeListener.Change<? extends Integer> change) -> {
            LOG.info("Row has changed...");
            
            clist = change.getList();
            boolean singlerow = clist.size() == 1;
            enableCSV(!singlerow);
            enableVisualize(singlerow);
            enableRemove(singlerow);
    
            tb_table.requestFocus();
            
        });
  
//        loadProfile();
//        ArrayList<FXFFieldDescriptor> descriptors = profile.getCaldescriptors();
        
        
//        for (FXFFieldDescriptor fxfd : descriptors){
//            String fieldname = fxfd.getName();
//            field = wdgmanager.getWidget(this, fieldname);
//            if (field !=null){
//                initField( field, fxfd);
//                //LOG.warning(String.format("Profile load said field %s was updated", fxfd.getName()));
//            }
//            else{
//                LOG.warning(String.format("Profile load said Failed do locate field %s on wdgmanager...", fxfd.getName()));
//            }
//        }
  
    
        sidebar_newsearch.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.SEARCH, "black", 6));
        sidebar_visualize.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.INFO, "black", 6));
        sidebar_remove.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.REMOVE, "black", 6));
        sidebar_csv.setGraphic(GlyphsBuilder.getAwesomeGlyph(FontAwesomeIcon.SHARE_ALT, "black", 6));

//        sidebar_newsearch.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.SEARCH).getText())
//                .sizeFactor(5).color(Color.ORANGE)
//                .useGradientEffect()
//        );
//        
//        sidebar_visualize.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.INFO).getText())
//                .sizeFactor(5).color(Color.ORANGE)
//                .useGradientEffect()
//        );
//        
//        sidebar_remove.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.REMOVE).getText())
//                .sizeFactor(5).color(Color.ORANGE)
//                .useGradientEffect()
//        );
// 
//        sidebar_csv.setGraphic(Glyph.create( "FontAwesome|" + new Glyph("FontAwesome", FontAwesome.Glyph.SHARE_ALT).getText())
//                .sizeFactor(5).color(Color.ORANGE)
//                .useGradientEffect()
//        );
        
    }
    
    
    @FXML
    void newsearch_action(MouseEvent event) {
        //updateTable(createDummyRecords());
        dbsv.loadDBRecords(buildQuery());
    }

    @FXML
    void remove_action(MouseEvent event) {

    }

    @FXML
    void csv_action(MouseEvent event) {

    }
    
    
    @FXML
    void visualize_action(MouseEvent event) {
        if (sidebar_visualize.getOpacity() == 1.0){
            visualizeRecords();
        }
    }
    
    
    
    private void enableVisualize(boolean enable){
        if (enable){
            sidebar_visualize.setOpacity(1.0);
            //sidebar_visualize.setDisable(false);
        }
        else{
            sidebar_visualize.setOpacity(0.7);
            //sidebar_visualize.setDisable(true);
        }
    }
    
    private void enableCSV(boolean enable){
        if (enable){
            sidebar_csv.setOpacity(1.0);
            //sidebar_csv.setDisable(false);
        }
        else{
            sidebar_csv.setOpacity(0.7);
            //sidebar_csv.setDisable(true);
        }
    }
    
    private void enableRemove(boolean enable){
        if (enable){
            sidebar_remove.setOpacity(1.0);
            //sidebar_remove.setDisable(false);
        }
        else{
            sidebar_remove.setOpacity(0.7);
            //sidebar_remove.setDisable(true);
        }
    }
    
    
    
    //==================================== TIME SERVICES =======================================================================
    
    private String timezone;
    private LinkedHashMap<String, Long> timesteps; 
    private LinkedHashMap<Long, String> minutesteps; 
    
    private ZonedDateTime dayfrom; // from datepicker (rouded to start of day)
    private ZonedDateTime timefrom; // datepicker + time on that day (from spinner or milis_dayfrom)
    private Long milis_dayfrom; // miliseconds on dayfrom (as per spinner)
    private Long milis_startime; // timestamp of the filter start;
    
    private ZonedDateTime timeto;
    private Long milis_endtime; // timestamp of the filter end;
    
    
    private void updateTimefilter(){
        
        timefrom = dayfrom.plus(milis_dayfrom, ChronoUnit.MILLIS);
        
        milis_startime = timefrom.toEpochSecond() * 1000;
   
        
        String period = cb_period.getValue();
        switch (period) {
            
            case "Até":
                timeto = timefrom;
                break;
                
            case "1 hora":
                timeto = timefrom.plusHours(1L);
                break;
            case "8 horas":
                timeto = timefrom.plusHours(8L);
                break;
            case "-1 hora":
                timeto = timefrom.minusHours(1L);
                break;
            case "-8 horas":
                timeto = timefrom.minusHours(8L);
                break;
                
                
            case "1 dia":
                timeto = timefrom.plusDays(1L);
                break;
            case "3 dias":
                timeto = timefrom.plusDays(3L);
                break;
            case "-1 dia":
                timeto = timefrom.minusDays(1L);
                break;
            case "-3 dias":
                timeto = timefrom.minusDays(3L);
                break;
                
            case "1 semana":
                timeto = timefrom.plusWeeks(1L);
                break;
            case "2 semanas":
                timeto = timefrom.plusWeeks(2L);
                break;
            case "-1 semana":
                timeto = timefrom.minusWeeks(1L);
                break;
            case "-2 semanas":
                timeto = timefrom.minusWeeks(2L);
                break;
                
            case "1 mes":
                timeto = timefrom.plusMonths(1L);
                break;
            case "3 meses":
                timeto = timefrom.plusMonths(3L);
                break;  
            case "-1 mes":
                timeto = timefrom.minusMonths(1L);
                break;
            case "-3 meses":
                timeto = timefrom.minusMonths(3L);
                break;  
                
            case "1 ano":
                timeto = timefrom.plusYears(1L);
                break;    
            case "-1 ano":
                timeto = timefrom.minusYears(1L);
                break;    
                
            default:
                break;
            
        }
        
        dp_dateto.setValue(timeto.toLocalDate());
        
        milis_endtime = timeto.toEpochSecond() * 1000;
        ZonedDateTime zdt_trunc = timeto.truncatedTo(ChronoUnit.DAYS);
        Long until = zdt_trunc.until(timeto, ChronoUnit.MINUTES);
        String minuteto = minutesteps.get(until);
        if (minuteto == null) minuteto = "00:00";
     
//        StringBuilder sb = new StringBuilder();
//        sb.append("Date Filter calculations\n\r");
//        sb.append(String.format("\tStart tag is : %tc or %d miliseconds\n\r", timefrom, milis_startime ));
//        sb.append(String.format("\tEnd   tag is : %tc or %d miliseconds\n\r", timeto, milis_endtime ));
//        sb.append("\n\r");
//        LOG.info(sb.toString());
        
        sp_timeto.getValueFactory().setValue(minuteto);
        
    }
    
    
    
    //==================================== TABLE SERVICES =======================================================================
    
    private ObservableList<Map<String, Object>> table_items = FXCollections.<Map<String,Object>>observableArrayList();
    private ObservableList<? extends Integer> clist;
    
    
    
    private void visualizeRecords(){
      
        ArrayList<Long> recs = getSelectedRecords();
        
    }
    
    
    
    public ArrayList<Long> getSelectedRecords(){
        
        ArrayList<Long> srec = new ArrayList<>();
        
        if (clist.size() == 1){
            Map<String, Object> item = table_items.get(clist.get(0));
            Long idx = (Long)item.get("timestamp_key");
            dbsv.loadDBRecord(idx);
            
            LOG.info(String.format("Item = %d", idx));    
        }
        return srec;
    }
    
    
    public void updateTable(ArrayList<DBRecord> records){
        
        final AtomicInteger count = new AtomicInteger(0); 
        ArrayList<Map<String, Object>> tempmap = new ArrayList<>();
        
        if (!table_items.isEmpty()){
            for(Map<String, Object> item : table_items){
                Boolean val = (Boolean)item.get("selected_key");
                if (val){
                    item.put("id_key", count.incrementAndGet());
                    tempmap.add(item);
                }
            }
        }

        table_items.clear();
        
//        if (records.isEmpty()){
//            enableVisualize(false);
//            enableCSV(false);
//            enableRemove(false);
//        }
        
        if (!tempmap.isEmpty()){
            for (Map<String, Object> mitem : tempmap){
                table_items.add(mitem);
            }
        }
        
        for (DBRecord dbrec : records){
            Map<String, Object> map = new HashMap<>();
            map.put("id_key", count.incrementAndGet());
            map.put("selected_key", Boolean.valueOf(false));
            map.put("timestamp_key", convertLong(dbrec.getId()));
            map.put("blaine_key", convertDouble(dbrec.getBlaine()));
            map.put("sid_key", dbrec.getSid());
            map.put("lote_key", dbrec.getLote());
            map.put("aux_key", dbrec.getAux());
            table_items.add(map);
        }
        
        tb_table.getItems().clear();
        tb_table.getItems().addAll(table_items);
        
        enableVisualize(false);
        enableCSV(false);
        enableRemove(false);
        
    }
    
    
    private Double convertDouble(String value) {    
        if (value != null && !value.isEmpty()){
            return Double.valueOf(value);
        }
        else {
            return 0.0;
        }
    }
    
    private Long convertLong(String value) {    
        if (value != null && !value.isEmpty()){
            return Long.valueOf(value);
        }
        else {
            return 0L;
        }
    }
    
    
    
    private ArrayList<DBRecord> createDummyRecords(){
        
        ArrayList<DBRecord> drec= new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            drec.add(DBRecord.createInstance());
        }       
        return drec;
    }
    
    
    public ObservableList<Map<String, Object>> getMapData(){
      
        final AtomicInteger count = new AtomicInteger(0); 
        
        ArrayList<DBRecord> records = createDummyRecords();
        
        for (DBRecord dbrec : records){
            Map<String, Object> map = new HashMap<>();
            map.put("id_key", count.incrementAndGet());
            map.put("selected_key", Boolean.valueOf(false));
            map.put("timestamp_key", Long.valueOf(dbrec.getId()));
            map.put("blaine_key", Double.valueOf(dbrec.getBlaine()));
            map.put("sid_key", dbrec.getSid());
            map.put("lote_key", dbrec.getLote());
            
            table_items.add(map);
        }
        return items; 
    }
   
    
    
    
    private TableColumn<Map, Number> generateIntegerColumn(String label, String key, boolean editable, Double size, String style){
         
        TableColumn<Map, Number> col = new TableColumn<>(label);
        
        if (size == -1){
            final Text text = new Text(label);
            size = text.getLayoutBounds().getWidth() + 40.0;
            col.setPrefWidth(size);
        }
        else if (size != 0){
            col.setPrefWidth(size);
        }
        
        if (style != null){
            col.setStyle(style);
        }
        else{
            col.setStyle( "-fx-alignment: center; -fx-text-fill: blue;");
        }
        
        if (editable){
            col.setCellValueFactory(cellData -> {
                Map p = cellData.getValue();
                Integer s = (Integer)p.get(key);
                return new SimpleIntegerProperty(s);
            });
            
            col.setEditable(true);
            col.setOnEditCommit( e -> {
                int row = e.getTablePosition().getRow();
                Map m = e.getRowValue();
                m.put(key, e.getNewValue());
            });
        }
        else{
            col.setCellValueFactory(new MapValueFactory<>(key)); 
        }
   
        return col;
    }
    
    private TableColumn<Map, Double> generateDoubleColumn(String label, String key){
        TableColumn<Map, Double> col = new TableColumn<>(label);
        col.setCellValueFactory(new MapValueFactory<>(key));      
        return col;
    }
    
    private TableColumn<Map, String> generateDateColumn(String label, String key){
        TableColumn<Map, String> col = new TableColumn<>(label);
        col.setCellValueFactory(new MapValueFactory<>(key));
        
        col.setCellValueFactory(cellData -> {
            Map p = cellData.getValue();
            String v = String.format("%1$td-%1$tm-%1$tY %1$tH:%1$tM:%1$tS",p.get(key));
            return new ReadOnlyStringWrapper(v);
        });
        
        col.setPrefWidth(180);
        return col;
    }
    
    
    
    private TableColumn<Map, String> generateTextColumn(String label, String key, boolean editable, Double size){
        
        TableColumn<Map, String> col = new TableColumn<>(label);
        col.setCellValueFactory(cellData -> {
            Map p = cellData.getValue();
            String s = (String)p.get(key);
            return new ReadOnlyStringWrapper(s);
        });
        col.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        
        if (editable){
            col.setEditable(true);
            col.setOnEditCommit( e -> {
                int row = e.getTablePosition().getRow();
                Map m = e.getRowValue();
                m.put(key, e.getNewValue());
            });
        }
        
        col.setPrefWidth(size);
        return col;
    }
    
    
    private TableColumn<Map, Boolean> generateCheckColumn(String label, String key){
        
        TableColumn<Map, Boolean> col = new TableColumn<>(label);
        col.setCellValueFactory(cellData -> {
            Map p = cellData.getValue();
            Boolean v = (Boolean)p.get(key);
            return new SimpleBooleanProperty(v);
        });
        
        
        col.setEditable(true);
        col.setCellFactory((TableColumn<Map, Boolean> p) -> {         
            CheckBox checkBox = new CheckBox();
            TableCell<Map, Boolean> tableCell = new TableCell<Map, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) 
                        setGraphic(null);
                    else {
                        setGraphic(checkBox);
                        checkBox.setSelected(item);
                    }
                }
            };

            checkBox.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                event.consume();
                checkBox.setSelected(!checkBox.isSelected());
                Map<String, Object>item = (Map<String, Object>)tableCell.getTableRow().getItem();
                item.put(key, checkBox.isSelected());
            });
            
            tableCell.setAlignment(Pos.CENTER);
            tableCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            return tableCell;
        });
        
        return col;
    }
    
    private TableColumn<Map, String> generateTypeDoubleColumn(String label, String key){
    
        TableColumn<Map, String> col = new TableColumn<>(label);
        col.setCellValueFactory(cellData -> {
            Map p = cellData.getValue();
            Double d = (Double)p.get(key);
            LinkedHashMap<Double,String> pointtype_map = columntype_maps.get(key);
            if (pointtype_map != null){
                String cellvalue = pointtype_map.get(d);
                if (cellvalue != null){
                    return new ReadOnlyStringWrapper(cellvalue); 
                }
            } 
            return new ReadOnlyStringWrapper("Default"); 
        });
        return col;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void lockUI (boolean lock){
      
        if (lock){
            //sidebar_btstore.setOpacity(0.5);
            //sidebar_btreport.setOpacity(0.5);
            //sidebar_btbroadcast.setOpacity(0.5);
        }
        else{
            //sidebar_btstore.setOpacity(1.0);
            //sidebar_btreport.setOpacity(1.0);
            //sidebar_btbroadcast.setOpacity(1.0);
        }
        
    }
    
//    private void lockVisualize(boolean lock){
//        if (lock){
//            
//        }
//        else{
//            
//        }
//    }
    
    
    
    // ========================================= DISPATCH SECTION =======================================================
 
    
    
    
    
    
    
    @Override
    public void clearCanvas(){
        //checklist1.setOpacity(0.0);
    }
    
    
    public void yieldFocus(FXFField field, boolean fwd){
        wdgmanager.yieldFocus(this, field, fwd);
    }
    
    
//    public InstanceContent getInstanceContent(){
//        return this.instanceContent;
//    }
    
    //public Scene getScene() { return scene;}

    @Override
    public void update() {
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