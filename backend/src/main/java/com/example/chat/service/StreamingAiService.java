package com.example.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class StreamingAiService {
  private final ObjectMapper mapper = new ObjectMapper();
  private final HttpClient http = HttpClient.newHttpClient();
  private final String apiKey = System.getenv("OPENAI_API_KEY");
  private final ExecutorService pool = Executors.newCachedThreadPool();

  public SseEmitter stream(String room, String user, String prompt) {
    // fall back to non-streaming stub if no key
    if (apiKey == null || apiKey.isBlank()) {
      SseEmitter e = new SseEmitter();
      try {
        e.send("AI helper (stub): " + prompt);
        e.complete();
      } catch (Exception ex) { e.completeWithError(ex); }
      return e;
    }

    SseEmitter emitter = new SseEmitter(0L); // no timeout
    pool.submit(() -> {
      try {
        String payload = """
          {"model":"gpt-4o-mini","stream":true,
           "messages":[{"role":"system","content":"You are a helpful assistant in a chat."},
                       {"role":"user","content":%s}]}
        """.formatted(mapper.writeValueAsString(prompt));

        HttpRequest req = HttpRequest.newBuilder()
          .uri(URI.create("https://api.openai.com/v1/chat/completions"))
          .header("Authorization","Bearer " + apiKey)
          .header("Content-Type","application/json")
          .POST(HttpRequest.BodyPublishers.ofString(payload))
          .build();

        http.send(req, HttpResponse.BodyHandlers.ofLines())
            .body()
            .forEach(line -> {
              try {
                if (!line.startsWith("data:")) return;
                String json = line.substring(5).trim();
                if (json.equals("[DONE]")) { emitter.complete(); return; }
                JsonNode n = mapper.readTree(json);
                String delta = n.path("choices").get(0)
                                .path("delta").path("content").asText("");
                if (!delta.isEmpty()) emitter.send(delta);
              } catch (Exception ignored) {}
            });

      } catch (Exception ex) {
        emitter.completeWithError(ex);
      }
    });
    return emitter;
  }
}