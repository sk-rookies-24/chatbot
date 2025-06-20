package com.example.chatbot.service;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("myEvalBean")
public class MyEvalBean {
    private static final Pattern DOLLAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    public String eval(String input) {
        if (input == null) return null;
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 타입 로케이터 추가 (이 부분이 핵심!)
        context.setTypeLocator(new StandardTypeLocator(getClass().getClassLoader()));

        // 1. ${...} 패턴 모두 SpEL로 치환
        Matcher m = DOLLAR_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String expr = m.group(1);
            Object val = null;
            try {
                val = parser.parseExpression(expr).getValue(context); // context 사용
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
                val = parser.parseExpression(expr).getValue(context); // context 사용
            } catch (Exception e) {
                val = "실패";
            }
            result = (val != null ? val.toString() : "") + " " + tail;
        }

        return result;
    }
}
