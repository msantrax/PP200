/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import java.awt.Font;
import java.util.ArrayList;
import javafx.scene.paint.Color;

/**
 *
 * @author opus
 */
public class ReportDescriptor {
    
    private ArrayList<ReportItem> items;
    private String strokecolor = Color.BLUE.toString();
    private String errorcolor = Color.DARKRED.toString();
    private Double xoffset = 0.0;
    private Double yoffset = 0.0;
    private String font = Font.SANS_SERIF.toString();
    private int fontsize = 12;
    private String watermark = "";
    
    
    public ReportDescriptor() {
        items = new ArrayList<>();
    }

    public ArrayList<ReportItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<ReportItem> items) {
        this.items = items;
    }

    public ReportDescriptor addItem(ReportItem item){
        items.add(item);
        return this;
    }
    
    public ReportItem getItem(String item){
        
        for (ReportItem ri : items){
            if (ri.getDatatype().equals(item)) return ri;
        }
        return null;
    }
    
    
    public String getStrokecolor() {
        return strokecolor;
    }

    public void setStrokecolor(String strokecolor) {
        this.strokecolor = strokecolor;
    }

    public String getErrorcolor() {
        return errorcolor;
    }

    public void setErrorcolor(String errorcolor) {
        this.errorcolor = errorcolor;
    }

    public Double getXoffset() {
        return xoffset;
    }

    public ReportDescriptor setXoffset(Double xoffset) {
        this.xoffset = xoffset;
        return this;
    }

    public Double getYoffset() {
        return yoffset;
    }

    public ReportDescriptor setYoffset(Double yoffset) {
        this.yoffset = yoffset;
        return this;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public int getFontsize() {
        return fontsize;
    }

    public void setFontsize(int fontsize) {
        this.fontsize = fontsize;
    }

    public String getWatermark() {
        return watermark;
    }

    public void setWatermark(String watermark) {
        this.watermark = watermark;
    }
    
    
    
    
    
    
    
}
