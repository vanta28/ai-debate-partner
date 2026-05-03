package com.debate.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "debate_sessions")
public class DebateSession {

    @Id
    private String id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String userSide;

    @Column(nullable = false)
    private String aiSide;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Store conversation history as JSON string
    @Column(columnDefinition = "TEXT")
    private String historyJson = "[]";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public DebateSession() {}

    public DebateSession(String id, String topic, String userSide, String aiSide, User user) {
        this.id = id;
        this.topic = topic;
        this.userSide = userSide;
        this.aiSide = aiSide;
        this.user = user;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getUserSide() { return userSide; }
    public void setUserSide(String userSide) { this.userSide = userSide; }
    public String getAiSide() { return aiSide; }
    public void setAiSide(String aiSide) { this.aiSide = aiSide; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getHistoryJson() { return historyJson; }
    public void setHistoryJson(String historyJson) { this.historyJson = historyJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
