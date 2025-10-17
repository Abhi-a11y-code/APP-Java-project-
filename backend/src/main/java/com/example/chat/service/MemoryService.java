package com.example.chat.service;

import com.example.chat.model.UserMemory;
import com.example.chat.repo.UserMemoryRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MemoryService {
  private final UserMemoryRepository repo;
  public MemoryService(UserMemoryRepository repo) { this.repo = repo; }

  public void remember(String user, String key, String value) {
    var existing = repo.findByUsernameAndK(user, key).orElse(null);
    if (existing == null) existing = new UserMemory();
    existing.setUsername(user); existing.setK(key); existing.setV(value);
    existing.setUpdatedAt(java.time.Instant.now());
    repo.save(existing);
  }

  public List<String> facts(String user) {
    var rows = repo.findTop10ByUsernameOrderByUpdatedAtDesc(user);
    List<String> out = new ArrayList<>();
    for (var r : rows) out.add(r.getK() + ": " + r.getV());
    return out;
  }
}