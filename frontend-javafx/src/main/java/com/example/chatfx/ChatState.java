package com.example.chatfx;

public class ChatState {
    private static final ChatState INSTANCE = new ChatState();
    private String username;
    private ChatState(){}
    public static ChatState get(){ return INSTANCE; }
    public void setUsername(String u){ this.username = u; }
    public String getUsername(){ return this.username; }
}
