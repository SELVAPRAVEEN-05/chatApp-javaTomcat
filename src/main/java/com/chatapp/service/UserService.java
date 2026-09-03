package com.chatapp.service;

import com.chatapp.dao.UserDAO;
import com.chatapp.model.User;

public class UserService {

    private final UserDAO userDAO;


    public UserService() {

        this.userDAO =
                new UserDAO();
    }


    // =========================================================
    // GET PROFILE
    // =========================================================

    public User getUserProfile(
            Long userId
    ) {

        if (
                userId == null ||
                        userId <= 0
        ) {

            return null;
        }


        return userDAO.findByUserId(
                userId
        );
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    public User updateProfile(
            Long userId,
            String name,
            String email
    ) throws Exception {


        // -----------------------------------------------------
        // USER ID VALIDATION
        // -----------------------------------------------------

        if (
                userId == null ||
                        userId <= 0
        ) {

            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }


        // -----------------------------------------------------
        // NAME VALIDATION
        // -----------------------------------------------------

        if (name == null) {

            throw new IllegalArgumentException(
                    "Name is required."
            );
        }


        name =
                name.trim();


        if (name.isEmpty()) {

            throw new IllegalArgumentException(
                    "Name cannot be empty."
            );
        }


        if (name.length() > 100) {

            throw new IllegalArgumentException(
                    "Name cannot exceed 100 characters."
            );
        }


        // -----------------------------------------------------
        // EMAIL
        // -----------------------------------------------------

        if (email != null) {

            email =
                    email.trim();
        }


        /*
         * Empty email means NULL.
         */
        if (
                email != null &&
                        email.isEmpty()
        ) {

            email = null;
        }


        // -----------------------------------------------------
        // EMAIL VALIDATION
        // -----------------------------------------------------

        if (
                email != null &&
                        email.length() > 255
        ) {

            throw new IllegalArgumentException(
                    "Email cannot exceed 255 characters."
            );
        }


        /*
         * Basic email validation.
         */
        if (
                email != null &&
                        !email.matches(
                                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                        )
        ) {

            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }


        // -----------------------------------------------------
        // CHECK DUPLICATE EMAIL
        // -----------------------------------------------------

        if (
                email != null &&
                        userDAO.existsByEmail(
                                email,
                                userId
                        )
        ) {

            throw new IllegalArgumentException(
                    "Email is already registered."
            );
        }


        // -----------------------------------------------------
        // UPDATE
        // -----------------------------------------------------

        return userDAO.updateProfile(
                userId,
                name,
                email
        );
    }
}