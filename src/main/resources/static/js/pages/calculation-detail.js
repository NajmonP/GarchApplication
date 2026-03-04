(function () {
    "use strict";

    const SELECTORS = {
        pageData: "#pageData",
        reloadBtn: "#reloadBtn",
        calcStatsBox: "#calcStatsBox",
        inputStatsBox: "#inputStatsBox",
        outputStatsBox: "#outputStatsBox"
    };

    const CHART = {
        input: { canvasId: "inputChart", placeholderId: "inputChartPlaceholder" },
        output: { canvasId: "resultChart", placeholderId: "resultChartPlaceholder" }
    };

    function getCalculationId() {
        const pageDataEl = document.querySelector(SELECTORS.pageData);
        return pageDataEl?.dataset?.calculationId || pageDataEl?.getAttribute("data-calculation-id") || null;
    }

    async function load(calculationId) {

        try {
            const url = `/calculation/detail/${encodeURIComponent(calculationId)}`;

            const res = await AppHttp.apiFetch(url, { method: "GET" });
            if (!res) return;

            const data = await res.json();

            // STATISTIKY / BLOKY
            renderCalculationStats(data)
            AppElManager.renderTimeSeriesStats(data?.input, SELECTORS.inputStatsBox);
            AppElManager.renderTimeSeriesStats(data?.output, SELECTORS.outputStatsBox);


            setDownloadBtn("inputDownloadBtn", {
                id: data?.input?.id,
                name: data?.input?.chartOfTimeSeriesDTO?.name
            });

            setDownloadBtn("outputDownloadBtn", {
                id: data?.output?.id,
                name: data?.output?.chartOfTimeSeriesDTO?.name
            });

            const inputChartDto = data?.input?.chartOfTimeSeriesDTO;
            const outputChartDto = data?.output?.chartOfTimeSeriesDTO;

            AppChart.renderChart(CHART.input.canvasId, inputChartDto, "input");
            AppChart.renderChart(CHART.output.canvasId, outputChartDto, "output");

        } catch (e) {
            AppModal?.showError?.("Chyba při načítání detailu kalkulace.");
        }
    }

    function renderCalculationStats(data){
        const calcStatsBox = document.querySelector(SELECTORS.calcStatsBox);
        if (!calcStatsBox || !window.AppElManager) return;

        AppElManager.clear(calcStatsBox);

        const rows = [
            AppElManager.createInfoRow("Status:", data?.status),
            AppElManager.createInfoRow("Předpověď:", AppFormatter.formatValue(data?.forecast)),
            AppElManager.createInfoRow("Počáteční rozptyl:", AppFormatter.formatValue(data?.startVariance)),
            AppElManager.createInfoRow("Konstantní rozptyl:", AppFormatter.formatValue(data?.constantVariance)),
            AppElManager.createInfoRow("Váhy minulých rozptylů:", data?.lastVariances),
            AppElManager.createInfoRow("Váhy minulých šoků:", data?.lastShocks)
        ];

        const frag = document.createDocumentFragment();
        rows.forEach(r => r && frag.appendChild(r));
        calcStatsBox.appendChild(frag);
    }


    function setDownloadBtn(btnId, ts) {
        const btn = document.getElementById(btnId);
        if (!btn) return;

        const id = ts?.id;
        if (!id) {
            btn.classList.add("d-none");
            delete btn.dataset.downloadId;
            return;
        }

        btn.dataset.downloadUrl = "/time-series/download";
        btn.dataset.downloadId = String(id);
        btn.dataset.filename = ts?.name ?? `time-series-${id}.xlsx`;
        btn.classList.remove("d-none");
    }


    document.addEventListener("DOMContentLoaded", () => {
        window.InitButtons?.initDownloadButtons?.(".js-download");

        const calculationId = getCalculationId();
        if (!calculationId) {
            AppModal?.showError?.("Chybí calculationId na stránce (data-calculation-id).");
            return;
        }

        const reloadBtn = document.querySelector(SELECTORS.reloadBtn);
        if (reloadBtn) {
            reloadBtn.addEventListener("click", () => load(calculationId));
        }

        load(calculationId);
    });

})();