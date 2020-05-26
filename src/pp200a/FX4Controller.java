/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;


import com.opus.fxsupport.FXFController;
import com.opus.syssupport.Profile;
import com.opus.fxsupport.SystemMenu;
import com.opus.fxsupport.FXFField;
import com.opus.fxsupport.FXFWidgetManager;
import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.syssupport.VirnaServiceProvider;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;



public class FX4Controller extends FXFController implements com.opus.fxsupport.FXFControllerInterface {

    private static final Logger LOG = Logger.getLogger(FX4Controller.class.getName());
    
    
    private static final int PANEL_WIDTH_INT = 1366;
    private static final int PANEL_HEIGHT_INT = 400;
    
    private String profilepath;
    private FXFWidgetManager wdgmanager;
    
    private Scene scene;
    
    
    @FXML
    private VBox rootpane;
    
    private Pane browserpane;
    

    public FX4Controller() {
        
    }
    
    
    public FX4Controller(FXMLLoader fxmlLoader) {
        wdgmanager = FXFWidgetManager.getInstance();
        this.fxmlLoader = fxmlLoader; 
    }
    
    public FX4Controller(FXMLLoader fxmlLoader, String profilepath) {
        
        wdgmanager = FXFWidgetManager.getInstance();
        this.fxmlLoader = fxmlLoader; 
        this.profilepath = profilepath;
    }
    
    public void activateModel(){
        //machine.activateModel(profile.getArgument());
    }
    
    
    
    @FXML
    void initialize() {

        LOG.info("FX4Controller initializing ...");
        
        //wdgmanager = FXFWidgetManager.getInstance();
        wdgmanager.addContext(this, fxmlLoader.getNamespace());
        wdgmanager.initFocus(this);
        
        browserpane = createBrowser();
        rootpane.getChildren().add(browserpane);
        
    }
    
    @Override
    public void update(Scene scene){
        this.scene = scene;
    }
    
    @Override
    public void update(){
        update(scene);
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
    
    private Pane createBrowser() {
            
        Double widthDouble = new Integer(PANEL_WIDTH_INT).doubleValue();
        Double heightDouble = new Integer(PANEL_HEIGHT_INT).doubleValue();
        
        WebView view = new WebView();
        view.setMinSize(1366.0, 400.0);
        view.setPrefSize(1366.0, 400.0);
        
        final WebEngine eng = view.getEngine();
        final Label warningLabel = new Label("Do you need to specify web proxy information?");
        //eng.load("http://google.com");
        eng.load("http://acpinstruments.com.br");
        
        ChangeListener handler = new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (warningLabel.isVisible()) {
                    warningLabel.setVisible(false);
                }
            }
        };
        eng.getLoadWorker().progressProperty().addListener(handler);

        //final TextField locationField = new TextField("http://localhost:8080/HTML_BS4");
        final TextField locationField = new TextField("http://acpinstruments.com.br");
        locationField.setMaxHeight(Double.MAX_VALUE);
        Button goButton = new Button("Go");
        goButton.setDefaultButton(true);
        EventHandler<ActionEvent> goAction = new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
//                eng.load(locationField.getText().startsWith("http://") ? locationField.getText()
//                        : "http://" + locationField.getText());
                String locationf = locationField.getText();
                eng.load(locationf);
            }
        };
        goButton.setOnAction(goAction);
        locationField.setOnAction(goAction);
        eng.locationProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                locationField.setText(newValue);
            }
        });
        GridPane grid = new GridPane();
        grid.setPrefSize(1366.0, 600.0);
        grid.setMaxSize(1366.0, 600.0);
        
        //grid.setPadding(new Insets(5));
        grid.setVgap(5);
        grid.setHgap(5);
        
        GridPane.setConstraints(locationField, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
        GridPane.setConstraints(goButton, 1, 0);
        GridPane.setConstraints(view, 0, 1, 2, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
        GridPane.setConstraints(warningLabel, 0, 2, 2, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
        
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(widthDouble - 200, widthDouble - 200, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true),
                new ColumnConstraints(40, 40, 40, Priority.NEVER, HPos.CENTER, true));
        grid.getChildren().addAll(locationField, goButton, warningLabel, view);
        return grid;
        
    }
    
    
    
    //  ========================================= FXF tools =======================================================
    
    private static final String UID = "FX4";
    @Override
    public String getUID() { return UID;}
    
    // =========================== PROFILE LINK ================================================
    private FXMLLoader fxmlLoader;
    public FXMLLoader getFxmlLoader() {return fxmlLoader;}
    public void setFxmlLoader(FXMLLoader fxmlLoader) {this.fxmlLoader = fxmlLoader;}
    
    
    //public FX4Controller getFXController(){return this;}
    
    // Application controller link 
    private VirnaServiceProvider ctrl;
    public void setAppController (VirnaServiceProvider ctrl){
        this.ctrl = ctrl;
    }
    
    
    // =========================== PROFILE LINK ================================================
    private Profile profile;
    private void loadProfile(){profile = Profile.getInstance();}
    public Profile getProfile() { return profile;}
   
    
    
    
    @Override
    public void sendSignal (PropertyLinkDescriptor pld, String sigtype){
        
//        ctrl.getQueue().offer(new SMTraffic(0l, 0l, 0, pld.getCallstate(), 
//                                   new VirnaPayload().setObject(pld)));
        
    }
    
    
    
    
    @Override
    public void clearCanvas(){
        //checklist1.setOpacity(0.0);
    }
    
    
    public void yieldFocus(FXFField field, boolean fwd){
        wdgmanager.yieldFocus(this, field, fwd);
    }

    @Override
    public void updateField(String fieldname, String value, boolean required) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}