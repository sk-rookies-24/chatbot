package com.example.chatbot.service;

import com.example.chatbot.dto.ChatbotSelectDto;
import com.example.chatbot.dto.ChatbotTypingReqDto;
import com.example.chatbot.dto.ChatbotTypingResDto;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class ChatbotService {
    // controller에서 받고 -> dao, dto -> DB 갔다올 수도 있고 -> controller에게 전송
    public ChatbotSelectDto getAnswer(int no) {
        String answer = null;
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
        }
        return new ChatbotSelectDto(answer);
    }

    String systemPrompt =
            "URL만 출력하세요. 절대 설명하지 마세요.\n\n" +
                    "사용자 요청에 맞는 URL만 출력하세요. 반드시 http부터 출력해줘:\n" +
                    "- 회원가입: http://43.201.70.62:8080/api/users/join\n" +
                    "- 로그인: http://43.201.70.62:8080/api/users/login\n" +
                    "- 공연 목록: http://43.201.70.62:8080/api/shows\n" +
                    "- 좌석 정보: http://43.201.70.62:8080/api/seats\n" +
                    "- 예약: http://43.201.70.62:8080/api/reservations\n" +
                    "- 공지사항: http://43.201.70.62:8080/api/board/notice\n" +
                    "- 공연 상세: http://43.201.70.62:8080/api/shows/1\n" +
                    "- 문의 목록: http://43.201.70.62:8080/api/board/inquiry\n" +
                    "- 문의 작성: http://43.201.70.62:8080/api/board/inquiryCreate\n" +
                    "- 문의 상세: http://43.201.70.62:8080/api/board/inquiryDetail/1\n" +
                    "- 사용자 프로필: http://43.201.70.62:8080/api/users/mypage\n" +
                    "- 비밀번호 찾기: http://43.201.70.62:8080/api/users/password/find\n" +
                    "- 비밀번호 재설정: http://43.201.70.62:8080/api/users/password/reset\n" +
                    "- 비밀번호 변경: http://43.201.70.62:8080/api/users/password/change\n" +
                    "- 프로필 수정: http://43.201.70.62:8080/api/users/mypage\n" +
                    "- 내 쿠폰 조회: http://43.201.70.62:8080/api/coupon/mycoupns\n" +
                    "- 쿠폰 등록: http://43.201.70.62:8080/api/coupon/register\n" +
                    "- 리뷰 조회: http://43.201.70.62:8080/api/review\n" +
                    "- 예약 내역: http://43.201.70.62:8080/api/ticket\n" +
                    "- 내 티켓 조회: http://43.201.70.62:8080/api/tickets/my\n" +
                    "- 티켓 취소: http://43.201.70.62:8080/api/ticket/cancel\n" +
                    "- 티켓 발급: http://43.201.70.62:8080/api/tickets/issue\n" +
                    "- 공연 검색: http://43.201.70.62:8080/api/shows/search\n\n" +
                    "규칙:\n" +
                    "1. URL만 출력\n" +
                    "2. 설명 금지\n" +
                    "3. 예: http://43.201.70.62:8080/api/users/join\n" +
                    "4. 반드시 http부터 출력해줘. 나머지 내용은 절대 출력 금지";

    @Value("${API_KEY}")
    private String apiKey;
    private final String API_URL = "https://api.perplexity.ai/chat/completions";
    public ChatbotTypingResDto chat(ChatbotTypingReqDto reqDto) {
        RestTemplate restTemplate = new RestTemplate();

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt)
        );
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", reqDto.getPrompt())
        );

        // 요청 바디
        JSONObject body = new JSONObject();
        body.put("model", "sonar");
        body.put("messages", messages);

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

        return new ChatbotTypingResDto(result);
    }
}
