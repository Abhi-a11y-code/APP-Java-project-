package com.example.chatfx;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class WSClient extends WebSocketClient {
    private final Consumer<String> onMsg;

    public WSClient(URI serverUri, Consumer<String> onMsg) {
        super(serverUri);
        this.onMsg = onMsg;
    }

    @Override public void onOpen(ServerHandshake handshakedata) { }
    @Override public void onMessage(String message) { if (onMsg != null) onMsg.accept(message); }
    @Override public void onClose(int code, String reason, boolean remote) { }
    @Override public void onError(Exception ex) { ex.printStackTrace(); }
}
