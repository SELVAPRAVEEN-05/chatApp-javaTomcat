const backButton =
    document.getElementById("backButton");

const profileImage =
    document.getElementById("profileImage");

const profileName =
    document.getElementById("profileName");

const profilePhone =
    document.getElementById("profilePhone");

const userId =
    document.getElementById("userId");

const userName =
    document.getElementById("userName");

const phoneNumber =
    document.getElementById("phoneNumber");

const email =
    document.getElementById("email");

const createdAt =
    document.getElementById("createdAt");

const logoutButton =
    document.getElementById("logoutButton");

const editButton =
    document.getElementById("editButton");

const editForm =
    document.getElementById("editForm");

const editName =
    document.getElementById("editName");

const editEmail =
    document.getElementById("editEmail");

const editError =
    document.getElementById("editError");

const editSuccess =
    document.getElementById("editSuccess");

const saveButton =
    document.getElementById("saveButton");

const cancelButton =
    document.getElementById("cancelButton");


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
// LOAD PROFILE
// =========================================================

async function loadProfile() {

    try {

        const response =
            await fetch(
                "/chatApp/api/profile",
                {
                    method: "GET",

                    credentials: "include",

                    cache: "no-store"
                }
            );


        // -------------------------------------------------
        // NOT AUTHENTICATED
        // -------------------------------------------------

        if (response.status === 401) {

            window.location.replace(
                "/chatApp/"
            );

            return false;
        }


        // -------------------------------------------------
        // OTHER ERROR
        // -------------------------------------------------

        if (!response.ok) {

            throw new Error(
                "Failed to load profile"
            );
        }


        // -------------------------------------------------
        // GET USER
        // -------------------------------------------------

        const user =
            await response.json();


        // -------------------------------------------------
        // USER ID
        // -------------------------------------------------

        userId.textContent =
            user.userId;


        // -------------------------------------------------
        // NAME
        // -------------------------------------------------

        userName.textContent =
            user.name;

        profileName.textContent =
            user.name;


        // -------------------------------------------------
        // PHONE
        // -------------------------------------------------

        phoneNumber.textContent =
            user.phoneNumber;

        profilePhone.textContent =
            user.phoneNumber;


        // -------------------------------------------------
        // EMAIL
        // -------------------------------------------------

        email.textContent =
            user.email || "Not provided";


        // -------------------------------------------------
        // PROFILE IMAGE
        // -------------------------------------------------

        if (user.profileImage) {

            profileImage.src =
                user.profileImage;

            profileImage.style.display =
                "block";

        } else {

            profileImage.style.display =
                "none";
        }


        // -------------------------------------------------
        // CREATED AT
        // -------------------------------------------------

        if (user.createdAt) {

            createdAt.textContent =
                formatDateTime(
                    user.createdAt
                );

        } else {

            createdAt.textContent =
                "Not available";
        }


        return true;


    } catch (error) {

        console.error(
            "Profile loading error:",
            error
        );

        profileName.textContent =
            "Unable to load profile";

        return false;
    }
}


// =========================================================
// OPEN EDIT FORM
// =========================================================

editButton.addEventListener(
    "click",
    function () {

        editError.textContent =
            "";

        editSuccess.textContent =
            "";


        /*
         * Put current values into inputs.
         */

        editName.value =
            userName.textContent;


        /*
         * "Not provided" should become empty.
         */

        if (
            email.textContent ===
            "Not provided"
        ) {

            editEmail.value =
                "";

        } else {

            editEmail.value =
                email.textContent;
        }


        editForm.style.display =
            "block";

        editButton.style.display =
            "none";
    }
);


// =========================================================
// CANCEL EDIT
// =========================================================

cancelButton.addEventListener(
    "click",
    function () {

        editForm.style.display =
            "none";

        editButton.style.display =
            "block";

        editError.textContent =
            "";

        editSuccess.textContent =
            "";
    }
);


// =========================================================
// UPDATE PROFILE
// =========================================================

editForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        editError.textContent =
            "";

        editSuccess.textContent =
            "";


        const name =
            editName.value.trim();


        const emailValue =
            editEmail.value.trim();


        // -------------------------------------------------
        // CLIENT VALIDATION
        // -------------------------------------------------

        if (!name) {

            editError.textContent =
                "Name cannot be empty.";

            return;
        }


        if (name.length > 100) {

            editError.textContent =
                "Name cannot exceed 100 characters.";

            return;
        }


        // -------------------------------------------------
        // DISABLE BUTTON
        // -------------------------------------------------

        saveButton.disabled =
            true;

        saveButton.textContent =
            "Saving...";


        try {

            const response =
                await fetch(
                    "/chatApp/api/profile",
                    {
                        method: "PATCH",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        credentials: "include",

                        cache: "no-store",

                        body:
                            JSON.stringify({

                                name:
                                name,

                                email:
                                    emailValue ||
                                    null

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
            // RESPONSE
            // -------------------------------------------------

            const data =
                await response.json();


            // -------------------------------------------------
            // ERROR
            // -------------------------------------------------

            if (!response.ok) {

                editError.textContent =
                    data.message ||
                    "Failed to update profile.";

                return;
            }


            // -------------------------------------------------
            // SUCCESS
            // -------------------------------------------------

            editSuccess.textContent =
                "Profile updated successfully.";


            /*
             * Update displayed profile immediately.
             */

            userName.textContent =
                data.name;

            profileName.textContent =
                data.name;


            email.textContent =
                data.email ||
                "Not provided";


            /*
             * Hide edit form after short delay.
             */

            setTimeout(
                function () {

                    editForm.style.display =
                        "none";

                    editButton.style.display =
                        "block";

                    editSuccess.textContent =
                        "";

                },
                1000
            );


        } catch (error) {

            console.error(
                "Update profile error:",
                error
            );

            editError.textContent =
                "Unable to connect to server.";

        } finally {

            saveButton.disabled =
                false;

            saveButton.textContent =
                "Save Changes";
        }

    }
);


// =========================================================
// LOGOUT
// =========================================================

logoutButton.addEventListener(
    "click",
    async function () {

        logoutButton.disabled =
            true;

        logoutButton.textContent =
            "Logging out...";


        try {

            const response =
                await fetch(
                    "/chatApp/api/logout",
                    {
                        method: "POST",

                        credentials: "include",

                        cache: "no-store"
                    }
                );


            // -------------------------------------------------
            // LOGOUT SUCCESS
            // -------------------------------------------------

            if (response.ok) {

                window.location.replace(
                    "/chatApp/"
                );

                return;
            }


            // -------------------------------------------------
            // ALREADY LOGGED OUT
            // -------------------------------------------------

            if (response.status === 401) {

                window.location.replace(
                    "/chatApp/"
                );

                return;
            }


            // -------------------------------------------------
            // OTHER ERROR
            // -------------------------------------------------

            let errorMessage =
                "Logout failed";


            try {

                const error =
                    await response.json();

                errorMessage =
                    error.message ||
                    errorMessage;

            } catch {
                // Response was not JSON
            }


            alert(
                errorMessage
            );


        } catch (error) {

            console.error(
                "Logout error:",
                error
            );

            alert(
                "Unable to logout."
            );


        } finally {

            logoutButton.disabled =
                false;

            logoutButton.textContent =
                "Logout";
        }

    }
);


// =========================================================
// FORMAT DATE
// =========================================================

function formatDateTime(
    dateValue
) {

    try {

        const date =
            new Date(dateValue);


        return date.toLocaleString(
            [],
            {
                year: "numeric",

                month: "short",

                day: "numeric"
            }
        );

    } catch {

        return "";
    }
}


// =========================================================
// INITIAL LOAD
// =========================================================

loadProfile();