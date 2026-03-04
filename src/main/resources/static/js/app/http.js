(function () {
    "use strict";

    function readCsrf() {
        const headerEl = document.getElementById("csrfHeaderName");
        const tokenEl = document.getElementById("csrfToken");

        if (headerEl?.value && tokenEl?.value) {
            return { headerName: headerEl.value, token: tokenEl.value };
        }

        const form = document.getElementById("csrfForm");
        if (form) {
            const headerName = headerEl?.value;
            const tokenInput = form.querySelector('input[type="hidden"][name]');
            const token = tokenInput?.value;

            if (headerName && token) {
                return { headerName, token };
            }
        }

        return null;
    }

    function addCsrf(headers) {
        const csrf = readCsrf();
        if (!csrf) return headers;
        headers[csrf.headerName] = csrf.token;
        return headers;
    }

    async function apiFetch(url, options = {}) {
        const {
            method = "GET",
            headers: customHeaders = {},
            json,
            body,
            credentials = "same-origin"
        } = options;

        const headers = { ...customHeaders };
        let finalBody = body;

        if (json !== undefined) {
            headers["Content-Type"] = headers["Content-Type"] ?? "application/json";
            finalBody = JSON.stringify(json);
        }

        addCsrf(headers);

        try {
            const res = await fetch(url, {
                method,
                headers,
                body: finalBody,
                credentials
            });

            if (!res.ok) {
                let errorMessage = "Došlo k chybě při komunikaci se serverem.";

                try {
                    const text = await res.text();
                    if (text) errorMessage = text;
                } catch (_) {}

                if (typeof AppModal.showError === "function") {
                    AppModal.showError(errorMessage);
                }

                return null;
            }

            return res;

        } catch (networkError) {
            if (typeof AppModal.showError === "function") {
                AppModal.showError("Chyba sítě nebo server není dostupný.");
            }
            return null;
        }
    }

    function reload() {
        window.location.reload();
    }

    window.AppHttp = { apiFetch, addCsrf, reload };
})();