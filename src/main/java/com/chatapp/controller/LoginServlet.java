package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class LoginServlet extends HttpServlet {

    private final LoginService loginService = new LoginService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {
// i am good
        // Response will be JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Read JSON request body
        LoginRequest loginRequest =
                objectMapper.readValue(
                        request.getReader(),
                        LoginRequest.class
                );

        String phoneNumber = loginRequest.getPhoneNumber();
        String password = loginRequest.getPassword();

        // Login
        User user = loginService.login(phoneNumber, password);

        // Login failed
        if (user == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().write("""
                    {
                        "message": "Invalid phone number or password"
                    }
                    """);

            return;
        }

        HttpSession session = request.getSession(true);

        session.setAttribute(
                "userId",
                user.getUserId()
        );

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        response.getWriter().write("""
                {
                    "message": "Login successful",
                    "userId": %d,
                    "name": "%s",
                    "phoneNumber": "%s"
                }
                """.formatted(
                user.getUserId(),
                user.getName(),
                user.getPhoneNumber()
        ));
    }

    // Class used to receive login JSON
    public static class LoginRequest {

        private String phoneNumber;
        private String password;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}