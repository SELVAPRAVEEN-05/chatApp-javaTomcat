package com.chatapp.dao;

import com.chatapp.model.User;
import com.chatapp.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {


    // =========================================================
    // FIND USER BY PHONE
    //
    // Used for LOGIN
    // =========================================================

    public User findByPhoneNumber(
            String phoneNumber
    ) {

        String sql = """
                SELECT
                    user_id,
                    name,
                    phone_number,
                    password,
                    email,
                    profile_image,
                    created_at
                FROM users
                WHERE phone_number = ?
                """;


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    phoneNumber
            );


            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    return mapUser(
                            resultSet,
                            true
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }


        return null;
    }


    // =========================================================
    // FIND USER BY ID
    //
    // Used for PROFILE
    // =========================================================

    public User findByUserId(
            Long userId
    ) {

        String sql = """
                SELECT
                    user_id,
                    name,
                    phone_number,
                    email,
                    profile_image,
                    created_at
                FROM users
                WHERE user_id = ?
                """;


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    userId
            );


            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    return mapUser(
                            resultSet,
                            false
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }


        return null;
    }


    // =========================================================
    // CHECK EMAIL
    //
    // excludeUserId prevents the current user's own email
    // from being considered a duplicate.
    // =========================================================

    public boolean existsByEmail(
            String email,
            Long excludeUserId
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM users
                WHERE email = ?
                  AND user_id <> ?
                LIMIT 1
                """;


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    email
            );

            statement.setLong(
                    2,
                    excludeUserId
            );


            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }
        }
    }


    // =========================================================
    // UPDATE PROFILE
    //
    // Only NAME and EMAIL can be changed.
    //
    // Phone number cannot be changed.
    // Password cannot be changed here.
    // =========================================================

    public User updateProfile(
            Long userId,
            String name,
            String email
    ) throws SQLException {

        String sql = """
                UPDATE users
                SET
                    name = ?,
                    email = ?
                WHERE user_id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    name
            );


            /*
             * Email can be NULL.
             */
            if (email == null ||
                    email.isBlank()) {

                statement.setNull(
                        2,
                        java.sql.Types.VARCHAR
                );

            } else {

                statement.setString(
                        2,
                        email
                );
            }


            statement.setLong(
                    3,
                    userId
            );


            int rowsAffected =
                    statement.executeUpdate();


            if (rowsAffected == 0) {

                return null;
            }
        }


        /*
         * Return updated user.
         */
        return findByUserId(userId);
    }


    // =========================================================
    // MAP RESULT SET TO USER
    // =========================================================

    private User mapUser(
            ResultSet resultSet,
            boolean includePassword
    ) throws SQLException {

        User user =
                new User();


        user.setUserId(
                resultSet.getLong("user_id")
        );


        user.setName(
                resultSet.getString("name")
        );


        user.setPhoneNumber(
                resultSet.getString("phone_number")
        );


        /*
         * Only login needs password.
         */
        if (includePassword) {

            user.setPassword(
                    resultSet.getString("password")
            );
        }


        user.setEmail(
                resultSet.getString("email")
        );


        user.setProfileImage(
                resultSet.getString("profile_image")
        );


        if (
                resultSet.getTimestamp(
                        "created_at"
                ) != null
        ) {

            user.setCreatedAt(
                    resultSet
                            .getTimestamp("created_at")
                            .toLocalDateTime()
            );
        }


        return user;
    }
}