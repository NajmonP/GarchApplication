(function () {
    "use strict";

    const API_URL = "/calculation/data";

    const cardsEl = document.getElementById("calculationCards");

    const isAuthenticated = (document.body.dataset.authenticated === "true");

    let allItems = [];

    function norm(s) {
        return String(s ?? "").toLowerCase().trim();
    }

    function renderActions(item) {
        const wrap = AppElManager.createEl("div", "d-flex justify-content-between align-items-center mt-3");

        const detail = AppElManager.createEl("a", "btn btn-outline-secondary btn-sm", "Zobrazit detaily");
        detail.href = `/calculation/${item.id}`;
        wrap.appendChild(detail);

        if (!isAuthenticated) return wrap;

        const delBtn = AppElManager.createEl("button", "btn btn-outline-danger btn-sm js-delete", "Smazat");
        delBtn.type = "button";
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

        body.appendChild(AppElManager.createInfoRow("Id:", item.id));
        body.appendChild(AppElManager.createInfoRow("Status:", item.status));
        body.appendChild(AppElManager.createInfoRow("Spustil:", item?.user?.username ?? item?.username ?? ""));
        body.appendChild(AppElManager.createInfoRow("Datum:", AppFormatter.formatInstant(item.runAt)));

        const inputLabel = inputName
            ? `${inputName} (${item.inputTimeSeriesId ?? ""})`
            : " - ";
        body.appendChild(AppElManager.createInfoRow("Vstupní časová řada:", inputLabel));

        const outputLabel = outputName
            ? `${outputName} (${item.resultTimeSeriesId ?? ""})`
            : " - ";
        body.appendChild(AppElManager.createInfoRow("Výstupní časová řada:", outputLabel));

        body.appendChild(renderActions(item));

        card.appendChild(body);
        col.appendChild(card);
        return col;
    }

    function renderList(list) {
        AppElManager.clear(cardsEl);

        if (!list || list.length === 0) {
            const empty = AppElManager.createEl("div", "text-muted", "Žádné výpočty nebyly nalezeny.");
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

    document.addEventListener("DOMContentLoaded", async () => {
        window.InitButtons?.initDeleteButtons?.(".js-delete");


        window.AppSearch.filterByMultipleDatasetContains({
            inputs: [
                { el: document.getElementById("inputTsSearch"), key: "input" },
                { el: document.getElementById("outputTsSearch"), key: "output" }
            ],
            itemsSelector: ".calculation-card",
            emptyDisplay: "",
            hiddenDisplay: "none"
        });

        await load();
    });

})();