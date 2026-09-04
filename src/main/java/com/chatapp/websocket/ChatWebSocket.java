
package com.chatapp.websocket;

import com.chatapp.model.Message;
import com.chatapp.service.MessageService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(
        value = "/ws/chat",
        configurator = WebSocketConfigurator.class
)
public class ChatWebSocket {

    // =========================================================
    // ACTIVE WEBSOCKET SESSIONS
    //
    // chatId -> sessions currently inside that chat
    //
    // Example:
    //
    // chat 1
    //    ├── session 0
    //    └── session 1
    //
    // =========================================================

    private static final Map<Long, Map<String, Session>>
            chatSessions =
            new ConcurrentHashMap<>();


    // =========================================================
    // USER ID FOR EACH WEBSOCKET SESSION
    //
    // sessionId -> userId
    //
    // Example:
    //
    // session 0 -> user 7
    // session 1 -> user 6
    //
    // =========================================================

    private static final Map<String, Long>
            sessionUsers =
            new ConcurrentHashMap<>();


    // =========================================================
    // CHAT ID FOR EACH WEBSOCKET SESSION
    //
    // sessionId -> chatId
    //
    // Example:
    //
    // session 0 -> chat 1
    // session 1 -> chat 1
    //
    // =========================================================

    private static final Map<String, Long>
            sessionChats =
            new ConcurrentHashMap<>();


    // =========================================================
    // OBJECT MAPPER
    //
    // IMPORTANT:
    //
    // JavaTimeModule is required because Message contains
    // LocalDateTime for createdAt.
    //
    // =========================================================

    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(
                            new JavaTimeModule()
                    )
                    .disable(
                            SerializationFeature
                                    .WRITE_DATES_AS_TIMESTAMPS
                    );


    // =========================================================
    // MESSAGE SERVICE
    // =========================================================

    private final MessageService messageService =
            new MessageService();


    // =========================================================
    // WEBSOCKET OPEN
    // =========================================================

    @OnOpen
    public void onOpen(Session session) {

        System.out.println(
                "WebSocket connected: " +
                        session.getId()
        );


        // -----------------------------------------------------
        // GET USER ID FROM HTTP SESSION
        // -----------------------------------------------------

        Object userIdObject =
                session.getUserProperties()
                        .get("userId");


        // -----------------------------------------------------
        // CHECK USER
        // -----------------------------------------------------

        if (userIdObject instanceof Long userId) {

            sessionUsers.put(
                    session.getId(),
                    userId
            );

            System.out.println(
                    "WebSocket user: " +
                            userId
            );

        } else {

            System.out.println(
                    "No authenticated user found for WebSocket session: " +
                            session.getId()
            );
        }
    }


    // =========================================================
    // RECEIVE MESSAGE FROM CLIENT
    // =========================================================

    @OnMessage
    public void onMessage(
            String message,
            Session session
    ) throws IOException {

        System.out.println(
                "Received from client: " +
                        message
        );


        // -----------------------------------------------------
        // PARSE JSON
        // -----------------------------------------------------

        JsonNode json;

        try {

            json =
                    objectMapper.readTree(
                            message
                    );

        } catch (Exception e) {

            sendError(
                    session,
                    "Invalid JSON"
            );

            return;
        }


        // -----------------------------------------------------
        // GET TYPE
        // -----------------------------------------------------

        JsonNode typeNode =
                json.get("type");


        if (
                typeNode == null ||
                        !typeNode.isTextual()
        ) {

            sendError(
                    session,
                    "type is required"
            );

            return;
        }


        String type =
                typeNode.asText();


        // =====================================================
        // JOIN
        // =====================================================

        if ("JOIN".equals(type)) {

            handleJoin(
                    json,
                    session
            );

            return;
        }


        // =====================================================
        // MESSAGE
        // =====================================================

        if ("MESSAGE".equals(type)) {

            handleMessage(
                    json,
                    session
            );

            return;
        }


        // =====================================================
        // UNKNOWN TYPE
        // =====================================================

        sendError(
                session,
                "Unknown message type"
        );
    }


    // =========================================================
    // JOIN CHAT
    // =========================================================

    private void handleJoin(
            JsonNode json,
            Session session
    ) throws IOException {

        // -----------------------------------------------------
        // GET USER ID
        // -----------------------------------------------------

        Long userId =
                sessionUsers.get(
                        session.getId()
                );


        if (userId == null) {

            sendError(
                    session,
                    "Authentication required"
            );

            return;
        }


        // -----------------------------------------------------
        // GET CHAT ID
        // -----------------------------------------------------

        JsonNode chatIdNode =
                json.get("chatId");


        if (
                chatIdNode == null ||
                        !chatIdNode.canConvertToLong()
        ) {

            sendError(
                    session,
                    "chatId must be a valid number"
            );

            return;
        }


        Long chatId =
                chatIdNode.asLong();


        if (chatId <= 0) {

            sendError(
                    session,
                    "chatId must be greater than 0"
            );

            return;
        }


        // -----------------------------------------------------
        // CHECK MEMBERSHIP
        // -----------------------------------------------------

        try {

            messageService.getChatMessages(
                    chatId,
                    userId
            );

        } catch (
                MessageService.UnauthorizedChatException e
        ) {

            sendError(
                    session,
                    "You are not a member of this chat"
            );

            return;

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    session,
                    "Unable to join chat"
            );

            return;
        }


        // -----------------------------------------------------
        // REMOVE OLD CHAT ASSOCIATION
        // -----------------------------------------------------

        Long oldChatId =
                sessionChats.get(
                        session.getId()
                );


        if (
                oldChatId != null &&
                        !oldChatId.equals(chatId)
        ) {

            Map<String, Session> oldSessions =
                    chatSessions.get(
                            oldChatId
                    );


            if (oldSessions != null) {

                oldSessions.remove(
                        session.getId()
                );


                if (oldSessions.isEmpty()) {

                    chatSessions.remove(
                            oldChatId
                    );
                }
            }
        }


        // -----------------------------------------------------
        // ADD SESSION TO CHAT
        // -----------------------------------------------------

        chatSessions
                .computeIfAbsent(
                        chatId,
                        id ->
                                new ConcurrentHashMap<>()
                )
                .put(
                        session.getId(),
                        session
                );


        // -----------------------------------------------------
        // REMEMBER CHAT
        // -----------------------------------------------------

        sessionChats.put(
                session.getId(),
                chatId
        );


        System.out.println(
                "Session " +
                        session.getId() +
                        " joined chat " +
                        chatId
        );


        // -----------------------------------------------------
        // SEND JOINED RESPONSE
        // -----------------------------------------------------

        Map<String, Object> response =
                new HashMap<>();


        response.put(
                "type",
                "JOINED"
        );


        response.put(
                "chatId",
                chatId
        );


        session.getBasicRemote().sendText(
                objectMapper.writeValueAsString(
                        response
                )
        );
    }


    // =========================================================
    // HANDLE MESSAGE
    // =========================================================

    private void handleMessage(
            JsonNode json,
            Session session
    ) throws IOException {

        // -----------------------------------------------------
        // GET USER ID
        // -----------------------------------------------------

        Long userId =
                sessionUsers.get(
                        session.getId()
                );


        if (userId == null) {

            sendError(
                    session,
                    "Authentication required"
            );

            return;
        }


        // -----------------------------------------------------
        // GET CHAT ID
        // -----------------------------------------------------

        Long chatId =
                sessionChats.get(
                        session.getId()
                );


        if (chatId == null) {

            sendError(
                    session,
                    "You must join a chat first"
            );

            return;
        }


        // -----------------------------------------------------
        // GET MESSAGE TEXT
        // -----------------------------------------------------

        JsonNode messageNode =
                json.get("message");


        if (
                messageNode == null ||
                        !messageNode.isTextual()
        ) {

            sendError(
                    session,
                    "message must be a string"
            );

            return;
        }


        String messageText =
                messageNode
                        .asText()
                        .trim();


        // -----------------------------------------------------
        // EMPTY MESSAGE
        // -----------------------------------------------------

        if (messageText.isBlank()) {

            sendError(
                    session,
                    "message cannot be empty"
            );

            return;
        }


        // =====================================================
        // SAVE MESSAGE
        // =====================================================

        Message createdMessage;

        try {

            createdMessage =
                    messageService.sendMessage(
                            chatId,
                            userId,
                            messageText
                    );

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    session,
                    "Unable to send message"
            );

            return;
        }


        // -----------------------------------------------------
        // CHECK CREATED MESSAGE
        // -----------------------------------------------------

        if (createdMessage == null) {

            sendError(
                    session,
                    "You are not a member of this chat"
            );

            return;
        }


        System.out.println(
                "Message saved successfully: " +
                        createdMessage.getMessageId()
        );


        // =====================================================
        // BROADCAST
        // =====================================================

        try {

            broadcastMessage(
                    chatId,
                    createdMessage
            );

        } catch (Exception e) {


            System.err.println(
                    "Broadcast failed"
            );

            e.printStackTrace();

            sendError(
                    session,
                    "Message saved but broadcast failed"
            );
        }
    }

    // =========================================================
    // BROADCAST MESSAGE
    // =========================================================

    private void broadcastMessage(
            Long chatId,
            Message message
    ) throws IOException {

        // -----------------------------------------------------
        // CREATE RESPONSE
        // -----------------------------------------------------

        Map<String, Object> response =
                new HashMap<>();


        response.put(
                "type",
                "MESSAGE"
        );


        response.put(
                "messageId",
                message.getMessageId()
        );


        response.put(
                "chatId",
                message.getChatId()
        );


        response.put(
                "senderId",
                message.getSenderId()
        );


        response.put(
                "senderName",
                message.getSenderName()
        );


        response.put(
                "message",
                message.getMessage()
        );


        response.put(
                "createdAt",
                message.getCreatedAt()
        );


        // -----------------------------------------------------
        // CONVERT TO JSON
        // -----------------------------------------------------

        String json =
                objectMapper.writeValueAsString(
                        response
                );


        System.out.println(
                "Broadcasting message to chat " +
                        chatId +
                        ": " +
                        json
        );


        // -----------------------------------------------------
        // GET CHAT SESSIONS
        // -----------------------------------------------------

        Map<String, Session> sessions =
                chatSessions.get(
                        chatId
                );


        if (sessions == null) {

            System.out.println(
                    "No WebSocket sessions found for chat: " +
                            chatId
            );

            return;
        }


        // -----------------------------------------------------
        // SEND TO EVERY USER
        // -----------------------------------------------------

        for (
                Session chatSession :
                sessions.values()
        ) {

            if (
                    chatSession == null ||
                            !chatSession.isOpen()
            ) {

                continue;
            }


            try {

                chatSession
                        .getBasicRemote()
                        .sendText(
                                json
                        );


                System.out.println(
                        "Message sent to WebSocket session: " +
                                chatSession.getId()
                );


            } catch (Exception e) {

                System.err.println(
                        "Failed to send message to session: " +
                                chatSession.getId()
                );

                e.printStackTrace();
            }
        }
    }


    // =========================================================
    // SEND ERROR
    // =========================================================

    private void sendError(
            Session session,
            String message
    ) throws IOException {

        Map<String, Object> response =
                new HashMap<>();


        response.put(
                "type",
                "ERROR"
        );


        response.put(
                "message",
                message
        );


        session.getBasicRemote().sendText(
                objectMapper.writeValueAsString(
                        response
                )
        );
    }


    // =========================================================
    // WEBSOCKET CLOSE
    // =========================================================

    @OnClose
    public void onClose(
            Session session
    ) {

        String sessionId =
                session.getId();


        // -----------------------------------------------------
        // GET CHAT ID
        // -----------------------------------------------------

        Long chatId =
                sessionChats.remove(
                        sessionId
                );


        // -----------------------------------------------------
        // REMOVE USER
        // -----------------------------------------------------

        sessionUsers.remove(
                sessionId
        );


        // -----------------------------------------------------
        // REMOVE FROM CHAT
        // -----------------------------------------------------

        if (chatId != null) {

            Map<String, Session> sessions =
                    chatSessions.get(
                            chatId
                    );


            if (sessions != null) {

                sessions.remove(
                        sessionId
                );


                if (sessions.isEmpty()) {

                    chatSessions.remove(
                            chatId
                    );
                }
            }
        }

        System.out.println(
                "WebSocket disconnected: " +
                        sessionId
        );
    }
}
