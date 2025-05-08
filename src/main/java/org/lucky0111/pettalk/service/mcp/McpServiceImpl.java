package org.lucky0111.pettalk.service.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.lucky0111.pettalk.assistants.McpUserAssistant;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpServiceImpl implements McpService {
    private final McpUserAssistant mcpUserAssistant;

    @Override
    public String userChat(String prompt) {
        log.info("user prompt: {}", prompt);
        return mcpUserAssistant.chat(prompt);
    }
}
