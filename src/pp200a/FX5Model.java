/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.fxsupport.PropertyLinkDescriptor;
import com.opus.syssupport.ActivityModel;
import com.opus.syssupport.Profile;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

/**
 *
 * @author opus
 */
public class FX5Model extends ActivityModel{

    private static final Logger LOG = Logger.getLogger(FX5Model.class.getName());

    private YaraDescriptor an;
    private boolean anclean = true;
    
    private CalibDescriptor cal;
    private CalibDescriptor lastcal;
    private boolean calibclean = true;
    
    
    public FX5Model() {
        proplink_uimap = new LinkedHashMap<>();
        proplink_modelmap = new LinkedHashMap<>();
        an = new YaraDescriptor();
        
    }

    
    // ======================================= GET SET ===============================================================
    
    public YaraDescriptor getAn() {
        return an;
    }

    public void setAn(YaraDescriptor an) {
        this.an = an;
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

    public boolean isAnclean() {
        return anclean;
    }

    public void setAnclean(boolean anclean) {
        this.anclean = anclean;
    }

    
    
    
}
