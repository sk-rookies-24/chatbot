package com.example.chatbot.service;

import com.example.chatbot.dao.DatabaseCheckDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseCheckService {
    private final DatabaseCheckDao databaseCheckDao;

    public DatabaseCheckService(DatabaseCheckDao databaseCheckDao) {
        this.databaseCheckDao = databaseCheckDao;
    }

    public List<String> getTop10UserNames() {
        return databaseCheckDao.findTop10UserNames();
    }
}
