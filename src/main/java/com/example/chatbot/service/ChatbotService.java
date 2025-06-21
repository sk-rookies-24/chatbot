package com.example.chatbot.service;

import com.example.chatbot.dto.ChatbotSelectDto;
import com.example.chatbot.dto.ChatbotTypingReqDto;
import com.example.chatbot.dto.ChatbotTypingResDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Value("${API_KEY}")
    private String apiKey;

    private final String API_URL = "https://api.perplexity.ai/chat/completions";

    // 외부 파일에서 로드될 시스템 프롬프트
    private String enhancedSystemPrompt;

    /**
     * 서비스 초기화 시 프롬프트 파일을 로드합니다.
     */
    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/system-prompt.txt");
            enhancedSystemPrompt = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            logger.info("system prompt loaded successfully from external file");
        } catch (IOException e) {
            logger.error("Failed to load system prompt file", e);
            throw new RuntimeException("시스템 프롬프트 파일을 읽을 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 번호 기반 답변 (하위 호환성 유지)
     */
    public ChatbotSelectDto getAnswer(int no) {
        String answer;
        switch (no) {
            case 1:
                answer = "1번 질문에 대한 답변입니다.";
                break;
            case 2:
                answer = "2번 질문에 대한 답변입니다.";
                break;
            case 3:
                answer = "3번 질문에 대한 답변입니다.";
                break;
            default:
                answer = "지원되지 않는 질문 번호입니다.";
                break;
        }
        return new ChatbotSelectDto(answer);
    }

    /**
     * 향상된 AI 채팅 응답 처리
     */
    public ChatbotTypingResDto chat(ChatbotTypingReqDto reqDto) {
        try {
            // 입력 유효성 검증
            if (!isValidRequest(reqDto.getPrompt())) {
                return new ChatbotTypingResDto("죄송합니다. 이해할 수 없는 요청입니다. 다시 시도해 주세요.");
            }

            RestTemplate restTemplate = new RestTemplate();
            JSONArray messages = new JSONArray();

            // 외부 파일에서 로드된 시스템 프롬프트 사용
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", enhancedSystemPrompt)
            );

            // 사용자 쿼리 향상
            String enhancedQuery = enhanceUserQuery(reqDto.getPrompt());
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", enhancedQuery)
            );

            // 요청 바디 구성
            JSONObject body = new JSONObject();
            body.put("model", "sonar");
            body.put("messages", messages);
            body.put("max_tokens", 100);
            body.put("temperature", 0.1);

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 응답 파싱
            JSONObject responseBody = new JSONObject(response.getBody());
            JSONArray choices = responseBody.getJSONArray("choices");
            String result = choices.getJSONObject(0).getJSONObject("message").getString("content");

            // URL 유효성 검증
            if (isValidUrl(result)) {
                return new ChatbotTypingResDto(result);
            } else {
                logger.warn("Invalid URL returned: {}", result);
                return new ChatbotTypingResDto("죄송합니다. 적절한 서비스를 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            logger.error("Error in chat processing: ", e);
            return new ChatbotTypingResDto("죄송합니다. 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    /**
     * 사용자 쿼리를 더 명확하게 만드는 헬퍼 메서드
     */
    private String enhanceUserQuery(String userQuery) {
        return "다음 요청에 가장 적합한 API URL을 찾아주세요: \"" + userQuery + "\"\n" +
                "분석 과정:\n" +
                "1. 사용자가 원하는 기능 파악\n" +
                "2. 해당 기능에 맞는 API 선택\n" +
                "3. URL만 응답\n\n" +
                "응답:";
    }

    /**
     * 요청 유효성 검증
     */
    private boolean isValidRequest(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        String lowerQuery = query.toLowerCase().trim();
        return lowerQuery.length() >= 2 && (
                containsAny(lowerQuery, "로그인", "가입", "회원", "프로필", "정보", "비밀번호", "login", "join", "profile", "password") ||
                        containsAny(lowerQuery, "공연", "콘서트", "뮤지컬", "연극", "티켓", "예매", "좌석", "show", "concert", "ticket", "seat") ||
                        containsAny(lowerQuery, "예약", "예매", "취소", "내역", "확인", "reservation", "booking", "cancel", "history") ||
                        containsAny(lowerQuery, "공지", "문의", "리뷰", "후기", "notice", "inquiry", "review") ||
                        containsAny(lowerQuery, "쿠폰", "할인", "coupon", "discount") ||
                        containsAny(lowerQuery, "보기", "확인", "조회", "검색", "찾기", "등록", "추가", "수정", "변경",
                                "view", "check", "search", "find", "register", "add", "edit", "change")
        );
    }

    /**
     * URL 유효성 검증
     */
    private boolean isValidUrl(String url) {
        return url != null && url.trim().startsWith("http://43.201.70.62:8080");
    }

    /**
     * 키워드 포함 여부 확인 헬퍼 메서드
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
