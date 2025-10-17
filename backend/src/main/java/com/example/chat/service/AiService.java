package com.example.chat.service;

import com.example.chat.model.Message;
import com.example.chat.repo.MessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.net.http.*; import java.net.*; import java.util.*;
import java.time.Instant;

@Service
public class AiService {
  private final MessageRepository messages;
  private final ObjectMapper mapper = new ObjectMapper();
  private final HttpClient http = HttpClient.newHttpClient();
  private final String apiKey = System.getenv("OPENAI_API_KEY");

  public AiService(MessageRepository messages) { this.messages = messages; }

  public Map<String,Object> reply(String user, String room, String prompt, List<Map<String,Object>> toolResults) {
    if (apiKey == null || apiKey.isBlank()) {
      return Map.of("provider","stub", "response","AI helper (stub): " + prompt);
    }

    // 1) Pull last N messages from the room for context
    List<Message> recent = messages.findTop25ByRoomOrderByTimestampDesc(room);
    Collections.reverse(recent);

    // 2) Build chat messages
    List<Map<String,String>> msgs = new ArrayList<>();
    msgs.add(msg("system",
      "You are a helpful AI assistant inside a team chat. " +
      "Be concise, helpful, and use available tools when appropriate. " +
      "If a tool was executed, incorporate its result."));
    // Optional short-term memory example (stub):
    // msgs.add(msg("system", "User 'abhimanyu' likes short bullet answers."));

    for (Message m : recent) {
      msgs.add(msg("user", "["+m.getTimestamp()+"] "+m.getSender()+": "+m.getContent()));
    }
    msgs.add(msg("user", prompt));

    // 3) Tools spec (we’ll define executors in step 3)
    List<Map<String,Object>> tools = List.of(
      tool("create_group", "Create a new chat group for users.",
        Map.of("type","object", "properties", Map.of(
            "groupName", Map.of("type","string","description","Name of the group"),
            "members", Map.of("type","array","items",Map.of("type","string"),"description","Usernames to add")
        ), "required", List.of("groupName"))),
      tool("list_rooms", "List available chat rooms.", Map.of("type","object","properties",Map.of()))
      // add more tools later...
    );

    // 4) Build request
    Map<String,Object> payload = Map.of(
      "model", "gpt-4o-mini",                  // or another chat model you have
      "messages", msgs,
      "tools", tools,
      "tool_choice", "auto"
    );

    try {
      HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create("https://api.openai.com/v1/chat/completions"))
        .header("Content-Type","application/json")
        .header("Authorization","Bearer "+apiKey)
        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
        .build();

      HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      JsonNode root = mapper.readTree(resp.body());

      JsonNode choice = root.path("choices").get(0);
      JsonNode toolCalls = choice.path("message").path("tool_calls");

      // 5) If model asked to call tools, execute them and do the “two-turn” loop
      if (toolCalls.isArray() && toolCalls.size() > 0) {
        for (JsonNode call : toolCalls) {
          String name = call.path("function").path("name").asText();
          String argStr = call.path("function").path("arguments").asText("{}");
          Map<String,Object> args = mapper.readValue(argStr, Map.class);

          Map<String,Object> result = ToolRegistry.execute(name, args); // step 3
          toolResults.add(Map.of("name", name, "args", args, "result", result));

          // Add tool result back to conversation
          msgs.add(msg("tool", "Tool "+name+" result: "+mapper.writeValueAsString(result)));
        }

        // Ask the model again to produce the final answer with tool outputs
        Map<String,Object> payload2 = Map.of(
          "model", "gpt-4o-mini",
          "messages", msgs
        );
        HttpRequest req2 = HttpRequest.newBuilder()
          .uri(URI.create("https://api.openai.com/v1/chat/completions"))
          .header("Content-Type","application/json")
          .header("Authorization","Bearer "+apiKey)
          .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload2)))
          .build();
        HttpResponse<String> resp2 = http.send(req2, HttpResponse.BodyHandlers.ofString());
        JsonNode finalText = mapper.readTree(resp2.body())
          .path("choices").get(0).path("message").path("content");
        return Map.of("provider","openai","response", finalText.asText(), "tools_used", toolResults);
      }

      // No tool call: return model’s content
      String content = choice.path("message").path("content").asText();
      return Map.of("provider","openai","response", content);

    } catch (Exception e) {
      return Map.of("provider","openai","error", e.getMessage());
    }
  }

  private Map<String,String> msg(String role, String content) {
    return Map.of("role", role, "content", content);
  }
  private Map<String,Object> tool(String name, String desc, Map<String,Object> jsonSchema) {
    return Map.of("type","function", "function", Map.of(
      "name", name, "description", desc, "parameters", jsonSchema));
  }
}