package actions;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;

import javax.swing.ImageIcon;

import org.json.JSONException;
import org.json.JSONObject;

import serialtalk.ArduinoDispatcher;
import serialtalk.ArduinoEvent;
import serialtalk.ArduinoPad;
import serialtalk.ArduinoSlider;
import util.EventType;
import bridge.ArduinoToPadBridge;
import bridge.ArduinoToSliderBridge;

public class SocketTalkAction implements UIAction, IOCallback {

  private static ArduinoDispatcher dispatcher;
  private static final ImageIcon ICON = new ImageIcon(
      "src/actions/images/websocket.png");

  public static void setDispatcher(ArduinoDispatcher newDispatcher) {
    dispatcher = newDispatcher;
  }

  private SocketIO socket;

  public SocketTalkAction(String serverAddress) {
    socket = new SocketIO();
    try {
      socket.connect(serverAddress, this);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  public void onMessage(JSONObject json, IOAcknowledge ack) {
    System.out.println("Server said:" + json.toString());
  }

  @Override
  public void onMessage(String data, IOAcknowledge ack) {
    System.out.println("Server said: " + data);
  }

  @Override
  public void onError(SocketIOException socketIOException) {
    System.out.println("an Error occured");
    socketIOException.printStackTrace();
  }

  @Override
  public void onDisconnect() {
    System.out.println("Connection terminated.");
  }

  @Override
  public void onConnect() {
    System.out.println("Connection established");
  }

  @Override
  public void on(String event, IOAcknowledge ack, Object... args) {
    System.out.println("Server triggered event '" + event + "'");
  }

  private String buildJSon() {
    JSONObject json = new JSONObject();
    ArduinoEvent event = dispatcher.lastEvent;
    EventType type = dispatcher.getType(event.whichSensor);

    try {
      json.put("type", type);
      json.put("direction", event.touchDirection);
      json.put("name", dispatcher.getBridgeForSensor(event.whichSensor)
          .toString());
      if (type == EventType.SLIDER) {
        json.put("position",
            ((ArduinoSlider) ((ArduinoToSliderBridge) dispatcher
                .getBridgeForSensor(event.whichSensor)).arduinoPiece)
                .positionInSlider(event.whichSensor));
      }
      if (type == EventType.PAD) {
        json.put("xposition", ((ArduinoPad) ((ArduinoToPadBridge) dispatcher
            .getBridgeForSensor(event.whichSensor)).arduinoPiece)
            .positionXInPad(event.whichSensor));
        json.put("yposition", ((ArduinoPad) ((ArduinoToPadBridge) dispatcher
            .getBridgeForSensor(event.whichSensor)).arduinoPiece)
            .positionYInPad(event.whichSensor));
      }

    } catch (JSONException e) {
      e.printStackTrace();
    }
    return json.toString();
  }

  public void doAction() {
    if (dispatcher.lastEvent != null) {
      socket.send(buildJSon());
      System.out.println(buildJSon());
    } else {
      socket.send("");
    }
    socket.disconnect();
  }

  public ImageIcon icon() {
    return ICON;
  }
}
