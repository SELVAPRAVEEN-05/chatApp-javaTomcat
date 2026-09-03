package com.chatapp.service;

import com.chatapp.dao.MessageDAO;
import com.chatapp.model.Chat;
import com.chatapp.model.ChatMessagesResponse;
import com.chatapp.model.Message;

import java.sql.SQLException;
import java.util.List;

public class MessageService {

    private final MessageDAO messageDAO;

    public MessageService() {

        this.messageDAO =
                new MessageDAO();
    }

    // =========================================================
    // GET CHAT + MESSAGES
    //
    // GET /api/chats/{chatId}/messages
    // =========================================================

    public ChatMessagesResponse getChatMessages(
            Long chatId,
            Long userId
    ) throws SQLException {

        // -----------------------------------------------------
        // 1. VALIDATE CHAT ID
        // -----------------------------------------------------

        if (
                chatId == null ||
                        chatId <= 0
        ) {

            return null;
        }

        // -----------------------------------------------------
        // 2. VALIDATE USER ID
        // -----------------------------------------------------

        if (
                userId == null ||
                        userId <= 0
        ) {

            return null;
        }

        // -----------------------------------------------------
        // 3. GET CHAT + CHECK MEMBERSHIP
        // -----------------------------------------------------

        Chat chat =
                messageDAO.findChatByIdAndUserId(
                        chatId,
                        userId
                );

        // If user is not a member,
        // DAO returns null.

        if (chat == null) {

            throw new UnauthorizedChatException(
                    "You are not a member of this chat"
            );
        }

        // -----------------------------------------------------
        // 4. GET MESSAGES
        // -----------------------------------------------------

        List<Message> messages =
                messageDAO.findMessagesByChatId(
                        chatId,
                        userId
                );

        // -----------------------------------------------------
        // 5. MARK LATEST MESSAGE AS READ
        // -----------------------------------------------------

        if (!messages.isEmpty()) {

            Message latestMessage =
                    messages.get(
                            messages.size() - 1
                    );

            Long latestMessageId =
                    latestMessage.getMessageId();

            messageDAO.markAsRead(
                    chatId,
                    userId,
                    latestMessageId
            );
        }

        // -----------------------------------------------------
        // 6. CREATE RESPONSE
        // -----------------------------------------------------

        return new ChatMessagesResponse(
                userId,
                chat,
                messages
        );
    }

    // =========================================================
    // SEND MESSAGE
    //
    // POST /api/chats/{chatId}/messages
    // =========================================================

    public Message sendMessage(
            Long chatId,
            Long senderId,
            String message
    ) throws SQLException {

        // -----------------------------------------------------
        // 1. VALIDATE CHAT ID
        // -----------------------------------------------------

        if (
                chatId == null ||
                        chatId <= 0
        ) {

            return null;
        }

        // -----------------------------------------------------
        // 2. VALIDATE USER ID
        // -----------------------------------------------------

        if (
                senderId == null ||
                        senderId <= 0
        ) {

            return null;
        }

        // -----------------------------------------------------
        // 3. VALIDATE MESSAGE
        // -----------------------------------------------------

        if (
                message == null ||
                        message.isBlank()
        ) {

            return null;
        }

        message =
                message.trim();

        // -----------------------------------------------------
        // 4. SEND MESSAGE
        // -----------------------------------------------------

        return messageDAO.sendMessage(
                chatId,
                senderId,
                message
        );
    }

    // =========================================================
    // CUSTOM EXCEPTION
    // =========================================================

    public static class UnauthorizedChatException
            extends RuntimeException {

        public UnauthorizedChatException(
                String message
        ) {

            super(message);
        }
    }
}