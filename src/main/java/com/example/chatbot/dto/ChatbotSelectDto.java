package com.example.chatbot.dto;

public class ChatbotSelectDto {
    private int no;
    private String answer;

    public ChatbotSelectDto() {}

    public ChatbotSelectDto(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

}
