package com.chatapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class LogoutServlet extends HttpServlet {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Override
    protected void doPost(
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
         * 1. GET EXISTING SESSION
         * =========================================
         *
         * false = don't create a new session.
         */
        HttpSession session =
                request.getSession(false);


        /*
         * =========================================
         * 2. CHECK SESSION
         * =========================================
         */

        if (session == null) {

            deleteSessionCookie(
                    request,
                    response
            );

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            "User is not logged in"
                    )
            );

            return;
        }


        /*
         * =========================================
         * 3. DESTROY SERVER-SIDE SESSION
         * =========================================
         */

        session.invalidate();


        /*
         * =========================================
         * 4. DELETE JSESSIONID FROM BROWSER
         * =========================================
         */

        deleteSessionCookie(
                request,
                response
        );


        /*
         * =========================================
         * 5. SUCCESS
         * =========================================
         */

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        objectMapper.writeValue(
                response.getWriter(),
                new SuccessResponse(
                        "Logout successful"
                )
        );
    }


    /*
     * =========================================
     * DELETE JSESSIONID COOKIE
     * =========================================
     */

    private void deleteSessionCookie(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Cookie cookie =
                new Cookie(
                        "JSESSIONID",
                        ""
                );


        /*
         * Cookie should belong to /chatApp
         */
        cookie.setPath(
                request.getContextPath()
        );


        /*
         * 0 = delete immediately
         */
        cookie.setMaxAge(0);


        /*
         * Tell browser to remove it.
         */
        response.addCookie(cookie);
    }


    /*
     * =========================================
     * SUCCESS RESPONSE
     * =========================================
     */

    private static class SuccessResponse {

        private final String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }


    /*
     * =========================================
     * ERROR RESPONSE
     * =========================================
     */

    private static class ErrorResponse {

        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}