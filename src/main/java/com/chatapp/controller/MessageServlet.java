package com.chatapp.controller;

import com.chatapp.model.ChatMessagesResponse;
import com.chatapp.model.Message;
import com.chatapp.service.MessageService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/chats/*")
public class MessageServlet extends HttpServlet {

    private final MessageService messageService =
            new MessageService();

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(
                            new JavaTimeModule()
                    )
                    .disable(
                            SerializationFeature
                                    .WRITE_DATES_AS_TIMESTAMPS
                    );

    // =========================================================
    // GET
    //
    // GET /api/chats/{chatId}/messages
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);

        // -----------------------------------------------------
        // 1. CHECK SESSION
        // -----------------------------------------------------

        HttpSession session =
                request.getSession(false);

        if (
                session == null ||
                        session.getAttribute("userId") == null
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required"
            );

            return;
        }

        // -----------------------------------------------------
        // 2. GET USER ID FROM SESSION
        // -----------------------------------------------------

        Long userId;

        try {

            userId =
                    (Long) session.getAttribute(
                            "userId"
                    );

        } catch (ClassCastException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid authentication session"
            );

            return;
        }

        // -----------------------------------------------------
        // 3. GET CHAT ID
        // -----------------------------------------------------

        Long chatId =
                extractChatId(
                        request,
                        response
                );

        if (chatId == null) {
            return;
        }

        // -----------------------------------------------------
        // 4. GET CHAT + MESSAGES
        // -----------------------------------------------------

        ChatMessagesResponse result;

        try {

            result =
                    messageService.getChatMessages(
                            chatId,
                            userId
                    );

        } catch (
                MessageService.UnauthorizedChatException e
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage()
            );

            return;

        } catch (SQLException e) {

            e.printStackTrace();

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error"
            );

            return;
        }

        // -----------------------------------------------------
        // 5. SUCCESS
        // -----------------------------------------------------

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        objectMapper.writeValue(
                response.getWriter(),
                result
        );
    }

    // =========================================================
    // POST
    //
    // POST /api/chats/{chatId}/messages
    //
    // JSON:
    //
    // {
    //     "message": "Hello"
    // }
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);

        // -----------------------------------------------------
        // 1. CHECK SESSION
        // -----------------------------------------------------

        HttpSession session =
                request.getSession(false);

        if (
                session == null ||
                        session.getAttribute("userId") == null
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required"
            );

            return;
        }

        // -----------------------------------------------------
        // 2. GET USER ID
        // -----------------------------------------------------

        Long userId;

        try {

            userId =
                    (Long) session.getAttribute(
                            "userId"
                    );

        } catch (ClassCastException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid authentication session"
            );

            return;
        }

        // -----------------------------------------------------
        // 3. GET CHAT ID
        // -----------------------------------------------------

        Long chatId =
                extractChatId(
                        request,
                        response
                );

        if (chatId == null) {
            return;
        }

        // -----------------------------------------------------
        // 4. READ JSON
        // -----------------------------------------------------

        JsonNode json;

        try {

            json =
                    objectMapper.readTree(
                            request.getReader()
                    );

        } catch (Exception e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid JSON"
            );

            return;
        }

        // -----------------------------------------------------
        // 5. CHECK JSON OBJECT
        // -----------------------------------------------------

        if (
                json == null ||
                        !json.isObject()
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Request body must be a JSON object"
            );

            return;
        }

        // -----------------------------------------------------
        // 6. CHECK MESSAGE
        // -----------------------------------------------------

        if (
                !json.has("message") ||
                        json.get("message").isNull()
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "message is required"
            );

            return;
        }

        if (
                !json.get("message").isTextual()
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "message must be a string"
            );

            return;
        }

        String message =
                json.get("message").asText();

        // -----------------------------------------------------
        // 7. VALIDATE MESSAGE
        // -----------------------------------------------------

        if (message.isBlank()) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "message cannot be empty"
            );

            return;
        }

        message =
                message.trim();

        // -----------------------------------------------------
        // 8. SEND MESSAGE
        // -----------------------------------------------------

        Message createdMessage;

        try {

            createdMessage =
                    messageService.sendMessage(
                            chatId,
                            userId,
                            message
                    );

        } catch (SQLException e) {

            e.printStackTrace();

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error"
            );

            return;
        }

        // -----------------------------------------------------
        // 9. NOT A MEMBER
        // -----------------------------------------------------

        if (createdMessage == null) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not a member of this chat"
            );

            return;
        }

        // -----------------------------------------------------
        // 10. SUCCESS
        // -----------------------------------------------------

        response.setStatus(
                HttpServletResponse.SC_CREATED
        );

        objectMapper.writeValue(
                response.getWriter(),
                createdMessage
        );
    }

    // =========================================================
    // EXTRACT CHAT ID
    //
    // Expected:
    //
    // /4/messages
    // =========================================================

    private Long extractChatId(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String pathInfo =
                request.getPathInfo();

        // -----------------------------------------------------
        // 1. CHECK PATH
        // -----------------------------------------------------

        if (
                pathInfo == null ||
                        pathInfo.isBlank()
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Invalid chat messages endpoint"
            );

            return null;
        }

        // -----------------------------------------------------
        // 2. REMOVE FIRST /
        // -----------------------------------------------------

        String cleanPath =
                pathInfo.startsWith("/")
                        ? pathInfo.substring(1)
                        : pathInfo;

        // Example:
        //
        // /4/messages
        //
        // becomes:
        //
        // 4/messages

        // -----------------------------------------------------
        // 3. SPLIT PATH
        // -----------------------------------------------------

        String[] parts =
                cleanPath.split("/");

        // Expected:
        //
        // parts[0] = "4"
        // parts[1] = "messages"

        // -----------------------------------------------------
        // 4. VALIDATE URL FORMAT
        // -----------------------------------------------------

        if (
                parts.length != 2 ||
                        !"messages".equals(parts[1])
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Invalid chat messages endpoint"
            );

            return null;
        }

        // -----------------------------------------------------
        // 5. CONVERT CHAT ID TO LONG
        // -----------------------------------------------------

        Long chatId;

        try {

            chatId =
                    Long.parseLong(
                            parts[0]
                    );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "chatId must be a valid number"
            );

            return null;
        }

        // -----------------------------------------------------
        // 6. CHAT ID MUST BE POSITIVE
        // -----------------------------------------------------

        if (chatId <= 0) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "chatId must be greater than 0"
            );

            return null;
        }

        return chatId;
    }

    // =========================================================
    // JSON RESPONSE
    // =========================================================

    private void setJsonResponse(
            HttpServletResponse response
    ) {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );
    }

    // =========================================================
    // ERROR RESPONSE
    // =========================================================

    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);

        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse(message)
        );
    }

    // =========================================================
    // ERROR MODEL
    // =========================================================

    private static class ErrorResponse {

        private final String message;

        public ErrorResponse(
                String message
        ) {

            this.message =
                    message;
        }

        public String getMessage() {

            return message;
        }
    }
}