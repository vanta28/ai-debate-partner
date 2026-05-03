package com.debate.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Dtos {

    public static class RegisterRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
        @NotBlank
        private String username;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthResponse {
        private String token;
        private String username;
        private String email;

        public AuthResponse(String token, String username, String email) {
            this.token = token;
            this.username = username;
            this.email = email;
        }

        public String getToken() { return token; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }

    public static class StartDebateRequest {
        @NotBlank
        private String topic;
        @NotBlank
        private String userSide;

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getUserSide() { return userSide; }
        public void setUserSide(String userSide) { this.userSide = userSide; }
    }

    public static class SendMessageRequest {
        @NotBlank
        private String sessionId;
        @NotBlank
        private String message;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class DebateResponse {
        private String sessionId;
        private String reply;
        private String topic;
        private String userSide;
        private String aiSide;

        public DebateResponse(String sessionId, String reply, String topic, String userSide, String aiSide) {
            this.sessionId = sessionId;
            this.reply = reply;
            this.topic = topic;
            this.userSide = userSide;
            this.aiSide = aiSide;
        }

        public String getSessionId() { return sessionId; }
        public String getReply() { return reply; }
        public String getTopic() { return topic; }
        public String getUserSide() { return userSide; }
        public String getAiSide() { return aiSide; }
    }

    public static class ErrorResponse {
        private String message;
        private int status;

        public ErrorResponse(String message, int status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() { return message; }
        public int getStatus() { return status; }
    }
}
