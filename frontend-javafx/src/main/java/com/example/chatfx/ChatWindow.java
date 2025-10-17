package com.example.chatfx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatWindow {
    public static void show(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(ChatWindow.class.getResource("/chat.fxml"));
            Scene scene = new Scene(loader.load(), 720, 520);
            Stage stage = new Stage();
            stage.setTitle("Interactive Chat - " + username);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
