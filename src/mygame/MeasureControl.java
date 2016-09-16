/*
 * Measure controls are used to add / delete / edit measures
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author SeanTheBest
 */
public class MeasureControl extends Node {
    
    // VARS //////////////////////////////////////////
    
    private AssetManager assetManager;
    private BitmapText measureLabel; // measure number label
    private Geometry insertMeasureButton;
    private Geometry delMeasureButton;
    private float delMeasureHeight;
    private ColorRGBA insertMeasureColor = ColorRGBA.Green.mult(0.8f); // multiply to darken a bit
    private ColorRGBA delMeasureColor = ColorRGBA.Red.mult(0.8f);
    private ColorRGBA delMeasureBorderColor = ColorRGBA.Red.mult(0.8f);
    private Material insertMeasureMat;
    private Material delMeasureMat;
    private Measure myMeasure; // what measure does this control belong to?
    private int deleteConfirm = 0; // deletions happen in 2 stages to confirm
    
    // METHODS /////////////////////////////////////////
    
    // constructor
    public MeasureControl(AssetManager assetManager, BitmapFont font, float controlWidth, float controlHeight, boolean hasDelete) {
        
        this.assetManager = assetManager;
        
        // init bitmap text
        measureLabel = new BitmapText(font, false);
        measureLabel.setSize(font.getCharSet().getRenderedSize());
        measureLabel.setColor(ColorRGBA.White);        
        
        // init our geometries...
        // first, our insert measure button
        float insertMeasureWidth = controlWidth;
        float insertMeasureHeight = controlHeight * 0.25f;
        Quad q = new Quad(insertMeasureWidth, insertMeasureHeight);
        insertMeasureButton = new Geometry("insertMeasureButton", q);
        insertMeasureMat = new Material(this.assetManager, "Materials/ShapeShader.j3md");
        insertMeasureMat.setColor("Color", insertMeasureColor);        
        insertMeasureMat.setColor("BorderColor", insertMeasureColor);
        insertMeasureMat.setFloat("BorderSize", 0);
        insertMeasureMat.setFloat("Curve", 2);
        insertMeasureMat.setFloat("RadiusW", 0.5f * insertMeasureHeight); // same as height?
        insertMeasureMat.setFloat("RadiusH", 0.5f * insertMeasureHeight);
        insertMeasureMat.setFloat("Height", insertMeasureHeight);
        insertMeasureMat.setFloat("Width", insertMeasureWidth);
        insertMeasureButton.setMaterial(insertMeasureMat);
        attachChild(insertMeasureButton);
        
        // now our delete measure button...
        float delMeasureWidth = controlWidth;
        delMeasureHeight = controlHeight * 0.25f;
        Quad q2 = new Quad(delMeasureWidth, delMeasureHeight);
        delMeasureButton = new Geometry("deleteMeasureButton", q2);
        delMeasureMat = new Material(this.assetManager, "Materials/ShapeShader.j3md");
        delMeasureMat.setColor("Color", delMeasureColor);        
        delMeasureMat.setColor("BorderColor", delMeasureBorderColor);
        delMeasureMat.setFloat("BorderSize", 0);
        delMeasureMat.setFloat("Curve", 2);
        delMeasureMat.setFloat("RadiusW", 0.5f * delMeasureHeight); // same as height?
        delMeasureMat.setFloat("RadiusH", 0.5f * delMeasureHeight);
        delMeasureMat.setFloat("Height", delMeasureHeight);
        delMeasureMat.setFloat("Width", delMeasureWidth);
        delMeasureButton.setMaterial(delMeasureMat);
        delMeasureButton.setLocalTranslation(0, (delMeasureHeight*1.5f), 0);
        
        if (hasDelete)
            attachChild(delMeasureButton);        
    }
    
    public void showDeleteButton(boolean show) {
        if (show) {
            if (!hasChild(delMeasureButton))
                attachChild(delMeasureButton);
        } else {
            if (hasChild(delMeasureButton))
                detachChild(delMeasureButton);
        }
                
    }
    
    public void initiateMeasureDeletion() {
        deleteConfirm = 1;
        delMeasureMat.setFloat("BorderSize", delMeasureHeight*0.2f);
        delMeasureColor.set(0f, 0f, 0f, 0f);
        delMeasureMat.setColor("Color", delMeasureColor);       
    }
    public void resetDeletion() {
        deleteConfirm = 0;
        delMeasureMat.setFloat("BorderSize", 0);
        delMeasureColor = ColorRGBA.Red.mult(0.8f);
        delMeasureMat.setColor("Color", delMeasureColor);          
    }
    
    /**
     * Resets the colors of all buttons...
     */
    public void resetColors() {
        insertMeasureMat.setColor("Color", insertMeasureColor);
        delMeasureMat.setColor("Color", delMeasureColor);
        delMeasureMat.setColor("BorderColor", delMeasureBorderColor);
    }
    
    public void brightenInsertMeasureButton() {
        ColorRGBA newColor = insertMeasureColor.mult(2f);
        insertMeasureMat.setColor("Color", newColor);
    }
    public void brightenDeleteMeasureButton() {
        //ColorRGBA newColor = delMeasureColor.clone();
        //newColor.interpolate(ColorRGBA.White, 0.3f);
        ColorRGBA newColor = delMeasureColor.mult(2f);
        ColorRGBA newColor2 = delMeasureBorderColor.mult(2f);
        delMeasureMat.setColor("Color", newColor);
        delMeasureMat.setColor("BorderColor", newColor2);
    }
    
    public void setMeasure(Measure measure) {
        myMeasure = measure;
        if (measure != null) {
            measureLabel.setText(Integer.toString(measure.index+1));
        } else {
            measureLabel.setText("Fin");
        }
    }
    public Measure getMeasure() {
        return myMeasure;
    }
    public BitmapText getMeasureLabel() {
        return measureLabel;
    }
    
    public int getDeleteConfirm() {
        return deleteConfirm;
    }
    
}
