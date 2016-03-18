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



Components of the system:

codez/
  mpr\_121/
    This directory contains code to be loaded onto an Arduino.
  midas\_cu/
    This directory contains Java Swing code that makes the interface run.


Other required things:
  I link these into my build in Eclipse, since they aren't mine and I don't
want to include their code in my project:
  jshapes.jar -- http://java-sl.com/download/jshapes.jar
  jnativehook -- http://code.google.com/p/jnativehook/
  socket.io-java-client -- https://github.com/Gottox/socket.io-java-client
    * note that you need to also link the json-org.jar and weberknect-0.1.1.jar
    * also note that this isn't a jar file itself, which is just rude, but
      you can add it as a source folder for your project
  Inkscape --
    FOR WINDOWS:
    1) Download this file: http://sourceforge.net/projects/inkscape/files/inkscape/0.48.2/inkscape-0.48.2-1-win32.zip
    2) Unzip to codez/midas_cu/
      2.1) After unzipping, codez/midas_cu/ should have a folder "inkscape", inside which contains many files, notably "inkscape.exe".
  
    FOR MAC OS X:
      1) Download this file: http://sourceforge.net/projects/inkscape/files/inkscape/0.48.2/Inkscape-0.48.2-1-SNOWLEOPARD.dmg
      2) Mount the .DMG file; an icon should appear on your Desktop as "Inkscape"
      3) Double click "Inkscape". A window containing the Inkscape Application (with a fancy Inkscape icon) should appear.
      4) Drag and drop the Inkscape Application into codez/midas_cu/
        4.1) After dropping, codes/midas_cu/ should have the Inkscape Application

  Also, if you want to load up the Arduino, you naturally need the Arduino IDE
or similar, from e.g. http://arduino.cc/hu/Main/Software
  For the Teensy, get Teensyduino - http://www.pjrc.com/teensy/teensyduino.html
