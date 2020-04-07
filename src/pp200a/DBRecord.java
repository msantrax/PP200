/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import java.util.Locale;

/**
 *
 * @author opus
 */
public class DBRecord {
    
    
    private String id;
    private String sid;
    private String lote;
    private String blaine;
    private String aux;

    
    public static DBRecord createInstance(){
        DBRecord dbr = new DBRecord().createDefault();
        return dbr;
    }
    
    public DBRecord() {
    }

    public DBRecord(String id, String sid, String lote, String blaine, String aux) {
        this.id = id;
        this.sid = sid;
        this.lote = lote;
        this.blaine = blaine;
        this.aux = aux;
    }

    
    public DBRecord createDefault(){
        
        Long rdate= Math.round(Math.random() * 10000);
        Double rblaine = 546.0 + (Math.random() * 10);
        
        id = String.valueOf(System.currentTimeMillis()+rdate);
        sid = "Sid of record @ " + id;
        lote = "Lote...";
        blaine = String.format(Locale.US, "%6.3f", rblaine);
        aux="";
        
        return this;
    }
    
    
    
    public String getId() {
        return id;
    }

    public DBRecord setId(String id) {
        this.id = id;
        return this;
    }

    public String getSid() {
        return sid;
    }

    public DBRecord setSid(String sid) {
        this.sid = sid;
        return this;
    }

    public String getLote() {
        return lote;
    }

    public DBRecord setLote(String lote) {
        this.lote = lote;
        return this;
    }

    public String getBlaine() {
        return blaine;
    }

    public DBRecord setBlaine(String blaine) {
        this.blaine = blaine;
        return this;
    }
    
    public String getAux() {
        return aux;
    }

    public DBRecord setAux(String aux) {
        this.aux = aux;
        return this;
    }
  
}
