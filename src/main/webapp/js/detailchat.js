const messagesContainer =
    document.getElementById("messagesContainer");

const messageForm =
    document.getElementById("messageForm");

const messageInput =
    document.getElementById("messageInput");

const sendButton =
    document.getElementById("sendButton");

const chatName =
    document.getElementById("chatName");

const chatType =
    document.getElementById("chatType");

const profileImage =
    document.getElementById("profileImage");

const backButton =
    document.getElementById("backButton");

const loadingMessage =
    document.getElementById("loadingMessage");


// =========================================================
// GET CHAT ID FROM URL
//
// Example:
//
// detailchat.html?chatId=1
// =========================================================

const urlParams =
    new URLSearchParams(
        window.location.search
    );


const chatId =
    urlParams.get("chatId");


// =========================================================
// CHECK CHAT ID
// =========================================================

if (!chatId) {

    alert("Chat ID is missing.");

    window.location.replace(
        "chats.html"
    );
}


// =========================================================
// CURRENT LOGGED-IN USER ID
// =========================================================
//
// This value comes from:
//
// GET /api/chats/{chatId}/messages
//
// Backend decides the user ID using HttpSession.
//
// We do NOT use sessionStorage.
//
// We do NOT manually provide userId to WebSocket.
//
// =========================================================

let loggedInUserId = null;


// =========================================================
// WEBSOCKET
// =========================================================

let socket = null;


// =========================================================
// BACK BUTTON
// =========================================================

backButton.addEventListener(
    "click",
    function () {

        /*
         * Close WebSocket before leaving
         * the chat page.
         */

        if (
            socket &&
            socket.readyState ===
            WebSocket.OPEN
        ) {

            socket.close();
        }


        window.location.href =
            "chats.html";
    }
);


// =========================================================
// LOAD CHAT + OLD MESSAGES
//
// ONE REST API REQUEST
//
// GET /api/chats/{chatId}/messages
// =========================================================

async function loadChatMessages() {

    try {

        loadingMessage.style.display =
            "block";

        loadingMessage.textContent =
            "Loading messages...";


        // -------------------------------------------------
        // GET CHAT + MESSAGES
        // -------------------------------------------------

        const response =
            await fetch(
                `/chatApp/api/chats/${encodeURIComponent(chatId)}/messages`,
                {
                    method: "GET",

                    credentials: "include",

                    cache: "no-store"
                }
            );


        // -------------------------------------------------
        // SESSION EXPIRED
        // -------------------------------------------------

        if (
            response.status === 401
        ) {

            window.location.replace(
                "/chatApp/"
            );

            return false;
        }


        // -------------------------------------------------
        // NOT A MEMBER
        // -------------------------------------------------

        if (
            response.status === 403
        ) {

            alert(
                "You are not a member of this chat."
            );

            window.location.replace(
                "chats.html"
            );

            return false;
        }


        // -------------------------------------------------
        // OTHER ERROR
        // -------------------------------------------------

        if (!response.ok) {

            throw new Error(
                "Failed to load chat"
            );
        }


        // -------------------------------------------------
        // GET JSON
        // -------------------------------------------------

        const data =
            await response.json();


        // -------------------------------------------------
        // CURRENT USER ID
        // -------------------------------------------------

        loggedInUserId =
            Number(
                data.currentUserId
            );


        console.log(
            "Current user ID:",
            loggedInUserId
        );


        // -------------------------------------------------
        // CHAT INFORMATION
        // -------------------------------------------------

        const chat =
            data.chat;


        // -------------------------------------------------
        // DISPLAY CHAT NAME
        // -------------------------------------------------

        chatName.textContent =
            chat.name || "Unknown";


        // -------------------------------------------------
        // DISPLAY CHAT TYPE
        // -------------------------------------------------

        chatType.textContent =
            chat.chatType || "";


        // -------------------------------------------------
        // DISPLAY PROFILE IMAGE
        // -------------------------------------------------

        if (
            chat.profileImage
        ) {

            profileImage.src =
                chat.profileImage;

            profileImage.style.display =
                "block";

        } else {

            profileImage.style.display =
                "none";
        }


        // -------------------------------------------------
        // GET OLD MESSAGES
        // -------------------------------------------------

        const messages =
            data.messages || [];


        // -------------------------------------------------
        // REMOVE LOADING
        // -------------------------------------------------

        loadingMessage.style.display =
            "none";


        // -------------------------------------------------
        // CLEAR CONTAINER
        // -------------------------------------------------

        messagesContainer.innerHTML =
            "";


        // -------------------------------------------------
        // NO MESSAGES
        // -------------------------------------------------

        if (
            messages.length === 0
        ) {

            showEmptyMessage();

        } else {

            // -------------------------------------------------
            // DISPLAY OLD MESSAGES
            // -------------------------------------------------

            messages.forEach(
                message => {

                    displayMessage(
                        message
                    );
                }
            );
        }


        // -------------------------------------------------
        // SCROLL TO BOTTOM
        // -------------------------------------------------

        scrollToBottom();


        return true;


    } catch (error) {

        console.error(
            "Chat loading error:",
            error
        );


        loadingMessage.style.display =
            "block";


        loadingMessage.textContent =
            "Unable to load chat.";


        return false;
    }
}


// =========================================================
// SHOW EMPTY MESSAGE
// =========================================================

function showEmptyMessage() {

    /*
     * Do not create multiple
     * "No messages yet." elements.
     */

    if (
        messagesContainer.querySelector(
            ".empty-message"
        )
    ) {

        return;
    }


    const emptyMessage =
        document.createElement("div");


    emptyMessage.className =
        "empty-message";


    emptyMessage.textContent =
        "No messages yet.";


    messagesContainer.appendChild(
        emptyMessage
    );
}


// =========================================================
// REMOVE EMPTY MESSAGE
// =========================================================

function removeEmptyMessage() {

    const emptyMessage =
        messagesContainer.querySelector(
            ".empty-message"
        );


    if (emptyMessage) {

        emptyMessage.remove();
    }
}


// =========================================================
// DISPLAY MESSAGE
// =========================================================

function displayMessage(
    message
) {

    /*
     * Remove "No messages yet."
     * when a real message arrives.
     */

    removeEmptyMessage();


    const messageElement =
        document.createElement("div");


    // -----------------------------------------------------
    // CHECK SENDER
    // -----------------------------------------------------

    if (
        Number(message.senderId) ===
        loggedInUserId
    ) {

        messageElement.className =
            "message my-message";

    } else {

        messageElement.className =
            "message other-message";
    }


    // -----------------------------------------------------
    // OTHER USER NAME
    // -----------------------------------------------------

    if (
        Number(message.senderId) !==
        loggedInUserId
    ) {

        const senderName =
            document.createElement("div");


        senderName.className =
            "sender-name";


        senderName.textContent =
            message.senderName ||
            "Unknown";


        messageElement.appendChild(
            senderName
        );
    }


    // -----------------------------------------------------
    // MESSAGE TEXT
    // -----------------------------------------------------

    const messageText =
        document.createElement("div");


    messageText.className =
        "message-text";


    messageText.textContent =
        message.message;


    messageElement.appendChild(
        messageText
    );


    // -----------------------------------------------------
    // MESSAGE TIME
    // -----------------------------------------------------

    if (
        message.createdAt
    ) {

        const messageTime =
            document.createElement("div");


        messageTime.className =
            "message-time";


        messageTime.textContent =
            formatDateTime(
                message.createdAt
            );


        messageElement.appendChild(
            messageTime
        );
    }


    // -----------------------------------------------------
    // ADD MESSAGE TO CONTAINER
    // -----------------------------------------------------

    messagesContainer.appendChild(
        messageElement
    );
}


// =========================================================
// SEND MESSAGE THROUGH WEBSOCKET
//
// IMPORTANT:
//
// We NO LONGER use:
//
// POST /api/chats/{chatId}/messages
//
// WebSocket is now responsible for:
//
// 1. Sending message
// 2. Authentication
// 3. Membership check
// 4. Saving message
// 5. Broadcasting message
//
// =========================================================

messageForm.addEventListener(
    "submit",
    function (event) {

        event.preventDefault();


        // -------------------------------------------------
        // GET MESSAGE
        // -------------------------------------------------

        const message =
            messageInput.value.trim();


        // -------------------------------------------------
        // EMPTY MESSAGE
        // -------------------------------------------------

        if (!message) {

            return;
        }


        // -------------------------------------------------
        // CHECK WEBSOCKET
        // -------------------------------------------------

        if (
            !socket ||
            socket.readyState !==
            WebSocket.OPEN
        ) {

            alert(
                "WebSocket is not connected."
            );

            return;
        }


        // -------------------------------------------------
        // DISABLE SEND BUTTON
        // -------------------------------------------------

        sendButton.disabled =
            true;


        try {

            // -------------------------------------------------
            // SEND MESSAGE THROUGH WEBSOCKET
            // -------------------------------------------------
            //
            // We DO NOT send:
            //
            // userId
            //
            // chatId
            //
            // The server already knows:
            //
            // userId -> WebSocket session
            // chatId -> JOINED chat
            //
            // -------------------------------------------------

            socket.send(
                JSON.stringify({

                    type:
                        "MESSAGE",

                    message:
                    message
                })
            );


            // -------------------------------------------------
            // CLEAR INPUT
            // -------------------------------------------------

            messageInput.value =
                "";


        } catch (error) {

            console.error(
                "WebSocket send error:",
                error
            );


            alert(
                "Unable to send message."
            );

        } finally {

            sendButton.disabled =
                false;


            messageInput.focus();
        }
    }
);


// =========================================================
// CONNECT TO WEBSOCKET
// =========================================================
//
// WebSocket:
//
// ws://localhost:8080/chatApp/ws/chat
//
// =========================================================

function connectWebSocket() {

    console.log(
        "Connecting to WebSocket..."
    );


    // -------------------------------------------------
    // CREATE CONNECTION
    // -------------------------------------------------

    socket =
        new WebSocket(
            "ws://localhost:8080/chatApp/ws/chat"
        );


    // =====================================================
    // WEBSOCKET OPEN
    // =====================================================

    socket.onopen =
        function () {

            console.log(
                "CONNECTED TO WEBSOCKET"
            );


            // -------------------------------------------------
            // JOIN CURRENT CHAT
            // -------------------------------------------------
            //
            // Example:
            //
            // {
            //     "type": "JOIN",
            //     "chatId": 1
            // }
            //
            // -------------------------------------------------

            socket.send(
                JSON.stringify({

                    type:
                        "JOIN",

                    chatId:
                        Number(chatId)
                })
            );


            console.log(
                "JOIN request sent for chat:",
                chatId
            );
        };


    // =====================================================
    // RECEIVE MESSAGE
    // =====================================================

    socket.onmessage =
        function (event) {

            console.log(
                "SERVER:",
                event.data
            );


            let data;


            // -------------------------------------------------
            // PARSE JSON
            // -------------------------------------------------

            try {

                data =
                    JSON.parse(
                        event.data
                    );

            } catch (error) {

                console.error(
                    "Invalid WebSocket JSON:",
                    error
                );

                return;
            }


            // =================================================
            // JOINED
            // =================================================

            if (
                data.type ===
                "JOINED"
            ) {

                console.log(
                    "Joined chat:",
                    data.chatId
                );

                return;
            }


            // =================================================
            // NEW MESSAGE
            // =================================================

            if (
                data.type ===
                "MESSAGE"
            ) {

                /*
                 * Server has already:
                 *
                 * 1. Authenticated user
                 * 2. Checked membership
                 * 3. Saved message
                 * 4. Broadcast message
                 *
                 * So simply display it.
                 */

                displayMessage(
                    data
                );


                scrollToBottom();


                return;
            }


            // =================================================
            // ERROR
            // =================================================

            if (
                data.type ===
                "ERROR"
            ) {

                console.error(
                    "WebSocket error:",
                    data.message
                );


                alert(
                    data.message
                );


                return;
            }


            // =================================================
            // UNKNOWN RESPONSE
            // =================================================

            console.log(
                "Unknown WebSocket response:",
                data
            );
        };


    // =====================================================
    // WEBSOCKET CLOSE
    // =====================================================

    socket.onclose =
        function (event) {

            console.log(
                "DISCONNECTED FROM WEBSOCKET"
            );


            console.log(
                "Close code:",
                event.code
            );


            console.log(
                "Close reason:",
                event.reason
            );
        };


    // =====================================================
    // WEBSOCKET ERROR
    // =====================================================

    socket.onerror =
        function (error) {

            console.error(
                "WEBSOCKET ERROR:",
                error
            );
        };
}

// =========================================================
// FORMAT TIME
// =========================================================

function formatDateTime(
    dateValue
) {

    try {

        const date =
            new Date(dateValue);


        return date.toLocaleTimeString(
            [],
            {
                hour: "2-digit",

                minute: "2-digit"
            }
        );

    } catch {

        return "";
    }
}


// =========================================================
// SCROLL TO BOTTOM
// =========================================================

function scrollToBottom() {

    messagesContainer.scrollTop =
        messagesContainer.scrollHeight;
}


// =========================================================
// INITIALIZE CHAT
// =========================================================
//
// 1. REST GET
//       ↓
//    Load old messages
//
// 2. Get currentUserId
//
// 3. Connect WebSocket
//
// 4. JOIN chat
//
// =========================================================

async function initializeChat() {

    console.log(
        "Initializing chat..."
    );


    // -------------------------------------------------
    // LOAD CHAT + OLD MESSAGES
    // -------------------------------------------------

    const chatLoaded =
        await loadChatMessages();


    // -------------------------------------------------
    // STOP IF REST REQUEST FAILED
    // -------------------------------------------------

    if (!chatLoaded) {

        return;
    }


    // -------------------------------------------------
    // CONNECT WEBSOCKET
    // -------------------------------------------------

    connectWebSocket();
}


// =========================================================
// START APPLICATION
// =========================================================

initializeChat();