package com.example.chat.controller;

import com.example.chat.model.Message;
import com.example.chat.repo.MessageRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/{room}")
    public List<Message> recent(@PathVariable String room) {
        return messageRepository.findTop50ByRoomNameOrderByCreatedAtDesc(room);
    }
}
