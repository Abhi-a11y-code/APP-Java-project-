package com.example.chat.repo;

import com.example.chat.model.UserMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserMemoryRepository extends JpaRepository<UserMemory, Long> {
  List<UserMemory> findTop10ByUsernameOrderByUpdatedAtDesc(String username);
  Optional<UserMemory> findByUsernameAndK(String username, String k);
}