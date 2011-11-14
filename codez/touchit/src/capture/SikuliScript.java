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
    /*Process catProc;
	try {
		catProc = Runtime.getRuntime().exec("/bin/cat " + scriptPath + "/*.py");
		catProc.waitFor();
		BufferedReader output = new BufferedReader(new InputStreamReader(catProc.getInputStream()));
		String line;
		while((line = output.readLine()) != null) {
			System.out.println(line);
			this.myDescription += line;
		}
	} catch (IOException e) {
		e.printStackTrace();
		this.myDescription = scriptPath;
	} catch (InterruptedException e) {
		e.printStackTrace();
		this.myDescription = scriptPath;
	}*/ myDescription = new File(scriptPath).getName();
  }
  
  public void doAction() {
    try {
      Runtime.getRuntime().exec(SikuliScript.SIKULI + " -s -r " + this.scriptPath);
    } catch (IOException e) {
      e.printStackTrace();
      JFrame errorPop = new JFrame("problems!");
      errorPop.add(new JLabel("there was a problem with that sikuli script. did you save it?"));
    }
  }
  
  public String toString() {
	  return myDescription;
  }
}
