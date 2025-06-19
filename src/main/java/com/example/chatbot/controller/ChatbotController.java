package com.example.chatbot.controller;

import com.example.chatbot.dto.ChatbotSelectDto;
import com.example.chatbot.dto.ChatbotTypingReqDto;
import com.example.chatbot.service.ChatbotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.expression.ThymeleafEvaluationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("api/chatbot")
@Tag(name = "챗봇 API", description = "챗봇 관련 기능")
public class ChatbotController {

    private final ChatbotService chatbotservice;
    private final SpringTemplateEngine templateEngine;
    private final ApplicationContext applicationContext;

    @Autowired
    public ChatbotController(SpringTemplateEngine templateEngine, ChatbotService chatbotService, ApplicationContext applicationContext) {
        this.templateEngine = templateEngine;
        this.chatbotservice = chatbotService;
        this.applicationContext = applicationContext;
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
        String result = chatbotservice.chat(reqDto).getResult();

        // 2. Thymeleaf WebContext에 값 주입 (Spring Boot 2.x + Thymeleaf 3.0.x)
        ServletContext servletContext = request.getServletContext();
        Locale locale = request.getLocale();
        WebContext context = new WebContext(request, response, servletContext, locale);
        context.setVariable("result", result);

        // ThymeleafEvaluationContext 주입
        context.setVariable(
                ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
                new ThymeleafEvaluationContext(applicationContext, null)
        );

        System.out.println("Value of 'result' passed to template: " + result);

        // 3. template.html의 Result fragment만 렌더링
        String html = templateEngine.process("template", context);
        System.out.println("Rendered HTML: " + html);
        Document doc = Jsoup.parse(html);
        Element fragment = doc.selectFirst("body > div > div");
        if (fragment == null) {
            fragment = doc.selectFirst("div > div");
        }
        String resultText = fragment != null ? fragment.text() : "";
        System.out.println("Extracted resultText: " + resultText);

        // 4. JSON으로 감싸서 반환
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("result", resultText);
        return resultMap;
    }
}
