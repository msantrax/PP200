/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.syssupport.ActivityDescriptor;
import com.opus.fxsupport.FXFWindowManager;
import com.opus.syssupport.ActivitiesMap;
import com.opus.syssupport.PicnoUtils;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.stage.Stage;


public class PP200a extends Application{

    private static final Logger LOG = Logger.getLogger(PP200a.class.getName());
  
    private static Controller ctrl;
    
    
    @Override
    public void start(Stage stage) throws Exception {
        
    
        FXFWindowManager.stage = stage;
        
//        stage.setFullScreen(false);
//        stage.setMaximized(false);
        
        FXFWindowManager wm = FXFWindowManager.getInstance();
        wm.setAppController(ctrl);
        PicnoUtils.appclass = this.getClass();
        
        wm.updateCanvasMap();
        wm.activateWindow("CANVAS", null, true);
  
        LOG.info("========================Operations1 Scene loaded ! ");    
       
    }
    
    private static void loadActivities(){
        
        // Blaine Analysis Activity
        ActivitiesMap.registerActivity("analise_blaine", new ActivityDescriptor (FX1Controller.class)
                                                            .setMachineclass(FX1SMachine.class)
                                                            .setModelclass(FX1Model.class)
                                                            .setArgument_prefix("analise_blaine")
                                                            .setName("Analises Blaine")
                                                            .setLabel("Nova Analise Blaine")
                                                            .setPublicstates(true)
        );
        
        // Yara Analysis Activity
        ActivitiesMap.registerActivity("analise_yara", new ActivityDescriptor (FX5Controller.class)
                                                            .setMachineclass(FX5SMachine.class)
                                                            .setModelclass(FX5Model.class)
                                                            .setArgument_prefix("analise_yara")
                                                            .setName("Analises Yara")
                                                            .setLabel("Nova Analise Yara")
                                                            .setPublicstates(true)
        );
        
        
        // Blaine Calibration Activity
        ActivitiesMap.registerActivity("calib_blane", new ActivityDescriptor (FX2Controller.class)
                                                            .setMachineclass(FX2SMachine.class)
                                                            .setModelclass(FX2Model.class)
                                                            .setArgument_prefix("calib_blaine")
                                                            .setName("Calibração Blaine")
                                                            .setLabel("Nova Calibração Blaine")
                                                            .setPublicstates(true)
        );
        
        // Database Search Activity
        ActivitiesMap.registerActivity("database", new ActivityDescriptor (FX3Controller.class)
                                                            .setMachineclass(null)
                                                            .setModelclass(null)
                                                            .setArgument_prefix("database")
                                                            .setName("Banco de Dados")
                                                            .setLabel("Nova Procura no Banco de Dados")
                                                            .setPublicstates(false)
        );
        
        
        // Blaine Calibration Activity
        ActivitiesMap.registerActivity("help", new ActivityDescriptor (FX4Controller.class)
                                                            .setMachineclass(null)
                                                            .setModelclass(null)
                                                            .setArgument_prefix("manual")
                                                            .setName("Manual do Instrumento")
                                                            .setLabel("Nova página do manual")
                                                            .setPublicstates(false)
        );
       
        
        
    }
    
    
    
    public static void main(String[] args) {
        
        
        PicnoUtils.loadConfig();
        loadActivities();
        
        ctrl = Controller.getInstance();
        ctrl.startService();
        
        PicnoUtils.setAppController(ctrl);
        
        launch(args);
        
    }
    
        
}








        //FXMLLoader headerloader = new FXMLLoader(FXFHeaderband.class.getResource("FXFHeaderband.fxml"));
        
        //AnchorPane headerpane = headerloader.load();
        //header_controller = (FXFHeaderband)canvasloader.getController();
        //header_controller.setFxmlLoader(canvasloader);
        
//        URL headerlocation = FXFHeaderband.class.getResource("FXFHeaderband.fxml");
//        FXMLLoader headerloader = new FXMLLoader();
//        headerloader.setLocation(headerlocation);
//        headerloader.setBuilderFactory(new JavaFXBuilderFactory());
//        header_controller = new FXFHeaderband();
//        //fxmlLoader.setController(fx3_controller);
//        //Parent headerroot = (Parent)FXFHeaderband.load(headerlocation.openStream()); 
 


        
        
//        VBox canvas1 = new VBox();
//        FXFHeaderband headerband = new FXFHeaderband();
//        FXFNavband navband = new FXFNavband();
//        
//        
//        URL location = getClass().getResource("FX3.fxml");
//        FXMLLoader fxmlLoader = new FXMLLoader();
//        fxmlLoader.setLocation(location);
//        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
//        fx3_controller = new FX3Controller(fxmlLoader);
//        fxmlLoader.setController(fx3_controller);
//        
//        VBox testroot = (VBox)fxmlLoader.load(location.openStream()); 
//        canvas1.getChildren().addAll(headerband, testroot, navband);
//        
//        Scene scene = new Scene(canvas1);   
//        scene.getStylesheets().add("com/opus/fxsupport/fxfsupport.css");

//        stage.setScene(scene);
//        fx3_controller.update();
        
        
        
        //fx_controller.setCtrl(ctrl);


//        FXMLLoader canvasloader = new FXMLLoader(getClass().getResource("canvas.fxml"));
//        Parent canvasroot = canvasloader.load();
//        fx_controller = (CanvasFXController)canvasloader.getController();
//        fx_controller.setFxmlLoader(canvasloader);