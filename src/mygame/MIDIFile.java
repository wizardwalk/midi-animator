/*
 * Opens and stores info about a midi file...
 */
package mygame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MetaMessage;

/**
 *
 * @author SeanTheBest
 */
public class MIDIFile {
        
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final int PROGRAM_CHANGE = 0xC0;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    public class MIDITempo {
        public float tempo;
        public float pos;
        public MIDITempo(float tempo, float pos) {
            this.tempo = tempo;
            this.pos = pos;
        }
    }
    
    private List<MIDITrack> myTracks = new ArrayList<MIDITrack>();
    private List<MIDITempo> myTempos = new ArrayList<MIDITempo>();
    private int ppq = 240;
    private int timeSigTop = 4;
    private int timeSigBottom = 4;
    private float sizeInW = 0f;
    
    // copied from stackoverflow...
    // http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    // end copy

    public MIDIFile(String filename, int qShift) throws Exception {
        // clear our current tracks...
        myTracks.clear();
        myTempos.clear();
        
        Sequence sequence = MidiSystem.getSequence(new File(filename));
        
        //System.out.println("Resolution: " + sequence.getResolution());
        ppq = sequence.getResolution();
        //System.out.println("Tick length: " + sequence.getTickLength());
        sizeInW = sequence.getTickLength() / (sequence.getResolution()*4f);
        //System.out.println("Division Type: " + sequence.getDivisionType());
        //System.out.println();

        int trackNumber = 0;
        int currentProgram = 0; // default midi instrument, piano
        for (int h = 0; h < sequence.getTracks().length; h++) {
            MIDITrack newTrack = new MIDITrack();
            trackNumber++;
            newTrack.trackNumber = trackNumber;
            
            //System.out.println("Track " + trackNumber + ": size = " + sequence.getTracks()[h].size());
            //System.out.println();
            for (int i=0; i < sequence.getTracks()[h].size(); i++) { 
                MidiEvent event = sequence.getTracks()[h].get(i);
                //System.out.print("@" + event.getTick() + " ");
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    //System.out.print("Channel: " + sm.getChannel() + " ");
                    if (sm.getCommand() == PROGRAM_CHANGE) {
                        currentProgram = sm.getData1();
                        //System.out.println("Program change, " + currentProgram);
                    }
                    else if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        int octave = (key / 12)-1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        //System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                        // create new note in track
                        newTrack.addNote(sm.getChannel(), currentProgram, key, velocity, (event.getTick()+qShift*ppq));
                    }
                    else if (sm.getCommand() == NOTE_OFF) {
                        int key = sm.getData1();
                        int octave = (key / 12)-1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        //System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                        // set note duration...
                        newTrack.endNote(key, (event.getTick()+qShift*ppq));
                        //System.out.println("NOTE ENDED: " + newTrack.endNote(key, event.getTick()));
                    } else {
                        //System.out.println("Command:" + sm.getCommand());
                    }
                } else if (message instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage) message;
                    String hexMessage = bytesToHex(mm.getMessage());
                    String eventType = hexMessage.substring(0, 6);
                    
                    // is this a tempo event?
                    // http://www.somascape.org/midi/tech/mfile.html
                    if (eventType.equals("FF5103")) {
                        String eventData = hexMessage.substring(6);
                        Long timePerQ = Long.parseLong(eventData, 16);
                        float secondsPerQ = timePerQ / 1000000f;
                        //System.out.println("NEW TEMPO: " + secondsPerQ);
                        // add to our tempo list...
                        float tempo = 60f / secondsPerQ; // convert to bpm (60 seconds in a minute)
                        MIDITempo newTempo = new MIDITempo(tempo, (float)(event.getTick()+qShift*ppq));
                        myTempos.add(newTempo);
                    }
                    // time sig event?
                    else if (eventType.equals("FF5804")) {
                        String numeratorData = hexMessage.substring(6, 8);
                        String denomData = hexMessage.substring(8, 10);
                        int numerator = Integer.parseInt(numeratorData, 16);
                        int denom = (int) Math.pow(2, Integer.parseInt(denomData, 16));
                        timeSigTop = numerator;
                        timeSigBottom = denom;
                        //System.out.println("NEW TIME SIG: " + numerator + "/" + denom);
                    }                    
                    else {
                        //System.out.println("META MESSAGE: " + eventType);
                    }
                } else {
                    //System.out.println("Other message: " + message.getClass());
                }
            }
            
            // add the track to our list...
            myTracks.add(newTrack);
        }

    }
    
    // gets and sets //////////////////////////
    
    public List<MIDITrack> getMyTracks() {
        return myTracks;
    }
    public List<MIDITempo> getMyTempos() {
        return myTempos;
    }
    public int getPPQ() {
        return ppq;
    }
    public int getTimeSigTop() {
        return timeSigTop;
    }
    public int getTimeSigBottom() {
        return timeSigBottom;
    }
    public float getSizeInW() {
        return sizeInW;
    }
}
