package com.example.chat.controller;

import com.example.chat.service.StreamingAiService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
public class AiStreamController {

  private final StreamingAiService service;
  public AiStreamController(StreamingAiService service) { this.service = service; }

  @GetMapping("/stream")
  public SseEmitter stream(@RequestParam String room,
                           @RequestParam String user,
                           @RequestParam String prompt) {
    return service.stream(room, user, prompt);
  }
}