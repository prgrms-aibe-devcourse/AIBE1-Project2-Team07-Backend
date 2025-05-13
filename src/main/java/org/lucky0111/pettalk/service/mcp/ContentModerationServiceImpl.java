package org.lucky0111.pettalk.service.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.assistants.McpModerationAssistant;
import org.lucky0111.pettalk.service.mcp.ContentModerationService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentModerationServiceImpl implements ContentModerationService {

    private final McpModerationAssistant moderationAssistant;

    @Override
    public boolean isSafeContent(String content) {
        try {
            String result = moderationAssistant.moderateContent(content);
            return "SAFE".equalsIgnoreCase(result.trim());
        } catch (Exception e) {
            log.error("콘텐츠 모더레이션 중 오류 발생", e);
            return false; // 오류 발생 시 안전을 위해 unsafe로 처리
        }
    }

    @Override
    public String filterContent(String content) {
        try {
            return moderationAssistant.filterContent(content);
        } catch (Exception e) {
            log.error("콘텐츠 필터링 중 오류 발생", e);
            return "필터링 중 오류가 발생했습니다. 다시 시도해 주세요.";
        }
    }
}