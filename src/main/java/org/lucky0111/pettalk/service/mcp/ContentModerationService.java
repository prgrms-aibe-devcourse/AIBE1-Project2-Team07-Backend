package org.lucky0111.pettalk.service.mcp;

public interface ContentModerationService {

    boolean isSafeContent(String content);

    String filterContent(String content);
}
