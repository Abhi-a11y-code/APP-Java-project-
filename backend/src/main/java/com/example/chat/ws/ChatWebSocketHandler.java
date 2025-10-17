package com.example.chat.ws;

import com.example.chat.dto.ChatMessageDTO;
import com.example.chat.model.Message;
import com.example.chat.repo.MessageRepository;
import com.example.chat.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final MessageRepository messageRepository;
    private final AiService aiService;

    public ChatWebSocketHandler(MessageRepository messageRepository, AiService aiService) {
        this.messageRepository = messageRepository;
        this.aiService = aiService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        ChatMessageDTO dto = mapper.readValue(message.getPayload(), ChatMessageDTO.class);

        // Persist user message
        if (dto != null && "message".equalsIgnoreCase(dto.type)) {
            messageRepository.save(new Message(dto.room, dto.sender, dto.content, false));
        }

        // Broadcast to all
        broadcast(message.getPayload());

        // AI helper: trigger when content starts with "@ai "
        if (dto != null && dto.content != null && dto.content.trim().toLowerCase().startsWith("@ai ")) {
            String replyText = aiService.reply(dto.content.trim().substring(4));
            ChatMessageDTO bot = new ChatMessageDTO();
            bot.type = "message";
            bot.room = dto.room;
            bot.sender = "AI Helper";
            bot.content = replyText;

            String botJson = mapper.writeValueAsString(bot);
            messageRepository.save(new Message(bot.room, bot.sender, bot.content, true));
            broadcast(botJson);
        }
    }

    private void broadcast(String payload) throws IOException {
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }
}
