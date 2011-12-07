package serialtalk;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.AWTException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import capture.UIAction;

/**
 * This code was stolen from the internet.
 */

public class SerialCommunication implements SerialPortEventListener {
  SerialPort serialPort;
  /** The port we're normally going to use. */
  private static final String PORT_NAMES[] = {
	  "/dev/tty.usbmodem411", // Mac, Arduino Uno
      "/dev/ttyACM0", // Linux, specifically for Arduino Uno
      //"COM3", // Windows
  };
  /** Buffered input stream from the port */
  private InputStream input;
  /** The output stream to the port */
  private OutputStream output;
  /** Milliseconds to block while waiting for port open */
  private static final int TIME_OUT = 2000;
  /** Default bits per second for COM port. */
  private static final int DATA_RATE = 9600;

  private ArduinoDispatcher dispatcher;

  // are we saving events?
  boolean capturing = false;
  boolean capturingSlider = false;
  boolean paused = false;
  List<ArduinoEvent> currentCapture = null;
  List<ArduinoSensor> currentSliderCapture = null;
  private String currentSerialInfo = new String();

  private Pattern matchOneArduinoMessage = Pattern.compile("(\\d{2})(U|D)");
  
  public JTextField whenIDo = new JTextField("when i do...");

  public void initialize() throws AWTException {
    CommPortIdentifier portId = null;
    System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
    Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
    dispatcher = new ArduinoDispatcher();
    currentCapture = new ArrayList<ArduinoEvent>();

    // iterate through, looking for the port
    while (portEnum.hasMoreElements()) {
      CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
          .nextElement();
      for (String portName : PORT_NAMES) {
        if (currPortId.getName().equals(portName)) {
          portId = currPortId;
          break;
        }
      }
    }

    if (portId == null) {
      System.out.println("Could not find COM port.");
      return;
    }

    try {
      // open serial port, and use class name for the appName.
      serialPort = (SerialPort) portId
          .open(this.getClass().getName(), TIME_OUT);

      // set port parameters
      serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,
          SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

      // open the streams
      input = serialPort.getInputStream();
      output = serialPort.getOutputStream();

      // add event listeners
      serialPort.addEventListener(this);
      serialPort.notifyOnDataAvailable(true);
      
    } catch (Exception e) {
      System.err.println(e.toString());
    }
  }

  /**
   * This should be called when you stop using the port. This will prevent port
   * locking on platforms like Linux.
   */
  public synchronized void close() {
    if (serialPort != null) {
      serialPort.removeEventListener();
      serialPort.close();
    }
  }

  public synchronized void toggleCapturing() {
    if (!capturing) {
      currentCapture = new ArrayList<ArduinoEvent>();
      currentSliderCapture = null;
    }
    capturing = !capturing;
  }

  public synchronized void toggleCapturingSlider() {
    if (!capturingSlider) {
      currentSliderCapture = new ArrayList<ArduinoSensor>();
      currentCapture = null;
    }
    capturingSlider = !capturingSlider;
  }

  public synchronized boolean isCapturing() {
    return capturing;
  }

  public synchronized boolean isCapturingSlider() {
    return capturingSlider;
  }
  
  public synchronized boolean isPaused() {
	  return paused;
  }
  
  public void togglePaused() {
	  paused = !paused;
  }

  /**
   * Handle an event on the serial port. Read the data and print it.
   */
  public synchronized void serialEvent(SerialPortEvent oEvent) {
    if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
      byte touched[] = new byte[3];
      try {
        input.read(touched, 0, 3);
        // Displayed results are codepage dependent
      } catch (Exception e) {
        System.err.println(e.toString());
        return;
      }

      currentSerialInfo = currentSerialInfo + new String(touched).trim();

      Matcher oneMessage;

      while ((oneMessage = matchOneArduinoMessage.matcher(currentSerialInfo))
          .lookingAt()) {
        currentSerialInfo = currentSerialInfo.substring(oneMessage.end());
        TouchDirection direction;
        if (oneMessage.group(2).equals("U")) {
          direction = TouchDirection.UP;
        } else {
          direction = TouchDirection.DOWN;
        }
        ArduinoEvent currentEvent = new ArduinoEvent(
            ArduinoSetup.sensors[Integer.parseInt(oneMessage.group(1))],
            direction);
        handleCompleteEvent(currentEvent);
      }
    }
    // Ignore all the other eventTypes, but you should consider the other ones.
  }

  public synchronized void registerCurrentCapture(UIAction outputAction) {
    if (currentCapture != null && currentCapture.size() > 0) {
      dispatcher.registerEvent(currentCapture, outputAction);
      currentCapture = null;
      capturing = false;
    }
  }
  
  public synchronized void registerCurrentCapture(UIAction ascendingAction, UIAction descendingAction) {
    if (currentSliderCapture != null && currentSliderCapture.size() > 0) {
      dispatcher.registerSliderAscendingEvent(cleanCurrentSlider(), ascendingAction);
      dispatcher.registerSliderDescendingEvent(cleanCurrentSlider(), descendingAction);
      currentSliderCapture = null;
      capturingSlider = false;
    }
  }

  private synchronized List<ArduinoSensor> cleanCurrentSlider() {
    // make sure all elements are unique but that we retain ORDER because that
    // is IMPORTANT
    return new ArrayList<ArduinoSensor>(new LinkedHashSet<ArduinoSensor>(
        currentSliderCapture));
  }

  public String currentCaptureToString() {
    return currentCapture.toString();
  }

  public String currentSliderCaptureToString() {
    return "slider : " + currentSliderCapture.toString();
  }

  public synchronized void handleCompleteEvent(ArduinoEvent e) {
    if (paused) {
      dispatcher.clearRecentEvents();
	  return;
	}
    if (capturing) {
      currentCapture.add(e);
      updateWhenIDo();
      return;
    }
    if (capturingSlider) {
      currentSliderCapture.add(e.whichSensor);
      updateWhenIDo();
      return;
    }
    dispatcher.handleEvent(e);
  }
  
  public void unregisterEvent(List<ArduinoEvent> l, UIAction a) {
    dispatcher.unregisterEvent(l, a);
  }
  
  public void unregisterSliderAscEvent(ArduinoSlider s, UIAction a) {
    dispatcher.unregisterSliderAscendingEvent(s, a);
  }
  
  public void unregisterSliderDescEvent(ArduinoSlider s, UIAction a) {
    dispatcher.unregisterSliderDescendingEvent(s, a);
  }
  
  public void clearAllInteractions() {
    dispatcher.clearAllInteractions();
  }

  public HashMap<List<ArduinoEvent>, List<UIAction>> eventsToHandlers() {
    return dispatcher.eventsToHandlers;
  }
  public HashMap<ArduinoSlider, List<UIAction>> slidersToAscHandlers() {
    return dispatcher.slidersToAscHandlers;
  }
  public HashMap<ArduinoSlider, List<UIAction>> slidersToDescHandlers() {
    return dispatcher.slidersToDescHandlers;
  }
  
  public void updateWhenIDo() {
	  if (currentCapture != null && currentCapture.size() > 0) {
		  whenIDo.setText(currentCaptureToString());
		  return;
	  }
	  if (currentSliderCapture != null && currentSliderCapture.size() > 0) {
		  whenIDo.setText(currentSliderCaptureToString());
		  return;
	  }
	  whenIDo.setText("when i do...");
  }
  
  public JTextArea getWhatISee() {
	  return dispatcher.whatISee;
  }
}