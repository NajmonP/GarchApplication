(function () {
    "use strict";

    const API_URL = "/configuration/data";
    const DOWNLOAD_URL = "/configuration/download";
    const DELETE_URL = "/configuration"
    const DELETE_MODEL_URL = "/model"
    const cardsEl = document.getElementById("configurationCards");
    const searchEl = document.getElementById("searchInput");
    const paginationEl = document.getElementById("pagination");

    const isAuthenticated = (document.body.dataset.authenticated === "true");

    let allItems = [];
    let currentPage = 0;
    const pageSize = 10;

    function renderActions(item) {
        const wrap = AppElManager.createEl("div", "d-flex gap-2 mt-3");

        const showModelsBtn = AppElManager.createEl("button", "btn btn-outline-secondary btn-sm js-show-models-btn", "Zobrazit modely");
        showModelsBtn.type = "button";
        showModelsBtn.dataset.configurationId = String(item.id);
        wrap.appendChild(showModelsBtn);

        if (!isAuthenticated) return wrap;

        const downloadBtn = AppElManager.createEl("button", "btn btn-outline-success btn-sm js-download-config-btn", "Stáhnout");
        downloadBtn.type = "button";
        downloadBtn.dataset.downloadUrl = DOWNLOAD_URL;
        downloadBtn.dataset.downloadId = String(item.id);
        downloadBtn.dataset.filename = String(item.name);
        wrap.appendChild(downloadBtn);

        const editBtn = AppElManager.createEl("button", "btn btn-outline-primary btn-sm js-edit-config-btn", "Upravit");
        editBtn.type = "button";
        editBtn.setAttribute("data-bs-toggle", "modal");
        editBtn.setAttribute("data-bs-target", "#editConfigurationModal");
        editBtn.dataset.id = String(item.id);
        editBtn.dataset.name = item.name ?? "";
        wrap.appendChild(editBtn);

        const deleteBtn = AppElManager.createEl("button", "btn btn-outline-danger btn-sm js-delete-config-btn", "Smazat");
        deleteBtn.type = "button";
        deleteBtn.dataset.deleteUrl = DELETE_URL;
        deleteBtn.dataset.deleteId = String(item.id);
        wrap.appendChild(deleteBtn);

        return wrap;
    }


    function renderCard(item) {
        const col = AppElManager.createEl("div", "col-12 col-md-6");
        col.dataset.name = (item.name ?? "").toLowerCase();

        const card = AppElManager.createEl("div", "card shadow-sm h-100");

        const body = AppElManager.createEl("div", "card-body");
        body.appendChild(AppElManager.createInfoRow("Id:", item.id ?? "-"));
        body.appendChild(AppElManager.createInfoRow("Název:", item.name ?? "-"));
        body.appendChild(AppElManager.createInfoRow("Vytvořeno:", AppFormatter.formatInstant(item.created)));

        const footer = AppElManager.createEl(
            "div",
            "card-footer d-flex justify-content-between align-items-center flex-wrap gap-2"
        );

        footer.appendChild(renderActions(item));

        const modelsWrap = AppElManager.createEl("div", "px-3 pb-3");
        const modelsContainer = AppElManager.createEl("div", "models-container d-none");
        modelsWrap.appendChild(modelsContainer);

        card.appendChild(body);
        card.appendChild(footer);
        card.appendChild(modelsWrap);

        col.appendChild(card);
        return col;
    }

    function renderList(list) {
        AppElManager.clear(cardsEl);

        if (!list || list.length === 0) {
            const empty = AppElManager.createEl("div", "text-muted", "Žádné konfigurace nebyly nalezeny.");
            cardsEl.appendChild(empty);
            return;
        }

        const frag = document.createDocumentFragment();
        list.forEach(item => frag.appendChild(renderCard(item)));
        cardsEl.appendChild(frag);
    }

    async function load() {

        const res = await AppHttp.apiFetch(API_URL, { method: "GET" });
        if (!res) return;

        const data = await res.json();

        allItems = Array.isArray(data) ? data : (data?.content ?? []);
        renderList(allItems);
    }

    function initEditModal() {
        const modalEl = document.getElementById("editConfigurationModal");
        if (!modalEl) return;

        let currentId = null;

        modalEl.addEventListener("show.bs.modal", (event) => {
            const button = event.relatedTarget;
            if (!button) return;

            currentId = button.getAttribute("data-id");
            const name = button.getAttribute("data-name");

            const nameInput = document.getElementById("editConfigName");
            if (nameInput) nameInput.value = name ?? "";
        });

        const form = document.getElementById("editConfigurationForm");
        if (!form) return;

        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            if (!currentId) return;

            const nameInput = document.getElementById("editConfigName");
            const name = nameInput?.value?.trim() ?? "";

            const res = await AppHttp.apiFetch(`/configuration/${currentId}`, {
                method: "PUT",
                json: { name }
            });

            if (!res) return;

            AppHttp.reload();
        });
    }

    // =======================
    // Listing models inside configuration cards
    // =======================

    function initModelListingForConfigCards() {
        const buttons = document.querySelectorAll(".js-show-models-btn");
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
        AppElManager.clear(container);

        toggleBtn.disabled = true;
        const originalText = toggleBtn.textContent;
        toggleBtn.textContent = "Načítám...";

            const response = await AppHttp.apiFetch(`/configuration/${configurationId}`, { method: "GET" });
            if (!response) {
                toggleBtn.textContent = originalText;
                toggleBtn.disabled = false;
                return;
                }

            const models = await response.json();

            if (!models || models.length === 0) {
                container.appendChild(createParagraph("Žádné modely pro tuto konfiguraci.", "text-muted mt-2 mb-0"));
            } else {
                const title = AppElManager.createEl("div");
                title.classList.add("fw-semibold", "mt-2");
                title.textContent = "Modely:";
                container.appendChild(title);

                const list = AppElManager.createEl("ul");
                list.classList.add("list-group", "mt-2");

                models.forEach(model => {
                    const li = buildModelListItem(template, model, configurationId);
                    list.appendChild(li);
                });

                container.appendChild(list);
            }

            container.dataset.loaded = "true";
            toggleBtn.textContent = "Skrýt modely";
            toggleBtn.disabled = false;
    }

    function buildModelListItem(template, model, configurationId) {
        const clone = template.content.cloneNode(true);
        const li = clone.querySelector("li");

        li.querySelector(".js-model-name").textContent = model.name ?? "Model";
        li.querySelector(".js-model-id").textContent = model.id ?? "—";
        li.querySelector(".js-start-variance").textContent = model.startVariance ?? "—";
        li.querySelector(".js-constant-variance").textContent = model.constantVariance ?? "—";
        li.querySelector(".js-last-variances").textContent = model.lastVariances;
        li.querySelector(".js-last-shocks").textContent = model.lastShocks;

        li.dataset.modelId = model.id;
        li.dataset.configurationId = configurationId;
        li.dataset.modelName = model.name ?? "";
        li.dataset.startVariance = model.startVariance ?? "";
        li.dataset.constantVariance = model.constantVariance ?? "";
        li.dataset.lastVariances = Array.isArray(model.lastVariances) ? model.lastVariances.join(",") : "";
        li.dataset.lastShocks = Array.isArray(model.lastShocks) ? model.lastShocks.join(",") : "";

        const editBtn = li.querySelector(".js-edit-model-btn");
        const deleteBtn = li.querySelector(".js-delete-model-btn");
        deleteBtn.dataset.deleteUrl = DELETE_MODEL_URL;
        deleteBtn.dataset.deleteId = model.id;

        if (editBtn) editBtn.addEventListener("click", () => openEditModelModal(li));

        return li;
    }

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

    function initEditGarchModelModal(){
        const saveBtn = document.getElementById("btnSaveModelChanges");
        if (!saveBtn) return;

        saveBtn.addEventListener("click", async () => {
            const id = document.getElementById("editModelId")?.value?.trim();
            const name = document.getElementById("editModelName")?.value ?? "";

            const startVariance = readNumber("editStartVariance");
            const constantVariance = readNumber("editConstantVariance");

            const lastVariancesStr = document.getElementById("editLastVariances")?.value ?? "";
            const lastShocksStr = document.getElementById("editLastShocks")?.value ?? "";

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
                const response = await AppHttp.apiFetch(`/model/${id}`, {
                    method: "PUT",
                    json: payload
                });

                if (!response) return;

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

    async function refreshModels(configurationId) {
        const btn = document.querySelector(`.js-show-models-btn[data-configuration-id="${configurationId}"]`);
        if (!btn) return;

        const card = btn.closest(".card");
        const container = card ? card.querySelector(".models-container") : null;
        const template = document.getElementById("tpl-model-item");
        if (!container || !template) return;

        container.dataset.loaded = "false";
        await loadModelsIntoContainer(configurationId, container, btn, template);
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

    function createParagraph(text, className = "") {
        const p = document.createElement("p");
        if (className) p.className = className;
        p.textContent = text;
        return p;
    }

    document.addEventListener("DOMContentLoaded", async () => {
        initEditModal();
        initEditGarchModelModal()
        window.InitButtons?.initUploadForm?.("#uploadForm");
        await load();

        window.InitButtons?.initDeleteButtons?.(".js-delete-config-btn");
        window.InitButtons?.initDeleteButtons?.(".js-delete-model-btn");
        window.InitButtons?.initDownloadButtons?.(".js-download-config-btn");
        initModelListingForConfigCards();

        if (searchEl && window.AppSearch?.filterByDatasetContains) {
            window.AppSearch.filterByDatasetContains({
                input: searchEl,
                itemsSelector: "#configurationCards [data-name]",
                itemDatasetKey: "name",
                emptyDisplay: "" ,
                hiddenDisplay: "none"
            });

        }
    });
})();