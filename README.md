Midas
=====

Midas is exploring how to support designers in rapidly creating and
iterating on interaction ideas for objects.  We are moving increasingly
towards a world where we can print any object we dream of in any material
we like, and electronics and circuits should be no exception.  By using
some sheets of copper foil and vinyl along with a vinyl cutter and an Arduino,
the interface and components contained in this repository permits a person
to create capacitive touch sensors in any desired shape and affix them to any
object.  The "capturing" interface then allows them to "program" an action
on the computer by demonstrating it, which will then be parroted back on the
assigned trigger.

You can check out a video summarizing the system on YouTube:
[![Midas Conference Video](https://img.youtube.com/vi/lS60AH2_Pbs/0.jpg)](https://www.youtube.com/watch?v=lS60AH2_Pbs)


Technical Details
=================

The interface for manipulating touch sensors is written in Java (Swing).
That code can be found in codez/midas\_cu. This depends upon a few extra
libraries that I didn't write myself, so they are not included in this repo.
  
  1. [jshapes.jar](http://java-sl.com/download/jshapes.jar)
  2. [jnativehook](http://code.google.com/p/jnativehook/)
  3. [socket.io-java-client](https://github.com/Gottox/socket.io-java-client) -- 
      note that you need to also link the json-org.jar and weberknect-0.1.1.jar,
      and that this isn't a jar file but you can link it as a source folder in
      your project.

For controlling the touch sensor board, there is Arduino code in
codez/mpr\_121.

To get SVGs out of this (yes, it was written before I knew Processing and all
its goodness), the code uses Inkscape. It has to be installed in a particular
way:
    
FOR WINDOWS:
  1) Download this file: http://sourceforge.net/projects/inkscape/files/inkscape/0.48.2/inkscape-0.48.2-1-win32.zip
  2) Unzip to codez/midas_cu/ . After unzipping, codez/midas_cu/ should have a folder "inkscape", inside which contains many files, notably "inkscape.exe".
  
FOR MAC OS X:
  1) Download this file: http://sourceforge.net/projects/inkscape/files/inkscape/0.48.2/Inkscape-0.48.2-1-SNOWLEOPARD.dmg
  2) Mount the .DMG file; an icon should appear on your Desktop as "Inkscape"
  3) Double click "Inkscape". A window containing the Inkscape Application (with a fancy Inkscape icon) should appear.
  4) Drag and drop the Inkscape Application into codez/midas_cu/ . After dropping, codes/midas_cu/ should have the Inkscape Application

You'll need an [Arduino](http://arduino.cc/hu/Main/Software) or
[Teensyduino](http://www.pjrc.com/teensy/teensyduino.html) IDE for installing
the code onto the board you're using.

Here be Dragons
=====================
This code was written as a part of my PhD, and thus it can be sketchy in
some places! It's not very well documented, but I wanted to provide it
on github for anyone curious about spelunking through it and hopefully
making use of it in some way.
