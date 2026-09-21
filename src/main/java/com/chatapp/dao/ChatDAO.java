package com.chatapp.dao;

import com.chatapp.model.Chat;
import com.chatapp.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {


    public List<Chat> findChatsByUserId(
            Long userId,
            String filter
    ) {

        List<Chat> chats =
                new ArrayList<>();


        /*
         * =========================================
         * BASE QUERY
         * =========================================
         */

        StringBuilder sql =
                new StringBuilder("""
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
                    END AS profile_image,

                    m.message AS last_message,

                    m.created_at AS last_message_at,

                    (
                        SELECT COUNT(*)

                        FROM messages unread_msg

                        WHERE unread_msg.chat_id =
                              c.chat_id

                          AND unread_msg.message_id >
                              COALESCE(
                                  cm.last_read_message_id,
                                  0
                              )

                          AND unread_msg.sender_id <> ?
                    ) AS unread_count

                FROM chat_members cm

                JOIN chats c
                    ON c.chat_id =
                       cm.chat_id

                LEFT JOIN personal_chats pc
                    ON pc.chat_id =
                       c.chat_id

                LEFT JOIN users u
                    ON u.user_id =
                        CASE

                            WHEN pc.user1_id =
                                 cm.user_id

                                THEN pc.user2_id

                            WHEN pc.user2_id =
                                 cm.user_id

                                THEN pc.user1_id

                        END

                LEFT JOIN group_chats g
                    ON g.chat_id =
                       c.chat_id

                LEFT JOIN messages m
                    ON m.message_id =
                       c.last_message_id

                WHERE cm.user_id = ?
                """);


        /*
         * =========================================
         * FILTER
         * =========================================
         */

        if ("unread".equals(filter)) {

            sql.append("""
                    
                    AND (
                        SELECT COUNT(*)

                        FROM messages unread_filter_msg

                        WHERE unread_filter_msg.chat_id =
                              c.chat_id

                          AND unread_filter_msg.message_id >
                              COALESCE(
                                  cm.last_read_message_id,
                                  0
                              )

                          AND unread_filter_msg.sender_id <> ?
                    ) > 0
                    """);
        }


        else if ("read".equals(filter)) {

            sql.append("""
                    
                    AND (
                        SELECT COUNT(*)

                        FROM messages read_filter_msg

                        WHERE read_filter_msg.chat_id =
                              c.chat_id

                          AND read_filter_msg.message_id >
                              COALESCE(
                                  cm.last_read_message_id,
                                  0
                              )

                          AND read_filter_msg.sender_id <> ?
                    ) = 0
                    """);
        }


        else if ("groups".equals(filter)) {

            sql.append("""
                    
                    AND c.chat_type = 'GROUP'
                    """);
        }
        /*
         * =========================================
         * ORDER
         * =========================================
         */

        sql.append("""
                
                ORDER BY
                    c.last_message_at DESC
                """);


        /*
         * =========================================
         * DATABASE
         * =========================================
         */

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql.toString()
                        )
        ) {


            /*
             * =====================================
             * PARAMETER 1
             *
             * unread_count
             * =====================================
             */

            int parameterIndex = 1;

            statement.setLong(
                    parameterIndex++,
                    userId
            );


            /*
             * =====================================
             * PARAMETER 2
             *
             * cm.user_id
             * =====================================
             */

            statement.setLong(
                    parameterIndex++,
                    userId
            );


            /*
             * =====================================
             * FILTER PARAMETER
             * =====================================
             */

            if (
                    "unread".equals(filter) ||
                            "read".equals(filter)
            ) {

                statement.setLong(
                        parameterIndex++,
                        userId
                );
            }


            /*
             * =====================================
             * EXECUTE
             * =====================================
             */

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (
                        resultSet.next()
                ) {

                    Chat chat =
                            new Chat();


                    /*
                     * Chat ID
                     */

                    chat.setChatId(
                            resultSet.getLong(
                                    "chat_id"
                            )
                    );


                    /*
                     * Chat type
                     */

                    chat.setChatType(
                            resultSet.getString(
                                    "chat_type"
                            )
                    );


                    /*
                     * Name
                     */

                    chat.setName(
                            resultSet.getString(
                                    "name"
                            )
                    );


                    /*
                     * Profile image
                     */

                    chat.setProfileImage(
                            resultSet.getString(
                                    "profile_image"
                            )
                    );


                    /*
                     * Last message
                     */

                    chat.setLastMessage(
                            resultSet.getString(
                                    "last_message"
                            )
                    );


                    /*
                     * Last message time
                     */

                    if (
                            resultSet.getTimestamp(
                                    "last_message_at"
                            ) != null
                    ) {

                        chat.setLastMessageAt(
                                resultSet
                                        .getTimestamp(
                                                "last_message_at"
                                        )
                                        .toLocalDateTime()
                        );
                    }


                    /*
                     * Unread count
                     */

                    chat.setUnreadCount(
                            resultSet.getInt(
                                    "unread_count"
                            )
                    );


                    /*
                     * Add chat
                     */

                    chats.add(chat);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return chats;
    }

}