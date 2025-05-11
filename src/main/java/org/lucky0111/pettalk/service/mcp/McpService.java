package org.lucky0111.pettalk.service.mcp;

import java.util.List;

public interface McpService {
    String userChat(String prompt);

    List<String> makeTagListForTrainer(String specializationText, String representativeCareer, String introduction);

    public List<String> makeTagListForPost(String title, String content, String tags);
}
