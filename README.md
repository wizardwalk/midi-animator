#midi-animator

MIDI Animator was programmed by [Sean Patrick Hannifin](http://www.wizardwalk.com/) made with [jMonkeyEngine 3.0](http://www.jmonkeyengine.org). You will need jMonkeyEngine to use this code.

The MIDI animator was created so that I could make custom visualizations of my music compositions for [my YouTube channel](http://www.youtube.com/wizardwalk). It is inspired by Stephen Malinowski's "Music Animation Machine".

This MIDI animator is NOT intended to be a MIDI editor or MIDI player, only a MIDI visualizer.

##Controls

* **space bar** - start / stop
* **ctrl+space** - render each frame to a screenshot

(ffmpeg can be used to create a video with the screenshots, see [here](http://hamelot.io/visualization/using-ffmpeg-to-convert-a-set-of-images-into-a-video/). This ensures the video will be 60 fps)

* **G** - toggle music grid
* **P** - toggle play line
* **W** - zoom in
* **S** - zoom out
* **E** - reset zoom
* **Q** - go to beginning
* **left click** - drag view
* **right click** - position cursor / start position

To change a track's color, hover mouse over a note, press ctrl and scroll mouse wheel.

MIDI file is loaded from the midiFileName string.
