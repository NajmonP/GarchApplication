(function () {
    "use strict";

    function initDeleteButtons(selector = ".js-delete") {
        document.addEventListener("click", async (e) => {
            const btn = e.target.closest(selector);
            if (!btn) return;

            const baseUrl = btn.dataset.deleteUrl;
            const id = btn.dataset.deleteId;
            const confirmMessage = btn.dataset.deleteConfirm ?? "Opravdu chceš položku smazat?";

            if (!baseUrl || !id) return;
            if (!confirm(confirmMessage)) return;

            const res = await AppHttp.apiFetch(`${baseUrl}/${id}`, { method: "DELETE" });
            if (!res) return;

            const shouldReload = btn.dataset.deleteReload !== "false";
            if (shouldReload) AppHttp.reload();
        });
    }

    function initDownloadButtons(selector = ".js-download") {
        document.addEventListener("click", async (e) => {
            const btn = e.target.closest(selector);
            if (!btn) return;

            const baseUrl = btn.dataset.downloadUrl;
            const id = btn.dataset.downloadId;

            if (!baseUrl || !id) return;

            const res = await AppHttp.apiFetch(`${baseUrl}/${id}`, { method: "GET" });
            if (!res) return;

            const blob = await res.blob();

            const cd = res.headers.get("Content-Disposition");

            const m = cd?.match(/filename\*\s*=\s*UTF-8''([^;]+)/i);
            const filenameFromHeader = m?.[1] ? decodeURIComponent(m[1]) : null;

            const filename =
                filenameFromHeader ||
                btn.dataset.filename ||
                `entity-${id}.xlsx`;


            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            a.remove();
            URL.revokeObjectURL(url);
        });
    }

    function initUploadForm(formSelector = "#uploadForm") {
        const form = document.querySelector(formSelector);
        if (!form) return;

        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const url = form.action;
            const fd = new FormData(form);

            const res = await AppHttp.apiFetch(url, {
                method: "POST",
                body: fd
            });

            if (!res) return;

            AppHttp.reload();
        });
    }

    window.InitButtons = { initDeleteButtons, initDownloadButtons, initUploadForm };
})();