/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.fxsupport.PropertyLinkDescriptor;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

/**
 *
 * @author opus
 */
public class FX2Model {

    private static final Logger log = Logger.getLogger(FX2Model.class.getName());
    
    
    private CalibDescriptor cal;
    private CalibDescriptor lastcal;
    private boolean calibclean = true;
    
    
    private LinkedHashMap<String,PropertyLinkDescriptor>cal_proplink_uimap;
    private LinkedHashMap<String,PropertyLinkDescriptor>cal_proplink_modelmap;

    
    
    public FX2Model() {
    
    }
    
    
    
}
