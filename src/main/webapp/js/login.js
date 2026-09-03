const loginForm = document.getElementById("loginForm");

const phoneNumberInput =
    document.getElementById("phoneNumber");

const passwordInput =
    document.getElementById("password");

const errorMessage =
    document.getElementById("errorMessage");

const loginButton =
    document.getElementById("loginButton");


loginForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    errorMessage.textContent = "";


    const phoneNumber =
        phoneNumberInput.value.trim();

    const password =
        passwordInput.value;


    if (!phoneNumber) {

        errorMessage.textContent =
            "Phone number is required.";

        return;
    }


    if (!password) {

        errorMessage.textContent =
            "Password is required.";

        return;
    }


    loginButton.disabled = true;

    loginButton.textContent = "Logging in...";


    try {

        const response = await fetch(
            "/chatApp/api/login",
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                credentials: "include",

                body: JSON.stringify({
                    phoneNumber: phoneNumber,
                    password: password
                })
            }
        );


        const data =
            await response.json();


        if (!response.ok) {

            errorMessage.textContent =
                data.message || "Login failed.";

            return;
        }


        console.log("Login successful:", data);


        /*
         * Login successful.
         *
         * The server has created the
         * HTTP session.
         *
         * Browser stores the session cookie.
         */

        window.location.href =
            "chats.html";


    } catch (error) {

        console.error(
            "Login error:",
            error
        );

        errorMessage.textContent =
            "Unable to connect to server.";

    } finally {

        loginButton.disabled = false;

        loginButton.textContent = "Login";
    }

});