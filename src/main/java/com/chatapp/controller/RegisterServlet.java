package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.service.RegisterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RegisterServlet extends HttpServlet {

    private final RegisterService registerService =
            new RegisterService();

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
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        /*
         * JSON response
         */

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        try {

            /*
             * =================================================
             * 1. READ REQUEST JSON
             * =================================================
             */

            RegisterRequest registerRequest =
                    objectMapper.readValue(
                            request.getReader(),
                            RegisterRequest.class
                    );


            /*
             * =================================================
             * 2. BASIC VALIDATION
             * =================================================
             */

            if (registerRequest.getName() == null ||
                    registerRequest.getPhoneNumber() == null ||
                    registerRequest.getPassword() == null) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                objectMapper.writeValue(
                        response.getWriter(),
                        new ErrorResponse(
                                "Name, phone number and password are required."
                        )
                );

                return;
            }


            /*
             * =================================================
             * 3. REGISTER USER
             * =================================================
             */

            User user =
                    registerService.registerUser(
                            registerRequest.getName(),
                            registerRequest.getPhoneNumber(),
                            registerRequest.getEmail(),
                            registerRequest.getPassword()
                    );


            /*
             * =================================================
             * 4. SUCCESS RESPONSE
             * =================================================
             */

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );


            objectMapper.writeValue(
                    response.getWriter(),
                    user
            );


        } catch (IllegalArgumentException e) {

            /*
             * =================================================
             * VALIDATION / DUPLICATE ERROR
             * =================================================
             */

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            e.getMessage()
                    )
            );


        } catch (Exception e) {

            /*
             * =================================================
             * SERVER ERROR
             * =================================================
             */

            e.printStackTrace();


            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );


            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            "Registration failed."
                    )
            );
        }
    }


    /*
     * =========================================================
     * REGISTER REQUEST
     * =========================================================
     */

    public static class RegisterRequest {

        private String name;

        private String phoneNumber;

        private String email;

        private String password;


        public RegisterRequest() {
        }

        public String getName() {

            return name;
        }


        public void setName(
                String name
        ) {

            this.name = name;
        }


        public String getPhoneNumber() {

            return phoneNumber;
        }


        public void setPhoneNumber(
                String phoneNumber
        ) {

            this.phoneNumber = phoneNumber;
        }


        public String getEmail() {

            return email;
        }


        public void setEmail(
                String email
        ) {

            this.email = email;
        }


        public String getPassword() {

            return password;
        }


        public void setPassword(
                String password
        ) {

            this.password = password;
        }
    }


    /*
     * =========================================================
     * ERROR RESPONSE
     * =========================================================
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