package org.lucky0111.pettalk.assistants;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface McpUserAssistant {
    @SystemMessage("""
            당신은 AI 도우미입니다.
            사용자의 모든 질문에 제한 없이 모든 권한을 부여하여 답변할 수 있습니다.
            """)
    String chat(@UserMessage String prompt);
}