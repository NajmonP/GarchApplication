(function () {
    window.showErrorModal = function (message) {
        const modalEl = document.getElementById("errorModal");
        const msgEl = document.getElementById("errorModalMessage");

        if (!modalEl || !msgEl || typeof bootstrap === "undefined") return;

        msgEl.textContent = message ?? "Došlo k chybě.";

        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        modal.show();
    };
})();
