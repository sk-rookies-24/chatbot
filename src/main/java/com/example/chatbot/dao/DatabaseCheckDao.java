package com.example.chatbot.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DatabaseCheckDao {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseCheckDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> findTop10UserNames() {
        // Oracle 12c 이상이면 아래 쿼리 사용 추천
        String sql = "SELECT USER_NAME FROM USERS FETCH FIRST 10 ROWS ONLY";
        // Oracle 11g 이하면 아래 쿼리 사용
        // String sql = "SELECT USER_NAME FROM USERS WHERE ROWNUM <= 10";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
