package util;

import javax.swing.ImageIcon;

import display.SetUp;

public class ImageIconUtil {
  /** Returns an ImageIcon, or null if the path was invalid. 
   * Copied from http://docs.oracle.com
   * */
  public static ImageIcon createImageIcon(String path,
                                             String description) {
      java.net.URL imgURL = SetUp.class.getResource(path);
      if (imgURL != null) {
          return new ImageIcon(imgURL, description);
      } else {
          System.err.println("Couldn't find file: " + path);
          return null;
      }
  }
}
