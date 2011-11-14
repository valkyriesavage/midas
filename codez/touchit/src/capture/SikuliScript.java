package capture;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class SikuliScript {
  public static final String SIKULI = "/home/valkyrie/src/Sikuli-X-1.0rc3/Sikuli-IDE/sikuli-ide.sh";
  public static final String SIKULI_SCRIPT_DIRECTORY = "/home/valkyrie/other/touch-it-data/";

  private String scriptPath;
  
  public SikuliScript(String scriptPath) {
    this.scriptPath = scriptPath;
  }
  
  public void doAction() {
    try {
      Process sikuli_proc = Runtime.getRuntime().exec(SikuliScript.SIKULI + " -s -r " + this.scriptPath);
    } catch (IOException e) {
      e.printStackTrace();
      JFrame errorPop = new JFrame("problems!");
      errorPop.add(new JLabel("there was a problem with that sikuli script. did you save it?"));
    }
  }
  
  public String toString() {
    return this.scriptPath;
  }
}
