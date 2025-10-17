package com.example.chat.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="user_memory")
public class UserMemory {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  String username;
  String k;
  @Column(columnDefinition="TEXT") String v;
  Instant updatedAt = Instant.now();
  // getters/setters
}