/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.fxsupport.FXFFieldDescriptor;
import com.opus.syssupport.Profile;
import java.util.ArrayList;

/**
 *
 * @author opus
 */
public class ProfileBlaine extends Profile{
    
    
    private static ProfileBlaine instance; 
    public static ProfileBlaine getInstance(){
        if (instance == null) {instance = new ProfileBlaine();}
        return instance;
    }
    
    
    public ProfileBlaine() {
       
        classtype="pp200a.ProfileBlane";
        csv_file="default.csv";
        report_template=new ArrayList<String>();
        report_template.add("pdf1.pdf");
        label="Profile";
        
        instance = this;
    }
    
    
    
    public void createDefault(){
        
        descriptors.clear();
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_porosidade")
                .setTooltip_message("Valor da Porosidade")
                .setUse_range(true)
                .setRanges(new Double[] {0.5, 10.0, 50.0, 0.0})
                .setDefault_value("0.5")
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_densidade")
                .setTooltip_message("Densidade medida")
                .setUse_range(true)
                .setRanges(new Double[] {2.0, 30.0, 80.0, 0.0})
                .setDefault_value("2.76")
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_massa_calculada")
                .setTooltip_message("Massa da amostra a ser medida")
                .setUse_range(true)
                .setRequired(false)
                .setRanges(new Double[] {2.0, 50.0, 80.0, 0.0})
        );
        
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_calibfile")
                .setTooltip_message("Identificação da ultima calibração")
                .setValidator_type("void")
                .setRequired(false)    
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_layervolume")
                .setTooltip_message("Volume da Camada conf. Calibração")
                .setUse_range(true)
                .setRequired(false)
                .setRanges(new Double[] {1.9, 50.0, 80.0, 0.0})
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_constantek")
                .setTooltip_message("Constante de calibração calculada")
                .setUse_range(true)
                .setRequired(false)
                .setRanges(new Double[] {2.13, 50.0, 80.0, 0.0})
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_temperature")
                .setTooltip_message("Temperatura da Calibração")
                .setUse_range(true)
                .setRequired(false)
                .setUse_windowrange(false)
                .setRanges(new Double[] {10.0, 18.0 , 30.0, 50.0})
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_analisetime")
                .setTooltip_message("Tempo do ensaio")
                .setMaybenull(true)
                .setRequired(false)
                .setUse_range(true)
                .setUse_windowrange(false)
                .setRanges(new Double[] {50.0, 60.0 , 170.0, 200.0})
                .setLocal_callback("UPDATETIME")
        );
        
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_analiseaverage")
                .setTooltip_message("Média dos ensaios")
                .setUse_range(true)
                .setRequired(false)
                .setUse_windowrange(false)
                .setRanges(new Double[] {50.0, 60.0 , 170.0, 200.0})
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_analisersd")
                .setTooltip_message("Reprodutibilidade das analises")
                .setUse_range(true)
                .setRequired(false)
                .setUse_windowrange(false)
                .setRanges(new Double[] {0.0001, 0.0002 , 1.5, 3.0})   
        );
        
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_blaineresult")
                .setTooltip_message("Resultado da analise")
                .setUse_range(true)
                .setUse_windowrange(true)
                .setRanges(new Double[] {5000.0, 10.0 , 20.0, 3.0})
                .setRequired(false)
                .setFormat("%5.3f")
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_sid")
                .setTooltip_message("Identificação da analise")
                .setValidator_type("notempty")
                .setRequired(false)
                .setUse_autocomplete(true)
                .setAutocomplete_file("sid_ac1.json")         
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_lote")
                .setTooltip_message("Lote da analise")
                .setValidator_type("void")
                .setRequired(false)
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_notas")
                .setTooltip_message("Notas diversas")
                .setValidator_type("void")
                .setRequired(false)
        );
        
    }
    
}
