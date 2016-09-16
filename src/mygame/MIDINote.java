/*
 * This class stores info about a note from a MIDI file...
 */
package mygame;

/**
 *
 * @author SeanTheBest
 */
public class MIDINote {
    
    public int velocity = 60;
    public int value = 40;
    public int channel = 0;
    public int program = 0;
    public float startPos = 0f; // in MIDI tricks
    public float duration = 10f; // in MIDI ticks
    
}
