package serialtalk;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.AWTException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextField;

import actions.SocketTalkAction;
import bridge.ArduinoToDisplayBridge;
import display.ArduinoSensorButton;


/**
 * This code was inspired by the Internet.
 */

public class SerialCommunication implements SerialPortEventListener {
  SerialPort serialPort;

  private static final String PORT_NAMES[] = {
    "/dev/tty.usbmodemfa131", // Mac, Arduino Uno, works for Ragnarok
    "/dev/tty.usbmodemfa121", // Mac, Arduino Uno, works for Ragnarok
    "/dev/ttyACM0", // Linux, Arduino Uno
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
  public List<ArduinoToDisplayBridge> bridgeObjects;

  boolean paused = false;
  private String currentSerialInfo = new String();

  private Pattern matchOneArduinoMessage = Pattern.compile("(\\d{2})(U|D)\n");
  private Pattern matchOneArduino2DMessage = Pattern.compile("((\\d{2})(U|D)){2}\n");
  private Pattern matchOneArduinoSliderMessage = Pattern.compile("(\\d{3})\n");
  
  public boolean isGridded = false;
  
  public void initialize(boolean test) throws AWTException {
    ArduinoSetup.initialize(test);
    
    CommPortIdentifier portId = null;
    // the following line is useful for computers with AMD processors.  it's stupid.
    //System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
    
    @SuppressWarnings("rawtypes")
    Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
    dispatcher = new ArduinoDispatcher();
    ArduinoToDisplayBridge.setDispatcher(dispatcher);
    ArduinoSensorButton.setDispatcher(dispatcher);
    SocketTalkAction.setDispatcher(dispatcher);
    bridgeObjects = dispatcher.bridgeObjects;

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
      if (test) {
        // we want to make a new window that we can use to inject ArduinoEvents
        ArduinoTestWindow.setDispatcher(dispatcher);
        new ArduinoTestWindow();
      }
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
  
  public synchronized void sendMessage(String message) {
    try {
      output.write(message.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public synchronized boolean isPaused() {
	  return paused;
  }
  
  public synchronized void togglePaused() {
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

      if (isGridded) {
        while ((oneMessage = matchOneArduino2DMessage.matcher(currentSerialInfo))
            .lookingAt()) {
          currentSerialInfo = currentSerialInfo.substring(oneMessage.end());
          TouchDirection direction;
          if (oneMessage.group(2).equals("U") && oneMessage.group(2).equals(oneMessage.group(4))) {
            direction = TouchDirection.TOUCH;
          } else {
            direction = TouchDirection.RELEASE;
          }
          ArduinoEvent currentEvent = new ArduinoEvent(
              ArduinoSetup.gridSensors[Integer.parseInt(oneMessage.group(1))][Integer.parseInt(oneMessage.group(3))],
              direction);
          handleCompleteEvent(currentEvent);
        }
      } else { //is not gridded
        while ((oneMessage = matchOneArduinoMessage.matcher(currentSerialInfo))
            .lookingAt()) {
          currentSerialInfo = currentSerialInfo.substring(oneMessage.end());
          TouchDirection direction;
          if (oneMessage.group(2).equals("U")) {
            direction = TouchDirection.TOUCH;
          } else {
            direction = TouchDirection.RELEASE;
          }
          ArduinoEvent currentEvent = new ArduinoEvent(
              ArduinoSetup.sensors[Integer.parseInt(oneMessage.group(1))],
              direction);
          handleCompleteEvent(currentEvent);
        }
      }
    }
  }

  public synchronized void handleCompleteEvent(ArduinoEvent e) {
    if (paused) {
      dispatcher.clearRecentEvents();
      return;
    }
    dispatcher.handleEvent(e);
  }
  
  public JTextField whatISee() {
    return dispatcher.whatISee;
  }
}