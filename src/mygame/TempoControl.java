/*
 * Tempo controls are added to the music grid and control the tempo...
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author SeanTheBest
 */
public class TempoControl extends Node {
    
    // VARS //////////////////////////////////////////////////////////
    
    // each tempo control node is made up of four elements...
    // 1. the control geo - a red circle
    // 2. a line pointing straight down to the music grid,
    // signifying the tempo's x position
    // 3. a line connecting the tempo to the next tempo, if
    // one exists, if the listener accelerando or ritardando
    // (these will be linear interpolations for now, I guess)
    // 4. a bitmap text to see the number... located in the circle
    
    private AssetManager assetManager;
    private Geometry controlGeo, lineGeo, connectionGeo;
    private Material mat;
    private ColorRGBA myColor, myBorderColor;
    private float controlSizeW, controlSizeH, borderSize;
    private float distance;
    private boolean connection = false;
    TempoControl nextTempo = null; // next tempo, if connected...
    private BitmapText tempoText;
    private ControlInteger tempoValue;
    
    // METHODS ///////////////////////////////////////////////////////
    
    // constructor
    public TempoControl(AssetManager assetManager, BitmapFont guiFont, float controlSize, float distance, float borderSize) {
        this.assetManager = assetManager;
        this.controlSizeW = controlSize;
        controlSizeH = controlSizeW * 0.5f;
        this.borderSize = borderSize;
        this.distance = distance;
        
        tempoValue = new ControlInteger(120, 20, 300);
        tempoText = new BitmapText(guiFont, false);
        tempoText.setSize(guiFont.getCharSet().getRenderedSize());
        tempoText.setText(Integer.toString(tempoValue.getValue()));
        tempoText.setColor(ColorRGBA.White);
        
        // init control geo...
        Quad quad = new Quad(controlSizeW, controlSizeH);
        controlGeo = new Geometry("NoteQuad", quad);
        controlGeo.center();        
        mat = new Material(this.assetManager, "Materials/ShapeShader.j3md");
        myColor = ColorRGBA.Red.mult(0.8f);
        myColor.a = 0.5f;
        myBorderColor = myColor.clone();
        myBorderColor.a = 1f;
        mat.setColor("BorderColor", myBorderColor);
        mat.setColor("Color", myColor);
        mat.setFloat("BorderSize", borderSize);
        mat.setFloat("Curve", 3);
        mat.setFloat("RadiusW", controlSizeH/2f);
        mat.setFloat("RadiusH", controlSizeH/2f);
        mat.setFloat("Height", controlSizeH);
        mat.setFloat("Width", controlSizeW);        
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        controlGeo.setMaterial(mat);
        attachChild(controlGeo);
        
        // init line
        Vector3f startPos = new Vector3f(0, -(controlSizeH/2f), 0);
        Vector3f endPos = new Vector3f(0, -(distance+0.5f), 0);
        Line line = new Line(startPos, endPos);
        line.setLineWidth(3f);
        Material lineMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        lineMat.setColor("Color", myColor);
        lineMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        lineGeo = new Geometry("controlLine", line);
        lineGeo.setMaterial(lineMat);
        attachChild(lineGeo);        
    }
    
    public void updateTempoConnectionLine() {
        if (connection && nextTempo != null) {
            if (hasChild(connectionGeo))
                detachChild(connectionGeo);            
            // actually create the connection line...
            Vector3f startPos = new Vector3f(controlSizeW/2f, 0, 0);
            Vector3f endPos = new Vector3f(nextTempo.getLocalTranslation().x-(controlSizeW/2f)-getLocalTranslation().x, 0, 0);
            Line line = new Line(startPos, endPos);
            line.setLineWidth(3f);
            Material lineMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            lineMat.setColor("Color", myColor);
            lineMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            connectionGeo = new Geometry("controlLine", line);
            connectionGeo.setMaterial(lineMat);
            attachChild(connectionGeo);            
        } else {
            if (hasChild(connectionGeo))
                detachChild(connectionGeo);
        }
    }
    
    public void setBorderColor(ColorRGBA newColor) {
        mat.setColor("BorderColor", newColor);
    }
    public void resetBorderColor() {
        mat.setColor("BorderColor", myBorderColor);
    }    
    // gets and sets

    public BitmapText getTempoText() {
        return tempoText;
    }
    public ControlInteger getTempoControl() {
        return tempoValue;
    }
    public boolean getConnected() {
        return connection;
    }
    public void setConnected(boolean connect) {
        connection = connect;
    }
    public void setNextTempo(TempoControl nextTempo) {
        this.nextTempo = nextTempo;
    }
    public TempoControl getNextTempo() {
        return nextTempo;
    }
}
