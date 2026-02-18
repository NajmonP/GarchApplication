document.addEventListener("DOMContentLoaded", () => {
    initEditModal();
    initDeleteButtons();
});

function initEditModal() {
    const modalEl = document.getElementById("editTimeSeriesModal");
    if (!modalEl) return;

    let currentId = null;

    modalEl.addEventListener("show.bs.modal", (event) => {
        const button = event.relatedTarget;
        if (!button) return;

        currentId = button.getAttribute("data-id");
        const name = button.getAttribute("data-name");

        const nameInput = document.getElementById("timeSeriesName");
        nameInput.value = name ?? "";
    });

    const form = document.getElementById("editTimeSeriesForm");
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        if (!currentId) return;

        const name = document.getElementById("timeSeriesName").value;

        const headers = { "Content-Type": "application/json" };
        attachCsrf(headers);

        try {
            const res = await fetch(`/time-series/${currentId}`, {
                method: "PUT",
                headers,
                body: JSON.stringify({ name }),
                credentials: "same-origin"
            });

            if (!res.ok) {
                alert("Nepodařilo se upravit časovou řadu.");
                return;
            }

            window.location.reload();
        } catch (err) {
            console.error(err);
            alert("Chyba při odesílání požadavku (fetch).");
        }
    });
}

function initDeleteButtons() {
    document.querySelectorAll(".js-delete-time-series").forEach(btn => {
        btn.addEventListener("click", async () => {
            const id = btn.getAttribute("data-id");
            if (!id) return;

            if (!confirm("Opravdu chceš smazat tuto časovou řadu?")) return;

            const headers = {};
            attachCsrf(headers);

            try {
                const res = await fetch(`/time-series/${id}`, {
                    method: "DELETE",
                    headers,
                    credentials: "same-origin"
                });

                if (!res.ok) {
                    alert("Nepodařilo se smazat časovou řadu.");
                    return;
                }

                window.location.reload();
            } catch (err) {
                console.error(err);
                alert("Chyba při odesílání požadavku (fetch).");
            }
        });
    });
}

function attachCsrf(headers) {
    const tokenEl = document.getElementById("csrfToken");
    const headerEl = document.getElementById("csrfHeaderName");
    if (!tokenEl || !headerEl) return;

    headers[headerEl.value] = tokenEl.value;
}
