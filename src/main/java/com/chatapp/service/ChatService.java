package com.chatapp.service;

import com.chatapp.dao.ChatDAO;
import com.chatapp.model.Chat;

import java.util.List;

public class ChatService {

    private final ChatDAO chatDAO;

    public ChatService() {

        this.chatDAO =
                new ChatDAO();
    }

    public List<Chat> getUserChats(
            Long userId,
            String filter
    ) {

        if (
                userId == null ||
                        userId <= 0
        ) {

            return List.of();
        }


        return chatDAO.findChatsByUserId(
                userId,
                filter
        );
    }
}