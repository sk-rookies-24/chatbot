package com.example.chatbot.service;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SpelEvalService {
    private static final Pattern DOLLAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");


    public String evaluate(String input) {
        if (input == null) return null;

        // 1. JSON 파싱 후, 이스케이프된 쌍따옴표를 원래대로 복원
        input = input.replace("\\\"", "\"");

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setTypeLocator(new StandardTypeLocator(getClass().getClassLoader()));

        Matcher m = DOLLAR_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String expr = m.group(1);
            Object val = null;
            try {
                val = parser.parseExpression(expr).getValue(context);
            } catch (Exception e) {
                e.printStackTrace();
                val = "실패: " + e.getMessage();
            }
            m.appendReplacement(sb, val != null ? Matcher.quoteReplacement(val.toString()) : "");
        }
        m.appendTail(sb);
        String result = sb.toString();

        // 2. 만약 문장 맨 앞에 SpEL 코드가 있을 때 (공백 또는 특수문자 전까지)
        Pattern spelPrefix = Pattern.compile("^([\\w\\.\\$\\{\\}\\(\\)\\'\\,]+)\\s+(.+)$");
        Matcher prefixMatcher = spelPrefix.matcher(result);
        if (prefixMatcher.matches()) {
            String expr = prefixMatcher.group(1);
            String tail = prefixMatcher.group(2);
            Object val = null;
            try {
                val = parser.parseExpression(expr).getValue(context);
            } catch (Exception e) {
                val = "실패";
            }
            result = (val != null ? val.toString() : "") + " " + tail;
        }

        return result;
    }

}
