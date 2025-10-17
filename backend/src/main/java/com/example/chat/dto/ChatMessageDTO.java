package com.example.chat.dto;

public class ChatMessageDTO {
    public String type;     // "message" | "join"
    public String room;
    public String sender;
    public String content;
}
