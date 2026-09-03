package com.chatapp.model;

import java.util.List;

public class ChatMessagesResponse {

    private Long currentUserId;

    private Chat chat;

    private List<Message> messages;


    public ChatMessagesResponse() {
    }


    public ChatMessagesResponse(
            Long currentUserId,
            Chat chat,
            List<Message> messages
    ) {

        this.currentUserId = currentUserId;
        this.chat = chat;
        this.messages = messages;
    }


    public Long getCurrentUserId() {
        return currentUserId;
    }


    public void setCurrentUserId(
            Long currentUserId
    ) {

        this.currentUserId = currentUserId;
    }


    public Chat getChat() {
        return chat;
    }


    public void setChat(Chat chat) {
        this.chat = chat;
    }


    public List<Message> getMessages() {
        return messages;
    }


    public void setMessages(
            List<Message> messages
    ) {

        this.messages = messages;
    }
}