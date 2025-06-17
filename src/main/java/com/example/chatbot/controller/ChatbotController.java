package com.example.chatbot.controller;


import com.example.chatbot.dto.ChatbotSelectDto;
import com.example.chatbot.dto.ChatbotTypingReqDto;
import com.example.chatbot.dto.ChatbotTypingResDto;
import com.example.chatbot.service.ChatbotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/chatbot")
@Tag(name = "챗봇 API", description = "챗봇 관련 기능")
public class ChatbotController {
//     라우팅 하고 -> service로 넘김 -> service에서 받아오고 response 응답

    private final ChatbotService chatbotservice;

    public ChatbotController(ChatbotService chatbotservice) {
        this.chatbotservice = chatbotservice;
    }
    @GetMapping("/select/{no}")
    public ChatbotSelectDto getAnswer (@PathVariable int no) {
        return chatbotservice.getAnswer(no);
    }

    @PostMapping("/typing")
    public ChatbotTypingResDto chat (@RequestBody ChatbotTypingReqDto reqDto) {
        return chatbotservice.chat(reqDto);
    }



}
