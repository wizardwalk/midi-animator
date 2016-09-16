/*
 * Makes MIDI sounds when a note is played...
 */

package mygame;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

/** this class is for playing midi sounds...
 */
public class MIDISynth {
    
    private Synthesizer synth;
    private MidiChannel[] mChannels;

    // contructor!
    public MIDISynth() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            mChannels = synth.getChannels();
            synth.loadAllInstruments(synth.getDefaultSoundbank());
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(MIDISynth.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void playMidiNote(int channel, int program, int noteValue, int velocity) {
        mChannels[channel].programChange(program);
        mChannels[channel].noteOn(noteValue, velocity);
    }
    public void stopMidiNote(int channel, int noteValue) {
        mChannels[channel].noteOff(noteValue);
    }
    
    public void stopAllNotes() {
        for (int i = 0; i < mChannels.length; i++) {
            mChannels[i].allNotesOff();
        }
    }
    
    public void closeSynth() {
        synth.close();
    }     
}

