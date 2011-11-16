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
import java.util.List;
import java.util.Map.Entry;

import antlr.StringUtils;

import capture.SikuliScript;

/**
 * This code was stolen from the internet.
 */

public class SerialCommunication implements SerialPortEventListener {
  SerialPort serialPort;
        /** The port we're normally going to use. */
  private static final String PORT_NAMES[] = { 
      "/dev/tty.usbserial-A9007UX1", // Mac OS X
      "/dev/ttyACM0", // Linux, specifically for Arduino Uno
      "COM3", // Windows
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
  List<ArduinoEvent> currentCapture;
  private ArduinoEvent currentEvent;
  
  public void initialize() throws AWTException {
    CommPortIdentifier portId = null;
    Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
    dispatcher = new ArduinoDispatcher();
    currentCapture = new ArrayList<ArduinoEvent>();

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
      serialPort = (SerialPort) portId.open(this.getClass().getName(),
          TIME_OUT);

      // set port parameters
      serialPort.setSerialPortParams(DATA_RATE,
          SerialPort.DATABITS_8,
          SerialPort.STOPBITS_1,
          SerialPort.PARITY_NONE);

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
   * This should be called when you stop using the port.
   * This will prevent port locking on platforms like Linux.
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
    }
    capturing = !capturing;
  }
  
  public synchronized boolean isCapturing() {
    return capturing;
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
      
      String info = new String(touched);

      if (currentEvent == null) {
          String which = info.substring(0,2);
          currentEvent = new ArduinoEvent(Integer.parseInt(which));
          if (info.length() > 1) {
        	  String dir = info.substring(2,3);
        	  TouchDirection direction;
        	  System.out.println("<<<" + dir + ">>>");
              if (dir.equals("D")) {
            	  direction = TouchDirection.DOWN;
              } else if (dir.equals("U")) {
            	  direction = TouchDirection.UP;
              } else {
            	  direction = null;
              }
              currentEvent.setDirection(direction);
          }
      } /*else {
    	  String dir = info.substring(0,1);
    	  TouchDirection direction;
    	  System.out.println("<<<" + dir + ">>>");
          if (dir.equals("D")) {
        	  direction = TouchDirection.DOWN;
          } else if (dir.equals("U")) {
        	  direction = TouchDirection.UP;
          } else {
        	  direction = null;
          }
          currentEvent.setDirection(direction);
      }*/
      
      
      System.out.println(currentEvent.toString() + "*****\n\n");
      
      if (currentEvent.isComplete()) {
    	  handleCompleteEvent(currentEvent);
    	  currentEvent = null;
      }
    }
    // Ignore all the other eventTypes, but you should consider the other ones.
  }
  
  public synchronized void registerSerialEvent(List<ArduinoEvent> l, SikuliScript s) {
    dispatcher.registerEvent(l, s);
  }
  
  public synchronized void registerCurrentCapture(SikuliScript outputAction) {
    dispatcher.registerEvent(currentCapture, outputAction);
    currentCapture = null;
    capturing = false;
  }
  
  public String currentCaptureToString() {
    return currentCapture.toString();
  }
  
  public synchronized void handleCompleteEvent(ArduinoEvent e) {
	  if (capturing) {
  	    currentCapture.add(e);
  	  } else {
        dispatcher.handleEvent(e);
	  }
  }
  
  public String listOfRegisteredEvents() {
    String retVal = new String();
    for (Entry<List<ArduinoEvent>, List<SikuliScript>> p:dispatcher.eventsToHandlers.entrySet()) {
      retVal += "" + p.getKey() + " -> " + p.getValue().toString() + "\n";
    }
    return retVal;
  }
}