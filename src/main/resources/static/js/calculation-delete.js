document.addEventListener("DOMContentLoaded", () => {
    const deleteButtons = document.querySelectorAll(".js-delete-calculation");

    // 1) vytáhni CSRF token + název hlavičky ze stránky
    const csrfTokenInput = document.querySelector("#csrfForm input[type='hidden'][name]");
    const csrfHeaderNameEl = document.getElementById("csrfHeaderName");

    const csrfToken = csrfTokenInput?.value;
    const csrfHeaderName = csrfHeaderNameEl?.value;

    deleteButtons.forEach(btn => {
        btn.addEventListener("click", async () => {
            const calculationId = btn.getAttribute("data-id");
            if (!calculationId) return;

            if (!confirm("Opravdu chceš smazat tuto kalkulaci?")) return;

            try {
                const headers = {};
                if (csrfToken && csrfHeaderName) {
                    headers[csrfHeaderName] = csrfToken; // typicky "X-CSRF-TOKEN"
                }

                const response = await fetch(`/calculation/${calculationId}`, {
                    method: "DELETE",
                    headers
                });

                if (!response.ok) {
                    const text = await response.text().catch(() => "");
                    console.error("Delete failed:", response.status, text);
                    alert(`Nepodařilo se smazat kalkulaci (HTTP ${response.status}).`);
                    return;
                }

                btn.closest(".card")?.remove();
            } catch (error) {
                console.error(error);
                alert("Chyba při mazání.");
            }
        });
    });
});