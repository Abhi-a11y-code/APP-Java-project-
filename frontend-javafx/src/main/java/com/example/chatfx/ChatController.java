package com.example.chatfx;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class ChatController {
    @FXML private TextArea chatArea;
    @FXML private TextField inputField;
    @FXML private Label roomLabel;
    @FXML private Button sendBtn;
    @FXML private Button aiBtn;

    private WSClient ws;
    private final ObjectMapper mapper = new ObjectMapper();
    private final OkHttpClient http = new OkHttpClient();
    private final String room = "general";

    @FXML
    public void initialize() {
        roomLabel.setText("#" + room);
        try {
            ws = new WSClient(new URI("ws://localhost:8080/ws/chat"), this::onMessage);
            ws.connectBlocking();
            sendJoin();
            // Load recent history
            Request req = new Request.Builder().url("http://localhost:8080/api/messages/" + room).get().build();
            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, java.io.IOException e) { }
                @Override public void onResponse(Call call, Response response) {
                    try {
                        var arr = mapper.readTree(response.body().bytes());
                        for (int i = arr.size()-1; i>=0; i--) {
                            var m = arr.get(i);
                            String line = String.format("[%s] %s: %s",
                                    m.get("createdAt").asText(), m.get("sender").asText(), m.get("content").asText());
                            append(line);
                        }
                    } catch (Exception ignored) {}
                }
            });
        } catch (Exception e) { append("Failed to connect WS: " + e.getMessage()); }
    }

    private void onMessage(String payload) {
        try {
            var node = mapper.readTree(payload);
            String type = node.get("type").asText("");
            if ("message".equals(type)) {
                String line = String.format("%s: %s", node.get("sender").asText(), node.get("content").asText());
                append(line);
            }
        } catch (Exception e) {
            append(payload);
        }
    }

    @FXML public void onSend() {
        String txt = inputField.getText();
        if (txt == null || txt.isBlank()) return;
        sendChat(txt);
        inputField.setText("");
    }

    @FXML public void onAskAi() {
        String txt = inputField.getText();
        if (txt == null || txt.isBlank()) return;
        sendChat("@ai " + txt);
        inputField.setText("");
    }

    private void sendJoin() {
        sendJson(Map.of(
                "type","join",
                "room", room,
                "sender", ChatState.get().getUsername(),
                "content", "joined at " + Instant.now().toString()
        ));
    }

    private void sendChat(String content) {
        sendJson(Map.of(
                "type","message",
                "room", room,
                "sender", ChatState.get().getUsername(),
                "content", content
        ));
    }

    private void sendJson(Map<String, Object> map) {
        try {
            ws.send(mapper.writeValueAsString(map));
        } catch (Exception e) {
            append("Send failed: " + e.getMessage());
        }
    }

    private void append(String line) {
        Platform.runLater(() -> chatArea.appendText(line + "\n"));
    }
}
