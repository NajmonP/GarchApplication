document.addEventListener("DOMContentLoaded", () => {

    document.querySelectorAll(".edit-config-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.dataset.configurationId;
            const name = btn.dataset.configurationName ?? "";

            document.getElementById("editConfigId").value = id;
            document.getElementById("editConfigName").value = name;
        });
    });


    const saveBtn = document.getElementById("btnSaveConfigChanges");
    if (saveBtn) {
        saveBtn.addEventListener("click", async () => {

            const id = document.getElementById("editConfigId")?.value?.trim();
            const name = document.getElementById("editConfigName")?.value?.trim();

            if (!id || !name) {
                alert("Název nesmí být prázdný.");
                return;
            }

            try {
                const response = await fetch(`/configuration/${id}`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        ...csrfHeaders()
                    },
                    body: JSON.stringify({ name })
                });

                if (!response.ok) {
                    const text = await response.text().catch(() => "");
                    console.error("UPDATE CONFIG FAILED:", response.status, text);
                    throw new Error(`Update failed: ${response.status}`);
                }

                // reload stránky po úspěchu
                location.reload();

            } catch (e) {
                console.error(e);
                alert("Nepodařilo se upravit konfiguraci.");
            }
        });
    }

    document.querySelectorAll(".delete-config-btn").forEach(btn => {
        btn.addEventListener("click", async () => {

            const id = btn.dataset.configurationId;
            if (!id) return;

            if (!confirm("Opravdu chceš smazat tuto konfiguraci?")) return;

            try {
                const response = await fetch(`/configuration/${id}`, {
                    method: "DELETE",
                    headers: {
                        ...csrfHeaders()
                    }
                });

                if (!response.ok) {
                    const text = await response.text().catch(() => "");
                    console.error("DELETE CONFIG FAILED:", response.status, text);
                    throw new Error(`Delete failed: ${response.status}`);
                }

                location.reload();

            } catch (e) {
                console.error(e);
                alert("Nepodařilo se smazat konfiguraci.");
            }
        });
    });

});

function csrfHeaders() {
    const form = document.getElementById("csrfForm");
    if (!form) return {};

    const tokenInput = form.querySelector('input[type="hidden"][name]');
    const headerName = document.getElementById("csrfHeaderName")?.value;

    const token = tokenInput?.value;
    if (!headerName || !token) return {};

    return { [headerName]: token };
}
