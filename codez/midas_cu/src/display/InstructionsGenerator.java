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
    ret += "<li>Load a sheet of vinyl into the cutter</li>";
    ret += "<li>Cut out <a href=''>this file</a> in vinyl</li>";
    ret += "<li>Load a sheet of copper into the cutter</li>";
    ret += "<li>Cut out <a href=''>this file</a> in copper</li>";
    ret += "<li>Using the transfer tape, put the piece of copper that looks like this onto your object: <img src=''/></li>";
    ret += "<li>Using the transfer tape, put the piece of vinyl that looks like this onto your object: <img src=''/></li>";
    // if we have < 12 sensors, don't do this next part
    ret += "<li>Using the transfer tape, put the piece of copper that looks like this onto your object so that it lines up with the copper below: <img src=''/></li>";
    ret += "<li>Using the transfer tape, put the piece of vinyl that looks like this onto your object: <img src=''/></li>";
    // endif
    ret += "<li>Attach one wire to each copper tail, as in <img src=''></li>";
    ret += "<li>Return to the interface to confirm locations!</li>";
    ret += "</body></html>";
    return ret;
  }
}
