/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import com.opus.syssupport.propertylink;
import com.opus.syssupport.propertyfieldmap;
import com.opus.syssupport.Config;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;


/**
 *
 * @author opus
 */
public class YaraDescriptor {

    private static final Logger log = Logger.getLogger(YaraDescriptor.class.getName());

    
    //======================================= PROPERTIES =======================================================================
    
        private Long uid;

    public static final String PROP_UID = "uid";

    @propertyfieldmap (propname = PROP_UID)
    public Long getUid() {
        return uid;
    }

    @propertylink (propname= PROP_UID, plink = "NONE")
    public void setUid(Long uid) {
        Long oldUid = this.uid;
        this.uid = uid;
        propertyChangeSupport.firePropertyChange(PROP_UID, oldUid, uid);
    }


        private long timestamp;

    public static final String PROP_TIMESTAMP = "timestamp";

    @propertyfieldmap (propname = PROP_TIMESTAMP)
    public long getTimestamp() {
        return timestamp;
    }

    @propertylink (propname= PROP_TIMESTAMP, plink = "date", input=false, callstate="NONE")
    public void setTimestamp(long timestamp) {
        long oldTimestamp = this.timestamp;
        this.timestamp = timestamp;
        propertyChangeSupport.firePropertyChange(PROP_TIMESTAMP, oldTimestamp, timestamp);
    }

    
        private String user;

    public static final String PROP_USER = "user";

    @propertyfieldmap (propname = PROP_USER)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        String oldUser = this.user;
        this.user = user;
        propertyChangeSupport.firePropertyChange(PROP_USER, oldUser, user);
    }

    
        private String profile;

    public static final String PROP_PROFILE = "profile";

    @propertyfieldmap (propname = PROP_PROFILE)
    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        String oldProfile = this.profile;
        this.profile = profile;
        propertyChangeSupport.firePropertyChange(PROP_PROFILE, oldProfile, profile);
    }

    
    
    // ======================================= Sample Mass calc ================================================================
    
        private String altura;

    public static final String PROP_ALTURA = "altura";

    @propertyfieldmap (propname = PROP_ALTURA)
    public String getAltura() {
        return altura;
    }

    @propertylink (propname= PROP_ALTURA, plink = "it_altura", input=true, callstate="CALCYARA")
    public void setAltura(String altura) {
        String oldAltura = this.altura;
        this.altura = altura;
        propertyChangeSupport.firePropertyChange(PROP_ALTURA, oldAltura, altura);
    }

    
        private String densidade;

    public static final String PROP_DENSIDADE = "densidade";

    @propertyfieldmap (propname = PROP_DENSIDADE)
    public String getDensidade() {
        return densidade;
    }

    @propertylink (propname= PROP_DENSIDADE , plink = "it_densidade", input=true, callstate="CALCYARA")
    public void setDensidade(String densidade) {
        String oldDensidade = this.densidade;
        this.densidade = densidade;
        propertyChangeSupport.firePropertyChange(PROP_DENSIDADE, oldDensidade, densidade);
    }

    
        private String massa_ensaio;

    public static final String PROP_MASSA_ENSAIO = "massa_ensaio";

    @propertyfieldmap (propname = PROP_MASSA_ENSAIO)
    public String getMassa_ensaio() {
        return massa_ensaio;
    }

    @propertylink (propname = PROP_MASSA_ENSAIO, plink = "it_massa", input=false, callstate="CALCYARA")
    public void setMassa_ensaio(String massa_ensaio) {
        String oldMassa_ensaio = this.massa_ensaio;
        this.massa_ensaio = massa_ensaio;
        propertyChangeSupport.firePropertyChange(PROP_MASSA_ENSAIO, oldMassa_ensaio, massa_ensaio);
    }
    
    
        private String temperature;

    public static final String PROP_TEMPERATURE = "temperature";
    
    @propertyfieldmap (propname = PROP_TEMPERATURE)
    public String getTemperature() {
        return temperature;
    }

    @propertylink (propname = PROP_TEMPERATURE, plink = "it_temperature", input=true, callstate="CALCYARA")
    public void setTemperature(String temperature) {
        String oldTemperature = this.temperature;
        this.temperature = temperature;
        propertyChangeSupport.firePropertyChange(PROP_TEMPERATURE, oldTemperature, temperature);
    }
    
    
    // ===============================  Calibration data =========================================================================
    
        private String poros;

    public static final String PROP_POROS = "poros";

    
    @propertyfieldmap (propname = PROP_POROS)
    public String getPoros() {
        return poros;
    }

    @propertylink (propname = PROP_POROS, plink = "it_poros", input=false, callstate="NONE")
    public void setPoros(String poros) {
        String oldPoros = this.poros;
        this.poros = poros;
        propertyChangeSupport.firePropertyChange(PROP_POROS, oldPoros, poros);
    }

    
    
        private String ssa;
    
    public static final String PROP_SSA = "ssa";

    @propertyfieldmap (propname = PROP_SSA)
    public String getSSA() {
        return ssa;
    }

    @propertylink (propname = PROP_SSA, plink = "it_ssa", input=false, callstate="ANALISEYARADONE")
    public void setSSA(String ssa) {
        String oldssa = this.ssa;
        this.ssa = ssa;
        propertyChangeSupport.firePropertyChange(PROP_SSA, oldssa, ssa);
    }
    
    
        private String perm;

    public static final String PROP_PERM = "perm";
    
    @propertyfieldmap (propname = PROP_PERM)
    public String getPerm() {
        return perm;
    }

    @propertylink (propname = PROP_PERM, plink = "it_perm", input=false, callstate="NONE")
    public void setPerm(String perm) {
        String oldperm = this.perm;
        this.perm = perm;
        propertyChangeSupport.firePropertyChange(PROP_PERM, oldperm, perm);
    }

    
    // ======================================= Analise Time Calc ==============================================================
    
        private ArrayList<String> tempos;

    public static final String PROP_TEMPOS = "tempos";

    @propertyfieldmap (propname = PROP_TEMPOS)
    public ArrayList<String> getTempos() {
        return tempos;
    }

    
    public void setTempos(ArrayList<String> tempos) {
        ArrayList<String> oldTempos = this.tempos;
        this.tempos = tempos;
        propertyChangeSupport.firePropertyChange(PROP_TEMPOS, oldTempos, tempos);
    }

    
        private String media;

    public static final String PROP_MEDIA = "media";

    @propertyfieldmap (propname = PROP_MEDIA)
    public String getMedia() {
        return media;
    }

    @propertylink (propname = PROP_MEDIA, plink = "it_analiseaverage", input=false, callstate="NONE")
    public void setMedia(String media) {
        String oldMedia = this.media;
        this.media = media;
        propertyChangeSupport.firePropertyChange(PROP_MEDIA, oldMedia, media);
    }

    
        private String rsd;

    public static final String PROP_RSD = "rsd";

    @propertyfieldmap (propname = PROP_RSD)
    public String getRsd() {
        return rsd;
    }

    @propertylink (propname = PROP_RSD, plink = "it_analisersd", input=false, callstate="CALCYARA")
    public void setRsd(String rsd) {
        String oldRsd = this.rsd;
        this.rsd = rsd;
        propertyChangeSupport.firePropertyChange(PROP_RSD, oldRsd, rsd);
    }

    
    

    
    
    // ================================================= Sample ID ===============================================================
    
        private String sid;

    public static final String PROP_SID = "sid";

    @propertyfieldmap (propname = PROP_SID)
    public String getSid() {
        return sid;
    }

    @propertylink (propname = PROP_SID, plink = "it_sid", input=true, callstate="ANALISEDONE")
    public void setSid(String sid) {
        String oldSid = this.sid;
        this.sid = sid;
        propertyChangeSupport.firePropertyChange(PROP_SID, oldSid, sid);
    }

        private String notas;

    public static final String PROP_NOTAS = "notas";

    @propertyfieldmap (propname = PROP_NOTAS)
    public String getNotas() {
        return notas;
    }

    @propertylink (propname = PROP_NOTAS, plink = "it_notas", input=true, callstate="NONE")
    public void setNotas(String notas) {
        String oldNotas = this.notas;
        this.notas = notas;
        propertyChangeSupport.firePropertyChange(PROP_NOTAS, oldNotas, notas);
    }

    
        private String lote;

    public static final String PROP_LOTE = "lote";

    @propertyfieldmap (propname = PROP_LOTE)
    public String getLote() {
        return lote;
    }

    @propertylink (propname = PROP_LOTE, plink = "it_lote", input=true, callstate="NONE")
    public void setLote(String lote) {
        String oldLote = this.lote;
        this.lote = lote;
        propertyChangeSupport.firePropertyChange(PROP_LOTE, oldLote, lote);
    }

    
    // ===================================================================================================================
    
    
    public YaraDescriptor() {
        
        loadDefaults();
        mapMethods();
    }

    
    private transient LinkedHashMap<String, Method> getmethods;
    public LinkedHashMap<String, Method> getMethods() { return getmethods;}
    
    private void mapMethods(){
        
        propertyfieldmap pfm;
        String fieldtag;
        
        getmethods = new LinkedHashMap<>();
        Class<?>c = this.getClass();
        
        for (Method mt : c.getDeclaredMethods() ){
            pfm = mt.getAnnotation(propertyfieldmap.class);
            if (pfm != null){
                fieldtag = pfm.propname();
                getmethods.put(fieldtag, mt);
            }
        }
    }
    
    public String getFieldAsString(String id){
        
        String out ;
        Type t;
        Method m = getmethods.get(id);
        if (m != null){
            try {
                String st = m.getReturnType().getTypeName();
                if (st.equals("java.lang.String")){
                    out = (String)m.invoke(this, null);
                    return out;
                }
                else{
                    //Class cl = Class.forName(st);
                    Object obj = m.invoke(this, null);
                    String s2 = String.valueOf(obj);
                    return s2;
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                //Exceptions.printStackTrace(ex);
//            } catch (ClassNotFoundException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }
        
        return null;
    }
    
    public void loadDefaults(){
        
        densidade = "1.1242";
        massa_ensaio = "8.6";
        altura = "2.8";
        temperature = "23.0";
        
        
        media = "";
        rsd = "";
       
        
        if (Config.getInstance().isUse_timesavings()){
            timestamp = System.currentTimeMillis() - 3600000L;
        }
        else{
            timestamp = System.currentTimeMillis();
        }
        
        uid = timestamp;
        user = "ACP-Raso";
        profile = "default";
        
        sid = "identificação de teste";
        notas = "Notas de teste - numero 1";
        lote = "CP2-123456";
    
        tempos = new ArrayList<>();
        //tempos.addAll(Arrays.asList("167.3", "167.7", "167.5"));
        
    }
    
    
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    
}


//    try {
//        m = c.getDeclaredMethod("getLote", null);
//        Type t = m.getReturnType();
//        String tn = t.getTypeName();          
//        String gs = m.toGenericString();
//
//        String teste = "";
//
//    } catch (NoSuchMethodException | SecurityException ex) {
//        Exceptions.printStackTrace(ex);
//    }