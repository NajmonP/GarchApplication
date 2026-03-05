(function () {
    "use strict";

    function clear(el) {
        if (el) el.replaceChildren();
    }

    function createEl(tag, className, text) {
        const el = document.createElement(tag);
        if (className) el.className = className;
        if (text != null) el.textContent = text;
        return el;
    }

    function createInfoRow(label, value) {
        const p = document.createElement("p");
        const strong = document.createElement("strong");
        strong.textContent = label;

        const span = document.createElement("span");
        span.textContent = value ?? "";

        p.appendChild(strong);
        p.append(" ");
        p.appendChild(span);
        return p;
    }

    function renderTimeSeriesStats(data, selector) {
        const statsBox = document.querySelector(selector);
        if (!statsBox || !window.AppElManager) return;

        AppElManager.clear(statsBox);

        const name = data?.chartOfTimeSeriesDTO?.name ?? "-";

        const items = [
            ["Název:", name],
            ["Pozorování:", AppFormatter.formatValue(data?.observations)],
            ["Průměr (mean):", AppFormatter.formatValue(data?.mean)],
            ["Šikmost (skewness):", AppFormatter.formatValue(data?.skewness)],
            ["Špičatost (kurtosis):", AppFormatter.formatValue(data?.kurtosis)],
            ["Minimum:", AppFormatter.formatValue(data?.min)],
            ["Maximum:", AppFormatter.formatValue(data?.max)]
        ];

        const frag = document.createDocumentFragment();

        for (let i = 0; i < items.length; i += 2) {

            const row = AppElManager.createEl("div", "row g-2 mb-1");

            const col1 = AppElManager.createEl("div", "col-6");
            col1.appendChild(AppElManager.createInfoRow(items[i][0], items[i][1]));
            row.appendChild(col1);

            if (items[i + 1]) {
                const col2 = AppElManager.createEl("div", "col-6");
                col2.appendChild(AppElManager.createInfoRow(items[i + 1][0], items[i + 1][1]));
                row.appendChild(col2);
            }

            frag.appendChild(row);
        }

        statsBox.appendChild(frag);
    }

    window.AppElManager = { clear, createEl, createInfoRow, renderTimeSeriesStats };
})();