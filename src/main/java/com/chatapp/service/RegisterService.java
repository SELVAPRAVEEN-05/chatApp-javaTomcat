package com.chatapp.service;

import com.chatapp.dao.RegisterDAO;
import com.chatapp.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterService {

    private final RegisterDAO registerDAO =
            new RegisterDAO();


    public User registerUser(
            String name,
            String phoneNumber,
            String email,
            String password
    ) throws Exception {


        // ===============================
        // VALIDATION
        // ===============================

        name = name.trim();

        phoneNumber = phoneNumber.trim();

        if (email != null) {
            email = email.trim();
        }


        if (name.isEmpty()) {

            throw new IllegalArgumentException(
                    "Name cannot be empty."
            );
        }


        if (phoneNumber.isEmpty()) {

            throw new IllegalArgumentException(
                    "Phone number cannot be empty."
            );
        }


        if (password.length() < 8) {

            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }


        // ===============================
        // PHONE CHECK
        // ===============================

        if (registerDAO.existsByPhoneNumber(
                phoneNumber
        )) {

            throw new IllegalArgumentException(
                    "Phone number already registered."
            );
        }


        // ===============================
        // EMAIL CHECK
        // ===============================

        if (email != null &&
                !email.isEmpty() &&
                registerDAO.existsByEmail(email)) {

            throw new IllegalArgumentException(
                    "Email already registered."
            );
        }


        // ===============================
        // HASH PASSWORD
        // ===============================

        String passwordHash =
                BCrypt.hashpw(
                        password,
                        BCrypt.gensalt()
                );


        // ===============================
        // CREATE USER
        // ===============================

        return registerDAO.createUser(
                name,
                phoneNumber,
                email,
                passwordHash
        );
    }
}