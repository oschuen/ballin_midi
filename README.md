# ballin_midi
Automatic Accompaniment for Midi Keyboard

This is a simple Accompaniment Software playing a guitar accompaniment based on recognized chords from the keyboard. 

Additionally a percussion accompaniment can also be configured. 

You can start it using java

java -jar JAccompaniment.jar

and rebuild it using Maven

mvn clean install 

within JAccompaniment folder.

I'm using it together with Qsynth and have following channel settings (GM):

* Channel 1 Grand Piano (Bank 0, Prog 0)
* Channel 2 Nylon String Guitar (Bank 0, Prog 24)
* Channel 10 Room 1 (Bank 128 Prog 9)
 
JAccompaniment uses these channels by default, but you can reconfigure it to use other channels.

I have the alsa virtual midi device installed and following connections (Transmitter -> Receiver) configured:

* USB-Midi (Keyboard) -> Virtual Raw MIDI 1-0
* Virtual Raw Midi 1-0 -> QSynth

There is no need to connect the USB-Midi directly to the synthesizer. JAccompaniment has a midi through functionality that can filter the accompaniment area of the keyboard.

In the Config Dialog of JAccompaniment following settings are active to connect to Virtual Midi Device

* Chord Recognition (Input) = VirMIDI [hw:1,0,0]
* Midi Through (Input) = VirMIDI [hw:1,0,0]
* Midi Through (Output) = VirMIDI [hw:1,0,0]
* Guitar (Output) = VirMIDI [hw:1,0,1]
* Guitar (Channel) 1
* Percussion (Output) = VirMIDI [hw:1,0,2]
* Percussion (Channel) = 9
* Midi Through = yes
* Filter Chord = yes

I wrote this program because I started to learn keyboard a few weeks ago. I bought me a keyboard school and a master keyboard not regarding that it has no accompaniment, what my keyboard school requires. So I implemented an accompaniment on my own. Since I'm notfamiliar with how keyboard accompaniment works, I implemented a guitar because I used to play guitar a few years ago and know how this works.


