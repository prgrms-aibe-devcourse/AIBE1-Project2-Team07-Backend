package org.lucky0111.pettalk.service.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.lucky0111.pettalk.assistants.McpTagAssistant;
import org.lucky0111.pettalk.assistants.McpUserAssistant;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpServiceImpl implements McpService {
    private final McpUserAssistant mcpUserAssistant;
    private final McpTagAssistant mcpTagAssistant;

    @Override
    public String userChat(String prompt) {
        log.info("user prompt: {}", prompt);
        return mcpUserAssistant.chat(prompt);
    }

    @Override
    public List<String> makeTagListForTrainer(String specializationText, String representativeCareer, String introduction) {

        String prompt = String.format(
                """
                ### 태그 생성 순서
                1. **DB에 저장된 태그 목록을 요청합니다.** 툴 이름: getTagsInDB
                2. DB에 있는 태그 목록을 기반으로 사용자가 제공한 정보에 맞는 태그를 생성합니다.
                **DB에 저장된 태그 목록을 요청**하여 DB에 있는 태그 목록을 기반으로 훈련사 정보를 태그로 생성하세요.
                태그 변환해야할 훈련사 정보:
                Specialization: %s
                Representative Career: %s
                Introduction: %s
                Specialization, Representative Career, Introduction를 전부 기반으로 훈련사 정보를 태그로 생성하세요.
                """
                , specializationText, representativeCareer, introduction);
        log.info("prompt: {}", prompt);

        String tagListString = mcpTagAssistant.tag(prompt);
        log.info("tagListString: {}", tagListString);

        List<String> tagList = stringToList(tagListString);
        log.info("tagList: {}", tagList);

        return tagList;
    }

    public static List<String> stringToList(String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 마크다운 코드 블록 제거
        String cleaned = input.replaceAll("```\\w*\\n|```", "");

        // 2. JSON 배열 형식 감지 및 처리
        if (cleaned.trim().startsWith("[") && cleaned.trim().endsWith("]")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(cleaned, new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
                // JSON 파싱 실패 시 계속 진행
            }
        }

        // 3. 이스케이프된 JSON 문자열 처리 (["배변", "소형견"]와 같은 형식)
        if (cleaned.contains("\\\"")) {
            String unescaped = cleaned.replace("\\\"", "\"");
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(unescaped, new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
                // 실패 시 계속 진행
            }
        }

        // 4. JSON 형식이 아닌 경우 쉼표로 분리
        // 대괄호나 따옴표 제거
        String withoutJsonSyntax = cleaned
                .replaceAll("^\\s*\\[|\\]\\s*$", "") // 앞뒤 대괄호 제거
                .replaceAll("\"", ""); // 모든 따옴표 제거

        // 쉼표로 분리하고 정리
        return Arrays.stream(withoutJsonSyntax.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> makeTagListForPost(String title, String content, String tags) {

        String prompt = String.format(
                """
                ### 태그 생성 순서
                1. **DB에 저장된 태그 목록을 요청합니다.** 툴 이름: getTagsInDB
                2. DB에 있는 태그 목록을 기반으로 사용자가 제공한 정보에 맞는 태그를 생성합니다.
                **DB에 저장된 태그 목록을 요청**하여 DB에 있는 태그 목록을 기반으로 게시글 정보를 태그로 생성하세요.
                태그 변환해야할 게시글 정보:
                title: %s
                content Career: %s
                tags: %s
                title, content, tags 전부 기반으로 게시글 정보를 태그로 생성하세요.
                """
                , title, content, tags);
        log.info("prompt: {}", prompt);

        String tagListString = mcpTagAssistant.tag(prompt);
        log.info("tagListString: {}", tagListString);

        List<String> tagList = stringToList(tagListString);
        log.info("tagList: {}", tagList);

        return tagList;
    }
}
