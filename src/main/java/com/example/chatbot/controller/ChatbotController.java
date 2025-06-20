package com.example.chatbot.controller;

import com.example.chatbot.dto.ChatbotSelectDto;
import com.example.chatbot.dto.ChatbotTypingReqDto;
import com.example.chatbot.service.ChatbotService;
import com.example.chatbot.service.SpelEvalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/chatbot")
@Tag(name = "챗봇 API", description = "챗봇 관련 기능")
public class ChatbotController {

    private final ChatbotService chatbotservice;
    private final SpelEvalService spelEvalService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService, SpelEvalService spelEvalService) {
        this.chatbotservice = chatbotService;
        this.spelEvalService = spelEvalService;
    }

    @GetMapping("/select/{no}")
    public ChatbotSelectDto getAnswer(@PathVariable int no) {
        return chatbotservice.getAnswer(no);
    }

    @PostMapping("/typing")
    public Map<String, String> chat(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody ChatbotTypingReqDto reqDto
    ) {
        // 1. Perplexity API에서 응답 받아오기
        String perplexityResult = chatbotservice.chat(reqDto).getResult();

        // 2. SpEL 평가 수행
        String resultText = spelEvalService.evaluate(perplexityResult);

        // 3. 결과를 JSON으로 반환
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("result", resultText);
        return resultMap;
    }
}
