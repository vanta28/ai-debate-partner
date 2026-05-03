package com.debate.service;

import com.debate.model.dto.Dtos.*;
import com.debate.model.entity.ChatMessage;
import com.debate.model.entity.DebateSession;
import com.debate.model.entity.User;
import com.debate.repository.DebateSessionRepository;
import com.debate.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DebateService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final DebateSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    public DebateService(WebClient.Builder webClientBuilder,
                         ObjectMapper objectMapper,
                         DebateSessionRepository sessionRepository,
                         UserRepository userRepository) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public DebateResponse startDebate(String topic, String userSide, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String sessionId = UUID.randomUUID().toString();
        String aiSide = userSide.equalsIgnoreCase("FOR") ? "AGAINST" : "FOR";

        DebateSession session = new DebateSession(sessionId, topic, userSide, aiSide, user);

        // Add opening prompt to history
        List<ChatMessage> history = new ArrayList<>();
        history.add(new ChatMessage("user", "Please give your passionate opening statement for this debate. Be compelling, use strong arguments, and set the stage."));

        String aiReply = callGroq(topic, aiSide, userSide, history);
        history.add(new ChatMessage("assistant", aiReply));

        session.setHistoryJson(toJson(history));
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return new DebateResponse(sessionId, aiReply, topic, userSide, aiSide);
    }

    public DebateResponse sendMessage(String sessionId, String userMessage, Long userId) {
        DebateSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        List<ChatMessage> history = fromJson(session.getHistoryJson());
        history.add(new ChatMessage("user", userMessage));

        String aiReply = callGroq(session.getTopic(), session.getAiSide(), session.getUserSide(), history);
        history.add(new ChatMessage("assistant", aiReply));

        session.setHistoryJson(toJson(history));
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return new DebateResponse(sessionId, aiReply, session.getTopic(), session.getUserSide(), session.getAiSide());
    }

    public List<DebateSession> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void deleteSession(String sessionId, Long userId) {
        DebateSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        sessionRepository.delete(session);
    }

    private String callGroq(String topic, String aiSide, String userSide, List<ChatMessage> history) {
        try {
            ArrayNode messagesArray = objectMapper.createArrayNode();

            // System message
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", buildSystemPrompt(topic, aiSide, userSide));
            messagesArray.add(systemMsg);

            // Conversation history
            for (ChatMessage msg : history) {
                ObjectNode msgNode = objectMapper.createObjectNode();
                msgNode.put("role", msg.getRole());
                msgNode.put("content", msg.getContent());
                messagesArray.add(msgNode);
            }

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_completion_tokens", 1024);
            requestBody.set("messages", messagesArray);

            String responseBody = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(responseBody);
            return responseJson.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Groq API: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(String topic, String aiSide, String userSide) {
        return String.format("""
                You are a sharp, passionate, and well-prepared debate partner.

                DEBATE TOPIC: "%s"
                YOUR SIDE: %s (arguing %s this topic)
                OPPONENT'S SIDE: %s (arguing %s this topic)

                Your role is to argue your position with conviction, logic, and rhetorical skill.
                - Use facts, statistics, historical examples, and logical reasoning
                - Directly counter the opponent's arguments when they make them
                - Be assertive but not rude — this is a respectful intellectual debate
                - Keep responses focused (3-5 sentences to a short paragraph)
                - Occasionally use rhetorical techniques: analogies, reductio ad absurdum, etc.
                - Never concede your position or agree with the opponent
                - End each response with a pointed question or challenge back to the opponent
                """,
                topic, aiSide,
                aiSide.equalsIgnoreCase("FOR") ? "in favor of" : "against",
                userSide,
                userSide.equalsIgnoreCase("FOR") ? "in favor of" : "against"
        );
    }

    private String toJson(List<ChatMessage> history) {
        try {
            return objectMapper.writeValueAsString(history);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<ChatMessage> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ChatMessage>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
