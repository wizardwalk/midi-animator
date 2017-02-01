/*
 * A track stores all the notes in a given track...
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author SeanTheBest
 */
public class Track extends Node {
    
    private ArrayList<Note> myNotes = new ArrayList<Note>(); // where we store all the notes in this track
    private ArrayList<Geometry> connectingLines = new ArrayList<Geometry>();
    private ColorRGBA myColor = ColorRGBA.Green;
    private ControlFloat myRed, myBlue, myGreen;
    private Float colorChangeAmount = 0.01f;
    private MusicGrid musicGrid;
    private AssetManager assetManager;
    private float zLayer = 1; // z pos of track...
    //private VSTPlugins ourPlugins;
    private int pluginNum = 0;
    private int midiChannel = 0; // 0 - 15 (16 MIDI tracks)
    private int myIndex = 0;
    private int soundState = 0; // is it solo=2, muted=1?
    private boolean velControlToggle = false;
    private MIDISynth midiSynth;
    
    // note style vars
    private NoteStyle myStyle;
    private boolean useGradient;
    
    public Track(AssetManager assetManager, MIDISynth midiSynth, int pluginNum, int midiChannel, MusicGrid grid, ColorRGBA myColor, 
            int index, NoteStyle newStyle, boolean useGradient) {
        this.assetManager = assetManager;
        this.myColor = myColor;
        this.musicGrid = grid;
        this.midiSynth = midiSynth;
        this.pluginNum = pluginNum;
        this.midiChannel = midiChannel;
        this.useGradient = useGradient;
        myIndex = index;
        myStyle = newStyle;
        
        myColor.a = 0.5f;
        
        // init color controls
        myRed = new ControlFloat(myColor.r, 0.01f, 1f);
        myBlue = new ControlFloat(myColor.b, 0.01f, 1f);
        myGreen = new ControlFloat(myColor.g, 0.01f, 1f);        
    }
    
    public void deleteNote(Note note) {
        myNotes.remove(note);
        detachChild(note);
    }
    
    public void setVelControl(boolean set) {
        velControlToggle = set;
        for (int i = 0; i < myNotes.size(); i++) {
            Geometry vControl = myNotes.get(i).getVelControl();
            if (velControlToggle && !myNotes.get(i).hasChild(vControl))
                myNotes.get(i).attachChild(vControl);
            if (!velControlToggle && myNotes.get(i).hasChild(vControl))
                myNotes.get(i).detachChild(vControl);
        }
    }
    
    // sets all the velocities after the passed in note
    // to the same velocity as the passed in note...
    public void setVelocitiesAfterNote(Note noteToUse) {
        int velocity = noteToUse.getVel();
        for (int i = 0; i < myNotes.size(); i++) {
            if (myNotes.get(i).getLocalTranslation().x >= noteToUse.getLocalTranslation().x) {
                myNotes.get(i).setVelocity(velocity);
            }
        }
    }
    
    public Note addNote(float length, float height, Vector3f pos, int midiChannel, int midiProgram, int velocity) {
        Note newNote;
        // if length is given to us as a unit of whole notes
        // (such that a whole note = 1, quarter note = 0.25 etc)
        // we must now calculate what its length would be in world units
        // based on the sized of the music grid...
        float noteWidth = length * musicGrid.getWidthPerWholeNote();
        float noteHeight = height * musicGrid.getLineHeight();
        float curveWidth;
        if (myStyle.proportionalSide)
            curveWidth = (noteHeight / noteWidth) / 2; // to make them equal...
        else
            curveWidth = myStyle.curveWidth;
        myColor.a = 1f;
        ColorRGBA myBorderColor = myColor.mult(1.5f);
        myColor.a = 0.5f;
        newNote = new Note(assetManager, this, noteHeight, noteWidth, myStyle.curve, myStyle.curveHeight, curveWidth, myStyle.borderWidth,
                myColor, myBorderColor, false, myStyle, midiChannel, midiProgram, velocity, useGradient); 
        // move the note into position... this assumes the grid's bottom left is at 0, 0
        newNote.setLocalTranslation((pos.x + (noteWidth/2)), pos.y, zLayer);
        // attach the note to this node
        myNotes.add(newNote);
        attachChild(newNote);
        if (soundState == 1) // if we're muted, the new note should be muted too...
            muteNotes(true);
        if (velControlToggle)
            newNote.attachChild(newNote.getVelControl());
        return newNote;
    }
    
    public void showConnectingLines(boolean show) {
        // clear current lines...
        // detach all current connectingLines from node...
        for (int i = 0; i < connectingLines.size(); i++) {
            if (hasChild(connectingLines.get(i)))
                detachChild(connectingLines.get(i));
        }
        connectingLines.clear();
        
        if (show) {            
            // first, sort our notes...
            Collections.sort(myNotes);
            // now create our lines based on where each note begins...
            for (int i = 1; i < myNotes.size(); i++) {
                float posX1 = myNotes.get(i-1).getLocalTranslation().x - myNotes.get(i-1).getWidth()/2f;
                float posX2 = myNotes.get(i).getLocalTranslation().x - myNotes.get(i).getWidth()/2f;
                // we only want to create a line if the distance between two notes is not too great...
                if (posX2 - posX1 < musicGrid.getWidthPerWholeNote()) {
                    Line newLine = new Line(new Vector3f(posX1, myNotes.get(i-1).getLocalTranslation().y, 0f), new Vector3f(posX2, myNotes.get(i).getLocalTranslation().y, 0f));
                    newLine.setLineWidth(2f);
                    Geometry newGeo = new Geometry("connectLine", newLine);
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", myColor);
                    newGeo.setMaterial(mat);
                    attachChild(newGeo);
                    connectingLines.add(newGeo);
                }
            }
        }
    }
    
    /**
     * Turns off all notes
     */
    public void stopAllNotes() {
        for (int i = 0; i < myNotes.size(); i++) {
            if (myNotes.get(i).isOn()) {
                // turn it off...
                myNotes.get(i).turnOff();
            }
        }
    }
    
    public int getNoteMIDIValue(Note note) {
        return Math.round(note.getLocalTranslation().y / musicGrid.getLineHeight()) + 21;
    }
    
    /**
     * Turns notes on and off based on the position of the playLine 
     * @param playPos The X position of the play line
     */
    public void playNotes(float playPosX) {
        // go through all our notes...
        // turn on notes that should be turned on
        // and turn off notes that should be turned off
        float endX = 0.0f; // cut off notes 0.01 before their actual end...?
        for (int i = 0; i < myNotes.size(); i++) {
            myNotes.get(i).updatePlayPos(playPosX);
            float notePosX = myNotes.get(i).getLocalTranslation().x - (myNotes.get(i).getWidth()/2f);
            if (notePosX <= playPosX && playPosX < (notePosX + myNotes.get(i).getWidth() - endX)) { // the note should be on
                if (myNotes.get(i).isOff()) {
                    // turn it on!
                    myNotes.get(i).turnOn(playPosX);
                    // send midi message, midi notes start at 21
                    int noteValue = getNoteMIDIValue(myNotes.get(i));
                    midiSynth.playMidiNote(myNotes.get(i).getMidiChannel(), myNotes.get(i).getMidiProgram(), noteValue, myNotes.get(i).getVel());
                }
            } else if (myNotes.get(i).isOn()) { // the note should be off
                // turn it off...
                myNotes.get(i).turnOff();
                // send midi message...
                int noteValue = getNoteMIDIValue(myNotes.get(i));
                midiSynth.stopMidiNote(myNotes.get(i).getMidiChannel(), noteValue);
            }
        }
    }
    
    public void resetAllNotes() {
        midiSynth.stopAllNotes();
        for (int i = 0; i < myNotes.size(); i++) {
            myNotes.get(i).resetOff();
        }
    }
    
    // returns values of notes playing on a currentX
    public List<Integer> getNoteValuesOnX(float currentX, List<Integer> noteValues) {
        for (int i = 0; i < myNotes.size(); i++) {
            if (!myNotes.get(i).isMuted()) {
                float noteStart = myNotes.get(i).getWorldTranslation().x - (myNotes.get(i).getWidth()/2f);
                float noteEnd = myNotes.get(i).getWorldTranslation().x + (myNotes.get(i).getWidth()/2f);
                if (currentX >= noteStart && currentX < noteEnd) {
                    noteValues.add(getNoteMIDIValue(myNotes.get(i)));
                }
            }
        }      
        return noteValues;
    }
    
    // updates the track's color based on passed in color...
    public void updateMyColor(ColorRGBA myNewColor) {
        ColorRGBA newColor = myNewColor.clone();
        ColorRGBA newBorderColor = newColor.mult(1.5f);
        myColor = newColor;
        myColor.a = 0.5f;
        // update the color of all the notes in this track...
        for (int i = 0; i < myNotes.size(); i++) {
            myNotes.get(i).setNewColors(newColor, newBorderColor);
        }
    }
    
    // mute or unmute all notes
    public void muteNotes(boolean mute) {
        for (int i = 0; i < myNotes.size(); i++) {
            myNotes.get(i).setMute(mute);
        }
    }
    
    // gets and sets...
    public List<Note> getNotes() {
        return myNotes;
    }
    public ColorRGBA getColor() {
        return myColor;
    }
    public float getZLayer() {
        return zLayer;
    }
    public void setZLayer(float layer) {
        zLayer = layer;
    }
    public int getPluginNum() {
        return pluginNum;
    }
    public void setPluginNum(int newNum) {
        pluginNum = newNum;
    }
    public int getMidiChannel() {
        return midiChannel;
    }
    public void setMidiChannel(int newChannel) {
        midiChannel = newChannel;
    }
    public float getColorChangeAmount() {
        return colorChangeAmount;
    }
    public ControlFloat getRedControl() {
        return myRed;
    }
    public ControlFloat getBlueControl() {
        return myBlue;
    }
    public ControlFloat getGreenControl() {
        return myGreen;
    }
    public int getIndex() {
        return myIndex;
    }
    public int nextSoundState() {
        int newState;
        switch (soundState) {
            case 0: newState = 1; break;
            case 1: newState = 2; break;
            case 2: newState = 0; break;
            default: newState = 0; break;
        }
        soundState = newState;
        return soundState;
    }
    public int getSoundState() {
        return soundState;
    }
}
