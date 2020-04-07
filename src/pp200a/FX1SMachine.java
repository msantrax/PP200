/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.smstate;
import java.util.logging.Logger;

/**
 *
 * @author opus
 */
public class FX1SMachine {

    private static final Logger LOG = Logger.getLogger(FX1SMachine.class.getName());
    
    
    private static FX1SMachine instance; 
    public static FX1SMachine getInstance(){
        if (instance == null) {instance = new FX1SMachine();}
        return instance;
    }

    
    public FX1SMachine() {
    
    }
    
    
    
    
    
    
    @smstate (state = "FX1TEST")
    public boolean st_fx1test(SMTraffic smm){
        LOG.info(String.format("FX1TEST was called from : %s", smm.getPayload().vstring));
        return true;
    }
    
    
    
    
    
    
    
    
}
