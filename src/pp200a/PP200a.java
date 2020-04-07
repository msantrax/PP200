/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.fxsupport.ActivityDescriptor;
import com.opus.fxsupport.FXFWindowManager;
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
        FXFWindowManager wm = FXFWindowManager.getInstance();
        wm.setAppController(ctrl);
        PicnoUtils.appclass = this.getClass();
        
        LinkedHashMap<String, ActivityDescriptor> activities = wm.getActivitiesmap();
        activities.put("Analises Blaine", new ActivityDescriptor ("FX1Controller", "Analises Blaine", 
                                                "Nova Analise Blaine", "profile_blaine" ));
        activities.put("Analises Yara", new ActivityDescriptor ("FX1Controller", "Analises Yara", 
                                                "Nova Analise Yara", "profile_yara" ));
        activities.put("Pesquisa de resultados", new ActivityDescriptor ("FX3Controller", "Pesquisa de resultados", 
                                                "Nova Pesquisa de Resultados", "none" ));
        activities.put("Manual do Instrumento", new ActivityDescriptor ("FX4Controller", "Manual do Instrumento", 
                                                "Manual / Ajuda", "help_page" ));
        activities.put("Calibração Blaine", new ActivityDescriptor ("FX1Controller", "Calibração Blaine", 
                                                "Nova Calibração Blaine", "profile_calib" ));
        activities.put("Configuração do Sistema", new ActivityDescriptor ("FX1Controller", "Configuração do Sistema", 
                                                "Nova Analise Blaine", "none" ));
        activities.put("Área de Serviços", new ActivityDescriptor ("FX1Controller", "Área de Serviços", 
                                                "Acesso a área de serviços", "none" ));
        
        
        wm.updateCanvasMap();
        wm.activateWindow("CANVAS");
  
        LOG.info("========================Operations1 Scene loaded ! ");    
       
    }
    
    
    public static void main(String[] args) {
        
        PicnoUtils.loadConfig();
        
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