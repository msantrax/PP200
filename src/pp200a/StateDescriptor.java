/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.syssupport.SMTraffic;
import com.opus.syssupport.VirnaPayload;
import java.lang.reflect.Method;
import java.util.ArrayDeque;

/**
 *
 * @author opus
 */
public class StateDescriptor {
    
    public StateDescriptor() {
        context_stack = new ArrayDeque<>();
    }
        
    private String SID;

    public String getSID() {
        return SID;
    }

    public StateDescriptor setSID(String SID) {
        this.SID = SID;
        return this;
    }

    private Method method;

    public Method getMethod() {
        return method;
    }

    public StateDescriptor setMethod(Method method) {
        this.method = method;
        return this;
    }

    private Class clazz;

    public Class getClazz() {
        return clazz;
    }

    public StateDescriptor setClazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    private Object instance;

    public Object getInstance() {
        return instance;
    }

    public StateDescriptor setInstance(Object instance) {
        this.instance = instance;
        return this;
    }
    
    private ArrayDeque<SMTraffic>context_stack;

    public SMTraffic getContext(){
        
        SMTraffic smt = context_stack.poll();
        if (smt != null){
            return smt;
        }
        else{
            return new SMTraffic( 0l, 0l, 0, SID, new VirnaPayload());
        }
    }
    
    public void setContext (SMTraffic smt){
        context_stack.push(smt);
    }
    
    
        
        
}
