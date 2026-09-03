package com.chatapp.dao;

import com.chatapp.model.User;
import com.chatapp.util.DBConnection;

import java.sql.*;

public class RegisterDAO {

    // =========================================================
    // CREATE USER
    // =========================================================

    public User createUser(
            String name,
            String phoneNumber,
            String email,
            String password
    ) throws SQLException {

        String sql = """
                INSERT INTO users
                    (
                        name,
                        phone_number,
                        email,
                        password,
                        created_at
                    )
                VALUES
                    (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;


        Connection connection = null;

        try {

            // -------------------------------------------------
            // GET DATABASE CONNECTION
            // -------------------------------------------------

            connection =
                    DBConnection.getConnection();


            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    sql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {

                // -------------------------------------------------
                // SET VALUES
                // -------------------------------------------------

                statement.setString(
                        1,
                        name
                );

                statement.setString(
                        2,
                        phoneNumber
                );

                statement.setString(
                        3,
                        email
                );

                statement.setString(
                        4,
                        password
                );


                // -------------------------------------------------
                // EXECUTE INSERT
                // -------------------------------------------------

                int rowsAffected =
                        statement.executeUpdate();


                if (rowsAffected == 0) {
                    throw new SQLException(
                            "User registration failed. No row inserted."
                    );
                }


                // -------------------------------------------------
                // GET AUTO-INCREMENT USER ID
                // -------------------------------------------------

                Long userId = null;

                try (
                        ResultSet keys =
                                statement.getGeneratedKeys()
                ) {

                    if (keys.next()) {

                        userId =
                                keys.getLong(1);

                    } else {

                        throw new SQLException(
                                "User registration failed. User ID was not generated."
                        );
                    }
                }


                // -------------------------------------------------
                // CREATE USER OBJECT
                // -------------------------------------------------

                User user =
                        new User();

                user.setUserId(
                        userId
                );

                user.setName(
                        name
                );

                user.setPhoneNumber(
                        phoneNumber
                );

                user.setEmail(
                        email
                );


                // profile_image is NULL initially
                user.setProfileImage(
                        null
                );


                // -------------------------------------------------
                // GET CREATED_AT
                // -------------------------------------------------

                String getUserSql = """
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
                        PreparedStatement getUserStatement =
                                connection.prepareStatement(
                                        getUserSql
                                )
                ) {

                    getUserStatement.setLong(
                            1,
                            userId
                    );


                    try (
                            ResultSet resultSet =
                                    getUserStatement.executeQuery()
                    ) {

                        if (resultSet.next()) {

                            user.setUserId(
                                    resultSet.getLong(
                                            "user_id"
                                    )
                            );

                            user.setName(
                                    resultSet.getString(
                                            "name"
                                    )
                            );

                            user.setPhoneNumber(
                                    resultSet.getString(
                                            "phone_number"
                                    )
                            );

                            user.setEmail(
                                    resultSet.getString(
                                            "email"
                                    )
                            );

                            user.setProfileImage(
                                    resultSet.getString(
                                            "profile_image"
                                    )
                            );


                            Timestamp timestamp =
                                    resultSet.getTimestamp(
                                            "created_at"
                                    );


                            if (timestamp != null) {

                                user.setCreatedAt(
                                        timestamp.toLocalDateTime()
                                );
                            }
                        }
                    }
                }


                // -------------------------------------------------
                // RETURN CREATED USER
                // -------------------------------------------------

                return user;
            }

        } finally {

            // -------------------------------------------------
            // CLOSE CONNECTION
            // -------------------------------------------------

            if (connection != null) {

                try {

                    connection.close();

                } catch (SQLException e) {

                    e.printStackTrace();
                }
            }
        }
    }


    // =========================================================
    // CHECK PHONE NUMBER
    // =========================================================

    public boolean existsByPhoneNumber(
            String phoneNumber
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM users
                WHERE phone_number = ?
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
                    phoneNumber
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
    // CHECK EMAIL
    // =========================================================

    public boolean existsByEmail(
            String email
    ) throws SQLException {

        // If email is optional, don't check empty email.
        if (email == null ||
                email.trim().isEmpty()) {

            return false;
        }


        String sql = """
                SELECT 1
                FROM users
                WHERE email = ?
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


            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }
        }
    }
}