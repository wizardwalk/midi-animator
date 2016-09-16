/*
 * A measure stores information about a musical measure... duh!
 */
package mygame;

/**
 *
 * @author SeanTheBest
 */
public class Measure {
    
    public int timeSigTop = 4;     // top of time signature
    public int timeSigBottom = 4;  // bottom of time signature
    public int smallestUnit = 16; // the smallest unit is a 16th note
    public int index = 0;
    
    // returns how many whole notes are in a measure...
    public float getWholeNoteValue() {
        return ((float) timeSigTop / (float) timeSigBottom);
    }
    
}
