(function () {
    "use strict";

    const PROFILE_DATA_URL = "/profile/data";
    const PROFILE_URL = "/profile";
    const PROFILE__URL_PASSWORD = "/profile/password";

    async function loadProfile() {
        const res = await AppHttp.apiFetch(PROFILE_DATA_URL, { method: "GET" });
        if (!res) return;

        const data = await res.json();
        fillProfileForm(data);
    }

    function fillProfileForm(data) {
        const idEl = document.getElementById("profileId");
        const usernameEl = document.getElementById("username");
        const emailEl = document.getElementById("email");
        const roleEl = document.getElementById("role");

        if (idEl) idEl.value = data?.id ?? "";
        if (usernameEl) usernameEl.value = data?.username ?? "";
        if (emailEl) emailEl.value = data?.email ?? "";
        if (roleEl) roleEl.value = data?.role ?? "";
    }

    function initProfileEditForm() {
        const form = document.getElementById("profileEditForm");
        if (!form) return;

        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const username = document.getElementById("username")?.value?.trim() ?? "";
            const email = document.getElementById("email")?.value?.trim() ?? "";

            const res = await AppHttp.apiFetch(PROFILE_URL, {
                method: "PUT",
                json: { username, email }
            });

            if (!res) return;

            AppHttp.reload();

            alert("Uživatelské údaje byly změněny.")
        });
    }

    function initDeleteProfileButton() {
        const btn = document.getElementById("deleteProfileBtn");
        if (!btn) return;

        btn.addEventListener("click", async () => {
            const confirmMessage = btn.dataset.deleteConfirm ?? "Opravdu chceš účet smazat?";

            if (!confirm(confirmMessage)) return;

            const res = await AppHttp.apiFetch(PROFILE_URL, {
                method: "DELETE"
            });

            if (!res) return;

            window.location.href = "/";
        });
    }

    function initChangePasswordForm() {
        const form = document.getElementById("changePasswordForm");
        if (!form) return;

        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const currentPassword = document.getElementById("currentPassword")?.value ?? "";
            const newPassword = document.getElementById("newPassword")?.value ?? "";
            const confirmNewPassword = document.getElementById("confirmNewPassword")?.value ?? "";

            const res = await AppHttp.apiFetch(PROFILE__URL_PASSWORD, {
                method: "PUT",
                json: {
                    currentPassword,
                    newPassword,
                    confirmNewPassword
                }
            });

            if (!res) return;

            document.getElementById("currentPassword").value = "";
            document.getElementById("newPassword").value = "";
            document.getElementById("confirmNewPassword").value = "";

            alert("Heslo bylo změněno.")
        });
    }

    document.addEventListener("DOMContentLoaded", async () => {
        initProfileEditForm();
        initDeleteProfileButton();
        initChangePasswordForm();
        await loadProfile();
    });
})();