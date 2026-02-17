document.addEventListener("DOMContentLoaded", () => {
    initModelPickerForCalculation();
    initModelListingForConfigCards();
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

function formatWeights(arr, digits = 4) {
    if (!Array.isArray(arr) || arr.length === 0) return "—";
    return arr
        .map(x => (typeof x === "number" ? x.toFixed(digits) : String(x)))
        .join(", ");
}

function parseWeights(str) {
    if (!str) return [];
    return str
        .split(",")
        .map(s => s.trim())
        .filter(s => s.length > 0)
        .map(s => Number(s.replace(",", ".")))
        .filter(n => Number.isFinite(n));
}

function readNumber(id) {
    const el = document.getElementById(id);
    if (!el) return NaN;
    const raw = (el.value ?? "").trim().replace(",", ".");
    return raw === "" ? NaN : Number(raw);
}

// CSRF headers for fetch (Spring Security)
function csrfHeaders() {
    const form = document.getElementById("csrfForm");
    if (!form) return {};

    const tokenInput = form.querySelector('input[type="hidden"][name]');
    const headerName = document.getElementById("csrfHeaderName")?.value;

    const token = tokenInput?.value;
    if (!headerName || !token) return {};

    return { [headerName]: token };
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

function initModelListingForConfigCards() {
    const buttons = document.querySelectorAll(".show-models-btn");
    if (!buttons || buttons.length === 0) return;

    const template = document.getElementById("tpl-model-item");
    if (!template) {
        console.error("Chybí <template id='tpl-model-item'> v HTML.");
        return;
    }

    buttons.forEach(btn => {
        btn.addEventListener("click", async function () {
            const configurationId = this.dataset.configurationId;
            if (!configurationId) return;

            const card = this.closest(".card");
            const container = card ? card.querySelector(".models-container") : null;
            if (!container) return;

            if (container.dataset.loaded === "true") {
                const hidden = container.classList.toggle("d-none");
                this.textContent = hidden ? "Zobraz modely" : "Skrýt modely";
                return;
            }

            await loadModelsIntoContainer(configurationId, container, this, template);
        });
    });
}

async function loadModelsIntoContainer(configurationId, container, toggleBtn, template) {
    container.classList.remove("d-none");
    clearElement(container);

    toggleBtn.disabled = true;
    const originalText = toggleBtn.textContent;
    toggleBtn.textContent = "Načítám...";

    try {
        const response = await fetch(`/configuration/${configurationId}`);
        if (!response.ok) throw new Error("Chyba při načítání modelů.");

        const models = await response.json();

        if (!models || models.length === 0) {
            container.appendChild(createParagraph("Žádné modely pro tuto konfiguraci.", "text-muted mt-2 mb-0"));
        } else {
            const title = document.createElement("div");
            title.classList.add("fw-semibold", "mt-2");
            title.textContent = "Modely:";
            container.appendChild(title);

            const list = document.createElement("ul");
            list.classList.add("list-group", "mt-2");

            models.forEach(model => {
                const li = buildModelListItem(template, model, configurationId);
                list.appendChild(li);
            });

            container.appendChild(list);
        }

        container.dataset.loaded = "true";
        toggleBtn.textContent = "Skrýt modely";

    } catch (e) {
        console.error(e);
        container.appendChild(createParagraph("Nepodařilo se načíst modely.", "text-danger mt-2 mb-0"));
        toggleBtn.textContent = originalText;
    } finally {
        toggleBtn.disabled = false;
    }
}

function buildModelListItem(template, model, configurationId) {
    const clone = template.content.cloneNode(true);
    const li = clone.querySelector("li");

    li.querySelector(".js-model-name").textContent = model.name ?? "Model";
    li.querySelector(".js-model-id").textContent = model.id ?? "—";
    li.querySelector(".js-start-variance").textContent = model.startVariance ?? "—";
    li.querySelector(".js-constant-variance").textContent = model.constantVariance ?? "—";
    li.querySelector(".js-last-variances").textContent = formatWeights(model.lastVariances);
    li.querySelector(".js-last-shocks").textContent = formatWeights(model.lastShocks);

    li.dataset.modelId = model.id;
    li.dataset.configurationId = configurationId;
    li.dataset.modelName = model.name ?? "";
    li.dataset.startVariance = model.startVariance ?? "";
    li.dataset.constantVariance = model.constantVariance ?? "";
    li.dataset.lastVariances = Array.isArray(model.lastVariances) ? model.lastVariances.join(",") : "";
    li.dataset.lastShocks = Array.isArray(model.lastShocks) ? model.lastShocks.join(",") : "";

    const editBtn = li.querySelector(".js-edit-model-btn");
    const deleteBtn = li.querySelector(".js-delete-model-btn");

    if (editBtn) editBtn.addEventListener("click", () => openEditModelModal(li));
    if (deleteBtn) deleteBtn.addEventListener("click", () => handleDeleteModel(li));

    return li;
}

// =======================
// Edit / Delete model actions (modal + endpoints)
// =======================

function openEditModelModal(li) {
    document.getElementById("editModelError")?.classList.add("d-none");
    document.getElementById("editModelSuccess")?.classList.add("d-none");

    document.getElementById("editModelId").value = li.dataset.modelId ?? "";
    document.getElementById("editModelName").value = li.dataset.modelName ?? "";
    document.getElementById("editStartVariance").value = li.dataset.startVariance ?? "";
    document.getElementById("editConstantVariance").value = li.dataset.constantVariance ?? "";
    document.getElementById("editLastVariances").value = li.dataset.lastVariances ?? "";
    document.getElementById("editLastShocks").value = li.dataset.lastShocks ?? "";
}

async function handleDeleteModel(li) {
    const modelId = li.dataset.modelId;
    const configurationId = li.dataset.configurationId;
    if (!modelId || !configurationId) return;

    if (!confirm("Opravdu chceš smazat tento model?")) return;

    try {
        const response = await fetch(`/model/${modelId}`, {
            method: "DELETE",
            headers: {
                ...csrfHeaders()
            }
        });

        if (!response.ok) {
            const text = await response.text().catch(() => "");
            console.error("DELETE failed:", response.status, response.statusText, text);
            throw new Error(`Delete failed: ${response.status}`);
        }

        await refreshModels(configurationId);
    } catch (e) {
        console.error(e);
        alert("Nepodařilo se smazat model.");
    }
}

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
