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

The code for generating touch sensors is created in Processing, and can be
found in codez/Midas.
To get SVGs out, the code uses InkScape. You'll have to tell the Processing
code where to find it.
    

For controlling the touch sensor board, there is Arduino code in
codez/mpr\_121.

You'll need an [Arduino](http://arduino.cc/hu/Main/Software) or
[Teensyduino](http://www.pjrc.com/teensy/teensyduino.html) IDE for installing
the code onto the board you're using.

Here be Dragons
=====================
This code was written as a part of my PhD, and thus it can be sketchy in
some places! It's not very well documented, but I wanted to provide it
on github for anyone curious about spelunking through it and hopefully
making use of it in some way.
