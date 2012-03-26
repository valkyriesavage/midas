package actions;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;

import javax.swing.ImageIcon;

import org.java_websocket.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import serialtalk.ArduinoDispatcher;

public class SocketTalkAction extends WebSocketClient implements UIAction {
  
  private static ArduinoDispatcher dispatcher;
  private static final ImageIcon ICON = new ImageIcon("src/actions/images/websocket.png");
  
  public static void setDispatcher(ArduinoDispatcher newDispatcher) {
    dispatcher = newDispatcher;
  }

	public SocketTalkAction(URI serverUri , Draft draft) {
		super(serverUri, draft);
	}

	public SocketTalkAction(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) { }

	@Override
	public void onMessage(String message) { }

	@Override
	public void onClose(int code, String reason, boolean remote) { }

	@Override
	public void onError(Exception ex) { }
	
	private String buildJSon() {
	  String retStr = "{";
	  
	  retStr += "};";
	  return retStr;
	}
	
	public void doAction() {
	  try {
	    if(dispatcher.lastEvent != null) {
	      send(buildJSon());
	    } else {
	      send("errorrrr");
	    }
    } catch (NotYetConnectedException e) {
      e.printStackTrace();
      System.out.println("we aren't connected!");
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
	}

	public ImageIcon icon() {
	  return ICON;
	}
}
