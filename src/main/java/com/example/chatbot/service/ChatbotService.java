package com.example.chatbot.service;

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

    String systemPrompt =
            "너는 루키즈 티켓 서비스의 대화형 챗봇이야. 사용자가 입력한 질문에 대해서 스스로 정확하게 판단한 후, 알맞은 답변을 제공해줘야해.\n\n" +
                    "만약 사용자가 아래에 있는 항목을 요청할 경우 맞은 URL을 출력해줘.\n" +
                    "- 공연 목록: http://43.201.70.62:8080/api/shows\n" +
                    "- 예약: http://43.201.70.62:8080/api/reservations\n" +
                    "- 공지사항: http://43.201.70.62:8080/api/board/notice\n" +
                    "- 문의 목록: http://43.201.70.62:8080/api/board/inquiry\n" +
                    "- 사용자 프로필: http://43.201.70.62:8080/api/users/mypage\n" +
                    "- 내 쿠폰 조회: http://43.201.70.62:8080/api/coupon/mycoupns\n" +
                    "- 쿠폰 등록: http://43.201.70.62:8080/api/coupon/register\n" +
                    "- 예약 내역: http://43.201.70.62:8080/api/ticket\n" +
                    "- 공연 검색: http://43.201.70.62:8080/api/shows/search\n\n" +
                    "답변 규칙은 아래와 같아\n" +
                    "1. http부터 출력\n" +
                    "2. 추가적인 설명 금지\n" +
                    "3. 만약 위에있는 URL들 중 답변이 없는 것 같으면 사용자가 원하는 답변을 만들어서 보내줘";


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
