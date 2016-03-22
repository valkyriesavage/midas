// much of this code comes from http://www.jeffreythompson.org/blog/2012/05/29/easy-processing-illustrator-export-bonus-svg-export/

import processing.pdf.*;

class SVGExporter {
  
  private final String FILENAME = "routing.svg";
  private final String SKETCHPATH = "/Users/valkyrie/projects/midas-github/codez/Midas";
  
  public SVGExporter() {}
   
  public void prepSVGSave() {
    // pretend we're creating a normal pdf
    beginRecord(PDF, FILENAME);
  }
   
  public void saveToSVG() {
    endRecord();
   
    // convert to svg
    // via: http://www.inkscapeforum.com/viewtopic.php?f=5&amp;t=5391
    String[] outputFile = split(FILENAME, '.');
    runUnixCommand("/Applications/Inkscape.app/Contents/Resources/script --without-gui " + SKETCHPATH + "/" + FILENAME + " --export-plain-svg=" + SKETCHPATH + "/" + outputFile[0] + ".svg", SKETCHPATH);
   
    runUnixCommand("rm " + FILENAME + " -f", SKETCHPATH);
   
    // X11 will still be running, so you'll have to quit it by hand...
  }
  
  private void runUnixCommand(String commandToRun, String dir) {
    File workingDir = new File(dir);          // where to do it - should be full path
    String returnedValues;                    // value to return any results
   
    // run the command!
    try {
      Process p = Runtime.getRuntime().exec(commandToRun, null, workingDir);
      int i = p.waitFor();
      if (i == 0) {
        BufferedReader stdInput = new BufferedReader(createReader(p.getInputStream()));
        while ( (returnedValues = stdInput.readLine ()) != null) {
          println(returnedValues);
        }
      }
      else {
        BufferedReader stdErr = new BufferedReader(createReader(p.getErrorStream()));
        while ( (returnedValues = stdErr.readLine ()) != null) {
          println(returnedValues);
        }
      }
    }
    catch (Exception e) {
      println("Error running command!");  
      println(e);
    }
  }
}