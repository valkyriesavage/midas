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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import capture.UIScript;

/**
 * This code was inspired by the Internet.
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
  @SuppressWarnings("unused")
  private OutputStream output;
  /** Milliseconds to block while waiting for port open */
  private static final int TIME_OUT = 2000;
  /** Default bits per second for COM port. */
  private static final int DATA_RATE = 9600;

  private ArduinoDispatcher dispatcher;

  boolean paused = false;
  private String currentSerialInfo = new String();

  //private Pattern matchOneArduinoMessage = Pattern.compile("(\\d{2})(U|D)");
  private Pattern matchOneArduino2DMessage = Pattern.compile("((\\d{2})(U|D)){2}");
  
  public void initialize() throws AWTException {
    ArduinoSetup.initialize();
    
    CommPortIdentifier portId = null;
    //System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
    @SuppressWarnings("rawtypes")
    Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
    dispatcher = new ArduinoDispatcher();

    // iterate through, looking for the port
    while (portEnum.hasMoreElements()) {
      CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
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

      Matcher one2DMessage;

      while ((one2DMessage = matchOneArduino2DMessage.matcher(currentSerialInfo))
          .lookingAt()) {
        System.out.println(one2DMessage.group());
        currentSerialInfo = currentSerialInfo.substring(one2DMessage.end());
        TouchDirection direction;
        if (one2DMessage.group(2).equals("U") && one2DMessage.group(2).equals(one2DMessage.group(4))) {
          direction = TouchDirection.TOUCH;
        } else {
          direction = TouchDirection.RELEASE;
        }
        ArduinoEvent currentEvent = new ArduinoEvent(
            ArduinoSetup.sensors[Integer.parseInt(one2DMessage.group(1))][Integer.parseInt(one2DMessage.group(3))],
            direction);
        handleCompleteEvent(currentEvent);
      }
    }
    // Ignore all the other eventTypes, but you should consider the other ones.
  }

  public synchronized void handleCompleteEvent(ArduinoEvent e) {
    if (paused) {
      dispatcher.clearRecentEvents();
	  return;
	}
    dispatcher.handleEvent(e);
  }
  
  public void clearAllInteractions() {
    dispatcher.clearAllInteractions();
  }

  public Map<ArduinoSensor, UIScript> buttonsToHandlers() {
    return dispatcher.buttonsToHandlers;
  }

  /*
   * This effectively zips the lists of ascending and descending handlers for sliders.
   * 
   * If a slider is missing an ascending or descending action, a null holds its place.
   */
  public Map<ArduinoSlider, List<UIScript>> slidersToHandlers() {
    Map<ArduinoSlider, List<UIScript>> slidersToHandlers = new HashMap<ArduinoSlider, List<UIScript>>();
    for (Map.Entry<ArduinoSlider, UIScript> e : dispatcher.slidersToAscHandlers.entrySet()) {
      List<UIScript> handlers = new ArrayList<UIScript>();
      handlers.add(e.getValue());
      handlers.add(null);
      slidersToHandlers.put(e.getKey(), handlers);
    }
    for (Map.Entry<ArduinoSlider, UIScript> e : dispatcher.slidersToDescHandlers.entrySet()) {
      if(slidersToHandlers.containsKey(e.getKey())) {
        slidersToHandlers.get(e.getKey()).remove(null);
        slidersToHandlers.get(e.getKey()).add(e.getValue());
      }
      else {
        List<UIScript> handlers = new ArrayList<UIScript>();
        handlers.add(null);
        handlers.add(e.getValue());
        slidersToHandlers.put(e.getKey(), handlers);
      }
    }
    return slidersToHandlers;
  }
  
  public Map<ArduinoSlider, UIScript> slidersToAscHandlers() {
    return dispatcher.slidersToAscHandlers;
  }
  
  public Map<ArduinoSlider, UIScript> slidersToDescHandlers() {
    return dispatcher.slidersToDescHandlers;
  }

  public Map<ArduinoSensor, UIScript> padsToHandlers() {
    return dispatcher.padsToHandlers;
  }
  public Map<List<ArduinoEvent>, UIScript> combosToHandlers() {
    return dispatcher.combosToHandlers;
  }
}