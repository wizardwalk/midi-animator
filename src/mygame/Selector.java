/*
 * The selector is used to select a group of notes...
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SeanTheBest
 */
public class Selector extends Node {
    
    // VARS ///////////////////////////////////////
    
    private AssetManager assetManager;
    private List<Track> ourTracks;
    private ColorRGBA myColorSelecting, myColorSelected;
    private float widthSelecting = 0f;
    private float heightSelecting = 0f;
    private float widthSelected = 0f; 
    private float heightSelected = 0f;
    private float selectedPadding = 0f;
    private float borderSize = 0f;
    private float selectorZ;
    private List<Note> selectingNotes = new ArrayList<Note>();
    private List<Note> selectedNotes = new ArrayList<Note>();
    private List<Note> clipboard = new ArrayList<Note>();
    private Geometry selecting, selected;
    private Material matSelecting, matSelected;
    private Vector3f startPos, endPos;
    
    // METHODS ////////////////////////////////////
    
    // constructor
    public Selector(AssetManager assetManager, List<Track> tracks, ColorRGBA colorSelecting, ColorRGBA colorSelected, float selectedPadding,
            float borderSize, float selectorZ) {
        this.assetManager = assetManager;
        ourTracks = tracks;        
        myColorSelecting = colorSelecting;
        myColorSelected = colorSelected;
        this.selectedPadding = selectedPadding;
        this.borderSize = borderSize;
        this.selectorZ = selectorZ;
        
        // init materials...
        matSelecting = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matSelecting.setColor("Color", colorSelecting);
        matSelecting.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        matSelected = new Material(this.assetManager, "Materials/ShapeShader.j3md");
        matSelected.setColor("Color", colorSelected);
        ColorRGBA borderColor = ColorRGBA.LightGray;
        borderColor.a = 0.5f;
        matSelected.setColor("BorderColor", borderColor);
        matSelected.setFloat("BorderSize", borderSize);
        matSelected.setFloat("Curve", 2f);
        matSelected.setFloat("RadiusW", 1f);
        matSelected.setFloat("RadiusH", 1f);
        matSelected.setFloat("Height", heightSelected);
        matSelected.setFloat("Width", widthSelected);
        
        matSelected.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        // init selected geometry
        Quad q = new Quad(10, 10);
        selected = new Geometry("selectedQuad", q);
        selected.setMaterial(matSelected);
        selected.center();
    }
    
    public void resetBorder() {
        ColorRGBA borderColor = ColorRGBA.LightGray;
        borderColor.a = 0.5f;
        matSelected.setColor("BorderColor", borderColor);
        matSelected.setFloat("BorderSize", borderSize); 
    }
    public void hoverBorder() {
        ColorRGBA borderColor = ColorRGBA.White;
        borderColor.a = 1f;
        matSelected.setColor("BorderColor", borderColor);
        matSelected.setFloat("BorderSize", borderSize * 1.2f);
    }

    // creates our selecting quad...
    public void createSelecting(Vector3f current3d) {       
        Quad selectingQuad = new Quad(0f, 0f);
        selecting = new Geometry("selectingQuad", selectingQuad);
        selecting.center();
        selecting.setMaterial(matSelecting);
        // add to the node...
        startPos = current3d.clone();
        selecting.setLocalTranslation(current3d);
        attachChild(selecting);
    }
    
    // (these features are not yet fully implemented)
    // copy currently selected notes to clipboard
    public void copySelectedNotes() {
        clipboard.clear();
        for (int i = 0; i < selectedNotes.size(); i++) {
            Note newNote = selectedNotes.get(i).cloneNote();
            newNote.getLocalTranslation().setX(selectedNotes.get(i).getWorldTranslation().x - selected.getLocalTranslation().x - selectedPadding - (newNote.getWidth()/2f));
            newNote.getLocalTranslation().setY(selectedNotes.get(i).getWorldTranslation().y - selected.getLocalTranslation().y - selectedPadding + (newNote.getHeight()/2f)
                    - (heightSelected - selectedPadding*2f));
            clipboard.add(newNote);
        }
    }
    public void cutSelectedNotes() {
        clipboard.clear();
        for (int i = 0; i < selectedNotes.size(); i++) {
            Note newNote = selectedNotes.get(i).cloneNote();
            newNote.getLocalTranslation().setX(selectedNotes.get(i).getWorldTranslation().x - selected.getLocalTranslation().x - selectedPadding - (newNote.getWidth()/2f));
            newNote.getLocalTranslation().setY(selectedNotes.get(i).getWorldTranslation().y - selected.getLocalTranslation().y - selectedPadding + (newNote.getHeight()/2f)
                    - (heightSelected - selectedPadding*2f));
            clipboard.add(newNote);
            selectedNotes.get(i).deleteMe();
        }
        selectingNotes.clear();
        selectedNotes.clear();
        updateSelectedGeo(false);
    }
    
    // looks through the tracks and determines which notes are selected
    // and change the colors of those notes to show that they are selected
    public void updateSelectedNotes() {
        Note currentNote = null;
        boolean yPass = false;
        boolean xPass = false;
        for (int i = 0; i < ourTracks.size(); i++) {
            for (int j = 0; j < ourTracks.get(i).getNotes().size(); j++) {
                // is the note selected?
                yPass = false;
                xPass = false;
                currentNote = ourTracks.get(i).getNotes().get(j);
                float noteTop = (currentNote.getHeight()/2f) + currentNote.getWorldTranslation().y;
                float noteBottom = (-currentNote.getHeight()/2f) + currentNote.getWorldTranslation().y;
                float noteRight = (currentNote.getWidth()/2f) + currentNote.getWorldTranslation().x;
                float noteLeft = (-currentNote.getWidth()/2f) + currentNote.getWorldTranslation().x;
                float selectingTop = Math.max(startPos.y, endPos.y);
                float selectingBottom = Math.min(startPos.y, endPos.y);
                float selectingRight = Math.max(startPos.x, endPos.x);
                float selectingLeft = Math.min(startPos.x, endPos.x);
                if ((noteTop < selectingTop && noteTop > selectingBottom) || (noteBottom < selectingTop && noteBottom > selectingBottom)
                        || (noteTop > selectingTop && noteBottom < selectingBottom))
                    yPass = true;
                if ((noteLeft < selectingRight && noteLeft > selectingLeft) || (noteRight < selectingRight && noteRight > selectingLeft)
                        || (noteLeft < selectingLeft && noteRight > selectingRight))
                    xPass = true;
                if (yPass && xPass) {
                    if (!selectingNotes.contains(currentNote))
                        selectingNotes.add(currentNote);
                    currentNote.setBorderSize(0f);
                    currentNote.brighten(4f);
                } else {
                    if (selectingNotes.contains(currentNote))
                        selectingNotes.remove(currentNote);
                    currentNote.resetBorder();
                    currentNote.resetColor();
                }
            }
        }
    }
    
    // resizes the selecting geometry...
    public void updateSize(Vector3f newEndPos) {
        endPos = newEndPos.clone();
        widthSelecting = Math.abs(endPos.x-startPos.x);
        heightSelecting = Math.abs(endPos.y-startPos.y);
        Quad selectingQuad = new Quad(widthSelecting, heightSelecting);
        selecting.setMesh(selectingQuad);
        selecting.center();
        float newPosX = Math.min(endPos.x, startPos.x);
        float newPosY = Math.min(endPos.y, startPos.y);
        selecting.setLocalTranslation(newPosX, newPosY, newEndPos.z);
        
        // update our selected notes...
        updateSelectedNotes();
    }
    
    // deselects all notes... duh
    private void deselectAllNotes() {
        for (int i = 0; i < selectedNotes.size(); i++) {
            selectedNotes.get(i).setSelected(false);
            selectedNotes.get(i).resetBorder();
            selectedNotes.get(i).resetColor();
        }
        selectedNotes.clear();
    }
    
    // creates or updates the selected geo based on our selected notes list
    public void updateSelectedGeo(boolean addToSelection) {
        float top = 0f;
        float bottom = 0f;
        float left = 0f;
        float right = 0f;
        Note currentNote = null;
        // since our code below works through the selectingNotes list only,
        // we have to add our currently selected notes back to that list
        // if addToSelection is true...
        if (addToSelection) {
            for (int i = 0; i < selectedNotes.size(); i++) {
                selectingNotes.add(selectedNotes.get(i));
            }
        } else {
            deselectAllNotes();
        }
        if (!selectingNotes.isEmpty()) {
            for (int i = 0; i < selectingNotes.size(); i++) {
                currentNote = selectingNotes.get(i);
                if (!selectedNotes.contains(currentNote)) {
                    selectedNotes.add(currentNote);
                    currentNote.setSelected(true);
                }
                // is this the first note?
                if (i == 0) {
                    top = currentNote.getWorldTranslation().y + currentNote.getHeight()/2f + selectedPadding;
                    bottom = currentNote.getWorldTranslation().y + -currentNote.getHeight()/2f + -selectedPadding;
                    left = currentNote.getWorldTranslation().x + -currentNote.getWidth()/2f + -selectedPadding;
                    right = currentNote.getWorldTranslation().x + currentNote.getWidth()/2f + selectedPadding;
                } else {
                    top = Math.max(top, (currentNote.getWorldTranslation().y + currentNote.getHeight()/2f + selectedPadding));
                    bottom = Math.min(bottom, (currentNote.getWorldTranslation().y + -currentNote.getHeight()/2f + -selectedPadding));
                    left = Math.min(left, (currentNote.getWorldTranslation().x + -currentNote.getWidth()/2f + -selectedPadding));
                    right = Math.max(right, (currentNote.getWorldTranslation().x + currentNote.getWidth()/2f + selectedPadding));
                }
                // reset the notes color / border
                currentNote.resetBorder();
                currentNote.resetColor();
            }
            // we should now know the size to create the box...
            widthSelected = right - left;
            heightSelected = top - bottom;
            Quad quad = new Quad(widthSelected, heightSelected);
            selected.setMesh(quad);
            selected.center();
            matSelected.setFloat("Height", heightSelected);
            matSelected.setFloat("Width", widthSelected);
            // position the new geometry...
            // based on average of our borders...
            selected.setLocalTranslation(((right+left)/2f) - (widthSelected/2f), ((top+bottom)/2f) - (heightSelected/2f), selectorZ);
            if (!hasChild(selected)) // attach it if it isn't already
                attachChild(selected);
        } else { // destroy the selected geo...
            if (hasChild(selected))
                detachChild(selected);
        }
    }
    
    // move the selection, including all the notes...
    public boolean moveSelected(Vector3f newPos) {
        Vector2f diff2d;
        if (!selected.getLocalTranslation().equals(newPos)) {
            diff2d = new Vector2f(selected.getLocalTranslation().x, selected.getLocalTranslation().y).subtract(new Vector2f(newPos.x, newPos.y));
            // go through and change all the notes' positions
            for (int i = 0; i < selectedNotes.size(); i++) {
                selectedNotes.get(i).setLocalTranslation(selectedNotes.get(i).getLocalTranslation().x - diff2d.x,
                        selectedNotes.get(i).getLocalTranslation().y - diff2d.y,
                        selectedNotes.get(i).getLocalTranslation().z);
            }
            // and now we do the same for the selected geo itself...
            selected.setLocalTranslation(selected.getLocalTranslation().x - diff2d.x,
                    selected.getLocalTranslation().y - diff2d.y,
                    selected.getLocalTranslation().z);
            return true;
        } else {
            return false;
        }
    }
    
    // delete selected notes...
    public void deleteSelected() {
        for (int i = 0; i < selectedNotes.size(); i++) {
            // remove the notes from the track...
            selectedNotes.get(i).deleteMe();
        }
        selectingNotes.clear();
        selectedNotes.clear();
        updateSelectedGeo(false);
    }
    
    // remove our selecting quad...
    public void destroySelecting(boolean addToSelection) {
        if (hasChild(selecting)) {
            detachChild(selecting);
        }
        updateSelectedGeo(addToSelection);
    }
    
    // gets and sets...
    public Geometry getSelected() {
        return selected;
    }
    public float getWidthSelected() {
        return widthSelected;
    }
    public float getHeightSelected() {
        return heightSelected;
    }
    public float getPadding() {
        return selectedPadding;
    }
    public List<Note> getSelectedNotes() {
        return selectedNotes;
    }
    public List<Note> getSelectingNotes() {
        return selectingNotes;
    }
    public List<Note> getClipboardNotes() {
        return clipboard;
    }
    
}
