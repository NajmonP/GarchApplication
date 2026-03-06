document.addEventListener("DOMContentLoaded", () => {
    initModelPickerForCalculation();
    initSaveModelChanges();
});

// =======================
// Helpers
// =======================

function clearElement(el) {
    while (el.firstChild) el.removeChild(el.firstChild);
}

function createParagraph(text, className = "") {
    const p = document.createElement("p");
    if (className) p.className = className;
    p.textContent = text;
    return p;
}




// =======================
// Model picker (radio list) - for calculation
// =======================

function initModelPickerForCalculation() {
    const configSelect = document.getElementById("configurationId");
    const modelList = document.getElementById("modelList");
    if (!configSelect || !modelList) return;

    configSelect.addEventListener("change", async function () {
        const configurationId = this.value;
        clearElement(modelList);
        if (!configurationId) return;

        try {
            const response = await fetch(`/configuration/${configurationId}`);
            if (!response.ok) throw new Error("Chyba při načítání modelů.");

            const models = await response.json();

            if (!models || models.length === 0) {
                modelList.appendChild(createParagraph("Žádné modely pro tuto konfiguraci.", "text-muted mt-2"));
                return;
            }

            const label = document.createElement("label");
            label.textContent = "Vyber model:";
            label.classList.add("form-label", "mt-3");
            modelList.appendChild(label);

            models.forEach(model => {
                const wrapper = document.createElement("div");
                wrapper.classList.add("form-check");

                const input = document.createElement("input");
                input.type = "radio";
                input.name = "modelId";
                input.value = model.id;
                input.id = `model-${model.id}`;
                input.required = true;
                input.classList.add("form-check-input");

                const lbl = document.createElement("label");
                lbl.setAttribute("for", input.id);
                lbl.classList.add("form-check-label");
                lbl.textContent = model.name ?? "Model";

                wrapper.appendChild(input);
                wrapper.appendChild(lbl);
                modelList.appendChild(wrapper);
            });

        } catch (e) {
            console.error(e);
            modelList.appendChild(createParagraph("Nepodařilo se načíst modely.", "text-danger mt-2"));
        }
    });
}

// =======================
// Listing models inside configuration cards
// =======================


// =======================
// Edit / Delete model actions (modal + endpoints)
// =======================

async function refreshModels(configurationId) {
    const btn = document.querySelector(`.show-models-btn[data-configuration-id="${configurationId}"]`);
    if (!btn) return;

    const card = btn.closest(".card");
    const container = card ? card.querySelector(".models-container") : null;
    const template = document.getElementById("tpl-model-item");
    if (!container || !template) return;

    container.dataset.loaded = "false";
    await loadModelsIntoContainer(configurationId, container, btn, template);
}

// =======================
// Save button in modal
// =======================

function initSaveModelChanges() {
    const saveBtn = document.getElementById("btnSaveModelChanges");
    if (!saveBtn) return;

    saveBtn.addEventListener("click", async () => {
        const id = document.getElementById("editModelId")?.value?.trim();
        const name = document.getElementById("editModelName")?.value ?? "";

        const startVariance = readNumber("editStartVariance");
        const constantVariance = readNumber("editConstantVariance");

        const lastVariancesStr = document.getElementById("editLastVariances")?.value ?? "";
        const lastShocksStr = document.getElementById("editLastShocks")?.value ?? "";

        console.log("editStartVariance raw:", document.getElementById("editStartVariance")?.value);
        console.log("editConstantVariance raw:", document.getElementById("editConstantVariance")?.value);
        console.log("parsed numbers:", startVariance, constantVariance);

        const errorBox = document.getElementById("editModelError");
        const successBox = document.getElementById("editModelSuccess");
        if (errorBox) { errorBox.classList.add("d-none"); errorBox.textContent = ""; }
        if (successBox) successBox.classList.add("d-none");

        if (!id || !Number.isFinite(startVariance) || !Number.isFinite(constantVariance)) {
            if (errorBox) {
                errorBox.textContent = "Zkontroluj vyplnění: startVariance a constantVariance musí být čísla.";
                errorBox.classList.remove("d-none");
            }
            return;
        }

        const payload = {
            name,
            startVariance,
            constantVariance,
            lastVariances: parseWeights(lastVariancesStr),
            lastShocks: parseWeights(lastShocksStr)
        };

        const li = document.querySelector(`li[data-model-id="${id}"]`);
        const configurationId = li?.dataset.configurationId;

        try {
            const response = await fetch(`/model/${id}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    ...csrfHeaders()
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const text = await response.text().catch(() => "");
                console.error("UPDATE failed:", response.status, response.statusText, text);
                throw new Error(`Update failed: ${response.status}`);
            }

            if (successBox) successBox.classList.remove("d-none");
            if (configurationId) await refreshModels(configurationId);

            const modalEl = document.getElementById("garchModelEditModal");
            const modal = bootstrap.Modal.getInstance(modalEl);
            if (modal) modal.hide();

        } catch (e) {
            console.error(e);
            if (errorBox) {
                errorBox.textContent = "Nepodařilo se uložit změny modelu.";
                errorBox.classList.remove("d-none");
            }
        }
    });
}
