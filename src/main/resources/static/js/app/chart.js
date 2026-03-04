(function () {
    "use strict";

    let chart = null;

    function renderChart(canvasId, data) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;

        if (chart) chart.destroy();

        chart = new Chart(canvas, {
            type: "line",
            data: {
                datasets: [{
                    label: data.name ?? "Výsledek",
                    data: data.points ?? [],
                    borderWidth: 2,
                    pointRadius: 0
                }]
            },
            options: {
                parsing: false,
                animation: false,
                scales: { x: { type: "linear" } }
            }
        });
    }

    window.AppChart = { renderChart };
})();