let chart;

document.addEventListener("DOMContentLoaded", () => {
    bindChartForm("form.form-manual", "/start-calculation-manual");
    bindChartForm("form.form-config", "/start-calculation-configuration");
});

function bindChartForm(formSelector, url) {
    const form = document.querySelector(formSelector);
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const formData = new FormData(form);

        const res = await fetch(url, {
            method: "POST",
            body: formData
        });

        if (!res.ok) {
            const msg = await res.text();
            showErrorModal(msg);
            return;
        }

        document.querySelector(".form-manual")?.reset();
        document.querySelector(".form-config")?.reset();

        let data;
        try {
            data = await res.json();
        } catch {
            showErrorModal("Server vrátil neočekávanou odpověď.");
            return;
        }


        document.getElementById("resultSection").classList.remove("d-none");

        const canvas = document.getElementById("resultChart");

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
    });
}