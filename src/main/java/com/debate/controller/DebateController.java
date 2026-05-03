package com.debate.controller;

import com.debate.model.dto.Dtos.*;
import com.debate.model.entity.DebateSession;
import com.debate.service.DebateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debate")
public class DebateController {

    private final DebateService debateService;

    public DebateController(DebateService debateService) {
        this.debateService = debateService;
    }

    @PostMapping("/start")
    public ResponseEntity<DebateResponse> startDebate(@Valid @RequestBody StartDebateRequest request,
                                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(debateService.startDebate(request.getTopic(), request.getUserSide(), userId));
    }

    @PostMapping("/message")
    public ResponseEntity<DebateResponse> sendMessage(@Valid @RequestBody SendMessageRequest request,
                                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(debateService.sendMessage(request.getSessionId(), request.getMessage(), userId));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<DebateSession>> getSessions(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(debateService.getUserSessions(userId));
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId,
                                               HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        debateService.deleteSession(sessionId, userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), 400));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleError(RuntimeException e) {
        return ResponseEntity.internalServerError().body(new ErrorResponse(e.getMessage(), 500));
    }
}
