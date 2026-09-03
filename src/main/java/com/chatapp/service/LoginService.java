package com.chatapp.service;

import com.chatapp.dao.UserDAO;
import com.chatapp.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class LoginService {

    private final UserDAO userDAO;

    public LoginService() {
        this.userDAO = new UserDAO();
    }

    public User login(
            String phoneNumber,
            String password
    ) {

        if (phoneNumber == null ||
                phoneNumber.isBlank()) {

            return null;
        }

        if (password == null ||
                password.isBlank()) {

            return null;
        }

        User user =
                userDAO.findByPhoneNumber(
                        phoneNumber
                );

        if (user == null) {
            return null;
        }


        if (!BCrypt.checkpw(
                password,
                user.getPassword()
        )) {

            return null;
        }

        return user;
    }
}