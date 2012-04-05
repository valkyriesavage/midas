package display;

public class InstructionsGenerator {
  private static final String MIDAS_DIR = "file:///Users/valkyrie/projects/midas_cu/";
  private static final String MIDAS_FIGS = MIDAS_DIR + "paper/figures/";

  private InstructionsGenerator() {}
  
  public static String instructions(boolean hellaSlider) {
    String ret = "";
    ret += head();
    ret += body(hellaSlider);
    return ret;
  }
  
  private static String head() {
    return "<html><head><title>Instructions to make shiny new sensors!</title></head>";
  }
  
  private static String body(boolean hellaSlider) {
    String ret = "<body><h2>Instructions to make shiny new sensors</h2>";
    ret += "<ol>";
    ret += "<li>Load the copper sheet into the cutter</li>";
    ret += "<li>Download <a href='"+MIDAS_DIR+"codez/midas_cu/outline.svg'>this file</a></li>";
    ret += "<li>Open SignCutPro <img src='"+MIDAS_FIGS+"signcut-icon.png'></li>";
    ret += "<li>Open your downloaded file</li>";
    ret += "<li>Use the mirror tool at the bottom to flip the image <img src='"+MIDAS_FIGS+"mirror-button.png'></li>";
    ret += "<li>Cut out the file <img src='"+MIDAS_FIGS+"cut-out.png'></li>";
    ret += "<li>Remove the unnecessary background pieces from the sensors and tails, leaving the sensors undisturbed if possible as in <img width=\"200\" src='"+MIDAS_FIGS+"copper-cutouts-no-background.jpg'></li>";
    ret += "<li>Using the transfer tape, put the newly-cut sensors onto your object as in <img width=\"200\" src='"+MIDAS_FIGS+"transfer-tape-on-phone.jpg'></li>";
    ret += "<li>Attach one rainbow wire to each copper tail that leads to a button, as in <img width=\"200\" src='"+MIDAS_FIGS+"attached-rainbow-wire.jpg'></li><br/>Note that you should begin with the brown wire attaching to the top tail.";
    
    if(hellaSlider)
      ret += "<li>Attach the grey, white, and black wires to the copper tails leading to the slider</li>";
    ret += "<li>Return to the interface to confirm locations!</li>";
    ret += "</body></html>";
    return ret;
  }
}
