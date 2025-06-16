package com.example.chatbot.dto;

public class ChatbotTypingReqDto {
    private String prompt;

    public ChatbotTypingReqDto() {}

    public ChatbotTypingReqDto(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
