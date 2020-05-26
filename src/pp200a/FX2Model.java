/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.syssupport.ActivityModel;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

/**
 *
 * @author opus
 */
public class FX2Model extends ActivityModel {

    private static final Logger log = Logger.getLogger(FX2Model.class.getName());
    
    
    private CalibDescriptor cal;
    private CalibDescriptor lastcal;
    private boolean calibclean = true;
    
    
    
    public FX2Model() {
        proplink_uimap = new LinkedHashMap<>();
        proplink_modelmap = new LinkedHashMap<>();
        cal = new CalibDescriptor();
    }
    

    public CalibDescriptor getCal() {
        return cal;
    }

    public void setCal(CalibDescriptor cal) {
        this.cal = cal;
    }

    public CalibDescriptor getLastcal() {
        return lastcal;
    }

    public void setLastcal(CalibDescriptor lastcal) {
        this.lastcal = lastcal;
    }

    public boolean isCalibclean() {
        return calibclean;
    }

    public void setCalibclean(boolean calibclean) {
        this.calibclean = calibclean;
    }

    
    
    
    
    
}
