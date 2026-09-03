package com.chatapp.dao;

import com.chatapp.model.Chat;
import com.chatapp.model.Message;
import com.chatapp.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    // =========================================================
    // GET MESSAGES
    // =========================================================

    public List<Message> findMessagesByChatId(
            Long chatId,
            Long userId
    ) throws SQLException {

        List<Message> messages =
                new ArrayList<>();

        String sql = """
                SELECT
                    m.message_id,
                    m.chat_id,
                    m.sender_id,
                    u.name AS sender_name,
                    m.message,
                    m.created_at
                FROM messages m
                JOIN users u
                    ON u.user_id = m.sender_id
                WHERE m.chat_id = ?
                ORDER BY m.message_id ASC
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    chatId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    Message message =
                            new Message();

                    message.setMessageId(
                            resultSet.getLong(
                                    "message_id"
                            )
                    );

                    message.setChatId(
                            resultSet.getLong(
                                    "chat_id"
                            )
                    );

                    message.setSenderId(
                            resultSet.getLong(
                                    "sender_id"
                            )
                    );

                    message.setSenderName(
                            resultSet.getString(
                                    "sender_name"
                            )
                    );

                    message.setMessage(
                            resultSet.getString(
                                    "message"
                            )
                    );

                    Timestamp timestamp =
                            resultSet.getTimestamp(
                                    "created_at"
                            );

                    if (timestamp != null) {

                        message.setCreatedAt(
                                timestamp.toLocalDateTime()
                        );
                    }

                    messages.add(message);
                }
            }
        }

        return messages;
    }

    // =========================================================
    // MARK AS READ
    // =========================================================

    public void markAsRead(
            Long chatId,
            Long userId,
            Long latestMessageId
    ) throws SQLException {

        if (
                latestMessageId == null ||
                        latestMessageId <= 0
        ) {

            return;
        }

        String sql = """
                UPDATE chat_members
                SET last_read_message_id = ?
                WHERE chat_id = ?
                  AND user_id = ?
                  AND (
                      last_read_message_id IS NULL
                      OR last_read_message_id < ?
                  )
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    latestMessageId
            );

            statement.setLong(
                    2,
                    chatId
            );

            statement.setLong(
                    3,
                    userId
            );

            statement.setLong(
                    4,
                    latestMessageId
            );

            statement.executeUpdate();
        }
    }

    // =========================================================
    // SEND MESSAGE
    // =========================================================

    public Message sendMessage(
            Long chatId,
            Long senderId,
            String messageText
    ) throws SQLException {

        String checkMemberSql = """
                SELECT 1
                FROM chat_members
                WHERE chat_id = ?
                  AND user_id = ?
                FOR UPDATE
                """;

        String insertMessageSql = """
                INSERT INTO messages
                    (
                        chat_id,
                        sender_id,
                        message,
                        created_at
                    )
                VALUES
                    (?, ?, ?, ?)
                """;

        String updateChatSql = """
                UPDATE chats
                SET
                    last_message_id = ?,
                    last_message_at = ?
                WHERE chat_id = ?
                """;

        String updateChatMembersSql = """
                UPDATE chat_members
                SET
                    last_message_at = ?
                WHERE chat_id = ?
                """;

        String updateSenderDeliverySql = """
                UPDATE chat_members
                SET
                    last_delivered_message_id = ?
                WHERE chat_id = ?
                  AND user_id = ?
                """;

        String getMessageSql = """
                SELECT
                    m.message_id,
                    m.chat_id,
                    m.sender_id,
                    u.name AS sender_name,
                    m.message,
                    m.created_at
                FROM messages m
                JOIN users u
                    ON u.user_id = m.sender_id
                WHERE m.message_id = ?
                """;

        Connection connection = null;

        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);

            // -------------------------------------------------
            // 1. CHECK SENDER MEMBERSHIP + LOCK
            // -------------------------------------------------

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    checkMemberSql
                            )
            ) {

                statement.setLong(
                        1,
                        chatId
                );

                statement.setLong(
                        2,
                        senderId
                );

                try (
                        ResultSet resultSet =
                                statement.executeQuery()
                ) {

                    if (!resultSet.next()) {

                        connection.rollback();

                        return null;
                    }
                }
            }

            // -------------------------------------------------
            // 2. CURRENT TIME
            // -------------------------------------------------

            LocalDateTime now =
                    LocalDateTime.now();

            // -------------------------------------------------
            // 3. INSERT MESSAGE
            // -------------------------------------------------

            Long messageId;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    insertMessageSql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {

                statement.setLong(
                        1,
                        chatId
                );

                statement.setLong(
                        2,
                        senderId
                );

                statement.setString(
                        3,
                        messageText
                );

                statement.setTimestamp(
                        4,
                        Timestamp.valueOf(now)
                );

                statement.executeUpdate();

                try (
                        ResultSet keys =
                                statement.getGeneratedKeys()
                ) {

                    if (!keys.next()) {

                        connection.rollback();

                        throw new SQLException(
                                "Failed to create message"
                        );
                    }

                    messageId =
                            keys.getLong(1);
                }
            }

            // -------------------------------------------------
            // 4. UPDATE CHAT
            // -------------------------------------------------

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    updateChatSql
                            )
            ) {

                statement.setLong(
                        1,
                        messageId
                );

                statement.setTimestamp(
                        2,
                        Timestamp.valueOf(now)
                );

                statement.setLong(
                        3,
                        chatId
                );

                statement.executeUpdate();
            }

            // -------------------------------------------------
            // 5. UPDATE CHAT MEMBERS
            // -------------------------------------------------

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    updateChatMembersSql
                            )
            ) {

                statement.setTimestamp(
                        1,
                        Timestamp.valueOf(now)
                );

                statement.setLong(
                        2,
                        chatId
                );

                statement.executeUpdate();
            }

            // -------------------------------------------------
            // 6. SENDER DELIVERY STATUS
            // -------------------------------------------------

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    updateSenderDeliverySql
                            )
            ) {

                statement.setLong(
                        1,
                        messageId
                );

                statement.setLong(
                        2,
                        chatId
                );

                statement.setLong(
                        3,
                        senderId
                );

                statement.executeUpdate();
            }

            // -------------------------------------------------
            // 7. GET CREATED MESSAGE
            // -------------------------------------------------

            Message createdMessage = null;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    getMessageSql
                            )
            ) {

                statement.setLong(
                        1,
                        messageId
                );

                try (
                        ResultSet resultSet =
                                statement.executeQuery()
                ) {

                    if (resultSet.next()) {

                        createdMessage =
                                new Message();

                        createdMessage.setMessageId(
                                resultSet.getLong(
                                        "message_id"
                                )
                        );

                        createdMessage.setChatId(
                                resultSet.getLong(
                                        "chat_id"
                                )
                        );

                        createdMessage.setSenderId(
                                resultSet.getLong(
                                        "sender_id"
                                )
                        );

                        createdMessage.setSenderName(
                                resultSet.getString(
                                        "sender_name"
                                )
                        );

                        createdMessage.setMessage(
                                resultSet.getString(
                                        "message"
                                )
                        );

                        Timestamp timestamp =
                                resultSet.getTimestamp(
                                        "created_at"
                                );

                        if (timestamp != null) {

                            createdMessage.setCreatedAt(
                                    timestamp.toLocalDateTime()
                            );
                        }
                    }
                }
            }

            // -------------------------------------------------
            // 8. COMMIT
            // -------------------------------------------------

            connection.commit();

            return createdMessage;

        } catch (SQLException e) {

            if (connection != null) {

                try {

                    connection.rollback();

                } catch (SQLException rollbackException) {

                    rollbackException.printStackTrace();
                }
            }

            throw e;

        } finally {

            if (connection != null) {

                try {

                    connection.setAutoCommit(true);
                    connection.close();

                } catch (SQLException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    // =========================================================
    // FIND CHAT BY CHAT ID + USER ID
    //
    // This does TWO things:
    //
    // 1. Checks that the user belongs to the chat
    // 2. Gets the chat information
    //
    // =========================================================

    public Chat findChatByIdAndUserId(
            Long chatId,
            Long userId
    ) throws SQLException {

        String sql = """
            SELECT
                c.chat_id,
                c.chat_type,

                CASE
                    WHEN c.chat_type = 'PERSONAL'
                        THEN u.name

                    WHEN c.chat_type = 'GROUP'
                        THEN g.name
                END AS name,

                CASE
                    WHEN c.chat_type = 'PERSONAL'
                        THEN u.profile_image

                    WHEN c.chat_type = 'GROUP'
                        THEN g.profile_image
                END AS profile_image

            FROM chat_members cm

            JOIN chats c
                ON c.chat_id = cm.chat_id

            LEFT JOIN personal_chats pc
                ON pc.chat_id = c.chat_id

            LEFT JOIN users u
                ON u.user_id =
                    CASE
                        WHEN c.chat_type = 'PERSONAL'
                             AND pc.user1_id = cm.user_id
                            THEN pc.user2_id

                        WHEN c.chat_type = 'PERSONAL'
                             AND pc.user2_id = cm.user_id
                            THEN pc.user1_id
                    END

            LEFT JOIN group_chats g
                ON g.chat_id = c.chat_id

            WHERE cm.chat_id = ?
              AND cm.user_id = ?
            """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    chatId
            );

            statement.setLong(
                    2,
                    userId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    Chat chat =
                            new Chat();

                    chat.setChatId(
                            resultSet.getLong(
                                    "chat_id"
                            )
                    );

                    chat.setChatType(
                            resultSet.getString(
                                    "chat_type"
                            )
                    );

                    chat.setName(
                            resultSet.getString(
                                    "name"
                            )
                    );

                    chat.setProfileImage(
                            resultSet.getString(
                                    "profile_image"
                            )
                    );

                    return chat;
                }
            }
        }

        // No matching chat/member
        return null;
    }
}