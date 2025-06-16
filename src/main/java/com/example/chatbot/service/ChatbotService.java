package com.example.chatbot.service;

import com.example.chatbot.dto.ChatbotSelectDto;
import org.springframework.stereotype.Service;

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
}
