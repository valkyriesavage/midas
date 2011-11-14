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
      byte touched[] = new byte[2];
      try {
        input.read(touched, 0, 1);
        // Displayed results are codepage dependent
        System.out.println(new String(touched));
      } catch (Exception e) {
        System.err.println(e.toString());
      }
      
      ArduinoEvent e = new ArduinoEvent((int)touched[0], TouchDirection.values()[touched[1]]);
      
      if (capturing) {
        currentCapture.add(e);
      } else {
        dispatcher.handleEvent(e);
      }
    }
    // Ignore all the other eventTypes, but you should consider the other ones.
  }
  
  public synchronized void registerSerialEvent(List<ArduinoEvent> l, SikuliScript s) {
    dispatcher.registerEvent(l, s);
  }
  
  public synchronized void registerCurrentCapture(SikuliScript outputAction) {
    dispatcher.registerEvent(currentCapture, outputAction);
    capturing = false;
  }
  
  public String currentCaptureToString() {
    return currentCapture.toString();
  }
  
  public synchronized void handleEvent_forTestingOnly(ArduinoEvent e) {
    dispatcher.handleEvent(e);
  }
  
  public String listOfRegisteredEvents() {
    String retVal = new String();
    for (Entry<List<ArduinoEvent>, List<SikuliScript>> p:dispatcher.eventsToHandlers.entrySet()) {
      retVal += "" + p.getKey() + " -> " + p.getValue().toString() + "\n";
    }
    return retVal;
  }
}