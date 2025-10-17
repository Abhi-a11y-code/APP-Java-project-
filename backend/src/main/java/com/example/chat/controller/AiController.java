package com.example.chat.controller;

import com.example.chat.service.AiService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
  private final AiService ai;
  public AiController(AiService ai) { this.ai = ai; }

  @PostMapping("/ask")
  public Map<String,Object> ask(@RequestBody Map<String,String> body) {
    String user  = body.getOrDefault("user","anonymous");
    String room  = body.getOrDefault("room","general");
    String prompt= body.getOrDefault("prompt","");
    List<Map<String,Object>> toolsUsed = new ArrayList<>();
    return ai.reply(user, room, prompt, toolsUsed);
  }
}