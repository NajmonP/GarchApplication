(function () {
    "use strict";

    const API_URL = "/time-series/data";
    const DOWNLOAD_URL = "/time-series/download";
    const DELETE_URL = "/time-series";

    const cardsEl = document.getElementById("timeSeriesCards");
    const secondaryCardsEl = document.getElementById("secondaryTimeSeriesCards");
    const paginationEl = document.getElementById("pagination");
    const searchEl = document.getElementById("searchInput");
    const uploadForm = document.getElementById("uploadForm");

    const isAuthenticated = (document.body.dataset.authenticated === "true");
    const isAdmin = (document.body.dataset.admin === "true");

    const pageSize = 10;

    let myItems = [];
    let publicPage = {
        content: [],
        page: 0,
        size: pageSize,
        totalElements: 0,
        totalPages: 0
    };

    function setUploadVisibility() {
        if (!uploadForm) return;
        uploadForm.style.display = isAuthenticated ? "" : "none";
    }

    function renderActions(item, section) {
        const wrap = AppElManager.createEl("div", "d-flex gap-2 mt-3");

        const detail = AppElManager.createEl("a", "btn btn-outline-secondary btn-sm", "Zobrazit detaily");
        detail.href = `/time-series/${item.id}`;
        wrap.appendChild(detail);

        const downloadBtn = AppElManager.createEl("button", "btn btn-outline-success btn-sm js-download", "Stáhnout");
        downloadBtn.type = "button";
        downloadBtn.dataset.downloadUrl = DOWNLOAD_URL;
        downloadBtn.dataset.downloadId = String(item.id);
        downloadBtn.dataset.filename = String(item.name);
        wrap.appendChild(downloadBtn);

        if (!isAuthenticated) return wrap;

        let canModify = false;

        if (section === "my") {
            canModify = true;
        } else if (section === "public") {
            canModify = isAdmin;
        }

        if (!canModify) return wrap;

        const editBtn = AppElManager.createEl("button", "btn btn-outline-primary btn-sm", "Upravit");
        editBtn.type = "button";
        editBtn.setAttribute("data-bs-toggle", "modal");
        editBtn.setAttribute("data-bs-target", "#editTimeSeriesModal");
        editBtn.dataset.id = String(item.id);
        editBtn.dataset.name = item.name ?? "";
        editBtn.dataset.visibility = item.visibility ?? "Private";
        wrap.appendChild(editBtn);

        const delBtn = AppElManager.createEl("button", "btn btn-outline-danger btn-sm js-delete", "Smazat");
        delBtn.type = "button";
        delBtn.dataset.deleteUrl = DELETE_URL;
        delBtn.dataset.deleteId = String(item.id);
        wrap.appendChild(delBtn);

        return wrap;
    }

    function renderCard(item, section) {
        const col = AppElManager.createEl("div", "col-12 col-md-6");
        col.dataset.name = (item.name ?? "").toLowerCase();

        const card = AppElManager.createEl("div", "card shadow-sm h-100");
        const body = AppElManager.createEl("div", "card-body");

        body.appendChild(AppElManager.createInfoRow("Id:", item.id));
        body.appendChild(AppElManager.createInfoRow("Název:", item.name));
        body.appendChild(AppElManager.createInfoRow("Vytvořeno:", AppFormatter.formatInstant(item.created)));
        body.appendChild(AppElManager.createInfoRow("Vytvořil:", item.username));
        body.appendChild(AppElManager.createInfoRow("Viditelnost:", item.visibility));

        body.appendChild(renderActions(item, section));

        card.appendChild(body);
        col.appendChild(card);
        return col;
    }

    function renderList(list, targetEl, section) {
        if (!targetEl) return;

        AppElManager.clear(targetEl);

        if (!list || list.length === 0) {
            const empty = AppElManager.createEl("div", "text-muted", "Žádné časové řady nebyly nalezeny.");
            targetEl.appendChild(empty);
            return;
        }

        const frag = document.createDocumentFragment();
        list.forEach(item => frag.appendChild(renderCard(item, section)));
        targetEl.appendChild(frag);
    }

    function renderMyList(list = myItems) {
        renderList(list, cardsEl, "my");
    }

    function renderPublicPage(pageData = publicPage) {
        if (!secondaryCardsEl) return;

        const content = Array.isArray(pageData?.content) ? pageData.content : [];
        const pageNumber = Number.isInteger(pageData?.page) ? pageData.page : 0;
        const totalPages = Number.isInteger(pageData?.totalPages) ? pageData.totalPages : 0;

        renderList(content, secondaryCardsEl, "public");

        if (paginationEl) {
            window.AppPagination?.renderPagination?.(
                paginationEl,
                totalPages,
                pageNumber,
                (p) => load(p, searchEl?.value?.trim() ?? "")
            );
        }
    }

    function filterMyItems(term) {
        const normalized = (term ?? "").trim().toLowerCase();

        if (!normalized) {
            renderMyList(myItems);
            return;
        }

        const filtered = myItems.filter(item =>
            (item.name ?? "").toLowerCase().includes(normalized)
        );

        renderMyList(filtered);
    }

    async function load(page = 0, searchTerm = "") {
        setUploadVisibility();

        const params = new URLSearchParams({
            page: String(page),
            size: String(pageSize)
        });

        if (searchTerm) {
            params.set("search", searchTerm);
        }

        const res = await AppHttp.apiFetch(`${API_URL}?${params.toString()}`, { method: "GET" });
        if (!res) return;

        const data = await res.json();

        myItems = Array.isArray(data?.myTimeSeries) ? data.myTimeSeries : [];
        publicPage = data?.publicTimeSeries ?? {
            content: [],
            page: 0,
            size: pageSize,
            totalElements: 0,
            totalPages: 0
        };

        filterMyItems(searchTerm);
        renderPublicPage(publicPage);
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
            const visibility = button.getAttribute("data-visibility");

            const nameInput = document.getElementById("timeSeriesName");
            const visibilityInput = document.getElementById("timeSeriesVisibility");

            if (nameInput) nameInput.value = name ?? "";
            if (visibilityInput) visibilityInput.checked = (visibility === "Public");
        });

        const form = document.getElementById("editTimeSeriesForm");
        if (!form) return;

        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            if (!currentId) return;

            const name = document.getElementById("timeSeriesName")?.value?.trim();
            const visibilityChecked = document.getElementById("timeSeriesVisibility")?.checked;
            const visibility = visibilityChecked ? "Public" : "Private";

            if (!name) {
                alert("Název nesmí být prázdný.");
                return;
            }

            const res = await AppHttp.apiFetch(`/time-series/${currentId}`, {
                method: "PUT",
                json: { name, visibility }
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
                emptyDisplay: "",
                hiddenDisplay: "none"
            });
        }

        await load();
    });

})();