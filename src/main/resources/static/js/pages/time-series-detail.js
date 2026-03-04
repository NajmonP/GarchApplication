(function () {
    "use strict";

    const SELECTORS = {
        pageData: "#pageData",
        reloadBtn: "#reloadBtn",
        statsBox: "#statsBox"
    };

    const CHART_CANVAS_ID = "timeSeriesChart";

    async function load(timeSeriesId) {

        try {
            const url = `/time-series/detail/${encodeURIComponent(timeSeriesId)}`;

            const res = await AppHttp.apiFetch(url, { method: "GET" });
            if (!res) return;

            const data = await res.json();

            renderStats(data);

            const chartDto = data?.chartOfTimeSeriesDTO;
            if (!chartDto) {
                AppModal.showError("V odpovědi chybí chartOfTimeSeriesDTO.");
                return;
            }

            if (typeof AppChart.renderChart !== "function") {
                AppModal.showError("Chybí renderChart() (nenačtený /js/app/chart.js?).");
                return;
            }

            AppChart.renderChart(CHART_CANVAS_ID, chartDto);

        } catch (e) {
            AppModal.showError("Chyba při načítání detailu.");
        }
    }

    function renderStats(data) {
        const statsBox = document.querySelector(SELECTORS.statsBox);
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

    document.addEventListener("DOMContentLoaded", () => {
        const pageDataEl = document.querySelector(SELECTORS.pageData);
        const timeSeriesId = pageDataEl?.dataset?.timeSeriesId;

        const reloadBtn = document.querySelector(SELECTORS.reloadBtn);
        if (reloadBtn) {
            reloadBtn.addEventListener("click", () => load(timeSeriesId));
        }

        load(timeSeriesId);
    });

})();