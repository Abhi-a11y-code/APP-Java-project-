package com.example.chatfx;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class AiClient {
  public static void stream(String room, String user, String prompt, Consumer<String> onChunk, Runnable onDone) {
    new Thread(() -> {
      try {
        String q = String.format("room=%s&user=%s&prompt=%s",
          url(room), url(user), url(prompt));
        URL url = new URL("http://localhost:8080/api/ai/stream?" + q);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("Accept", "text/event-stream");

        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
          String line;
          while ((line = br.readLine()) != null) {
            if (line.startsWith("data:")) {
              String text = line.substring(5).trim();
              Platform.runLater(() -> onChunk.accept(text));
            }
          }
        }
      } catch (Exception e) {
        Platform.runLater(() -> onChunk.accept("[stream error: " + e.getMessage() + "]"));
      } finally {
        Platform.runLater(onDone);
      }
    }, "AI-Stream").start();
  }

  private static String url(String s) {
    return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}