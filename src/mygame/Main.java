/**
 * MIDI Animator
 * programmed by Sean Patrick Hannifin
 * last updated 1 February 2017
 */

package mygame;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Line;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MIDI Animator
 */
public class Main extends SimpleApplication {
    
    ////// VARIABLES ////////////////////////////////////////////////////////////////
    
    private String midiFileName = "assets/MIDIFiles/Hannifin-AWinterWish.mid"; // the midi file to load
    private float musicGridWidthPerWholeNote = 8f; // This will determine horizontal spacing / stretch
    private boolean useGradients = true; // true or false
    
    private BitmapText controlText; // text for control decisions...
    
    private float frustumSize = 1;
    private float zoom = 0;
    private int currentState = 0;
    private int currentNoteEditState = 0;
    private int lastState = 0;
    private int controlSelected = 0; // what button (control) we are hovering...
    private Vector2f lastMousePos; // stored for camera panning
    private boolean ctrlPressed = false;
    private boolean shiftPressed = false;
    private boolean altPressed = false;
    private boolean rMousePressed = false;
    private float noteHeight = 1.5f; // height of notes...
    
    // playing
    private float playZ;
    private float playLineWidth = 2f;
    private float playLineOvershoot = 2.5f;
    private ColorRGBA playLineColor = ColorRGBA.Yellow;
    private Vector3f playBottomPos;
    private Vector3f playTopPos;
    private Line playLine;
    private Geometry playGeo;
    private Material playMat;
    private float playTempo = 120; // bpm in quarter notes...
    private float lastTempo;
    private float playTime = 0f; // how long has it been playing?
    private float playStartPos = -0.2f;
    private float playCurrentX = 0f;
    private boolean playLineToggle = true;
    
    // variables for storing measures and our musicGrid            
    private MusicGrid musicGrid;
    private float musicGridHeight = 100f;
    private int defaultMeasureTop = 4;
    private int defaultMeasureBottom = 4;
    private int defaultSmallestUnit = 16;
    private boolean gridToggle = true;
    // when auto-zooming to height, the padding is multiplied by the gridHeight to obtain the zoom
    private float zoomHeightPadding = 1.05f;
    private boolean measureLabelToggle = false;
    private int lastTempoCreated = 120;
    
    // for MIDI playback
    private MIDISynth midiSynth = new MIDISynth();
    
    // our music tracks...
    private List<Track> myTracks = new ArrayList<Track>();
    private boolean velControlToggle = false;
    
    private int pixelsToSide = 5; // how many pixels far away should the mouse be from a note's edge to resize?
    private Note noteHovered = null;
    private boolean selectionHovered = false;
    private MeasureControl mControlHovered = null;
    private TempoControl tempoControlHovered = null;
    private float noteStart = 0f;
    private float noteEnd = 0f;
    private Vector2f clickedNoteDistance; // for moving notes, saves the distance between mouse and note position
    
    // our selector!! for selecting notes...
    private Selector selector;
    private float selectorOffset = 0f;
    private float selectorZ = 9f; // z level of the selector...
    
    // controls
    private ControlInteger measuresToAdd;
    private ControlInteger measuresToDelete;
    private ControlInteger measureToAddTop;
    private ControlInteger measureToAddBottom;
    private ControlInteger smallestUnits;
        
    // screenshot files will appear as "Main#.png" in main folder
    private ScreenshotAppState screenShot = new ScreenshotAppState("");
    
    // MIDI files to open...
    private MIDIFile midiFile; // initiated in appInit...
    
    // note styles...
    private class NoteStyles {
        public final NoteStyle DEFAULT = new NoteStyle(2f, 0.5f, 0.5f, 0.2f, false, false);
        public final NoteStyle DIAMOND = new NoteStyle(1f, 0.5f, 0.5f, 0.2f, false, false);
        public final NoteStyle ROUNDEND = new NoteStyle(2f, 0.5f, 0.5f, 0.2f, true, false);
    }
    private NoteStyles myNoteStyles = new NoteStyles();
    
    private class EditState {
        public final static int NORMAL = 0; // normal view state...
        public final static int DRAGVIEW = 1; // dragging the view around with right mouse...
        public final static int DRAGNOTE = 2; // dragging a note around after clicking on it...
        public final static int DRAGSELECTION = 3; // dragging the selection around...
        public final static int RESIZE_NOTE = 4; // for resizing note left or right
        public final static int SELECTING = 5; // for selecting notes...
        public final static int PLAYING = 6;
        public final static int PLAYING_SCREENSHOT = 7;
        public final static int CONTROL_SETTING = 8; // setting a control
        public final static int DRAG_TEMPO_NODE = 9;
    };
    private class NoteEditState {
        public final static int DRAG = 0;
        public final static int RESIZE_R = 1;
        public final static int RESIZE_L = 2;
    };
    private class Controls { 
        public final static int NULL = 0;
        public final static int INSERT_MEASURE = 1;
        public final static int INSERT_MEASURES = 2; // insert a measure an arbitrary number of times...
        public final static int INSERT_MEASURE_OPTIONS = 3; // insert one measure with specified time sig...
        public final static int DELETE_MEASURE = 4; // delete a measure (or measures?)
    }
    
    ///// FUNCTIONS //////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {        
        Main app = new Main();       
        app.start();
    }

    @Override
    public void simpleInitApp() {
        
        setPauseOnLostFocus(false);
        
        viewPort.getQueue().setGeometryComparator(RenderQueue.Bucket.Transparent, new CustomComparator());
        
        // init controls
        measuresToDelete = new ControlInteger(1, 1, 100);
        measuresToAdd = new ControlInteger(1, 1, 100);
        measureToAddTop = new ControlInteger(4, 1, 50);
        measureToAddBottom = new ControlInteger(4, 1, 32);
        smallestUnits = new ControlInteger(defaultSmallestUnit, 4, 64);
        
        // init control text...
        controlText = new BitmapText(guiFont, false);
        controlText.setSize(guiFont.getCharSet().getRenderedSize());
        controlText.setColor(ColorRGBA.Yellow);
        guiNode.attachChild(controlText);
        
        // init selector
        selector = new Selector(assetManager, myTracks, new ColorRGBA(0.9f, 0.9f, 0.9f, 0.2f), new ColorRGBA(0.3f, 0.5f, 0.8f, 0.25f), 0.75f, 0.2f, selectorZ); 
        selector.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(selector);
        
        // init playLine
        playZ = 15f;
        playBottomPos = new Vector3f(playStartPos, -playLineOvershoot, playZ);
        playTopPos = new Vector3f(playStartPos, musicGridHeight+playLineOvershoot, playZ); // musicGrid.getHeight() should also work...
        playLine = new Line(playBottomPos, playTopPos);
        playLine.setLineWidth(playLineWidth);
        playGeo = new Geometry("playLine", playLine);
        playMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        playMat.setColor("Color", playLineColor);
        playGeo.setMaterial(playMat);
        
        // change the background color...
        viewPort.setBackgroundColor(ColorRGBA.Black);
        
        //this.flyCam.setEnabled(false);
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        //this.setDisplayFps(false);
        this.setDisplayStatView(false);
        this.setDisplayFps(false);

        // create white material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        
        // set up parallel cam (for 2D viewing)
        cam.setParallelProjection(true);
        float aspect = (float) cam.getWidth() / cam.getHeight();
        frustumSize = (float) Math.exp(zoom);
        cam.setFrustum(-1000, 1000, -aspect * frustumSize, aspect * frustumSize, frustumSize, -frustumSize);
        
        stateManager.attach(screenShot);
        
        // input mapping        
        inputManager.addListener(analogListener, "Size-", "Size+", "scrollWheelUp", "scrollWheelDown");
        inputManager.addListener(analogListener, "mouseMove", "mouseLeft", "mouseRight", "mouseUp", "mouseDown");
        inputManager.addListener(actionListener, "rightMousePressed", "leftMousePressed", "autoZoomHeight", "toggleGrid", "gotoStart");
        inputManager.addListener(actionListener, "altPressed");
        inputManager.addListener(actionListener, "ctrlPressed", "delPressed", "spacePressed", "shiftPressed", "measureLabelToggle", "insertTempo", "playLineToggle");
        inputManager.addListener(actionListener, "velControlToggle", "suggestGC");
        inputManager.addListener(actionListener, "plus", "minus");
        inputManager.addMapping("spacePressed", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("ctrlPressed", new KeyTrigger(KeyInput.KEY_RCONTROL), new KeyTrigger(KeyInput.KEY_LCONTROL));
        inputManager.addMapping("shiftPressed", new KeyTrigger(KeyInput.KEY_RSHIFT), new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("delPressed", new KeyTrigger(KeyInput.KEY_DELETE));
        inputManager.addMapping("altPressed", new KeyTrigger(KeyInput.KEY_RMENU), new KeyTrigger(KeyInput.KEY_LMENU));
        inputManager.addMapping("toggleGrid", new KeyTrigger(KeyInput.KEY_G));
        inputManager.addMapping("autoZoomHeight", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("gotoStart", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("Size+", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Size-", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("insertTempo", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addMapping("measureLabelToggle", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("velControlToggle", new KeyTrigger(KeyInput.KEY_V));
        inputManager.addMapping("playLineToggle", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("suggestGC", new KeyTrigger(KeyInput.KEY_END));
        inputManager.addMapping("plus", new KeyTrigger(KeyInput.KEY_ADD));
        inputManager.addMapping("minus", new KeyTrigger(KeyInput.KEY_SUBTRACT));
        inputManager.addMapping("rightMousePressed", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("leftMousePressed", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("scrollWheelUp", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("scrollWheelDown", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("mouseLeft", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("mouseRight", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("mouseUp", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("mouseDown", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        
        // create our musicGrid...
        musicGridHeight = 100f;
        musicGrid = new MusicGrid(assetManager, guiNode, guiFont, musicGridWidthPerWholeNote, musicGridHeight, 1.5f, false, false);
        musicGrid.addMeasure(0, defaultMeasureTop, defaultMeasureBottom, defaultSmallestUnit, myTracks, selector, false);
        musicGrid.createGrid();
        musicGrid.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(musicGrid);  
        
        // calculate new zoom level
        zoom = (float) Math.log((musicGridHeight/2f) * zoomHeightPadding);
        // set cam to new zoom level...
        frustumSize = (float) Math.exp(zoom);
        aspect = (float) cam.getWidth() / cam.getHeight();
        cam.setFrustum(-1000, 1000, -aspect * frustumSize, aspect * frustumSize, frustumSize, -frustumSize);
        // move the cam to the appropriate y location...
        float newYPos = musicGridHeight/2f;
        cam.setLocation(new Vector3f(cam.getLocation().x, newYPos, cam.getLocation().z));       
        
        try {            
            // load a midi file...
            int qShift = 0; // if we don't want the MIDI to start right at the beginning... this will shift it a quarter note or something
            midiFile = new MIDIFile(midiFileName, qShift);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        createTracksFromMIDI();
    }
    
    // creates the tracks and notes from a midi file...
    private void createTracksFromMIDI() {
        // first let's set up our music grid...
        // what's our time sig?
        int timeSigTop = midiFile.getTimeSigTop();
        int timeSigBottom = midiFile.getTimeSigBottom();
        // how many measures will we need?
        // multiply by reciprocal...
        float measures = midiFile.getSizeInW() * ((float)timeSigBottom / (float)timeSigTop);
        System.out.println("Measures = " + measures + " = " + midiFile.getSizeInW() + " * (" + timeSigTop + " / " + timeSigBottom + ")");
        // create the measures...
        musicGrid.deleteMeasures(lastState, lastTempoCreated, selector, myTracks);
        for (float d = 0f; d < measures; d += 1f) {
            musicGrid.addMeasure(0, timeSigTop, timeSigBottom, defaultSmallestUnit, myTracks, selector, true);
        }
        musicGrid.createGrid();
        
        // add the tempos...
        for (int h = 0; h < midiFile.getMyTempos().size(); h++) {
            float tempoPos = (midiFile.getMyTempos().get(h).pos / midiFile.getPPQ()) * musicGrid.getWidthPerWholeNote() * 0.25f;
            int tempoVal = Math.round(midiFile.getMyTempos().get(h).tempo);
            musicGrid.createTempoControl(tempoVal, tempoPos, false);
        }
        
        float clock = 0f; // round and round it goes... used to cycle through colors below
        for (int i = midiFile.getMyTracks().size()-1; i >= 0; i--) {
            if (midiFile.getMyTracks().get(i).notes.isEmpty()) {
                continue; // continue to the next iteration... this track is useless!
            }
            clock += 0.371f;
            if (clock > 1f)
                clock -= 1f;
            Color myColor = Color.getHSBColor(clock, 0.85f, 0.9f);
            ColorRGBA trackColor = new ColorRGBA(myColor.getRed()/255f, myColor.getGreen()/255f, myColor.getBlue()/255f, myColor.getAlpha()/255f);
            NoteStyle newStyle = myNoteStyles.DEFAULT;
            if (i % 3 == 1)
                newStyle = myNoteStyles.DIAMOND;
            if (i % 3 == 2)
                newStyle = myNoteStyles.ROUNDEND;
            Track newTrack = new Track(assetManager, midiSynth, 0, 0, musicGrid, trackColor, 0, newStyle, useGradients);
            
            // for loop to add all the notes in the given track...
            for (int j = 0; j < midiFile.getMyTracks().get(i).notes.size(); j++) {
                // calculate note's length and position
                // here, we multiply by 0.25 because ppq is ticks per quarter note...
                float noteLength = (midiFile.getMyTracks().get(i).notes.get(j).duration / midiFile.getPPQ()) * 0.25f;
                float notePosX = (midiFile.getMyTracks().get(i).notes.get(j).startPos / midiFile.getPPQ()) * musicGrid.getWidthPerWholeNote() * 0.25f;
                // here we subtract 21 from the note value, because midi note values start at 21; we want 21 to be shifted to 0...
                float notePosY = ((midiFile.getMyTracks().get(i).notes.get(j).value - 21) * musicGrid.getLineHeight());
                Vector3f notePos = new Vector3f(notePosX, notePosY, 0f);
                int mChannel = midiFile.getMyTracks().get(i).notes.get(j).channel;
                int mProgram = midiFile.getMyTracks().get(i).notes.get(j).program;
                int velocity = midiFile.getMyTracks().get(i).notes.get(j).velocity;
                newTrack.addNote(noteLength, noteHeight, notePos, mChannel, mProgram, velocity);
            }
            
            newTrack.showConnectingLines(newStyle.connectingLines);
            
            myTracks.add(newTrack);
            rootNode.attachChild(newTrack);              
        }
    }
    
    // what to do when buttons are pressed...
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("rightMousePressed") && currentState != EditState.PLAYING) {
                rMousePressed = isPressed;
                if (isPressed) {
                    if (mControlHovered != null) { // we clicked on a measure control...
                        if (controlSelected == Controls.INSERT_MEASURE) { // we have clicked our "insert measure" button
                            currentState = EditState.CONTROL_SETTING;
                            controlSelected = Controls.INSERT_MEASURE_OPTIONS; // because we clicked with the right, we want this sort of inserting measures
                            // update our control text... 
                            String text = Integer.toString(measureToAddTop.getValue()) + " / " + Integer.toString(measureToAddBottom.getValue());
                            controlText.setText(text);
                            controlText.setLocalTranslation((cam.getWidth()/2f)-(controlText.getLineWidth()/2f), controlText.getLineHeight(), 0);
                        }
                        else if (controlSelected == Controls.DELETE_MEASURE) {
                            if (mControlHovered.getDeleteConfirm() != 0) {
                                musicGrid.resetDeletions();
                            }
                        }
                    }
                    else {
                        Vector2f click2d = inputManager.getCursorPosition();
                        lastState = currentState;
                        currentState = EditState.DRAGVIEW;
                        lastMousePos = click2d.clone();             
                    }                    
                } else {
                    if (currentState == EditState.CONTROL_SETTING) { // we are releasing the mouse button after clicking on measurecontrol
                        if (controlSelected == Controls.INSERT_MEASURE_OPTIONS) {
                            // here we insert our new measure
                            int measurePos = musicGrid.getMeasureNum();
                            int measureTop = measureToAddTop.getValue();
                            int measureBottom = measureToAddBottom.getValue();
                            int smallestUnit = musicGrid.getMeasures().get(measurePos-1).smallestUnit;
                            if (mControlHovered.getMeasure() != null) {
                                measurePos = mControlHovered.getMeasure().index;
                                smallestUnit = mControlHovered.getMeasure().smallestUnit;
                            }
                            musicGrid.addMeasure(measurePos, measureTop, measureBottom, smallestUnit, myTracks, selector, true);
                            // recreate the music grid...
                            musicGrid.createGrid();
                            // control text back to blank...
                            controlText.setText("");
                            // go back to normal state...
                            currentState = EditState.NORMAL;
                            // reset control selected...
                            controlSelected = Controls.NULL;
                        }
                    } else if (currentState != EditState.CONTROL_SETTING)
                        currentState = lastState;
                }
            }
            else if (name.equals("leftMousePressed") && currentState != EditState.PLAYING) {
                if (isPressed && currentState == EditState.NORMAL) {
                    // get our mouse position data...
                    Vector2f mouse2d = inputManager.getCursorPosition();
                    // if the mouse is hovering over a note, do this...
                    if (noteHovered != null) {
                        if (currentNoteEditState == NoteEditState.DRAG) {
                            currentState = EditState.DRAGNOTE;                          
                        }
                        else if (currentNoteEditState == NoteEditState.RESIZE_R || currentNoteEditState == NoteEditState.RESIZE_L) {
                            currentState = EditState.RESIZE_NOTE;                            
                            noteStart = noteHovered.getLocalTranslation().x - noteHovered.getWidth()/2f;
                            noteEnd = noteHovered.getLocalTranslation().x + noteHovered.getWidth()/2f;
                        }
                        // note's 2d position - mouse 2d position (in world)                        
                        Vector3f current3d = cam.getWorldCoordinates(new Vector2f(mouse2d.x, mouse2d.y), 0f).clone();
                        Vector2f notePos = new Vector2f(noteHovered.getLocalTranslation().x - (noteHovered.getWidth()/2f), noteHovered.getLocalTranslation().y);
                        clickedNoteDistance = new Vector2f(current3d.x, current3d.y).subtract(notePos);
                    } else if (selectionHovered) { // if the mouse is hovering over the selection...
                        currentState = EditState.DRAGSELECTION;
                        float yPos = selector.getSelected().getLocalTranslation().y + (selector.getHeightSelected()/2f);
                        Vector3f current3d = cam.getWorldCoordinates(new Vector2f(mouse2d.x, mouse2d.y), 0f).clone();
                        Vector2f selectionPos = new Vector2f(selector.getSelected().getLocalTranslation().x+selector.getPadding(), yPos);
                        clickedNoteDistance = new Vector2f(current3d.x, current3d.y).subtract(selectionPos);
                        // selections can be y-centered between note lines, so we have to consider an offset...
                        // in this case, the remainder
                        selectorOffset = yPos - musicGrid.getClosestGridY(yPos);
                    } else if (mControlHovered != null && !rMousePressed) { // if the mouse is hovering over measure control
                        if (controlSelected == Controls.INSERT_MEASURE) { // we have clicked our "insert measure" button
                            currentState = EditState.CONTROL_SETTING;
                            controlSelected = Controls.INSERT_MEASURES; // because we clicked with the left, we want this sort of inserting measures
                            // update our control text...
                            controlText.setText(Integer.toString(measuresToAdd.getValue()));
                            controlText.setLocalTranslation((cam.getWidth()/2f)-(controlText.getLineWidth()/2f), controlText.getLineHeight(), 0);
                        } else if (controlSelected == Controls.DELETE_MEASURE) {
                            if (mControlHovered.getDeleteConfirm() == 0) {
                                // reset all other deletions... 
                                measuresToDelete.reset();
                                musicGrid.resetDeletions();
                                currentState = EditState.CONTROL_SETTING;
                                // update our control text...
                                controlText.setText(Integer.toString(measuresToDelete.getValue()));
                                controlText.setLocalTranslation((cam.getWidth()/2f)-(controlText.getLineWidth()/2f), controlText.getLineHeight(), 0); 
                                musicGrid.updateDeleteGeo(mControlHovered.getMeasure().index, measuresToDelete.getValue());
                            } else { // time to delete our measures!!
                                musicGrid.deleteMeasures(mControlHovered.getMeasure().index, measuresToDelete.getValue(), selector, myTracks);
                            }
                        }                      
                    } else if (tempoControlHovered != null) { // we have clicked on a tempo control node
                        if (shiftPressed) {
                            musicGrid.toggleTempoConnection(tempoControlHovered);
                        } else
                            currentState = EditState.DRAG_TEMPO_NODE;
                    } else { // if the mouse is NOT hovering over a note, selection, tempo, or control we'll go to our selector...
                        Vector3f current3d = cam.getWorldCoordinates(new Vector2f(mouse2d.x, mouse2d.y), 0f).clone();
                        musicGrid.updateCursor(current3d);
                        current3d.setZ(10f);
                        currentState = EditState.SELECTING;
                        selector.createSelecting(current3d);
                    }
                } else {
                    if (!ctrlPressed || currentState == EditState.DRAGNOTE || currentState == EditState.RESIZE_NOTE // the mouse is NOT pressed...
                            || currentState == EditState.SELECTING || currentState == EditState.DRAGSELECTION 
                            || currentState == EditState.CONTROL_SETTING || currentState == EditState.DRAG_TEMPO_NODE) { // if ctrl isn't pressed, go back to normal state...
                        // if ctrl is pressed, we want to add to the selection, thus ctrlPressed is passed...
                        selector.destroySelecting(ctrlPressed); // destroy any selection quad that may be visible
                        if (currentState == EditState.CONTROL_SETTING) {
                            if (controlSelected == Controls.INSERT_MEASURES) {
                                // we want to add our measures now...
                                int measurePos = musicGrid.getMeasureNum();
                                int measureTop, measureBottom, smallestUnit;
                                if (measurePos > 0) {
                                    measureTop = musicGrid.getMeasures().get(measurePos-1).timeSigTop;
                                    measureBottom = musicGrid.getMeasures().get(measurePos-1).timeSigBottom;
                                    smallestUnit = musicGrid.getMeasures().get(measurePos-1).smallestUnit;                                    
                                } else {
                                    measureTop = defaultMeasureTop;
                                    measureBottom = defaultMeasureBottom;
                                    smallestUnit = defaultSmallestUnit;
                                }
                                if (mControlHovered.getMeasure() != null) {
                                    measurePos = mControlHovered.getMeasure().index+1;
                                    measureTop = mControlHovered.getMeasure().timeSigTop;
                                    measureBottom = mControlHovered.getMeasure().timeSigBottom;
                                    smallestUnit = mControlHovered.getMeasure().smallestUnit;
                                } else {
                                    measurePos += 1;
                                }
                                for (int i = 0; i < measuresToAdd.getValue(); i++) {
                                    musicGrid.addMeasure(measurePos, measureTop, measureBottom, smallestUnit, myTracks, selector, false);
                                }
                                // recreate the music grid...
                                musicGrid.createGrid();
                                // reset our measure nums...
                                measuresToAdd.reset();
                                // control text back to blank...
                                controlText.setText("");
                                // reset control selected...
                                controlSelected = Controls.NULL;                                
                            }
                            else if (controlSelected == Controls.DELETE_MEASURE) {
                                if (mControlHovered.getDeleteConfirm() == 0) { // delete step one...
                                    // we move to step 2 of the deletion process...
                                    mControlHovered.initiateMeasureDeletion();
                                    // control text back to blank...
                                    controlText.setText("");
                                    // reset control selected...
                                    controlSelected = Controls.NULL;                                     
                                }
                            }
                        }
                        if (controlSelected != Controls.INSERT_MEASURE_OPTIONS) // this would imply the right mouse is clicked...
                            currentState = EditState.NORMAL;
                    }
                }
            }
            else if (name.equals("delPressed") && isPressed) {
                if (tempoControlHovered != null) { // if we're hovering over a tempo control, delete that...
                    musicGrid.deleteTempo(tempoControlHovered);
                }
                else
                    selector.deleteSelected();
            }
            else if (name.equals("suggestGC") && isPressed) {
                System.gc(); // suggest garbage collection...
            }
            else if (name.equals("autoZoomHeight") && isPressed) {
                // calculate new zoom level
                zoom = (float) Math.log((musicGridHeight/2f) * zoomHeightPadding);
                // set cam to new zoom level...
                frustumSize = (float) Math.exp(zoom);
                float aspect = (float) cam.getWidth() / cam.getHeight();
                cam.setFrustum(-1000, 1000, -aspect * frustumSize, aspect * frustumSize, frustumSize, -frustumSize);
                System.out.println("frustum = " + frustumSize);
                // move the cam to the appropriate y location...
                float newYPos = musicGridHeight/2f;
                cam.setLocation(new Vector3f(cam.getLocation().x, newYPos, cam.getLocation().z));
            }
            else if (name.equals("gotoStart") && isPressed) {
                cam.setLocation(new Vector3f(0f, cam.getLocation().y, cam.getLocation().z));
            }
            else if (name.equals("toggleGrid") && isPressed) {
                if (gridToggle)
                    rootNode.detachChild(musicGrid);
                else
                    rootNode.attachChild(musicGrid);
                gridToggle = !gridToggle;
            }
            else if (name.equals("plus") && ctrlPressed && isPressed) {
                // increase our smallest units...
                smallestUnits.timesTwo();
                musicGrid.setSmallestUnit(smallestUnits.getValue());
            }
            else if (name.equals("minus") && ctrlPressed && isPressed) {
                // decrease our smallest units...
                smallestUnits.divTwo();
                musicGrid.setSmallestUnit(smallestUnits.getValue());
            }
            else if (name.equals("measureLabelToggle") && isPressed) {
                measureLabelToggle = !measureLabelToggle;
            }
            else if (name.equals("insertTempo") && isPressed) {
                if (ctrlPressed && currentState == EditState.NORMAL) {  // insert tempo
                    // insert the new tempo mark...
                    musicGrid.createTempoControl(lastTempoCreated);
                }
            }
            else if (name.equals("velControlToggle") && isPressed && !ctrlPressed) {
                // toggle velocity control visibility for all notes...
                velControlToggle = !velControlToggle;
                for (int i = 0; i < myTracks.size(); i++) {
                    myTracks.get(i).setVelControl(velControlToggle);
                }
            }
            else if (name.equals("playLineToggle") && isPressed) {
                playLineToggle = !playLineToggle;
                if (playLineToggle && !rootNode.hasChild(playGeo))
                    rootNode.attachChild(playGeo);
                else if (!playLineToggle && rootNode.hasChild(playGeo))
                    rootNode.detachChild(playGeo);
            }
            else if (name.equals("ctrlPressed")) {
                ctrlPressed = isPressed;
            }
            else if (name.equals("shiftPressed")) {
                shiftPressed = isPressed;
            }
            else if (name.equals("altPressed")) {
                altPressed = isPressed;
            }
            else if (name.equals("spacePressed")) {
                if (isPressed) {
                    // if we're not playing, play!
                    if (currentState == EditState.NORMAL) {
                        lastTempo = playTempo;
                        playCurrentX = musicGrid.getCursorPos().x;
                        // reset position
                        playLine = new Line(playBottomPos, playTopPos);
                        playLine.setLineWidth(playLineWidth);
                        playGeo.setMesh(playLine);
                        playTime = playStartPos;
                        playGeo.setLocalTranslation(playCurrentX, 0, 0);
                        if (playLineToggle)
                            rootNode.attachChild(playGeo);
                        if (ctrlPressed)
                            currentState = EditState.PLAYING_SCREENSHOT;
                        else {
                            currentState = EditState.PLAYING;
                        }
                    } else if (currentState == EditState.PLAYING || currentState == EditState.PLAYING_SCREENSHOT) { // otherwise stop playing
                        currentState = EditState.NORMAL;
                        rootNode.detachChild(playGeo);
                        for (int i = 0; i < myTracks.size(); i++) {
                            myTracks.get(i).resetAllNotes(); // this will also stop all midi notes
                            myTracks.get(i).stopAllNotes();
                        }
                    }
                }
            }
        }
    }; 
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            // scroll wheel...
            if (name.equals("scrollWheelUp") || name.equals("scrollWheelDown")) {
                if (currentState == EditState.CONTROL_SETTING) {
                    if (controlSelected == Controls.INSERT_MEASURES) { // we use the scroll wheel to decide how many measures to add
                        if (name.equals("scrollWheelUp"))
                            measuresToAdd.add(1);
                        else
                            measuresToAdd.subtract(1);
                        // update our control text...
                        controlText.setText(Integer.toString(measuresToAdd.getValue()));
                    } else if (controlSelected == Controls.INSERT_MEASURE_OPTIONS) { // we're using the mouse to set the measure options... (measureTop)
                        if (name.equals("scrollWheelUp"))
                            measureToAddBottom.timesTwo();
                        else
                            measureToAddBottom.divTwo();
                        // update our control text...
                        String text = Integer.toString(measureToAddTop.getValue()) + " / " + Integer.toString(measureToAddBottom.getValue());
                        controlText.setText(text);                        
                    } else if (controlSelected == Controls.DELETE_MEASURE) {
                        if (name.equals("scrollWheelUp"))
                            measuresToDelete.add(1);
                        else
                            measuresToDelete.subtract(1);
                        musicGrid.updateDeleteGeo(mControlHovered.getMeasure().index, measuresToDelete.getValue());
                        controlText.setText(Integer.toString(measuresToDelete.getValue()));
                    }
                    // update the text location to center it...
                    controlText.setLocalTranslation((cam.getWidth()/2f)-(controlText.getLineWidth()/2f), controlText.getLineHeight(), 0);
                }
                if (currentState == EditState.NORMAL) {
                    if (noteHovered != null && currentNoteEditState == NoteEditState.DRAG) {
                        int toAdd = 1;
                        if (!ctrlPressed && shiftPressed && !altPressed) {
                            if (name.equals("scrollWheelUp"))
                                noteHovered.getVelControlVar().add(toAdd);
                            else
                                noteHovered.getVelControlVar().subtract(toAdd);
                            noteHovered.updateVelControl();
                            controlText.setText(Integer.toString(noteHovered.getVel()));
                            controlText.setLocalTranslation((cam.getWidth()/2f)-(controlText.getLineWidth()/2f), controlText.getLineHeight(), 0);
                        }
                        if (ctrlPressed && shiftPressed && !altPressed) {
                            if (name.equals("scrollWheelUp"))
                                noteHovered.getVelControlVar().add(toAdd);
                            else
                                noteHovered.getVelControlVar().subtract(toAdd);
                            noteHovered.getTrack().setVelocitiesAfterNote(noteHovered);
                            controlText.setText(Integer.toString(noteHovered.getVel()));
                            controlText.setLocalTranslation((cam.getWidth()/2f)-(controlText.getLineWidth()/2f), controlText.getLineHeight(), 0);
                        }
                        // if only ctrl is pressed, change track hue
                        if (ctrlPressed && !shiftPressed && !altPressed) {
                            ColorRGBA currentColor = noteHovered.getTrack().getColor();
                            float[] newColorVals = Color.RGBtoHSB(Math.round(currentColor.r*255f), Math.round(currentColor.g*255f), Math.round(currentColor.b*255f), null);
                            float newHue = newColorVals[0];
                            if (name.equals("scrollWheelUp")) {                                
                                newHue += 0.01f;
                                if (newHue > 1f)
                                    newHue -= 1f;
                            } else {
                                newHue -= 0.01f;
                                if (newHue < 0f)
                                    newHue += 1f;
                            }
                            Color myColor = Color.getHSBColor(newHue, newColorVals[1], newColorVals[2]);
                            ColorRGBA newColor = new ColorRGBA(myColor.getRed()/255f, myColor.getGreen()/255f, myColor.getBlue()/255f, myColor.getAlpha()/255f);
                            noteHovered.getTrack().updateMyColor(newColor);
                        }
                        // if ctrl and alt pressed, change track saturation with scroll wheel
                        if (ctrlPressed && !shiftPressed && altPressed) {
                            ColorRGBA currentColor = noteHovered.getTrack().getColor();
                            float[] newColorVals = Color.RGBtoHSB(Math.round(currentColor.r*255f), Math.round(currentColor.g*255f), Math.round(currentColor.b*255f), null);
                            float newSaturation = newColorVals[1];
                            System.out.println("old saturation = " + newSaturation);
                            if (name.equals("scrollWheelUp")) {
                                newSaturation = Math.min(newSaturation+0.01f, 1f);
                            } else {
                                newSaturation = Math.max(newSaturation-0.01f, 0f);
                            }
                            System.out.println("new saturation = " + newSaturation);
                            Color myColor = Color.getHSBColor(newColorVals[0], newSaturation, newColorVals[2]);
                            ColorRGBA newColor = new ColorRGBA(myColor.getRed()/255f, myColor.getGreen()/255f, myColor.getBlue()/255f, myColor.getAlpha()/255f);
                            noteHovered.getTrack().updateMyColor(newColor);                            
                        }
                        // if ctrl and alt and shift pressed, change track brightness with scroll wheel
                        if (ctrlPressed && shiftPressed && altPressed) {
                            ColorRGBA currentColor = noteHovered.getTrack().getColor();
                            float[] newColorVals = Color.RGBtoHSB(Math.round(currentColor.r*255f), Math.round(currentColor.g*255f), Math.round(currentColor.b*255f), null);
                            float newBrightness = newColorVals[2];
                            System.out.println("old brightness = " + newBrightness);
                            if (name.equals("scrollWheelUp")) {
                                newBrightness = Math.min(newBrightness+0.01f, 1f);
                            } else {
                                newBrightness = Math.max(newBrightness-0.01f, 0f);
                            }
                            System.out.println("new saturation = " + newBrightness);
                            Color myColor = Color.getHSBColor(newColorVals[0], newColorVals[1], newBrightness);
                            ColorRGBA newColor = new ColorRGBA(myColor.getRed()/255f, myColor.getGreen()/255f, myColor.getBlue()/255f, myColor.getAlpha()/255f);
                            noteHovered.getTrack().updateMyColor(newColor);                            
                        }
                    }
                    else if (tempoControlHovered != null) {
                        int toAdd = 10;
                        if (shiftPressed)
                            toAdd = 1;
                        if (name.equals("scrollWheelUp"))
                            tempoControlHovered.getTempoControl().add(toAdd);
                        else
                            tempoControlHovered.getTempoControl().subtract(toAdd);
                        lastTempoCreated = tempoControlHovered.getTempoControl().getValue();
                        tempoControlHovered.getTempoText().setText(Integer.toString(lastTempoCreated));
                    }
                }
            }
            // mouse movement...
            else if (name.equals("mouseLeft") || name.equals("mouseRight")) {
                if (currentState == EditState.CONTROL_SETTING) {
                    if (controlSelected == Controls.INSERT_MEASURE_OPTIONS && ctrlPressed) { // ctrl must also be pressed
                        if (name.equals("mouseLeft"))
                            measureToAddTop.add(1);
                        else
                            measureToAddTop.subtract(1);
                        // update our control text...
                        String text = Integer.toString(measureToAddTop.getValue()) + " / " + Integer.toString(measureToAddBottom.getValue());
                        controlText.setText(text);                          
                    }
                    // update the text location to center it...
                    controlText.setLocalTranslation((cam.getWidth()/2f)-(controlText.getLineWidth()/2f), controlText.getLineHeight(), 0);
                }
            }
            else if (name.equals("Size-") && !ctrlPressed) {
                zoom += tpf * 2;
                frustumSize = (float) Math.exp(zoom);
                float aspect = (float) cam.getWidth() / cam.getHeight();
                cam.setFrustum(-1000, 1000, -aspect * frustumSize, aspect * frustumSize, frustumSize, -frustumSize);
            }
            else if (name.equals("Size+") && !ctrlPressed) {
                zoom -= tpf * 2;
                frustumSize = (float) Math.exp(zoom);
                float aspect = (float) cam.getWidth() / cam.getHeight();
                cam.setFrustum(-1000, 1000, -aspect * frustumSize, aspect * frustumSize, frustumSize, -frustumSize);
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        // update our measure label positions
        musicGrid.updateMeasureLabelPos(cam, measureLabelToggle);
        musicGrid.updateTempoLabelPos(cam, gridToggle);
            
        // move our ghost cursor...
        Vector2f mouse2d = inputManager.getCursorPosition();
        Vector3f current3d = cam.getWorldCoordinates(new Vector2f(mouse2d.x, mouse2d.y), 0f).clone();
        
        // is the mouse button is pressed, pan the camera based on the mouse's movement
        // (that is, the difference between its last location and its current location)
        if (currentState == EditState.PLAYING) {
            // find what tempo we should be playing at...
            playTempo = musicGrid.getClosestTempo(playGeo.getLocalTranslation().x, playTempo);
            if (playTempo != lastTempo) {
                lastTempo = playTempo;
                playTime = 0f;
                playCurrentX = playGeo.getLocalTranslation().x;
            }
            playTime += tpf;
            // calculate line location based on playTime...
            // we divide by 240 because bpm is divided 60 (60 s in a minute) and
            // widthPerWholeNote is divided by 4 to get width per quarter note... 60*4 = 240
            float newXPos = playCurrentX + ((playTempo*playTime*musicGrid.getWidthPerWholeNote()) / 240f);
            playGeo.setLocalTranslation(newXPos, playGeo.getLocalTranslation().y, playGeo.getLocalTranslation().z);
            cam.setLocation(new Vector3f(playGeo.getLocalTranslation().x, cam.getLocation().y, cam.getLocation().z));
            // go through our tracks and update notes...
            for (int i = 0; i < myTracks.size(); i++) {
                myTracks.get(i).playNotes(newXPos);
            }
        }
        if (currentState == EditState.PLAYING_SCREENSHOT) {
            // similar to above, but now we play at a certain speed and take screenshots on each frame...
            // we can then string together the screenshots with ffmpeg for a video that is certain to be perfect 60 fps
            // find what tempo we should be playing at...
            playTempo = musicGrid.getClosestTempo(playGeo.getLocalTranslation().x, playTempo);
            if (playTempo != lastTempo) {
                lastTempo = playTempo;
                playTime = 0f;
                playCurrentX = playGeo.getLocalTranslation().x;
            }
            playTime += 1f / 60f; // 1/60th of a second for 60 fps
            screenShot.takeScreenshot();
            // calculate line location based on playTime...
            // we divide by 240 because bpm is divided 60 (60 s in a minute) and
            // widthPerWholeNote is divided by 4 to get width per quarter note... 60*4 = 240
            float newXPos = playCurrentX + ((playTempo*playTime*musicGrid.getWidthPerWholeNote()) / 240f);
            playGeo.setLocalTranslation(newXPos, playGeo.getLocalTranslation().y, playGeo.getLocalTranslation().z);
            cam.setLocation(new Vector3f(playGeo.getLocalTranslation().x, cam.getLocation().y, cam.getLocation().z));
            // go through our tracks and update notes...
            for (int i = 0; i < myTracks.size(); i++) {
                myTracks.get(i).playNotes(newXPos);
            }
        }
        else if (currentState == EditState.NORMAL) {            
            // use collision detection to find out what the mouse is hovering over...
            CollisionResults results = new CollisionResults();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(mouse2d.x, mouse2d.y), 1f).subtractLocal(current3d).normalizeLocal();
            Ray ray = new Ray(current3d, dir);
            rootNode.collideWith(ray, results);
            if (noteHovered != null) {
                noteHovered.resetBorder();
                noteHovered.resetColor();
                if (!velControlToggle && !noteHovered.isTrackNode() && noteHovered.hasChild(noteHovered.getVelControl())) {
                    noteHovered.detachChild(noteHovered.getVelControl());
                }
                controlText.setText("");
                noteHovered = null;
            }
            if (mControlHovered != null) {
                mControlHovered.resetColors();
                mControlHovered = null;
            }
            if (tempoControlHovered != null) {
                tempoControlHovered.resetBorderColor();
                tempoControlHovered = null;
            }
            // reset the selector
            selector.resetBorder();
            selectionHovered = false;
            // change material for all resulting notes...
            for (int i = 0; i < results.size(); i++) {
                if (results.getCollision(i).getGeometry().getParent() instanceof Note && noteHovered == null) {
                    Note currentNote = (Note) results.getCollision(i).getGeometry().getParent();
                    if (!currentNote.getIsTrackNode()) {
                        // is the mouse 10 pixels away from the right edge?
                        Vector3f noteRightEdge = new Vector3f(currentNote.getLocalTranslation().x + currentNote.getWidth()/2f, currentNote.getLocalTranslation().y, currentNote.getLocalTranslation().z);
                        Vector3f noteLeftEdge = new Vector3f(currentNote.getLocalTranslation().x - currentNote.getWidth()/2f, currentNote.getLocalTranslation().y, currentNote.getLocalTranslation().z);
                        Vector3f noteRight2d = cam.getScreenCoordinates(noteRightEdge);
                        Vector3f noteLeft2d = cam.getScreenCoordinates(noteLeftEdge);
                        if (noteRight2d.x - mouse2d.x < pixelsToSide) {
                            currentNote.setBorderColor(ColorRGBA.Yellow, 1f);
                            currentNote.setBorderSize(0.25f);
                            currentNoteEditState = NoteEditState.RESIZE_R;
                        } else if (mouse2d.x - noteLeft2d.x < pixelsToSide) {
                            currentNote.setBorderColor(ColorRGBA.Yellow, 1f);
                            currentNote.setBorderSize(0.25f);
                            currentNoteEditState = NoteEditState.RESIZE_L;
                        } else {
                            currentNote.setBorderColor(ColorRGBA.White, 1f);
                            currentNote.setBorderSize(0.25f);
                            // set the velControl to visible if necessary...
                            if (shiftPressed && !currentNote.isTrackNode()) {                                
                                controlText.setText(Integer.toString(currentNote.getVel()));
                                controlText.setLocalTranslation((cam.getWidth()/2f)-(controlText.getLineWidth()/2f), controlText.getLineHeight(), 0);
                                if (!velControlToggle && !currentNote.hasChild(currentNote.getVelControl())) {
                                    currentNote.attachChild(currentNote.getVelControl());
                                }
                            }
                            currentNoteEditState = NoteEditState.DRAG;
                        }
                        noteHovered = currentNote;
                        break;                        
                    }
                } else if (results.getCollision(i).getGeometry().getParent() instanceof Selector) {
                    selectionHovered = true;
                    selector.hoverBorder();
                } else if (results.getCollision(i).getGeometry().getParent() instanceof MeasureControl) {
                    mControlHovered = (MeasureControl) results.getCollision(i).getGeometry().getParent();
                    if (results.getCollision(i).getGeometry().getName().equals("insertMeasureButton")) {
                        mControlHovered.brightenInsertMeasureButton();
                        controlSelected = Controls.INSERT_MEASURE;
                    }
                    else if (results.getCollision(i).getGeometry().getName().equals("deleteMeasureButton")) {
                        mControlHovered.brightenDeleteMeasureButton();
                        controlSelected = Controls.DELETE_MEASURE;
                    }
                } else if (results.getCollision(i).getGeometry().getParent() instanceof TempoControl) {
                    tempoControlHovered = (TempoControl) results.getCollision(i).getGeometry().getParent();
                    tempoControlHovered.setBorderColor(ColorRGBA.White);
                }
            }
        }
        // we only have to do this if a note is actually being hovered... (noteHovered)
        // moving a note around...
        else if (currentState == EditState.DRAGNOTE && noteHovered != null) {
            // we must move any hovered notes, snapping them to the grid closest to the mouse's current location...
            // we must calculate the closest grid position that we may "snap" the hovered notes to...
            float noteWidth = noteHovered.getWidth();
            float newX = musicGrid.getClosestGridX((current3d.x-clickedNoteDistance.x), noteWidth);
            float newY = musicGrid.getClosestGridY(current3d.y-clickedNoteDistance.y);
            // move note to new position...
            noteHovered.setLocalTranslation(newX + (noteWidth/2f), newY, noteHovered.getLocalTranslation().z);
            // the hovered note may be part of a selection... update our selector just in case...
            selector.updateSelectedGeo(false);
        }
        else if (currentState == EditState.DRAG_TEMPO_NODE && tempoControlHovered != null) {
            // find the closest grid X position
            musicGrid.moveTempoControl(current3d.x, tempoControlHovered);
        }
        else if (currentState == EditState.DRAGSELECTION) { // are we dragging the selection?
            // we must move any hovered notes, snapping them to the grid closest to the mouse's current location...
            // we must calculate the closest grid position that we may "snap" the hovered notes to...
            float selectionWidth = selector.getWidthSelected();
            float selectionHeight = selector.getHeightSelected();
            float newX = musicGrid.getClosestGridX((current3d.x-clickedNoteDistance.x), selectionWidth - (selector.getPadding()*2f));
            float newY = musicGrid.getClosestGridY((current3d.y-clickedNoteDistance.y), selectorOffset,
                    (selectionHeight - (selector.getPadding()*2f) - (noteHeight*musicGrid.getLineHeight()))); 
            // move note to new position...
            Vector3f newPos = new Vector3f(newX - selector.getPadding(), newY - (selectionHeight/2f), selectorZ);
            selector.moveSelected(newPos);
        }
        else if (currentState == EditState.RESIZE_NOTE) { // are we resizing a note?
            // we must move any hovered notes, snapping them to the grid closest to the mouse's current location...
            // we must calculate the closest grid position that we may "snap" the hovered note to...
            float widthDiff = noteHovered.getWidth();
            float resizeToX = musicGrid.getClosestGridX(current3d.x, 0f);
            float newX = noteHovered.getLocalTranslation().x;
            // if we're resizing to the right... changing the note's end position...
            if (currentNoteEditState == NoteEditState.RESIZE_R) {
                if (resizeToX != noteEnd && resizeToX > noteStart) {
                    noteHovered.setWidth(resizeToX - noteStart);
                    noteEnd = resizeToX;
                }
                // move note to new position...
                widthDiff -= noteHovered.getWidth();
                noteHovered.setLocalTranslation(newX - widthDiff/2f, noteHovered.getLocalTranslation().y, noteHovered.getLocalTranslation().z);                 
            } else if (currentNoteEditState == NoteEditState.RESIZE_L) { // resizing to the left... changing the note's start position
                if (resizeToX != noteStart && resizeToX < noteEnd) {
                    noteHovered.setWidth(noteEnd - resizeToX);
                    noteStart = resizeToX;
                }
                // move note to new position...
                widthDiff -= noteHovered.getWidth();
                noteHovered.setLocalTranslation(newX + widthDiff/2f, noteHovered.getLocalTranslation().y, noteHovered.getLocalTranslation().z); 
            } 
            // the note may be part of a selection... update our selector just in case...
            selector.updateSelectedGeo(false);
        }
        // dragging a selector...
        else if (currentState == EditState.SELECTING) {
            current3d.setZ(10f);
            selector.updateSize(current3d);
        }
        // dragging the view around...
        else if (currentState == EditState.DRAGVIEW) {
            Vector3f last3d = cam.getWorldCoordinates(new Vector2f(lastMousePos.x, lastMousePos.y), 0f).clone();
            if (shiftPressed) { // if shift is pressed, only move along x-axis...
                cam.setLocation(new Vector3f(cam.getLocation().subtract(current3d.subtract(last3d)).x, cam.getLocation().y, cam.getLocation().z));
            } else {
                cam.setLocation(cam.getLocation().subtract(current3d.subtract(last3d)));
            }
            lastMousePos = mouse2d.clone();
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }
    
    @Override
    public void destroy() {
        midiSynth.stopAllNotes();
        midiSynth.closeSynth();
        super.destroy();
    }
}
