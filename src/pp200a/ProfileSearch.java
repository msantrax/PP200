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
public class ProfileSearch extends Profile{
    
    
    private static ProfileSearch instance; 
    public static ProfileSearch getInstance(){
        if (instance == null) {instance = new ProfileSearch();}
        return instance;
    }
    
    
    public ProfileSearch() {
       
        classtype="pp200a.ProfileSearch";
        csv_file="default.csv";
        report_template=new ArrayList<String>();
        report_template.add("pdf1.pdf");
        label="Profile";
        
        instance = this;
    }
    
    
    
    public void createDefault(){
        
        descriptors.clear();
        
        
        
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
     
    }
    
}
