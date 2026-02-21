let inputChartInstance = null;
let outputChartInstance = null;

document.addEventListener("DOMContentLoaded", () => {
    const pageDataEl = document.getElementById("pageData");
    const calculationId = pageDataEl?.getAttribute("data-calculation-id");

    const errorBox = document.getElementById("errorBox");
    const reloadBtn = document.getElementById("reloadBtn");

    if (!calculationId) {
        showError("Chybí calculationId na stránce (data-calculation-id).");
        return;
    }

    loadDetail(calculationId);

    if (reloadBtn) {
        reloadBtn.addEventListener("click", () => loadDetail(calculationId));
    }

    async function loadDetail(id) {
        hideError();

        try {
            const res = await fetch(`/calculation/detail/${id}`, {
                headers: { Accept: "application/json" }
            });

            if (!res.ok) {
                showError("Nepodařilo se načíst detail kalkulace.");
                destroyCharts();
                return;
            }

            const data = await res.json();


            fillCalculationInfo(data);

            fillTimeSeriesBlock(data?.input, "in");
            fillTimeSeriesBlock(data?.output, "out");

            const inputData = data?.input?.chartOfTimeSeriesDTO;
            const outputData = data?.output?.chartOfTimeSeriesDTO;

            const hasInput = Array.isArray(inputData?.points) && inputData.points.length > 0;
            const hasOutput = Array.isArray(outputData?.points) && outputData.points.length > 0;

            toggleChartPlaceholder("inputChart", "inputChartPlaceholder", hasInput);
            toggleChartPlaceholder("resultChart", "resultChartPlaceholder", hasOutput); // HTML má canvas id="resultChart" pro výstup

            if (hasInput) {
                inputChartInstance = renderChart("inputChart", inputData, inputChartInstance);
            } else if (inputChartInstance) {
                inputChartInstance.destroy();
                inputChartInstance = null;
            }

            if (hasOutput) {
                outputChartInstance = renderChart("resultChart", outputData, outputChartInstance);
            } else if (outputChartInstance) {
                outputChartInstance.destroy();
                outputChartInstance = null;
            }

            const inIdEl = document.getElementById("inputTsId");
            if (inIdEl) inIdEl.textContent = data?.input?.id != null ? `ID: ${data.input.id}` : "-";

            const outIdEl = document.getElementById("resultTsId");
            if (outIdEl) outIdEl.textContent = data?.output?.id != null ? `ID: ${data.output.id}` : "-";

            setDownloadButton("inputDownloadBtn", data?.input?.id);
            setDownloadButton("outputDownloadBtn", data?.output?.id);

        } catch (e) {
            showError("Chyba při načítání: " + (e?.message ?? e));
            destroyCharts();
            toggleChartPlaceholder("inputChart", "inputChartPlaceholder", false);
            toggleChartPlaceholder("resultChart", "resultChartPlaceholder", false);
        }
    }

    function destroyCharts() {
        if (inputChartInstance) {
            inputChartInstance.destroy();
            inputChartInstance = null;
        }
        if (outputChartInstance) {
            outputChartInstance.destroy();
            outputChartInstance = null;
        }
    }

    function fillCalculationInfo(data) {
        setText("calcForecast", data?.forecast ?? "-");
        setText("calcStatus", data?.status ?? "-");

        setText("calcRunAt", data?.runAt ?? "-");

        setText("calcUser", data?.user ?? "-");
    }

    function fillTimeSeriesBlock(ts, prefix) {
        const chartDto = ts?.chartOfTimeSeriesDTO;
        const points = chartDto?.points;

        if (!chartDto || !Array.isArray(points) || points.length === 0) {
            setText(prefix + "Name", ts?.name ?? "-");
            setText(prefix + "Obs", Array.isArray(points) ? points.length : "-");
            setText(prefix + "Mean", formatNum(ts?.mean));
            setText(prefix + "Skew", formatNum(ts?.skewness));
            setText(prefix + "Kurt", formatNum(ts?.kurtosis));
            setText(prefix + "Min", "-");
            setText(prefix + "Max", "-");
            return;
        }

        const ys = points
            .map((p) => Number(p?.y))
            .filter((v) => Number.isFinite(v));

        const min = ys.length ? Math.min(...ys) : null;
        const max = ys.length ? Math.max(...ys) : null;

        setText(prefix + "Name", chartDto?.name ?? ts?.name ?? "-");
        setText(prefix + "Obs", points.length);

        setText(prefix + "Mean", formatNum(ts?.mean));
        setText(prefix + "Skew", formatNum(ts?.skewness));
        setText(prefix + "Kurt", formatNum(ts?.kurtosis));

        setText(prefix + "Min", min == null ? "-" : formatNum(min));
        setText(prefix + "Max", max == null ? "-" : formatNum(max));
    }

    function renderChart(canvasId, data, prevChartInstance) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return prevChartInstance ?? null;

        if (prevChartInstance) prevChartInstance.destroy();

        return new Chart(canvas, {
            type: "line",
            data: {
                datasets: [
                    {
                        label: data?.name ?? "Výsledek",
                        data: data?.points ?? [],
                        borderWidth: 2,
                        pointRadius: 0
                    }
                ]
            },
            options: {
                parsing: false,
                animation: false,
                responsive: true,
                maintainAspectRatio: false,
                scales: { x: { type: "linear" } },
                plugins: { legend: { display: true } }
            }
        });
    }

    function toggleChartPlaceholder(canvasId, placeholderId, hasData) {
        const canvas = document.getElementById(canvasId);
        const ph = document.getElementById(placeholderId);

        if (!canvas) return;

        if (ph) {
            if (hasData) {
                canvas.classList.remove("d-none");
                ph.classList.add("d-none");
            } else {
                canvas.classList.add("d-none");
                ph.classList.remove("d-none");
            }
        } else {
            if (hasData) canvas.classList.remove("d-none");
            else canvas.classList.add("d-none");
        }
    }

    function setDownloadButton(buttonId, timeSeriesId) {
        const btn = document.getElementById(buttonId);
        if (!btn) return;

        if (timeSeriesId != null) {
            btn.href = `/time-series/download/${timeSeriesId}`;
            btn.classList.remove("d-none");
        } else {
            btn.classList.add("d-none");
            btn.removeAttribute("href");
        }
    }

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) el.textContent = value;
    }

    function formatNum(v) {
        const n = Number(v);
        if (!Number.isFinite(n)) return "-";
        return n.toFixed(6);
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