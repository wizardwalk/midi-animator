/*
 * Stores info about a track from a MIDI file...
 */
package mygame;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SeanTheBest
 */
public class MIDITrack {
    
    public int trackNumber = 0;
    public int midiChannel = 0;
    public List<MIDINote> notes = new ArrayList<MIDINote>();
    
    public void addNote(int channel, int program, int value, int velocity, float startPos) {
        MIDINote newNote = new MIDINote();
        newNote.channel = channel;
        newNote.program = program;
        newNote.value = value;
        newNote.velocity = velocity;
        newNote.startPos = startPos;
        notes.add(newNote);
    }
    
    public boolean endNote(int value, float endPos) {
        // find the last note that was added with this value...
        for (int i = notes.size()-1; i >= 0; i--) {
            if (notes.get(i).value == value) {
                notes.get(i).duration = endPos - notes.get(i).startPos;
                return true;
            }
        }
        return false;
    }
    
}
