(function () {
    "use strict";

    const API_URL = "/calculation/data";

    const cardsEl = document.getElementById("calculationCards");
    const secondaryCardsEl = document.getElementById("secondaryCalculationCards");
    const paginationEl = document.getElementById("pagination");

    const inputSearchEl = document.getElementById("inputTsSearch");
    const outputSearchEl = document.getElementById("outputTsSearch");

    const isAuthenticated = (document.body.dataset.authenticated === "true");
    const isAdmin = (document.body.dataset.admin === "true");

    const pageSize = 10;

    let myItems = [];
    let allCalculationsPage = {
        content: [],
        page: 0,
        size: pageSize,
        totalElements: 0,
        totalPages: 0
    };

    function norm(s) {
        return String(s ?? "").toLowerCase().trim();
    }

    function renderActions(item) {
        const wrap = AppElManager.createEl("div", "d-flex justify-content-between align-items-center mt-3");

        const detail = AppElManager.createEl("a", "btn btn-outline-secondary btn-sm", "Zobrazit detaily");
        detail.href = `/calculation/${item.id}`;
        wrap.appendChild(detail);

        if (!isAuthenticated) return wrap;

        const delBtn = AppElManager.createEl("button", "btn btn-outline-danger btn-sm js-delete");
        delBtn.type = "button";
        delBtn.textContent = "Smazat";
        delBtn.dataset.deleteUrl = "/calculation";
        delBtn.dataset.deleteId = String(item.id);

        wrap.appendChild(delBtn);

        return wrap;
    }

    function renderCard(item) {
        const col = AppElManager.createEl("div", "col-12 col-md-6 calculation-card");

        const inputName = item.inputTimeSeriesName ?? "";
        const outputName = item.resultTimeSeriesName ?? "";

        col.dataset.input = norm(inputName);
        col.dataset.output = norm(outputName);

        const card = AppElManager.createEl("div", "card shadow-sm h-100");
        const body = AppElManager.createEl("div", "card-body");

        body.appendChild(AppElManager.createInfoRow("Id:", item.id ?? "-"));
        body.appendChild(AppElManager.createInfoRow("Status:", item.status ?? "-"));
        body.appendChild(AppElManager.createInfoRow("Spustil:", item?.user?.username ?? item?.username ?? "-"));
        body.appendChild(AppElManager.createInfoRow("Datum:", AppFormatter.formatInstant(item.runAt)));

        const inputLabel = inputName
            ? `${inputName} (${item.inputTimeSeriesId ?? ""})`
            : "-";
        body.appendChild(AppElManager.createInfoRow("Vstupní časová řada:", inputLabel));

        const outputLabel = outputName
            ? `${outputName} (${item.resultTimeSeriesId ?? ""})`
            : "-";
        body.appendChild(AppElManager.createInfoRow("Výstupní časová řada:", outputLabel));

        body.appendChild(renderActions(item));

        card.appendChild(body);
        col.appendChild(card);

        return col;
    }

    function renderList(list, targetEl, emptyText = "Žádné výpočty nebyly nalezeny.") {
        if (!targetEl) return;

        AppElManager.clear(targetEl);

        if (!list || list.length === 0) {
            const empty = AppElManager.createEl("div", "text-muted", emptyText);
            targetEl.appendChild(empty);
            return;
        }

        const frag = document.createDocumentFragment();
        list.forEach(item => frag.appendChild(renderCard(item)));
        targetEl.appendChild(frag);
    }

    function renderMyList(list = myItems) {
        renderList(list, cardsEl);
    }

    function renderAllCalculationsPage(pageData = allCalculationsPage) {
        if (!secondaryCardsEl) return;

        const content = Array.isArray(pageData?.content) ? pageData.content : [];
        const pageNumber = Number.isInteger(pageData?.page) ? pageData.page : 0;
        const totalPages = Number.isInteger(pageData?.totalPages) ? pageData.totalPages : 0;

        renderList(content, secondaryCardsEl);

        if (paginationEl) {
            window.AppPagination?.renderPagination?.(
                paginationEl,
                totalPages,
                pageNumber,
                (p) => load(p)
            );
        }
    }

    function filterMyItems() {
        const inputTerm = norm(inputSearchEl?.value);
        const outputTerm = norm(outputSearchEl?.value);

        const filtered = myItems.filter(item => {
            const inputName = norm(item.inputTimeSeriesName);
            const outputName = norm(item.resultTimeSeriesName);

            const matchesInput = !inputTerm || inputName.includes(inputTerm);
            const matchesOutput = !outputTerm || outputName.includes(outputTerm);

            return matchesInput && matchesOutput;
        });

        renderMyList(filtered);
        window.InitButtons?.initDeleteButtons?.(".js-delete");
    }

    function bindDynamicCardButtons() {
        window.InitButtons?.initDeleteButtons?.(".js-delete");
    }

    async function load(page = 0) {
        const params = new URLSearchParams({
            page: String(page),
            size: String(pageSize)
        });

        const res = await AppHttp.apiFetch(`${API_URL}?${params.toString()}`, { method: "GET" });
        if (!res) return;

        const data = await res.json();

        myItems = Array.isArray(data?.myCalculations) ? data.myCalculations : [];
        allCalculationsPage = data?.allCalculations ?? {
            content: [],
            page: 0,
            size: pageSize,
            totalElements: 0,
            totalPages: 0
        };

        filterMyItems();

        if (isAdmin) {
            renderAllCalculationsPage(allCalculationsPage);
        }

        bindDynamicCardButtons();
    }

    function initSearch() {
        if (inputSearchEl) {
            inputSearchEl.addEventListener("input", filterMyItems);
        }

        if (outputSearchEl) {
            outputSearchEl.addEventListener("input", filterMyItems);
        }
    }

    document.addEventListener("DOMContentLoaded", async () => {
        initSearch();
        await load();
    });

})();