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

        const rows = [
            AppElManager.createInfoRow("Název:", name),
            AppElManager.createInfoRow("Pozorování:", AppFormatter.formatValue(data?.observations)),
            AppElManager.createInfoRow("Průměr (mean):", AppFormatter.formatValue(data?.mean)),
            AppElManager.createInfoRow("Šikmost (skewness):", AppFormatter.formatValue(data?.skewness)),
            AppElManager.createInfoRow("Špičatost (kurtosis):", AppFormatter.formatValue(data?.kurtosis)),
            AppElManager.createInfoRow("Minimum:", AppFormatter.formatValue(data?.min)),
            AppElManager.createInfoRow("Maximum:", AppFormatter.formatValue(data?.max))
        ];

        const frag = document.createDocumentFragment();
        rows.forEach(r => r && frag.appendChild(r));
        statsBox.appendChild(frag);
    }

    window.AppElManager = { clear, createEl, createInfoRow, renderTimeSeriesStats };
})();