const registerForm =
    document.getElementById("registerForm");


const nameInput =
    document.getElementById("name");


const phoneInput =
    document.getElementById("phoneNumber");


const emailInput =
    document.getElementById("email");


const passwordInput =
    document.getElementById("password");


const confirmPasswordInput =
    document.getElementById("confirmPassword");


const registerButton =
    document.getElementById("registerButton");


const errorMessage =
    document.getElementById("errorMessage");


const successMessage =
    document.getElementById("successMessage");



/*
 * =========================================================
 * REGISTER
 * =========================================================
 */

registerForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        errorMessage.textContent = "";

        successMessage.textContent = "";


        const name =
            nameInput.value.trim();


        const phoneNumber =
            phoneInput.value.trim();


        const email =
            emailInput.value.trim();


        const password =
            passwordInput.value;


        const confirmPassword =
            confirmPasswordInput.value;



        /*
         * =====================================================
         * CLIENT VALIDATION
         * =====================================================
         */

        if (!name) {

            errorMessage.textContent =
                "Please enter your name.";

            return;
        }


        if (!phoneNumber) {

            errorMessage.textContent =
                "Please enter your phone number.";

            return;
        }


        if (password.length < 8) {

            errorMessage.textContent =
                "Password must contain at least 8 characters.";

            return;
        }


        if (password !== confirmPassword) {

            errorMessage.textContent =
                "Passwords do not match.";

            return;
        }



        /*
         * =====================================================
         * DISABLE BUTTON
         * =====================================================
         */

        registerButton.disabled = true;

        registerButton.textContent =
            "Creating account...";



        try {

            /*
             * =================================================
             * SEND REQUEST
             * =================================================
             */

            const response =
                await fetch(
                    "/chatApp/api/register",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        credentials: "include",

                        body:
                            JSON.stringify({

                                name:
                                name,

                                phoneNumber:
                                phoneNumber,

                                email:
                                    email || null,

                                password:
                                password

                            })
                    }
                );



            /*
             * =================================================
             * RESPONSE
             * =================================================
             */

            const data =
                await response.json();



            /*
             * Registration successful
             */

            if (response.status === 201) {

                successMessage.textContent =
                    "Registration successful! Redirecting to login...";


                registerForm.reset();


                setTimeout(
                    function () {

                        window.location.href =
                            "/chatApp/";

                    },
                    1500
                );


                return;
            }



            /*
             * Validation / duplicate error
             */

            if (response.status === 400) {

                errorMessage.textContent =
                    data.message ||
                    "Invalid registration details.";

                return;
            }



            /*
             * Server error
             */

            errorMessage.textContent =
                data.message ||
                "Registration failed.";

        } catch (error) {

            console.error(
                "Registration error:",
                error
            );


            errorMessage.textContent =
                "Unable to connect to server.";

        } finally {

            registerButton.disabled = false;

            registerButton.textContent =
                "Register";
        }

    }
);