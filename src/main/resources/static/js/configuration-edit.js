document.addEventListener("DOMContentLoaded", () => {
    // naplnění modal fields při kliknutí na edit tlačítko
    document.querySelectorAll(".edit-config-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.dataset.configurationId;
            const name = btn.dataset.configurationName ?? "";

            document.getElementById("editConfigId").value = id ?? "";
            document.getElementById("editConfigName").value = name;
        });
    });

    // uložit změny
    const saveBtn = document.getElementById("btnSaveConfigChanges");
    if (saveBtn) {
        saveBtn.addEventListener("click", async () => {
            const id = document.getElementById("editConfigId")?.value?.trim();
            const name = document.getElementById("editConfigName")?.value?.trim();

            if (!id || !name) {
                alert("Název nesmí být prázdný.");
                return;
            }

            const res = await AppHttp.apiFetch(`/configuration/${id}`, {
                method: "PUT",
                json: { name }
            });

            if (!res) return;

            AppHttp.reload();
        });
    }

    // smazat konfiguraci
    document.querySelectorAll(".delete-config-btn").forEach(btn => {
        btn.addEventListener("click", async () => {
            const id = btn.dataset.configurationId;
            if (!id) return;

            if (!confirm("Opravdu chceš smazat tuto konfiguraci?")) return;

            const res = await AppHttp.apiFetch(`/configuration/${id}`, {
                method: "DELETE"
            });

            if (!res) return;

            AppHttp.reload();
        });
    });
});