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
public class ProfileYara extends Profile{
    
    
    private static ProfileYara instance; 
    public static ProfileYara getInstance(){
        if (instance == null) {instance = new ProfileYara();}
        return instance;
    }
    
    
    public ProfileYara() {
       
        classtype="pp200a.ProfileYara";
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
