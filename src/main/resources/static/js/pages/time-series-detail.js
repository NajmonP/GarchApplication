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

            AppElManager.renderTimeSeriesStats(data, SELECTORS.statsBox);

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