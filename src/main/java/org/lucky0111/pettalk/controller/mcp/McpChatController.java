package org.lucky0111.pettalk.controller.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.mcp.McpTagDTO;
import org.lucky0111.pettalk.service.mcp.McpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp")
@RequiredArgsConstructor
@Slf4j
public class McpChatController {
    private final McpService mcpService;

    @GetMapping("/chat")
    public ResponseEntity<?> userChat(@RequestParam String prompt) {
        return ResponseEntity.ok(Map.of("message", mcpService.userChat(prompt)));
    }

    @GetMapping("/tag/trainer")
    public ResponseEntity<?> makeTagListForTrainer(@RequestParam String specializationText,
                                                    @RequestParam String representativeCareer,
                                                    @RequestParam String introduction) {
        return ResponseEntity.ok(Map.of("tags", mcpService.makeTagListForTrainer(specializationText, representativeCareer, introduction)));
    }

    @PostMapping("/tag/post")
    public ResponseEntity<?> makeTagListForPost(@RequestBody McpTagDTO mcpTagDTO) {
        return ResponseEntity.ok(Map.of("tags",
                mcpService.makeTagListForPost(mcpTagDTO.title(), mcpTagDTO.content())));
    }
}
