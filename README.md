# midi-animator
MIDI Animator
programmed by Sean Patrick Hannifin (wizardwalk.com)
made with jMonkeyEngine 3.0 (jmonkeyengine.org)

You will need to jMonkeyEngine to use this code.

The MIDI animator was created so that I could make custom visualizations of my music compositions for YouTube. (youtube.com/wizardwalk)

It is inspired by Stephen Malinowski's "Music Animation Machine".

It is not intended to be a MIDI editor or MIDI player; only a MIDI visualizer.

CONTROLS

SPACE - start / stop
CTRL+SPACE - render each frame to a screenshot

(ffmpeg can be used to create a video with the screenshots;
see http://hamelot.io/visualization/using-ffmpeg-to-convert-a-set-of-images-into-a-video/ ... this ensures the video will be 60 fps)

G - toggle music grid
P - toggle play line

W - zoom in
S - zoom out
E - reset zoom
Q - go to beginning

left click - drag view
right click - set start position

to change a track's color, hover mouse over a note, press CTRL,
and scroll mouse wheel

MIDI file is loaded from midiFileName string

