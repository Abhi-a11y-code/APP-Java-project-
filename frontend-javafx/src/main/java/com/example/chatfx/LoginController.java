package com.example.chatfx;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @FXML
    public void onRegister(ActionEvent e) {
        try {
            Map<String, String> payload = Map.of("username", usernameField.getText(),
    "password", passwordField.getText());
            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/auth/register")
                    .post(RequestBody.create(mapper.writeValueAsBytes(payload), JSON))
                    .build();
            try (Response resp = http.newCall(request).execute()) {
                if (resp.isSuccessful()) {
                    alert("Registered! Now log in.", Alert.AlertType.INFORMATION);
                } else {
                    alert("Register failed: " + resp.body().string(), Alert.AlertType.ERROR);
                }
            }
        } catch (IOException ex) { alert(ex.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML
    public void onLogin(ActionEvent e) {
        try {
            Map<String, String> payload = Map.of("username", usernameField.getText(), "password", passwordField.getText());
            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/auth/login")
                    .post(RequestBody.create(mapper.writeValueAsBytes(payload),JSON))
                    .build();
            try (Response resp = http.newCall(request).execute()) {
                if (resp.isSuccessful()) {
                    Map data = mapper.readValue(resp.body().bytes(), Map.class);
                    String username = (String) data.get("username");
                    ChatState.get().setUsername(username);
                    // Switch scene
                    usernameField.getScene().getWindow().hide();
                    ChatWindow.show(username);
                } else {
                    alert("Login failed: " + resp.body().string(), Alert.AlertType.ERROR);
                }
            }
        } catch (IOException ex) { alert(ex.getMessage(), Alert.AlertType.ERROR); }
    }

    private void alert(String msg, Alert.AlertType type) {
        Alert a = new Alert(type, msg);
        a.showAndWait();
    }
}
