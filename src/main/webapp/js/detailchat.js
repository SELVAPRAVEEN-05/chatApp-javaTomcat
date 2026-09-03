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
// chat.html?chatId=1
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
// BACK BUTTON
// =========================================================

backButton.addEventListener(
    "click",
    function () {

        window.location.href =
            "chats.html";
    }
);


// =========================================================
// LOAD CHAT + MESSAGES
//
// ONE API REQUEST
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
        // ONE REQUEST ONLY
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

        if (response.status === 401) {

            window.location.replace(
                "/chatApp/"
            );

            return;
        }


        // -------------------------------------------------
        // NOT A MEMBER
        // -------------------------------------------------

        if (response.status === 403) {

            alert(
                "You are not a member of this chat."
            );

            window.location.replace(
                "chats.html"
            );

            return;
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
        // GET RESPONSE
        // -------------------------------------------------

        const data =
            await response.json();


        // -------------------------------------------------
        // CURRENT USER ID
        // -------------------------------------------------

        const loggedInUserId =
            Number(
                data.currentUserId
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

        if (chat.profileImage) {

            profileImage.src =
                chat.profileImage;

            profileImage.style.display =
                "block";

        } else {

            profileImage.style.display =
                "none";
        }


        // -------------------------------------------------
        // GET MESSAGES
        // -------------------------------------------------

        const messages =
            data.messages || [];


        // -------------------------------------------------
        // REMOVE LOADING
        // -------------------------------------------------

        loadingMessage.style.display =
            "none";


        // -------------------------------------------------
        // CLEAR MESSAGES
        // -------------------------------------------------

        messagesContainer.innerHTML =
            "";


        // -------------------------------------------------
        // NO MESSAGES
        // -------------------------------------------------

        if (messages.length === 0) {

            const emptyMessage =
                document.createElement("div");

            emptyMessage.className =
                "empty-message";

            emptyMessage.textContent =
                "No messages yet.";

            messagesContainer.appendChild(
                emptyMessage
            );

            return;
        }


        // -------------------------------------------------
        // DISPLAY MESSAGES
        // -------------------------------------------------

        messages.forEach(
            message => {

                displayMessage(
                    message,
                    loggedInUserId
                );
            }
        );


        // -------------------------------------------------
        // SCROLL TO BOTTOM
        // -------------------------------------------------

        scrollToBottom();


    } catch (error) {

        console.error(
            "Chat loading error:",
            error
        );


        loadingMessage.style.display =
            "block";


        loadingMessage.textContent =
            "Unable to load chat.";
    }
}


// =========================================================
// DISPLAY MESSAGE
// =========================================================

function displayMessage(
    message,
    loggedInUserId
) {

    const messageElement =
        document.createElement("div");


    // -----------------------------------------------------
    // CHECK WHETHER MESSAGE BELONGS TO CURRENT USER
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
            message.senderName;


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

    if (message.createdAt) {

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
    // ADD TO CONTAINER
    // -----------------------------------------------------

    messagesContainer.appendChild(
        messageElement
    );
}


// =========================================================
// SEND MESSAGE
//
// POST /api/chats/{chatId}/messages
// =========================================================

messageForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        const message =
            messageInput.value.trim();


        // -------------------------------------------------
        // EMPTY MESSAGE
        // -------------------------------------------------

        if (!message) {

            return;
        }


        // -------------------------------------------------
        // DISABLE BUTTON
        // -------------------------------------------------

        sendButton.disabled =
            true;


        try {

            // -------------------------------------------------
            // SEND REQUEST
            // -------------------------------------------------

            const response =
                await fetch(
                    `/chatApp/api/chats/${encodeURIComponent(chatId)}/messages`,
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        credentials: "include",

                        body:
                            JSON.stringify({
                                message:
                                message
                            })
                    }
                );


            // -------------------------------------------------
            // SESSION EXPIRED
            // -------------------------------------------------

            if (response.status === 401) {

                window.location.replace(
                    "/chatApp/"
                );

                return;
            }


            // -------------------------------------------------
            // NOT MEMBER
            // -------------------------------------------------

            if (response.status === 403) {

                alert(
                    "You are not a member of this chat."
                );

                window.location.replace(
                    "chats.html"
                );

                return;
            }


            // -------------------------------------------------
            // OTHER ERROR
            // -------------------------------------------------

            if (!response.ok) {

                const error =
                    await response.json();


                alert(
                    error.message ||
                    "Failed to send message."
                );

                return;
            }


            // -------------------------------------------------
            // CREATED MESSAGE
            // -------------------------------------------------

            const createdMessage =
                await response.json();


            // -------------------------------------------------
            // CLEAR INPUT
            // -------------------------------------------------

            messageInput.value =
                "";


            // -------------------------------------------------
            // GET CURRENT USER ID
            //
            // We can determine it from senderId here because
            // this message was created by the logged-in user.
            // -------------------------------------------------

            displayMessage(
                createdMessage,
                Number(createdMessage.senderId)
            );


            // -------------------------------------------------
            // SCROLL
            // -------------------------------------------------

            scrollToBottom();


        } catch (error) {

            console.error(
                "Send message error:",
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
// INITIAL LOAD
// =========================================================

loadChatMessages();