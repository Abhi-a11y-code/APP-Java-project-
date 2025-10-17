package com.example.chat.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;
    private String sender;
    @Column(length = 4000)
    private String content;
    private Instant createdAt;
    private boolean ai;

    public Message() {}

    public Message(String roomName, String sender, String content, boolean ai) {
        this.roomName = roomName;
        this.sender = sender;
        this.content = content;
        this.createdAt = Instant.now();
        this.ai = ai;
    }

    public Long getId() { return id; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public boolean isAi() { return ai; }
    public void setAi(boolean ai) { this.ai = ai; }
}
