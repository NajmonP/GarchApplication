document.addEventListener("DOMContentLoaded", function () {
    const configSelect = document.getElementById("configurationId");
    const modelList = document.getElementById("modelList");

    if (!configSelect || !modelList) return;

    configSelect.addEventListener("change", function () {
        const configurationId = this.value;
        modelList.innerHTML = "";

        if (!configurationId) return;

        fetch(`/configuration/${configurationId}`)
            .then(response => {
                if (!response.ok) throw new Error("Chyba při načítání modelů.");
                return response.json();
            })
            .then(models => {
                if (!models || models.length === 0) {
                    modelList.innerHTML = `<p class="text-muted mt-2">Žádné modely pro tuto konfiguraci.</p>`;
                    return;
                }

                const label = document.createElement("label");
                label.textContent = "Vyber model:";
                label.classList.add("form-label", "mt-3");
                modelList.appendChild(label);

                models.forEach(model => {
                    const div = document.createElement("div");
                    div.classList.add("form-check");

                    div.innerHTML = `
                        <input class="form-check-input" type="radio" 
                               name="modelId" id="model-${model.id}" value="${model.id}" required>
                        <label class="form-check-label" for="model-${model.id}">
                            ${model.modelName || "Model"} (ID: ${model.id})
                        </label>
                    `;

                    modelList.appendChild(div);
                });
            })
            .catch(error => {
                console.error(error);
                modelList.innerHTML = `<p class="text-danger mt-2">Nepodařilo se načíst modely.</p>`;
            });
    });
});
