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
    private final ContentModerationService contentModerationService;

    @Override
    public String userChat(String prompt) {

        log.info("user prompt: {}", prompt);

        // 1. 입력 콘텐츠 모더레이션
        if (!contentModerationService.isSafeContent(prompt)) {
            log.warn("Unsafe content detected in user prompt");
            return "해당 내용은 서비스 정책에 맞지 않습니다. 다른 질문을 해주세요.";
        }

        // 2. Gemini로 응답 생성
        String response = mcpUserAssistant.chat(prompt);

        // 3. 출력 콘텐츠 모더레이션 (필요한 경우)
        if (!contentModerationService.isSafeContent(response)) {
            log.warn("Unsafe content detected in AI response");
            return contentModerationService.filterContent(response);
        }

        return response;
    }

    @Override
    public List<String> makeTagListForTrainer(String specializationText, String representativeCareer, String introduction) {

        String prompt = String.format(
                """
                ### 태그 생성 시작
                1. 반드시 먼저 'getTagsInDB' 도구를 호출하여 DB에 저장된 태그 목록을 가져오세요.
                2. 가져온 DB 태그 목록에서만 훈련사 정보에 맞는 태그를 선택하세요.
                
                ### 태그 생성 순서
                1. **DB에 저장된 태그 목록을 요청합니다.** 툴 이름: getTagsInDB
                2. DB에 있는 태그 목록을 기반으로 사용자가 제공한 정보에 맞는 태그를 생성합니다.
                **DB에 저장된 태그 목록을 요청**하여 DB에 있는 태그 목록을 기반으로 훈련사 정보를 태그로 생성하세요.
                
                ### 훈련사 정보
                전문 분야: %s
                대표 경력: %s
                소개: %s
    
                ### 태그 선택 지침
                - 반드시 먼저 'getTagsInDB' 도구를 호출하세요.
                - 훈련사의 전문 분야, 대표 경력, 소개 정보를 모두 분석하세요.
                - 분석한 정보와 관련된 태그를 DB 태그 목록에서만 선택하세요.
                - 반려동물 종류, 훈련 분야, 문제 행동, 훈련 방법과 관련된 태그에 집중하세요.
                - 태그는 DB에 있는 형태 그대로 사용하세요. 변형하지 마세요.
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
                ### 태그 생성 시작
                1. 반드시 먼저 'getTagsInDB' 도구를 호출하여 DB에 저장된 태그 목록을 가져오세요.
                2. 가져온 DB 태그 목록에서만 게시글 정보에 맞는 태그를 선택하세요.
                
                ### 태그 생성 순서
                1. **DB에 저장된 태그 목록을 요청합니다.** 툴 이름: getTagsInDB
                2. DB에 있는 태그 목록을 기반으로 사용자가 제공한 정보에 맞는 태그를 생성합니다.
                **DB에 저장된 태그 목록을 요청**하여 DB에 있는 태그 목록을 기반으로 게시글 정보를 태그로 생성하세요.
                
                태그 변환해야할 게시글 정보:
                title: %s
                content Career: %s
                tags: %s
                
                ### 태그 선택 지침
                - 반드시 먼저 'getTagsInDB' 도구를 호출하세요.
                - 게시글의 제목, 내용, 기존 태그 정보를 모두 분석하세요.
                - 분석한 정보와 관련된 태그를 DB 태그 목록에서만 선택하세요.
                - 반려동물 종류(강아지, 고양이 등), 문제 행동(분리불안, 짖음 등), 훈련 방법(기본훈련, 행동교정 등)과 관련된 태그에 집중하세요.
                - 기존 태그가 있다면 이를 우선적으로 고려하되, DB에 있는 태그만 선택하세요.
                - 태그는 DB에 있는 형태 그대로 사용하세요. 변형하거나 새로 만들지 마세요.
                - 게시글의 주제와 가장 관련성 높은 태그를 5개 이내로 선택하세요.
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
