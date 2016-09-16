/*
 * Our music grid renders our measures with
 * lines in a certain style
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SeanTheBest
 */
public class MusicGrid extends Node {
    
    // VARS ///////////////////////////////////////////////////////
    private List<Measure> measures = new ArrayList<Measure>();
    private List<MeasureControl> measureControls = new ArrayList<MeasureControl>(); // buttons for each measure
    private List<TempoControl> tempoControls = new ArrayList<TempoControl>(); // our tempos...
    private float measureControlSpacing = 0f;
    private List<Line> vLines = new ArrayList<Line>(); // vertical measure lines
    private Node hLinesNode = new Node();
    private float lineWidth;
    private AssetManager assetManager;
    private BitmapFont guiFont;
    private Node guiNode;
    private Geometry deleteGeo;
    private float tempoControlSize, tempoControlHeight;
    
    // our cursor
    private ColorRGBA cursorColor = ColorRGBA.Gray;
    private Geometry cursor; // our cursor is used for copy/pasting and stuff
    private float cursorSize;
    private float cursorZLayer = 0.5f;
    
    public static final int TOTALNOTES = 87; // since 0 counts as one
    
    private float height; // the height of the rendered grid in world units
    private float widthPerWholeNote; // the width of a whole note in world units
    
    // METHODS ////////////////////////////////////////////////////
    
    // constructor
    public MusicGrid(AssetManager assetManager, Node guiNode, BitmapFont guiFont, float widthPerWholeNote, float height, float lineWidth,
            boolean createDefaultTempo, boolean createDefaultChord) {
        this.widthPerWholeNote = widthPerWholeNote;
        this.height = height;
        this.lineWidth = lineWidth;
        this.assetManager = assetManager;
        this.guiFont = guiFont;
        this.guiNode = guiNode;
        measureControlSpacing = (height / TOTALNOTES) * 1.1f;
        
        // init our cursors
        cursorSize = widthPerWholeNote / 32f;
        Quad quad = new Quad(cursorSize, cursorSize);
        cursorColor.a = 0.45f;
        cursor = new Geometry("NoteQuad", quad);
        cursor.center();     
        Material cursorMat = new Material(this.assetManager, "Materials/ShapeShader.j3md");
        cursorMat.setColor("BorderColor", cursorColor);
        cursorMat.setColor("Color", cursorColor);
        cursorMat.setFloat("BorderSize", 0f);
        cursorMat.setFloat("Curve", 2f);
        cursorMat.setFloat("RadiusW", cursorSize/2f);
        cursorMat.setFloat("RadiusH", cursorSize/2f);
        cursorMat.setFloat("Height", cursorSize);
        cursorMat.setFloat("Width", cursorSize);        
        cursorMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        cursor.setMaterial(cursorMat);        
        cursor.setQueueBucket(RenderQueue.Bucket.Transparent);
        
        // create our first measure control... we should always have one more than we have measures...
        MeasureControl measureControl = new MeasureControl(assetManager, guiFont, widthPerWholeNote * 0.05f, (height/TOTALNOTES)*3, false);
        measureControls.add(measureControl);
        
        // add a default tempo control...
        tempoControlSize = widthPerWholeNote*0.2f;
        tempoControlHeight = (height/TOTALNOTES)*5.75f;
        if (createDefaultTempo) {
            TempoControl defaultTempo = new TempoControl(assetManager, guiFont, tempoControlSize, tempoControlHeight, 0.3f);
            defaultTempo.setLocalTranslation(0, height+tempoControlHeight, 0.6f);
            tempoControls.add(defaultTempo);            
        }
    }
    
    // cover which measures have been selected for deletion...
    public void updateDeleteGeo(int position, int measuresToDelete) {
        if (hasChild(deleteGeo))
            detachChild(deleteGeo);
        float posX = getPositionOnGrid(0, position+1, 0f).x;
        float width = measures.get(position).getWholeNoteValue() * widthPerWholeNote;
        for (int i = 1; i < measuresToDelete; i++) {
            if (position+i < measures.size()) {
                width += measures.get(position+i).getWholeNoteValue() * widthPerWholeNote;
            }
        }
        Quad q = new Quad(width, height);
        deleteGeo = new Geometry("deleteGeo", q);
        Material deleteMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        deleteMat.setColor("Color", new ColorRGBA(0.8f, 0.2f, 0.2f, 0.2f));
        deleteMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        deleteGeo.setMaterial(deleteMat);
        deleteGeo.setLocalTranslation(posX, 0, 5f);
        
        // attach it if it's not already
        attachChild(deleteGeo);
    }
    
    // add a measure to our measure list...
    public void addMeasure(int position, int topSig, int bottomSig, int smallestUnit, List<Track> ourTracks, Selector selector, boolean addOne) {
        // we're not going to delete any measures if we're adding them
        resetDeletions();
        
        Measure newMeasure = new Measure();
        newMeasure.smallestUnit = smallestUnit;
        newMeasure.timeSigTop = topSig;
        newMeasure.timeSigBottom = bottomSig;
        if (position < measures.size())
            measures.add(position, newMeasure);
        else
            measures.add(newMeasure);
        
        // create some measure controls to go with it...
        measureControls.get(measureControls.size()-1).showDeleteButton(true);
        MeasureControl measureControl = new MeasureControl(assetManager, guiFont, widthPerWholeNote * 0.05f, (height/TOTALNOTES)*3, true);
        measureControls.add(position, measureControl);        
        
        // now we've got to move our notes over...
        if (addOne)
            position += 1;
        float newMeasurePosX = getPositionOnGrid(0, position, 0f).x;
        float newMeasureSize = widthPerWholeNote * newMeasure.getWholeNoteValue();
        // so, if a note is equal to or greater than our newMeasurePosX,
        // then we must add to its X location our newMeasureSize...
        for (int i = 0; i < ourTracks.size(); i++) {
            for (int j = 0; j < ourTracks.get(i).getNotes().size(); j++) {
                if (ourTracks.get(i).getNotes().get(j).getLocalTranslation().x - (ourTracks.get(i).getNotes().get(j).getWidth()/2f) >= newMeasurePosX) {
                    Vector3f currentPos = ourTracks.get(i).getNotes().get(j).getLocalTranslation();
                    ourTracks.get(i).getNotes().get(j).setLocalTranslation(currentPos.x + newMeasureSize, currentPos.y, currentPos.z);
                }
            }
        }
        
        selector.updateSelectedGeo(false); // if selected notes have been shifted, we need to update the selector geo...
        
        // turn off delete button on last measure control
        measureControls.get(measureControls.size()-1).showDeleteButton(false);
    }
    
    // delete measures from our grid...
    public void deleteMeasures(int position, int measuresToDelete, Selector selector, List<Track> ourTracks) {
        System.out.println("remove " + position);
        float posX = getPositionOnGrid(0, position+1, 0f).x;
        System.out.println("posx = " + posX);
        float widthShift = measures.remove(position).getWholeNoteValue() * widthPerWholeNote;
        guiNode.detachChild(measureControls.get(position).getMeasureLabel());
        measureControls.remove(position);
        for (int i = 0; i < measuresToDelete-1; i++) {
            if (position < measures.size()) {
                widthShift += measures.remove(position).getWholeNoteValue() * widthPerWholeNote;
                guiNode.detachChild(measureControls.get(position).getMeasureLabel());
                measureControls.remove(position);
            }
        }
        System.out.println("widthx = " + widthShift);
        
        List<Note> notesToDelete = new ArrayList<Note>();
        
        // shift our notes...
        for (int i = 0; i < ourTracks.size(); i++) {
            System.out.println("notes in track = " + ourTracks.get(i).getNotes().size());
            for (int j = 0; j < ourTracks.get(i).getNotes().size(); j++) {
                float notePosX = ourTracks.get(i).getNotes().get(j).getLocalTranslation().x - (ourTracks.get(i).getNotes().get(j).getWidth()/2f);
                System.out.println("notePosX = " + notePosX);
                if (notePosX >= posX) {
                    if (notePosX < posX+widthShift) {
                        // the note must sadly be deleted... forever :-(
                        //ourTracks.get(i).detachChild(ourTracks.get(i).getNotes().get(j));
                        if (selector.getSelectedNotes().contains(ourTracks.get(i).getNotes().get(j)))
                            selector.getSelectedNotes().remove(ourTracks.get(i).getNotes().get(j));
                        if (selector.getSelectingNotes().contains(ourTracks.get(i).getNotes().get(j)))
                            selector.getSelectingNotes().remove(ourTracks.get(i).getNotes().get(j));
                        notesToDelete.add(ourTracks.get(i).getNotes().get(j));
                        System.out.println("Delete " + j);
                    } else {
                        System.out.println("Shift " + j);
                        Vector3f currentPos = ourTracks.get(i).getNotes().get(j).getLocalTranslation();
                        ourTracks.get(i).getNotes().get(j).setLocalTranslation(currentPos.x - widthShift, currentPos.y, currentPos.z);                        
                    }
                }
            }
        }
        
        // delete the notes we found must be deleted...
        // (we couldn't do this in the loop, since removing a note from the track would
        // change the size of getNotes().size...)
        while (notesToDelete.size() > 0) {
            notesToDelete.get(0).deleteMe();
            notesToDelete.remove(0);
        }
        
        selector.updateSelectedGeo(false);
        resetDeletions();
        if (cursor.getLocalTranslation().x >= posX) // don't want the cursor to be off the grid now! heh heh
            updateCursor(new Vector3f(0, 0, 0));
        createGrid();
    }
    
    // given a note value, measure value, and location within the measure, return world coordinates
    // this assumes grid bottom left is at 0, 0
    public Vector3f getPositionOnGrid(int noteValue, int measureNum, float wholeNotes) {
        float x = 0;
        // add up the widths of the preceding measures...
        for (int i = 1; i < measureNum; i++) {
            float measureWidth = widthPerWholeNote * (measures.get(i-1).getWholeNoteValue());
            x += measureWidth;
        }
        x += wholeNotes * widthPerWholeNote;
        float y = noteValue * (height / TOTALNOTES);        
        Vector3f newPosition = new Vector3f(x, y, 0);        
        return newPosition;
    }
    
    // change the smallest unit on all of our measures
    public void setSmallestUnit(int smallestUnit) {
        for (int i = 0; i < measures.size(); i++) {
            measures.get(i).smallestUnit = smallestUnit;
        }
        // and recreate our grid...
        createGrid();
    }
    
    // create our lines and add them to the node
    public void createGrid() {
        // first get rid of any nodes we already have...
        detachAllChildren();
        vLines.clear();
        
        // create lines
        // we'll create our vertical lines first
        // to do this, we have to cycle through our measure list...
        // we also need to keep track of our current xPos so that we can increment as we go...
        float currentPos = 0f; // we start at 0
        float measureControlPosX;
        for (int i = 0; i < measures.size(); i++) {
            measureControlPosX = currentPos; // this will be used to position our measure control buttons
            
            // our measureSizeGoal is where our measure SHOULD end...
            float measureSizeGoal = currentPos + (widthPerWholeNote * ((float)measures.get(i).timeSigTop / (float)measures.get(i).timeSigBottom));
            
            // how many lines we have to create in this measure is based
            // on our time signature and our smallest unit value
            // we want to draw the measure regardless, so we need to check to see if our
            // number of lines would be less than 2...
            int numLines = (measures.get(i).smallestUnit * measures.get(i).timeSigTop) / measures.get(i).timeSigBottom;
            if (numLines < 2) {
                // in this case, we only draw a start line; nothing more...
                Vector3f pos1 = new Vector3f(measureControlPosX, 0, 0);
                Vector3f pos2 = new Vector3f(measureControlPosX, height, 0);
                Line newLine = new Line(pos1, pos2);
                Geometry newGeo = new Geometry("vLine", newLine);
                newLine.setLineWidth(lineWidth * 3f);
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.setColor("Color", new ColorRGBA(0.1f, 0.4f, 0.7f, 1f));
                newGeo.setMaterial(mat);
                // add it to both our node and our list
                attachChild(newGeo);
                vLines.add(newLine);                
                // increment our currentPos...
                currentPos += (widthPerWholeNote * ((float)measures.get(i).timeSigTop / (float)measures.get(i).timeSigBottom));
            }
            else {
                int measureSize = numLines;
                // now we must create each of these lines and position them appropriately
                for (int j = 0; j < numLines; j++) {
                    // first we must figure out the begin and end positions of our new line...
                    float xPos = currentPos;
                    Vector3f pos1 = new Vector3f(xPos, 0, 0);
                    Vector3f pos2 = new Vector3f(xPos, height, 0);
                    Line newLine = new Line(pos1, pos2);
                    Geometry newGeo = new Geometry("vLine", newLine);
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

                    // start vLine
                    if (j == 0) {
                        newLine.setLineWidth(lineWidth * 3f);
                        mat.setColor("Color", new ColorRGBA(0.1f, 0.4f, 0.7f, 1f));
                    }
                    // half beat
                    else if ((measureSize/2 & 1) == 0 && j == measureSize/2 && (measures.get(i).timeSigTop & 1) == 0) {
                        newLine.setLineWidth(lineWidth * 2f);
                        mat.setColor("Color", new ColorRGBA(0.6f, 0.2f, 0.2f, 1f));
                    }
                    // beat line
                    else if ((j & (measureSize/measures.get(i).timeSigTop)-1) == 0) {
                        newLine.setLineWidth(lineWidth);
                    }
                    // all others (regular)
                    else {
                        newLine.setLineWidth(lineWidth);
                        mat.setColor("Color", new ColorRGBA(0.3f, 0.3f, 0.3f, 1f));
                    }

                    newGeo.setMaterial(mat);
                    // add it to both our node and our list
                    attachChild(newGeo);
                    vLines.add(newLine);

                    // increment our currentPos...
                    currentPos += (widthPerWholeNote / measures.get(i).smallestUnit);
                    // if we're at the end, but the measure is still supposed to be longer
                    // (such as if we're drawing a 14/32 time sig measure with smallest units of 8)
                    // we need to add an offset so that the measure is still however long it should be...
                    if (j+1 >= numLines && currentPos < measureSizeGoal)
                        currentPos = measureSizeGoal;
                }
            }
            
            // we must remember to add our measure controls!!
            // position gui controls first...
            measures.get(i).index = i;
            measureControls.get(i).setLocalTranslation(measureControlPosX, height + measureControlSpacing, 1f);
            attachChild(measureControls.get(i));
            // always set the corresponding measure when redrawing the grid...
            measureControls.get(i).setMeasure(measures.get(i));
            
            // finally, add our cursors...
            attachChild(cursor);
        }
        // add our end line...
        Line endLine = new Line(new Vector3f(currentPos, 0, 0), new Vector3f(currentPos, height, 0));
        Geometry geo = new Geometry("vLine", endLine);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        endLine.setLineWidth(lineWidth * 3f);
        mat.setColor("Color", new ColorRGBA(0.1f, 0.4f, 0.7f, 1f));
        geo.setMaterial(mat);
        attachChild(geo);
        vLines.add(endLine);
        // and our last measurecontrol for the endline...
        measureControlPosX = currentPos;
        measureControls.get(measures.size()).setLocalTranslation(measureControlPosX, height + measureControlSpacing, 1f);
        attachChild(measureControls.get(measures.size()));
        // these controls have no corresponding measure...
        measureControls.get(measures.size()).setMeasure(null);
        // end vertical line creation
        
        drawHLines();
        attachChild(hLinesNode);
        
        // attach our tempoControls...
        for (int i = 0; i < tempoControls.size(); i++) {
            attachChild(tempoControls.get(i));
        }
    }
    
    // draws the horizontal lines for our music grid...
    public void drawHLines() {
        // remove all hLines currently stored...
        hLinesNode.detachAllChildren();
        
        // horizontal lines now...
        float gridWidth = getGridWidth();
        // define colors we'll use...
        ColorRGBA lineColor = new ColorRGBA(0.3f, 0.3f, 0.3f, 0.3f);
        
        // now we draw lines for each chord...
        if (measures.size() > 0) { // we only have to draw these if we actually have measures...
            float startX = 0;
            float endX = gridWidth;
            for (int j = 0; j < TOTALNOTES+1; j++) {
                float yPos = j * (height/TOTALNOTES);
                Vector3f pos1 = new Vector3f(startX, yPos, -1);
                Vector3f pos2 = new Vector3f(endX, yPos, -1);
                Line newLine = new Line(pos1, pos2);
                //newLine.setLineWidth(lineWidth);
                Geometry newGeo = new Geometry("hLine", newLine);
                Material mat2 = new Material(assetManager, "Materials/ShapeShader.j3md");
                newLine.setLineWidth(lineWidth);
                mat2.setColor("Color", lineColor);
                mat2.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);                
                newGeo.setMaterial(mat2);
                hLinesNode.attachChild(newGeo);
            }
        }
    }
    
    // updates the position of the ghost cursor based on where the mouse is in 3d space
    public void updateCursor(Vector3f current3d) {
        float newPosX = getClosestGridX(current3d.x, 0f) - (cursorSize/2f);
        float newPosY = getClosestGridY(current3d.y) - (cursorSize/2f);
        cursor.setLocalTranslation(newPosX, newPosY, cursorZLayer);
    }
    
    // finds the closest tempo given a position.x
    public float getClosestTempo(float posX, float currentTempo) {
        TempoControl useThisTempo = null;
        float length = 0f;
        boolean nextFound = false;
        for (int i = 0; i < tempoControls.size(); i++) {
            if (tempoControls.get(i).getLocalTranslation().x <= posX) {
                float newLength = posX - tempoControls.get(i).getLocalTranslation().x;
                if (!nextFound) {
                    length = newLength;
                    useThisTempo = tempoControls.get(i);
                    nextFound = true;
                }
                else {
                    if (newLength < length) {
                        length = newLength;
                        useThisTempo = tempoControls.get(i);
                    }                        
                }
            }
        }
        if (useThisTempo == null)
            return currentTempo;
        else {
            if (useThisTempo.getConnected() && useThisTempo.getNextTempo() != null) {
                // we must interpolate between two tempos...
                float distance = useThisTempo.getNextTempo().getLocalTranslation().x - useThisTempo.getLocalTranslation().x;
                float tempoChange = useThisTempo.getNextTempo().getTempoControl().getValue() - useThisTempo.getTempoControl().getValue();
                float tempo = useThisTempo.getTempoControl().getValue() + ((length/distance) * tempoChange);
                return tempo;
            } else
                return useThisTempo.getTempoControl().getValue();
        }
    }
    
    // sets the tempo label positions...
    public void updateTempoLabelPos(Camera cam, boolean toggle) {
        for (int i = 0; i < tempoControls.size(); i++) {
            BitmapText text = tempoControls.get(i).getTempoText();
            if (toggle) {
                Vector3f screenPos = cam.getScreenCoordinates(tempoControls.get(i).getLocalTranslation());
                if (!guiNode.hasChild(text)) {
                    guiNode.attachChild(text);
                }
                screenPos.setX(screenPos.x - (text.getLineWidth()/2f));
                screenPos.setY(screenPos.y + (text.getLineHeight()/2f));
                text.setLocalTranslation(screenPos.x, screenPos.y, 2f);                
            } else {
                if (guiNode.hasChild(text))
                    guiNode.detachChild(text);
            }
        }
    }
    
    // move the tempo control to the closest x position...
    public void moveTempoControl(float currentX, TempoControl tempoControl) {
        Vector3f current = tempoControl.getLocalTranslation().clone();
        tempoControl.setLocalTranslation(getClosestGridX(currentX, 0f), current.y, current.z);
        if (current.x != tempoControl.getLocalTranslation().x)
            updateTempoConnections();
    }
    
    // find the next tempo after this tempo...
    // it would be more efficient to just sort the tempos according to their x coordinates, but
    // assuming you don't have a bazillion tempo controls, this will work fine, inefficient as it is
    public TempoControl findNextTempo(TempoControl firstTempo) {
        TempoControl nextTempo = null; 
        float length = 0f;
        boolean nextFound = false;
        for (int i = 0; i < tempoControls.size(); i++) {
            if (tempoControls.get(i).getLocalTranslation().x > firstTempo.getLocalTranslation().x) {
                float newLength = tempoControls.get(i).getLocalTranslation().x - firstTempo.getLocalTranslation().x;
                if (!nextFound) {
                    length = newLength;
                    nextTempo = tempoControls.get(i);
                    nextFound = true;
                }
                else {
                    if (newLength < length) {
                        length = newLength;
                        nextTempo = tempoControls.get(i);
                    }                        
                }
            }
        }        
        return nextTempo;
    }
    
    public void updateTempoConnections() {
        for (int i = 0; i < tempoControls.size(); i++) {
            if (tempoControls.get(i).getConnected()) {
                tempoControls.get(i).setNextTempo(findNextTempo(tempoControls.get(i)));
            } else {
                tempoControls.get(i).setNextTempo(null);
            }
            tempoControls.get(i).updateTempoConnectionLine();
        }
    }
    
    // toggle the tempo control's connection...
    public void toggleTempoConnection(TempoControl tempoToConnect) {
        tempoToConnect.setConnected(!tempoToConnect.getConnected());
        updateTempoConnections();
    }
    
    // create a new tempo control...
    public void createTempoControl(int tempo) {
        TempoControl newTempo = new TempoControl(assetManager, guiFont, tempoControlSize, tempoControlHeight, 0.3f);
        newTempo.getTempoControl().setValue(tempo);
        newTempo.getTempoText().setText(Integer.toString(tempo));
        newTempo.setLocalTranslation(cursor.getLocalTranslation().x+(cursorSize/2f), height+tempoControlHeight, 0.6f);
        tempoControls.add(newTempo);
        attachChild(newTempo);
        updateTempoConnections();
    }
    public void createTempoControl(int tempo, float posX, boolean connected) {
        TempoControl newTempo = new TempoControl(assetManager, guiFont, tempoControlSize, tempoControlHeight, 0.3f);
        newTempo.getTempoControl().setValue(tempo);
        newTempo.getTempoText().setText(Integer.toString(tempo));
        newTempo.setLocalTranslation(posX, height+tempoControlHeight, 0.6f);
        newTempo.setConnected(connected);
        tempoControls.add(newTempo);
        attachChild(newTempo);
        updateTempoConnections();
    }
    
    // delete a tempo
    public void deleteTempo(TempoControl tempoToDelete) {
        if (tempoControls.contains(tempoToDelete))
            tempoControls.remove(tempoToDelete);
        if (hasChild(tempoToDelete))
            detachChild(tempoToDelete);
        if (guiNode.hasChild(tempoToDelete.getTempoText()))
            guiNode.detachChild(tempoToDelete.getTempoText());
        updateTempoConnections();
    }
    
    // sets the measure label positions...
    public void updateMeasureLabelPos(Camera cam, boolean toggle) {
        for (int i = 0; i < measureControls.size(); i++) {
            if (toggle) {
                // along with our measure controls, we add our measure labels...
                if (!guiNode.hasChild(measureControls.get(i).getMeasureLabel()))
                    guiNode.attachChild(measureControls.get(i).getMeasureLabel());
                Vector3f screenPos = cam.getScreenCoordinates(measureControls.get(i).getLocalTranslation());
                if (screenPos.y > cam.getHeight())
                    screenPos.setY(cam.getHeight());
                if (screenPos.x < 0) {
                    if (cam.getScreenCoordinates(new Vector3f(measureControls.get(i).getLocalTranslation().x+getMeasureWidth(i), measureControls.get(i).getLocalTranslation().y, measureControls.get(i).getLocalTranslation().z)).x 
                            >= 0) {
                        screenPos.setX(0);
                    }
                }
                measureControls.get(i).getMeasureLabel().setLocalTranslation(screenPos.x + 5, screenPos.y, 2f);                
            } else {
                if (guiNode.hasChild(measureControls.get(i).getMeasureLabel()))
                    guiNode.detachChild(measureControls.get(i).getMeasureLabel());
            }
        }        
    }
    
    private float getMeasureWidth(int i) {
        if (i < measures.size())
            return widthPerWholeNote * measures.get(i).getWholeNoteValue();
        else
            return getGridWidth();
    }
    
    // returns the X position of a given note width...
    private float getLastPossibleXPosition(float noteWidth) {
        float newX;
        
        float currentWidth = 0;
        int currentMeasure = measures.size()-1;
        for (int i = measures.size()-1; i >= 0; i--) {
            // will the noteWidth fit in this measure?
            float measureWidth = (widthPerWholeNote / measures.get(i).smallestUnit)
                    * ((measures.get(i).smallestUnit / measures.get(i).timeSigBottom) * measures.get(i).timeSigTop);
            currentWidth += measureWidth;
            if (noteWidth <= currentWidth) {
                currentMeasure = i;
                break;
            }
        }
        
        // now, what's the last smallestUnit position noteWidth will fit at?
        double unitsPerNote = Math.ceil(noteWidth / (widthPerWholeNote / measures.get(currentMeasure).smallestUnit));
        newX = getGridWidth() - ((widthPerWholeNote / measures.get(currentMeasure).smallestUnit) * (float) unitsPerNote);
        
        return newX;
    }
    
    // returns closest grid Y position from given y position...
    public float getClosestGridY(float currentY) {
        float newY = 0f;
        float heightPerNote = height / TOTALNOTES;        
        if (currentY > 0f) {
            if (currentY > height)
                newY = height;
            else {
                newY = Math.round(currentY / heightPerNote) * heightPerNote;
            }
        }        
        return newY;
    }
    
    // returns closest grid Y position from given y position...
    // this was probably a really stupid inefficient way to do this...
    // should've done it note-oriented... but oh well, it works...
    public float getClosestGridY(float currentY, float offset, float selectionHeight) {
        float newY = selectionHeight / 2f;
        float heightPerNote = height / TOTALNOTES;        
        if (currentY - (selectionHeight/2f) + offset > 0f) {
            if (currentY + (selectionHeight/2f) + offset > height) {
                newY = height - (selectionHeight/2f);
            }
            else {
                newY = Math.round(currentY / heightPerNote) * heightPerNote + offset;
            }
        }        
        return newY;
    }
    
    // returns the closest grid x position from a given x position...
    // this assumes vLines are in order... which they should be...
    public float getClosestGridX(float currentX, float noteWidth) {
        float newX;
        float possibleNewX = 0f;
        float gridWidth = getGridWidth();
        
        if (currentX > 0f) {
            if (currentX > gridWidth) {
                // last possible X pos...
                possibleNewX = getLastPossibleXPosition(noteWidth);
            } else {
                for (int i = 0; i < vLines.size(); i++) {
                    if (currentX >= vLines.get(i).getEnd().x) {
                        if (i+1 < vLines.size()) {
                            if (currentX < vLines.get(i+1).getEnd().x) { // current X is between these two lines... which one is closer?
                                if (Math.abs(currentX - vLines.get(i).getEnd().x) < Math.abs(currentX - vLines.get(i+1).getEnd().x)) {
                                    // this means currentX is closer to the first line...
                                    possibleNewX = vLines.get(i).getEnd().x;
                                } else {
                                    possibleNewX = vLines.get(i+1).getEnd().x;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (possibleNewX + noteWidth <= gridWidth)
            newX = possibleNewX;
        else
            newX = getLastPossibleXPosition(noteWidth);
        
        return newX;
    }
    
    // calculate the grid width...
    public float getGridWidth() {
        float gridWidth = 0f;
        for (int i = 0; i < measures.size(); i++) {
            gridWidth += widthPerWholeNote * measures.get(i).getWholeNoteValue();
        }        
        return gridWidth;
    }
    
    // resets all deletion stages in our measure controls...
    public void resetDeletions() {
        if (hasChild(deleteGeo))
            detachChild(deleteGeo);
        for (int i = 0; i < measureControls.size(); i++) {
            measureControls.get(i).resetDeletion();
        }
    }
    
    // GETS AND SETS /////////////////////////////////

    public float getWidthPerWholeNote() {
        return widthPerWholeNote;
    }
    public void setWidthPerWholeNote(float wpwn) {
        widthPerWholeNote = wpwn;
    }
    public float getLineHeight() {
        return height / TOTALNOTES;
    }
    public float getHeight() {
        return height;
    }
    public void setHeight(float newHeight) {
        height = newHeight;
    }
    public int getMeasureNum() {
        return measures.size();
    }
    public List<Measure> getMeasures() {
        return measures;
    }
    public List<MeasureControl> getMeasureControls() {
        return measureControls;
    }
    public List<TempoControl> getTempoControls() {
        return tempoControls;
    }
    public Vector3f getCursorPos() {
        Vector3f cursorPos = cursor.getLocalTranslation().clone();
        cursorPos.setX(cursorPos.x+(cursorSize/2f));
        cursorPos.setY(cursorPos.y+(cursorSize/2f));
        return cursorPos;
    }
    
}
