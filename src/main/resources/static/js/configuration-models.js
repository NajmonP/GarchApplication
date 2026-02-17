document.addEventListener("DOMContentLoaded", function () {
    initModelPickerForCalculation();
    initModelListingForConfigCards();
});

// =======================
// Helpers
// =======================

function clearElement(el) {
    while (el.firstChild) {
        el.removeChild(el.firstChild);
    }
}

function formatWeights(arr, digits = 4) {
    if (!Array.isArray(arr) || arr.length === 0) return "—";
    return arr
        .map(x => (typeof x === "number" ? x.toFixed(digits) : String(x)))
        .join(", ");
}

function createParagraph(text, className = "") {
    const p = document.createElement("p");
    if (className) p.className = className;
    p.textContent = text;
    return p;
}

// =======================
// Model picker (radio list)
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
                modelList.appendChild(
                    createParagraph("Žádné modely pro tuto konfiguraci.", "text-muted mt-2")
                );
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

                const label = document.createElement("label");
                label.setAttribute("for", input.id);
                label.classList.add("form-check-label");
                label.textContent = model.name ?? "Model";

                wrapper.appendChild(input);
                wrapper.appendChild(label);
                modelList.appendChild(wrapper);
            });

        } catch (error) {
            console.error(error);
            modelList.appendChild(
                createParagraph("Nepodařilo se načíst modely.", "text-danger mt-2")
            );
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

            container.classList.remove("d-none");
            clearElement(container);

            this.disabled = true;
            const originalText = this.textContent;
            this.textContent = "Načítám...";

            try {
                const response = await fetch(`/configuration/${configurationId}`);
                if (!response.ok) throw new Error("Chyba při načítání modelů.");

                const models = await response.json();

                if (!models || models.length === 0) {
                    container.appendChild(
                        createParagraph("Žádné modely pro tuto konfiguraci.", "text-muted mt-2 mb-0")
                    );
                } else {

                    const title = document.createElement("div");
                    title.classList.add("fw-semibold", "mt-2");
                    title.textContent = "Modely:";
                    container.appendChild(title);

                    const list = document.createElement("ul");
                    list.classList.add("list-group", "mt-2");

                    models.forEach(model => {
                        const clone = template.content.cloneNode(true);
                        const li = clone.querySelector("li");

                        // vyplnění template přes textContent
                        const nameEl = li.querySelector(".js-model-name");
                        const idEl = li.querySelector(".js-model-id");
                        const startEl = li.querySelector(".js-start-variance");
                        const constEl = li.querySelector(".js-constant-variance");
                        const varEl = li.querySelector(".js-last-variances");
                        const shockEl = li.querySelector(".js-last-shocks");

                        if (nameEl) nameEl.textContent = model.name ?? "Model";
                        if (idEl) idEl.textContent = model.id ?? "—";
                        if (startEl) startEl.textContent = model.startVariance ?? "—";
                        if (constEl) constEl.textContent = model.constantVariance ?? "—";
                        if (varEl) varEl.textContent = formatWeights(model.lastVariances);
                        if (shockEl) shockEl.textContent = formatWeights(model.lastShocks);

                        list.appendChild(clone);
                    });

                    container.appendChild(list);
                }

                container.dataset.loaded = "true";
                this.textContent = "Skrýt modely";

            } catch (e) {
                console.error(e);
                container.appendChild(
                    createParagraph("Nepodařilo se načíst modely.", "text-danger mt-2 mb-0")
                );
                this.textContent = originalText;
            } finally {
                this.disabled = false;
            }
        });
    });
}
