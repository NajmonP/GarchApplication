(function () {
    "use strict";

    const SELECTORS = {
        pageData: "#pageData",
        reloadBtn: "#reloadBtn",
        rerunBtn: "#rerunBtn",
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

    function canRerunCalculation(status) {
        return status === "MISSING_OUTPUT_SERIES";
    }

    function updateRerunButton(data) {
        const rerunBtn = document.querySelector(SELECTORS.rerunBtn);
        if (!rerunBtn) return;

        const visible = canRerunCalculation(data?.status);

        rerunBtn.classList.toggle("d-none", !visible);

        if (visible) {
            rerunBtn.dataset.calculationId = data?.id ?? "";
            rerunBtn.dataset.timeSeriesId = data?.input?.id ?? "";
        } else {
            delete rerunBtn.dataset.calculationId;
            delete rerunBtn.dataset.timeSeriesId;
        }
    }

    async function load(calculationId) {

        try {
            const url = `/calculation/detail/${encodeURIComponent(calculationId)}`;

            const res = await AppHttp.apiFetch(url, { method: "GET" });
            if (!res) return;

            const data = await res.json();

            // STATISTIKY / BLOKY
            renderCalculationStats(data)
            updateRerunButton(data);
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
            AppElManager.createInfoRow("konstantní člen:", AppFormatter.formatValue(data?.constantVariance)),
            AppElManager.createInfoRow("Alfa koeficienty:", data?.lastVariances),
            AppElManager.createInfoRow("Beta koeficienty:", data?.lastShocks)
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

    async function rerunCalculation(calculationId, timeSeriesId) {
        try {
            const res = await AppHttp.apiFetch(`/calculation/${calculationId}/rerun`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(timeSeriesId)
            });

            if (!res) return;

            await load(calculationId);

        } catch (e) {
            AppModal?.showError?.("Nepodařilo se spustit kalkulaci znovu.");
        }
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

        const rerunBtn = document.querySelector(SELECTORS.rerunBtn);
        if (rerunBtn) {
            rerunBtn.addEventListener("click", () => {

                const calculationId = rerunBtn.dataset.calculationId;
                const timeSeriesId = rerunBtn.dataset.timeSeriesId;

                rerunCalculation(calculationId, timeSeriesId);
            });
        }

        load(calculationId);
    });

})();