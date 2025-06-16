package com.example.chatbot.controller;

import com.example.chatbot.service.DatabaseCheckService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DatabaseCheckController {
    private final DatabaseCheckService databaseCheckService;

    public DatabaseCheckController(DatabaseCheckService databaseCheckService) {
        this.databaseCheckService = databaseCheckService;
    }

    @GetMapping("/db-check/top10-users")
    public List<String> getTop10UserNames() {
        return databaseCheckService.getTop10UserNames();
    }
}
