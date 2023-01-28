package com.example.demo.Service;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface UserServiceInt {

    void saveUser(Message msg);
    void acceptRegister(Long chatId);
    void personBooks(Long chatId, Long bookId);
    boolean isAdmin(Long chatId);
}
