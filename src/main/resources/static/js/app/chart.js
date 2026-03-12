(function () {
    "use strict";

    const instances = {
        input: null,
        output: null
    };

    function renderInput(canvasId, data) {
        instances.input = renderInto(canvasId, data, instances.input);
        return instances.input;
    }

    function renderOutput(canvasId, data) {
        instances.output = renderInto(canvasId, data, instances.output);
        return instances.output;
    }

    function renderInto(canvasId, data, prev) {
        const points = data?.points ?? [];
        const maxX = points.length ? Math.max(...points.map(p => p.x)) : 0;
        const pointRadiusSetup = points.length < 250 ? 3 : 0;

        const canvas = document.getElementById(canvasId);
        if (!canvas) return prev ?? null;

        if (prev) prev.destroy();

        return new Chart(canvas, {
            type: "line",
            data: {
                datasets: [{
                    label: data?.name ?? "Výsledek",
                    data: points,
                    borderWidth: 2,
                    pointRadius: pointRadiusSetup
                }]
            },
            options: {
                parsing: false,
                animation: false,
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        type: "linear",
                        min: 1,
                        max: maxX
                    }
                }
            }
        });
    }

    function renderChart(canvasId, data, which = "input") {
        if (which === "input") return renderInput(canvasId, data);
        return renderOutput(canvasId, data);
    }

    window.AppChart = { renderChart };
})();