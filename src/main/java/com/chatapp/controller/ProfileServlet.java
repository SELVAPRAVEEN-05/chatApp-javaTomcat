package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.service.UserService;
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

public class ProfileServlet extends HttpServlet {

    private final UserService userService =
            new UserService();


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
    // GET PROFILE
    //
    // GET /api/profile
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);


        // -----------------------------------------------------
        // 1. GET EXISTING SESSION
        // -----------------------------------------------------

        HttpSession session =
                request.getSession(false);


        // -----------------------------------------------------
        // 2. AUTHENTICATION
        // -----------------------------------------------------

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
        // 3. GET USER ID FROM SESSION
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
        // 4. GET PROFILE
        // -----------------------------------------------------

        User user =
                userService.getUserProfile(
                        userId
                );


        if (user == null) {

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "User not found"
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
                user
        );
    }


    // =========================================================
    // UPDATE PROFILE
    //
    // PUT /api/profile
    //
    // JSON:
    //
    // {
    //     "name": "Selvapraveen ",
    //     "email": "abc@gmail.com"
    // }
    //
    // Only name and email are accepted.
    // =========================================================

    @Override
    protected void doPatch(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);


        // -----------------------------------------------------
        // 1. GET EXISTING SESSION
        // -----------------------------------------------------

        HttpSession session =
                request.getSession(false);


        // -----------------------------------------------------
        // 2. AUTHENTICATION
        // -----------------------------------------------------

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
        // 3. GET USER ID FROM SESSION
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
        // 6. GET NAME
        // -----------------------------------------------------

        String name = null;

        if (
                json.has("name") &&
                        !json.get("name").isNull()
        ) {

            if (
                    !json.get("name").isTextual()
            ) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "name must be a string"
                );

                return;
            }


            name =
                    json.get("name").asText();
        }


        // -----------------------------------------------------
        // 7. GET EMAIL
        // -----------------------------------------------------

        String email = null;

        if (
                json.has("email") &&
                        !json.get("email").isNull()
        ) {

            if (
                    !json.get("email").isTextual()
            ) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "email must be a string"
                );

                return;
            }


            email =
                    json.get("email").asText();
        }


        // -----------------------------------------------------
        // 8. UPDATE PROFILE
        // -----------------------------------------------------

        try {

            User updatedUser =
                    userService.updateProfile(
                            userId,
                            name,
                            email
                    );


            if (updatedUser == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "User not found"
                );

                return;
            }


            // -------------------------------------------------
            // 9. SUCCESS
            // -------------------------------------------------

            response.setStatus(
                    HttpServletResponse.SC_OK
            );


            objectMapper.writeValue(
                    response.getWriter(),
                    updatedUser
            );


        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );


        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to update profile"
            );
        }
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