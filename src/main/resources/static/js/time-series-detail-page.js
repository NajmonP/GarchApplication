document.addEventListener("DOMContentLoaded", () => {
    const pageDataEl = document.getElementById("pageData");
    const timeSeriesId = pageDataEl?.getAttribute("data-time-series-id");

    const errorBox = document.getElementById("errorBox");
    const reloadBtn = document.getElementById("reloadBtn");

    if (!timeSeriesId) {
        showError("Chybí timeSeriesId na stránce (data-time-series-id).");
        return;
    }

    loadDetail(timeSeriesId);

    if (reloadBtn) {
        reloadBtn.addEventListener("click", () => loadDetail(timeSeriesId));
    }

    async function loadDetail(timeSeriesId) {
        hideError();

        try {
            const res = await fetch(`/time-series/detail/${timeSeriesId}`, {
                headers: { "Accept": "application/json" }
            });

            if (!res.ok) {
                showError(`Nepodařilo se načíst detail. HTTP ${res.status}`);
                return;
            }

            const data = await res.json();

            fillStatistics(data);

            const chartDto = data?.chartOfTimeSeriesDTO;
            if (!chartDto) {
                showError("V odpovědi chybí chartOfTimeSeriesDTO.");
                return;
            }

            renderChart("timeSeriesChart", chartDto);

        } catch (e) {
            showError("Chyba při načítání detailu: " + (e?.message ?? e));
        }
    }

    function fillStatistics(data) {
        const nameEl = document.getElementById("tsName");
        const obseEl = document.getElementById("tsObservations");
        const meanEl = document.getElementById("tsMean");
        const skewEl = document.getElementById("tsSkewness");
        const kurtEl = document.getElementById("tsKurtosis");
        const minEl = document.getElementById("tsMin");
        const maxEl = document.getElementById("tsMax");

        const name = data?.chartOfTimeSeriesDTO?.name ?? "-";
        const mean = formatNumber(data?.mean);
        const obse = data?.observations;
        const skew = formatNumber(data?.skewness);
        const kurt = formatNumber(data?.kurtosis);
        const min = formatNumber(data?.min);
        const max = formatNumber(data?.max);

        if (nameEl) nameEl.textContent = name;
        if (obseEl) obseEl.textContent = obse;
        if (meanEl) meanEl.textContent = mean;
        if (skewEl) skewEl.textContent = skew;
        if (kurtEl) kurtEl.textContent = kurt;
        if (minEl) minEl.textContent = min;
        if (maxEl) maxEl.textContent = max;
    }

    function formatNumber(v) {
        if (typeof v !== "number" || Number.isNaN(v)) return "-";
        return v.toFixed(6);
    }

    function showError(msg) {
        if (!errorBox) return;
        errorBox.textContent = msg;
        errorBox.classList.remove("d-none");
    }

    function hideError() {
        if (!errorBox) return;
        errorBox.textContent = "";
        errorBox.classList.add("d-none");
    }
});