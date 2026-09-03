const chatList =
    document.getElementById("chatList");

const profileButton =
    document.getElementById("profileButton");

const filterButtons =
    document.querySelectorAll(".filter-button");


/*
 * =========================================
 * CURRENT FILTER
 * =========================================
 */

let currentFilter = "all";


/*
 * =========================================
 * LOAD CHATS
 * =========================================
 */

async function loadChats(filter = "all") {

    try {

        /*
         * Show loading
         */

        chatList.innerHTML = `
            <p class="loading">
                Loading chats...
            </p>
        `;


        /*
         * Build URL
         */

        const url =
            `/chatApp/api/chats?filter=${encodeURIComponent(filter)}`;


        /*
         * Send request
         */

        const response =
            await fetch(
                url,
                {
                    method: "GET",

                    credentials: "include",

                    cache: "no-store"
                }
            );


        /*
         * Authentication failed
         */

        if (response.status === 401) {

            window.location.replace(
                "/chatApp/"
            );

            return;
        }


        /*
         * Convert response
         */

        const data =
            await response.json();


        /*
         * Other error
         */

        if (!response.ok) {

            showError(
                data.message ||
                "Failed to load chats."
            );

            return;
        }


        /*
         * Display chats
         */

        renderChats(data);

    } catch (error) {

        console.error(
            "Error loading chats:",
            error
        );

        showError(
            "Unable to connect to server."
        );
    }
}


/*
 * =========================================
 * RENDER CHATS
 * =========================================
 */

function renderChats(chatData) {

    chatList.innerHTML = "";


    /*
     * No chats
     */

    if (
        !chatData ||
        chatData.length === 0
    ) {

        chatList.innerHTML = `
            <p class="empty">
                No chats found.
            </p>
        `;

        return;
    }


    /*
     * Create every chat
     */

    chatData.forEach(
        chat => {

            const chatElement =
                createChatElement(chat);

            chatList.appendChild(
                chatElement
            );
        }
    );
}


/*
 * =========================================
 * CREATE CHAT ELEMENT
 * =========================================
 */

function createChatElement(chat) {

    const chatItem =
        document.createElement("div");

    chatItem.className =
        "chat-item";


    /*
     * Open chat
     */

    chatItem.addEventListener(
        "click",
        function () {

            window.location.href =
                `chat.html?chatId=${chat.chatId}`;
        }
    );


    /*
     * =====================================
     * IMAGE
     * =====================================
     */

    const image =
        document.createElement("img");

    image.className =
        "chat-image";

    image.src =
        chat.profileImage
            ? chat.profileImage
            : "images/default-profile.png";


    image.onerror =
        function () {

            this.style.display =
                "none";
        };


    /*
     * =====================================
     * INFORMATION
     * =====================================
     */

    const chatInfo =
        document.createElement("div");

    chatInfo.className =
        "chat-info";


    /*
     * =====================================
     * TOP ROW
     * =====================================
     */

    const chatTop =
        document.createElement("div");

    chatTop.className =
        "chat-top";


    /*
     * Name
     */

    const name =
        document.createElement("div");

    name.className =
        "chat-name";

    name.textContent =
        chat.name || "Unknown";


    /*
     * Time
     */

    const time =
        document.createElement("div");

    time.className =
        "chat-time";

    time.textContent =
        formatDate(
            chat.lastMessageAt
        );


    chatTop.appendChild(name);

    chatTop.appendChild(time);


    /*
     * =====================================
     * BOTTOM ROW
     * =====================================
     */

    const chatBottom =
        document.createElement("div");

    chatBottom.className =
        "chat-bottom";


    /*
     * Last message
     */

    const lastMessage =
        document.createElement("div");

    lastMessage.className =
        "last-message";

    lastMessage.textContent =
        chat.lastMessage || "";


    chatBottom.appendChild(
        lastMessage
    );


    /*
     * =====================================
     * UNREAD COUNT
     * =====================================
     */

    if (
        chat.unreadCount > 0
    ) {

        const unread =
            document.createElement("div");

        unread.className =
            "unread-count";

        unread.textContent =
            chat.unreadCount;

        chatBottom.appendChild(
            unread
        );
    }


    /*
     * Add rows
     */

    chatInfo.appendChild(
        chatTop
    );

    chatInfo.appendChild(
        chatBottom
    );


    /*
     * Add everything
     */

    chatItem.appendChild(
        image
    );

    chatItem.appendChild(
        chatInfo
    );


    return chatItem;
}


/*
 * =========================================
 * FORMAT DATE
 * =========================================
 */

function formatDate(dateValue) {

    if (!dateValue) {
        return "";
    }


    const date =
        new Date(dateValue);


    if (isNaN(date.getTime())) {

        return "";
    }


    const now =
        new Date();


    /*
     * Same day
     */

    if (
        date.toDateString() ===
        now.toDateString()
    ) {

        return date.toLocaleTimeString(
            [],
            {
                hour: "2-digit",

                minute: "2-digit"
            }
        );
    }


    /*
     * Different day
     */

    return date.toLocaleDateString(
        [],
        {
            day: "2-digit",

            month: "short"
        }
    );
}


/*
 * =========================================
 * FILTER BUTTONS
 * =========================================
 */

filterButtons.forEach(
    button => {

        button.addEventListener(
            "click",
            function () {

                /*
                 * Get filter
                 */

                const filter =
                    this.dataset.filter;


                /*
                 * Save current filter
                 */

                currentFilter =
                    filter;


                /*
                 * Remove active
                 * from every button
                 */

                filterButtons.forEach(
                    btn => {

                        btn.classList.remove(
                            "active"
                        );
                    }
                );


                /*
                 * Activate clicked button
                 */

                this.classList.add(
                    "active"
                );


                /*
                 * Ask backend for
                 * only required chats
                 */

                loadChats(filter);
            }
        );
    }
);


/*
 * =========================================
 * PROFILE
 * =========================================
 */

profileButton.addEventListener(
    "click",
    function () {

        window.location.href =
            "profile.html";
    }
);


/*
 * =========================================
 * ERROR
 * =========================================
 */

function showError(message) {

    chatList.innerHTML = `
        <p class="error">
            ${message}
        </p>
    `;
}


/*
 * =========================================
 * INITIAL LOAD
 * =========================================
 */

loadChats("all");