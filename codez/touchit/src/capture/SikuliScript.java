package capture;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class SikuliScript {
  public static final String SIKULI = "/home/valkyrie/src/Sikuli-X-1.0rc3/Sikuli-IDE/sikuli-ide.sh";
  public static final String SIKULI_SCRIPT_DIRECTORY = "/home/valkyrie/other/touch-it-data/";

  private String scriptPath;
  private String myDescription = new String();
  
  public SikuliScript(String scriptPath) {
    this.scriptPath = scriptPath;
    myDescription = new File(scriptPath).getName();
  }
  
  public void doAction() {
    try {
      String commandStr = new String(SikuliScript.SIKULI + " -s -r " + this.scriptPath);
      System.out.println(commandStr);
      Process sikuliProc = Runtime.getRuntime().exec(commandStr);
      sikuliProc.waitFor();
    } catch (IOException e) {
      e.printStackTrace();
      JFrame errorPop = new JFrame("problems!");
      errorPop.add(new JLabel("there was a problem with that sikuli script. did you save it?"));
      errorPop.setVisible(true);
    } catch (InterruptedException e) {
	  e.printStackTrace();
	}
  }
  
  public String toString() {
	  return myDescription;
  }
}
