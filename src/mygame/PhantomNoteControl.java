/*
 * The "phantom note" is a special "note" that is
 * created when a note is played -- it changes as it is played
 * so this is where the animation magic happens
 */
package mygame;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author SeanTheBest
 */
public class PhantomNoteControl extends AbstractControl {
    
    private float startAlpha = 1f;
    private Note note;
    private float noteWidth = 0f;
    private float startPosX = 0f;
    
    public PhantomNoteControl() {}
    
    public PhantomNoteControl(Note note, float noteWidth, float playPosX) {
        this.note = note;
        this.noteWidth = noteWidth;
        startPosX = playPosX;
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
    }
    
    // here we animate a note as it is being played
    @Override
    protected void controlUpdate(float tpf) {
        if (spatial != null) {
            if (note.getPlayPos() > startPosX + noteWidth) {
                note.deleteMe();
            } else {
                ColorRGBA newColor = note.getColor().clone();
                float percentComplete = Math.max(((note.getPlayPos() - startPosX) / noteWidth), 0f);
                newColor.a = startAlpha - (startAlpha * percentComplete);
                ColorRGBA newBorderColor = new ColorRGBA(0f, 0f, 0f, 0f);
                note.setNewColors(newColor, newBorderColor);
                float newScaleY = 1f + (8f * percentComplete);
                //float newScaleX = 1f + (2f * percentComplete);
                note.setLocalScale(1f, newScaleY, 1f);
                //float newCurve = 1f - (1f * percentComplete);
                //note.setCurve(newCurve);
                //Vector3f current = note.getLocalTranslation();
                //float currentY = 11f * percentComplete;
                //note.setLocalTranslation(current.x, currentY, current.z);
                //note.rotate(0f, 0f, 0.1f); 
            }
        }
    }
    
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        final PhantomNoteControl control = new PhantomNoteControl();
        control.setSpatial(spatial);
        return control;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
    }
    
}
