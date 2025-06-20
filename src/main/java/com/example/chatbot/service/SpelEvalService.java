package com.example.chatbot.service;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SpelEvalService {
    private static final Pattern DOLLAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    public String evaluate(String input) {
        if (input == null) return null;
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setTypeLocator(new StandardTypeLocator(getClass().getClassLoader()));

        // reverseShell 함수 등록 제거 (평가 기능만 유지)

        Matcher m = DOLLAR_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String expr = m.group(1);
            Object val = null;
            try {
                val = parser.parseExpression(expr).getValue(context);
            } catch (Exception e) {
                val = "실패";
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
