package display;

public class InstructionsGenerator {

  private InstructionsGenerator() {}
  
  public static String instructions() {
    String ret = "";
    ret += head();
    ret += body();
    return ret;
  }
  
  private static String head() {
    return "<html><head><title>Instructions to make shiny new sensors!</title></head>";
  }
  
  private static String body() {
    String ret = "<body><h2>Instructions to make shiny new sensors</h2>";
    ret += "<ol>";
    ret += "<li>Load the copper sheet into the cutter</li>";
    ret += "<li>Open SignCut Pro and click 'Import', then cut out the file</li>";
    ret += "<li>Remove the unnecessary background pieces from the sensors and tails, leaving the sensors undisturbed if possible</li>";
    ret += "<li>Using the transfer tape, put the newly-cut sensors onto your object</li>";
    ret += "<li>Attach one rainbow wire to each copper tail that leads to a button, as in <img src=''></li>.  Note that you should begin with the brown wire attaching to the top tail.";
    ret += "<li>Attach the grey, white, and black wires to the copper tails leading to the slider</li>";
    ret += "<li>Return to the interface to confirm locations!</li>";
    ret += "</body></html>";
    return ret;
  }
}
