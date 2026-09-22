package com.chatapp.controller;

import com.chatapp.model.Chat;
import com.chatapp.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/chats")
public class ChatServlet extends HttpServlet {

    private final ChatService chatService =
            new ChatService();

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(
                            new JavaTimeModule()
                    )
                    .disable(
                            SerializationFeature
                                    .WRITE_DATES_AS_TIMESTAMPS
                    );


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        /*
         * =========================================
         * 1. CHECK SESSION
         * =========================================
         */

        HttpSession session =
                request.getSession(false);


        if (
                session == null ||
                        session.getAttribute("userId") == null
        ) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            "Authentication required"
                    )
            );

            return;
        }


        /*
         * =========================================
         * 2. GET USER ID
         * =========================================
         */

        Long userId;

        try {

            userId =
                    (Long) session.getAttribute(
                            "userId"
                    );

        } catch (ClassCastException e) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            "Invalid authentication session"
                    )
            );

            return;
        }

//hi

        // how are you
        /*
         * =========================================
         * 3. GET FILTER
         * =========================================
         * Examples:
         * /api/chats?filter=all
         * /api/chats?filter=unread
         * /api/chats?filter=read
         * /api/chats?filter=groups
         *
         */

        String filter =
                request.getParameter("filter");


        /*
         * If no filter is provided,
         * use ALL.
         */

        if (
                filter == null ||
                        filter.isBlank()
        ) {

            filter = "all";
        }


        /*
         * Convert to lowercase.
         */

        filter =
                filter.trim().toLowerCase();


        /*
         * =========================================
         * 4. VALIDATE FILTER
         * =========================================
         */

        if (
                !filter.equals("all") &&
                        !filter.equals("unread") &&
                        !filter.equals("read") &&
                        !filter.equals("groups")
        ) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            "Invalid filter"
                    )
            );

            return;
        }


        /*
         * =========================================
         * 5. GET CHATS
         * =========================================
         */

        List<Chat> chats =
                chatService.getUserChats(
                        userId,
                        filter
                );


        /*
         * =========================================
         * 6. RESPONSE
         * =========================================
         */

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        objectMapper.writeValue(
                response.getWriter(),
                chats
        );
    }


    /*
     * =========================================
     * ERROR RESPONSE
     * =========================================
     */

    private static class ErrorResponse {

        private final String message;


        public ErrorResponse(
                String message
        ) {

            this.message = message;
        }


        public String getMessage() {

            return message;
        }
    }
}