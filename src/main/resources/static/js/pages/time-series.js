(function () {
    "use strict";

    const API_URL = "/time-series/data";
    const DOWNLOAD_URL = "/time-series/download";
    const DELETE_URL = "/time-series";
    const cardsEl = document.getElementById("timeSeriesCards");
    const searchEl = document.getElementById("searchInput");
    const uploadForm = document.getElementById("uploadForm");

    const isAuthenticated = (document.body.dataset.authenticated === "true");

    let allItems = [];

    function setUploadVisibility() {
        if (!uploadForm) return;
        uploadForm.style.display = isAuthenticated ? "" : "none";
    }

    function renderActions(item) {
        const wrap = AppElManager.createEl("div", "d-flex gap-2 mt-3");

        // Detail (link)
        const detail = AppElManager.createEl("a", "btn btn-outline-secondary btn-sm", "Zobrazit detaily");
        detail.href = `/time-series/${item.id}`;
        wrap.appendChild(detail);

        if (!isAuthenticated) return wrap;

        // Download
        const downloadBtn = AppElManager.createEl("button", "btn btn-outline-success btn-sm js-download", "Stáhnout");
        downloadBtn.type = "button";
        downloadBtn.dataset.downloadUrl = DOWNLOAD_URL;
        downloadBtn.dataset.downloadId = String(item.id);
        downloadBtn.dataset.filename = String(item.name);
        wrap.appendChild(downloadBtn);

        // Edit (modal)
        const editBtn = AppElManager.createEl("button", "btn btn-outline-primary btn-sm", "Upravit");
        editBtn.type = "button";
        editBtn.setAttribute("data-bs-toggle", "modal");
        editBtn.setAttribute("data-bs-target", "#editTimeSeriesModal");
        editBtn.dataset.id = String(item.id);
        editBtn.dataset.name = item.name ?? "";
        wrap.appendChild(editBtn);


        const delBtn = AppElManager.createEl("button", "btn btn-outline-danger btn-sm js-delete", "Smazat");
        delBtn.type = "button";
        delBtn.dataset.deleteUrl = DELETE_URL;
        delBtn.dataset.deleteId = String(item.id);

        wrap.appendChild(delBtn);

        return wrap;
    }

    function renderCard(item) {
        const col = AppElManager.createEl("div", "col-12 col-md-6");
        col.dataset.name = (item.name ?? "").toLowerCase();

        const card = AppElManager.createEl("div", "card shadow-sm h-100");
        const body = AppElManager.createEl("div", "card-body");

        body.appendChild(AppElManager.createInfoRow("Id:", item.id));
        body.appendChild(AppElManager.createInfoRow("Název:", item.name));
        body.appendChild(AppElManager.createInfoRow("Vytvořeno:", AppFormatter.formatInstant(item.created)));
        body.appendChild(AppElManager.createInfoRow("Vytvořil:", item.username));
        body.appendChild(AppElManager.createInfoRow("Viditelnost:", item.visibility));

        body.appendChild(renderActions(item));

        card.appendChild(body);
        col.appendChild(card);
        return col;
    }

    function renderList(list) {
        AppElManager.clear(cardsEl);

        if (!list || list.length === 0) {
            const empty = AppElManager.createEl("div", "text-muted", "Žádné časové řady nebyly nalezeny.");
            cardsEl.appendChild(empty);
            return;
        }

        const frag = document.createDocumentFragment();
        list.forEach(item => frag.appendChild(renderCard(item)));
        cardsEl.appendChild(frag);
    }

    async function load() {
        setUploadVisibility();

        const res = await AppHttp.apiFetch(API_URL, { method: "GET" });
        if (!res) return;

        const data = await res.json();

        allItems = Array.isArray(data) ? data : (data?.content ?? []);
        renderList(allItems);
    }

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
            if (nameInput) nameInput.value = name ?? "";
        });

        const form = document.getElementById("editTimeSeriesForm");
        if (!form) return;

        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            if (!currentId) return;

            const name = document.getElementById("timeSeriesName")?.value?.trim();
            if (!name) {
                alert("Název nesmí být prázdný.");
                return;
            }

            const res = await AppHttp.apiFetch(`/time-series/${currentId}`, {
                method: "PUT",
                json: { name }
            });

            if (!res) return;

            AppHttp.reload();
        });
    }

    document.addEventListener("DOMContentLoaded", async () => {
        initEditModal();
        window.InitButtons?.initDeleteButtons?.(".js-delete");
        window.InitButtons?.initDownloadButtons?.(".js-download");
        window.InitButtons?.initUploadForm?.("#uploadForm");

        if (searchEl && window.AppSearch?.filterByDatasetContains) {
            window.AppSearch.filterByDatasetContains({
                input: searchEl,
                itemsSelector: "#timeSeriesCards [data-name]",
                itemDatasetKey: "name",
                emptyDisplay: "" ,
                hiddenDisplay: "none"
            });
        }

        await load();
    });

})();