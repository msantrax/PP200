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
        report_template.add("yara.pdf");
        label="Profile";
        
        instance = this;
    }
    
    
    
    public void createDefault(){
        
        descriptors.clear();
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_altura")
                .setTooltip_message("Altura da camada de analise")
                .setUse_range(true)
                .setRanges(new Double[] {2.8, 10.0, 50.0, 0.0})
                .setDefault_value("2.8")
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_densidade")
                .setTooltip_message("Densidade medida")
                .setUse_range(true)
                .setRanges(new Double[] {1.12, 30.0, 80.0, 0.0})
                .setDefault_value("1.1242")
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_massa")
                .setTooltip_message("Peso da amostra a ser medida")
                .setUse_range(true)
                .setRequired(true)
                .setRanges(new Double[] {8.6, 50.0, 80.0, 0.0})
                .setDefault_value("8.6")
        );
       
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_temperature")
                .setTooltip_message("Temperatura da Calibração")
                .setUse_range(true)
                .setRequired(true)
                .setUse_windowrange(false)
                .setRanges(new Double[] {10.0, 18.0 , 30.0, 50.0})
        );
        
        
        // ===============================================================================================================
//        
//        descriptors.add(new FXFFieldDescriptor()
//                .setName("it_analisetime")
//                .setTooltip_message("Tempo de escoamento")
//                .setMaybenull(true)
//                .setRequired(false)
//                .setUse_range(true)
//                .setUse_windowrange(false)
//                .setRanges(new Double[] {50.0, 60.0 , 170.0, 200.0})
//                .setLocal_callback("UPDATETIME")
//        );
        
        
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
        
        
        
        // ===================================================================================================
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_poros")
                .setTooltip_message("Porosidade da amostra")
                .setUse_range(true)
                .setUse_windowrange(true)
                .setRanges(new Double[] {0.7, 10.0 , 20.0, 3.0})
                .setRequired(false)
                .setFormat("%5.3f")
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_ssa")
                .setTooltip_message("SSA")
                .setUse_range(true)
                .setUse_windowrange(true)
                .setRanges(new Double[] {2300.0, 10.0 , 20.0, 3.0})
                .setRequired(false)
                .setFormat("%6.2f")
        );
        
        descriptors.add(new FXFFieldDescriptor()
                .setName("it_perm")
                .setTooltip_message("Perm")
                .setUse_range(true)
                .setUse_windowrange(true)
                .setRanges(new Double[] {24.0, 10.0 , 20.0, 3.0})
                .setRequired(false)
                .setFormat("%6.2f")
        );
        
        
        // ===========================================================================================================
        
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
