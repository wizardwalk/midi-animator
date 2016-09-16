/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author SeanTheBest
 */
public class Note extends Node implements Comparable<Note> {
    
    private AssetManager assetManager;
    private Material mat;
    private Geometry geo;
    private Geometry controlGeo; // geometry for control bar
    private ControlInteger velocityControl;
    private float controlWidth = 0.5f;
    private float controlMaxHeight, controlHeight;
    private float height, width, curve, radiusH, radiusW, borderSize;
    private ColorRGBA myColor, myBorderColor, mySelectedBorderColor, defaultColor;
    private boolean selected = false;
    private boolean on = false; // is the note on or off?
    private Track myTrack;
    private boolean isTrackNode = false;
    private boolean isMuted = false;
    
    // MIDI info
    private int midiChannel = 0;
    private int midiProgram = 0;
    
    // "phantom notes" are just the nodes that expand and fade when the note is being played...
    private Note phantom = null;
    public boolean amPhantom = false; // am I a phantom note?
    private float playPosX; // the current position of the play bar
    
    private NoteStyle myStyle;
    
    
    // for copying a note, rather than copying a reference...
    public Note cloneNote() {
        Note newNote = new Note(assetManager, myTrack, height, width, curve, radiusH, radiusW, borderSize,
                myColor.clone(), myBorderColor.clone(), isTrackNode, myStyle, midiChannel, midiProgram, velocityControl.getValue());
        newNote.setVelocity(velocityControl.getValue());
        newNote.setMute(isMuted);
        return newNote;
    }
    
    // constructor
    public Note(AssetManager assetManager, Track myTrack, float height, float width, float curve, float radiusH, float radiusW, float borderSize,
            ColorRGBA myColor, ColorRGBA myBorderColor, boolean isTrackNode, NoteStyle newStyle, int midiChannel, int midiProgram, int velocity) {        
        
        this.assetManager = assetManager;
        this.isTrackNode = isTrackNode;
        this.myTrack = myTrack;
        this.height = height;
        this.width = width;
        this.curve = curve;
        this.radiusH = radiusH;
        this.radiusW = radiusW;
        this.borderSize = borderSize;
        this.myColor = myColor;
        defaultColor = myColor.clone();
        this.myBorderColor = myBorderColor;
        mySelectedBorderColor = ColorRGBA.LightGray;
        myStyle = newStyle;
        this.midiChannel = midiChannel;
        this.midiProgram = midiProgram;
        
        // init velocity control
        velocityControl = new ControlInteger(velocity, 1, 127);
        
        // init note quad / geometry
        Quad quad = new Quad(width, height);
        geo = new Geometry("NoteQuad", quad);
        geo.center();        
        mat = new Material(this.assetManager, "Materials/ShapeShader.j3md");
        mat.setColor("BorderColor", myBorderColor);
        mat.setColor("Color", myColor);
        mat.setFloat("BorderSize", borderSize);
        mat.setFloat("Curve", curve);
        mat.setFloat("RadiusW", radiusW * width);
        mat.setFloat("RadiusH", radiusH * height);
        mat.setFloat("Height", this.height);
        mat.setFloat("Width", this.width);        
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);        
        geo.setQueueBucket(RenderQueue.Bucket.Transparent);
        attachChild(geo);
        
        // init control Geo...
        if (!isTrackNode) {
            ColorRGBA controlColor = ColorRGBA.Red.mult(0.8f);
            controlColor.a = 0.6f;
            controlMaxHeight = height*1.9f;
            controlHeight = height*0.1f + (controlMaxHeight*(velocityControl.getValue()/127f));
            Quad q = new Quad(controlWidth, controlHeight);
            controlGeo = new Geometry("ControlQuad", q);
            controlGeo.center();        
            Material controlMat = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            controlMat.setColor("Color", controlColor);       
            controlMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            controlGeo.setMaterial(controlMat);        
            controlGeo.setQueueBucket(RenderQueue.Bucket.Transparent);
            controlGeo.setLocalTranslation(-(width/2f), -(controlHeight/2f), 0.5f);
            //attachChild(controlGeo);            
        }
    }
    
    // Humpty dumbpty sat on a wall
    // he was stupid
    
    // update the velocity control geo...
    public void updateVelControl() {
        boolean vis = hasChild(controlGeo);
        if (vis)
            detachChild(controlGeo);
        ColorRGBA controlColor = ColorRGBA.Red.mult(0.8f);
        controlColor.a = 0.6f;
        controlMaxHeight = height*1.9f;
        controlHeight = height*0.1f + (controlMaxHeight*(velocityControl.getValue()/127f));
        Quad q = new Quad(controlWidth, controlHeight);
        controlGeo = new Geometry("ControlQuad", q);
        controlGeo.center();        
        Material controlMat = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        controlMat.setColor("Color", controlColor);       
        controlMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        controlGeo.setMaterial(controlMat);        
        controlGeo.setQueueBucket(RenderQueue.Bucket.Transparent);
        controlGeo.setLocalTranslation(-(width/2f), -(controlHeight/2f), 0.5f);
        if (vis) // what is 'vis' I see?
            attachChild(controlGeo);
    }
    
    // delete this note...
    public void deleteMe() {
        myTrack.deleteNote(this);
        removeFromParent(); // <- not sure if necessary, but whatever
    }
    
    // sets new colors
    public void setNewColors(ColorRGBA newColor, ColorRGBA newBorderColor) {
        myColor = newColor.clone();
        defaultColor = newColor.clone();
        myBorderColor = newBorderColor.clone();
        mat.setColor("Color", myColor);
        mat.setColor("BorderColor", myBorderColor);
    }
    
    public void resetBorder() {
        if (selected)
            mat.setColor("BorderColor", mySelectedBorderColor);
        else
            mat.setColor("BorderColor", myBorderColor);
        mat.setFloat("BorderSize", borderSize);
    }
    public void resetColor() {
        //myColor = defaultColor.clone();
        mat.setColor("Color", myColor);
    }
    public void setBorderColor(ColorRGBA newColor, float alpha) {
        newColor.a = alpha;
        mat.setColor("BorderColor", newColor); 
    }
    public void setBorderSize(float newSize) {
        mat.setFloat("BorderSize", newSize);
    }
    public void brighten(float scale) {
        ColorRGBA newColor = myColor.mult(scale);
        mat.setColor("Color", newColor);            
    }
    public void hollow() {
        ColorRGBA newColor = ColorRGBA.Black;
        newColor.a = 0f;
        mat.setColor("Color", newColor);
        ColorRGBA newColor2 = myBorderColor.clone();
        newColor2.a = 0.3f;
        mat.setColor("BorderColor", newColor2);
    }
    // a phantom note is used for animation while playing...
    public void createPhantomNote(float playPosX) {
        phantom = cloneNote();
        phantom.amPhantom = true;
        PhantomNoteControl control = new PhantomNoteControl(phantom, getWidth(), playPosX);
        phantom.addControl(control);
        attachChild(phantom);
    }
    // change the width of a note... also requires us to recalc the material...
    public void setWidth(float newWidth) {
        this.width = newWidth;
        this.detachChild(geo);
        Quad quad = new Quad(newWidth, height); 
        geo = new Geometry("NoteQuad", quad);
        geo.center();
        if (myStyle.proportionalSide) {
            mat.setFloat("RadiusW", height/2f); // to make it equal with height radius
            mat.setFloat("RadiusH", radiusH * height);
        } else {
            mat.setFloat("RadiusW", width * myStyle.curveHeight); // to make it equal with height radius
            mat.setFloat("RadiusH", height * myStyle.curveWidth);
        }
        mat.setFloat("Height", this.height);
        mat.setFloat("Width", this.width);
        geo.setMaterial(mat);
        geo.setQueueBucket(RenderQueue.Bucket.Transparent);
        this.attachChild(geo);        
        // update location of controlgeo...
        controlGeo.setLocalTranslation(-(width/2f), -(controlHeight/2f), 0.5f);
    }
    
    public void setMuteControl(boolean mute) {
        System.out.println("mute = " + mute);
        isMuted = mute;
        if (isMuted) {
            myColor.a = 0f;
            mat.setColor("Color", myColor);
        } else {
            myColor = defaultColor.clone();
            resetBorder();
            resetColor();
        }
    }
    
    public void setSoloControl(boolean solo) {
        if (solo) {
            detachChild(geo);
            float newWidth = width*1.3f;
            float newHeight = height*1.3f;
            Quad quad = new Quad(newWidth, newHeight);
            geo = new Geometry("NoteQuad", quad);
            geo.center();
            mat.setFloat("RadiusW", newHeight/2f); // to make it equal with height radius
            mat.setFloat("RadiusH", radiusH * newHeight);
            mat.setFloat("Height", newHeight);
            mat.setFloat("Width", newWidth);
            geo.setMaterial(mat);
            geo.setQueueBucket(RenderQueue.Bucket.Transparent);
            attachChild(geo);
        } else {
            detachChild(geo);
            float newWidth = width;
            float newHeight = height;
            Quad quad = new Quad(newWidth, newHeight);
            geo = new Geometry("NoteQuad", quad);
            geo.center();
            mat.setFloat("RadiusW", newHeight/2f); // to make it equal with height radius
            mat.setFloat("RadiusH", radiusH * newHeight);
            mat.setFloat("Height", newHeight);
            mat.setFloat("Width", newWidth);
            geo.setMaterial(mat);
            geo.setQueueBucket(RenderQueue.Bucket.Transparent);
            attachChild(geo);
            resetBorder();
            resetColor();
        }
    }
    
    // getters and setters
    public void updatePlayPos(float playPosX) {
        if (amPhantom) {
            this.playPosX = playPosX;
        } else if (phantom != null) {
            phantom.updatePlayPos(playPosX);
        }
    }
    public float getPlayPos() {
        return playPosX;
    }
    public Material getMaterial() {
        return mat;
    }
    public float getWidth() {
        return width;
    }
    public float getHeight() {
        return height;
    }
    public int getVel() {
        return velocityControl.getValue();
    }
    public void setVelocity(int newVel) {
        velocityControl.setValue(newVel);
        updateVelControl();
    }
    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean isSelected) {
        selected = isSelected;
    }
    public boolean isOff() {
        return !on;
    }
    public boolean isOn() {
        return on;
    }
    public void turnOn(float playPosX) {
        on = true;
        //setBorderSize(4f);
        hollow();
        createPhantomNote(playPosX);
    }
    public void turnOff() {
        on = false;
        //resetColor();
        //resetBorder();
    }
    // similar to turnOff, but only called when
    // playing is stopped completely...
    public void resetOff() {
        on = false;
        resetColor();
        resetBorder();
        if (phantom != null) {
            if (hasChild(phantom))
                detachChild(phantom);
            phantom.deleteMe();
        }
    }
    public boolean getIsTrackNode() {
        return isTrackNode;
    }
    public Track getTrack() {
        return myTrack;
    }
    public void setMute(boolean mute) {
        isMuted = mute;
        if (isMuted) {
            myColor.a = 0.2f;
            myBorderColor.a = 0.1f;
        } else {
            myColor.a = 0.5f;
            myBorderColor.a = 1f;
        }
        mat.setColor("Color", myColor);
        mat.setColor("BorderColor", myBorderColor);
    }
    // tends to lower frame rate, so try not to use too much...
    public void setCurve(float newCurve) {
        mat.setFloat("Curve", newCurve);
    }
    public boolean isMuted() {
        return isMuted;
    }
    public Geometry getVelControl() {
        return controlGeo;
    }
    public boolean isTrackNode() {
        return isTrackNode;
    }
    public ControlInteger getVelControlVar() {
        return velocityControl;
    }
    public ColorRGBA getColor() {
        return myColor;
    }
    public int getMidiChannel() {
        return midiChannel;
    }
    public int getMidiProgram() {
        return midiProgram;
    }
    
    // for comparisons...
    @Override
    public int compareTo(Note othernote) {
        float myPosX = getLocalTranslation().x - width/2f;
        float otherPosX = othernote.getLocalTranslation().x - othernote.getWidth()/2f;
        return (myPosX < otherPosX ? -1 : (myPosX == otherPosX ? 0 : 1));
    }
    
}
