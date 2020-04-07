/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

// ======================================================================================

import java.lang.reflect.Method;


public class ReportItem {

    private Double xpos = 0.0;
    private Double ypos = 0.0;
    private String datatype = "field";
    private String datatag = "";
    private String defaultdata = "";
    private String auxdata = "";
    private String format = "";
    private int fontsize = 12;
    
    private transient Method method;
    private transient String classtype;
    
    
    
    public ReportItem(Double xpos, Double ypos, String field, String format) {
        this.xpos = xpos;
        this.ypos = ypos;
        this.datatype = field;
        this.format = format;
    }

    public ReportItem(String field) {
        this.datatype = field;
    }

    public Double getXpos() {
        return xpos;
    }

    public void setXpos(Double xpos) {
        this.xpos = xpos;
    }

    public Double getYpos() {
        return ypos;
    }

    public void setYpos(Double ypos) {
        this.ypos = ypos;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getFontsize() {
        return fontsize;
    }

    public void setFontsize(int fontsize) {
        this.fontsize = fontsize;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getClasstype() {
        return classtype;
    }

    public void setClasstype(String classtype) {
        this.classtype = classtype;
    }

    public String getDatatag() {
        return datatag;
    }

    public void setDatatag(String datatag) {
        this.datatag = datatag;
    }

    public String getDefaultdata() {
        return defaultdata;
    }

    public void setDefaultdata(String defaultdata) {
        this.defaultdata = defaultdata;
    }

    public String getAuxdata() {
        return auxdata;
    }

    public void setAuxdata(String auxdata) {
        this.auxdata = auxdata;
    }
    
}
