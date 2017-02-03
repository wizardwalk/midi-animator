#midi-animator

MIDI Animator was programmed by [Sean Patrick Hannifin](http://www.wizardwalk.com/) using [jMonkeyEngine 3.0](http://www.jmonkeyengine.org). You will need jMonkeyEngine to use this code.

The MIDI animator was created so that I could make custom visualizations of my music compositions for [my YouTube channel](http://www.youtube.com/wizardwalk). It is inspired by Stephen Malinowski's "Music Animation Machine". For an example use case, see [here](https://www.youtube.com/watch?v=hNEt7KOGing). This MIDI visualizer was used ONLY to create the animation; titles were added and audio was synced separately in a 3rd party video editor.

This MIDI animator is NOT intended to be a MIDI editor or MIDI player, only a MIDI visualizer.

##To use

Download and start jMonkeyEngine and create a new project. Copy and paste these files and folders into the project directory, merging folders and overwriting files as necessary, and you should be set to go. Check out the YouTube video below for a quick walkthrough:

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/IcsnUmPj3dI/0.jpg)](https://www.youtube.com/watch?v=IcsnUmPj3dI)

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

##Disclaimer

I never actually intended to share this code, so it's rather sloppy.
