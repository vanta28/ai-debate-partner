package com.debate.repository;

import com.debate.model.entity.DebateSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DebateSessionRepository extends JpaRepository<DebateSession, String> {
    List<DebateSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<DebateSession> findByIdAndUserId(String id, Long userId);
}
