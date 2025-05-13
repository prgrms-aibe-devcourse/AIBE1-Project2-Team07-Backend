package org.lucky0111.pettalk.config.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.lucky0111.pettalk.assistants.McpModerationAssistant;
import org.lucky0111.pettalk.assistants.McpTagAssistant;
import org.lucky0111.pettalk.assistants.McpUserAssistant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class McpAssistantConfig {

    @Value("${langchain4j.mcp.sse-url}")
    private String sseUrl;

    @Bean
    public McpTransport transport() {
        return new HttpMcpTransport.Builder()
                .sseUrl(sseUrl)
                .timeout(Duration.ofSeconds(600))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public McpClient mcpClient(McpTransport transport) {
        return new DefaultMcpClient.Builder()
                .transport(transport)
                .build();
    }

    @Bean
    public McpToolProvider toolProvider(McpClient mcpClient) {
        return McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }

    @Bean
    public McpUserAssistant mcpUserAssistant(ChatModel chatLanguageModel,
                                             McpToolProvider toolProvider, ChatMemory chatMemory) {
        return AiServices.builder(McpUserAssistant.class)
                .chatModel(chatLanguageModel)
                .toolProvider(toolProvider)
                .chatMemory(chatMemory)
                .build();
    }

    @Bean
    public McpTagAssistant mcpTagAssistant(ChatModel chatLanguageModel,
                                           McpToolProvider toolProvider) {
        return AiServices.builder(McpTagAssistant.class)
                .chatModel(chatLanguageModel)
                .toolProvider(toolProvider)
                .build();
    }

    @Bean
    public McpModerationAssistant mcpModerationAssistant(ChatModel chatLanguageModel) {
        return AiServices.builder(McpModerationAssistant.class)
                .chatModel(chatLanguageModel)
                .build();
    }
}