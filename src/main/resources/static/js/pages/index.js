(function () {
    "use strict";

    function fillSelect(selectEl, items, placeholderText) {
        if (!selectEl) return;

        selectEl.replaceChildren();

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.disabled = true;
        placeholder.selected = true;
        placeholder.textContent = placeholderText;
        selectEl.appendChild(placeholder);

        items.forEach(item => {
            const option = document.createElement("option");
            option.value = String(item.id ?? "");
            option.textContent = `${item.name ?? "-"} (id ${item.id ?? "-"})`;
            selectEl.appendChild(option);
        });
    }

    async function loadTimeSeries() {
        const response = await AppHttp.apiFetch("/time-series/select/data");
        if (!response) return [];
        return await response.json();
    }

    async function loadConfigurations() {
        const response = await AppHttp.apiFetch("/configuration/select/data");
        if (!response) return [];
        return await response.json();
    }

    async function init() {
        try {
            const [timeSeriesList, configurationList] = await Promise.all([
                loadTimeSeries(),
                loadConfigurations()
            ]);

            fillSelect(
                document.getElementById("timeSeriesIdManual"),
                timeSeriesList,
                "— vyber —"
            );

            fillSelect(
                document.getElementById("timeSeriesId"),
                timeSeriesList,
                "— vyber —"
            );

            fillSelect(
                document.getElementById("configurationId"),
                configurationList,
                "— vyber —"
            );

        } catch (error) {
            console.error("Nepodařilo se načíst data pro formulář.", error);
        }
    }

    function createParagraph(text, className = "") {
        const p = document.createElement("p");
        p.textContent = text;
        if (className) p.className = className;
        return p;
    }

    function initModelPickerForCalculation() {
        const configSelect = document.getElementById("configurationId");
        const modelList = document.getElementById("modelList");
        if (!configSelect || !modelList) return;

        configSelect.addEventListener("change", async function () {
            const configurationId = this.value;
            modelList.replaceChildren();

            if (!configurationId) return;

            try {
                const response = await AppHttp.apiFetch(`/configuration/${configurationId}`);
                if (!response) return;

                const data = await response.json();
                const models = Array.isArray(data) ? data : (data.models ?? []);

                if (models.length === 0) {
                    modelList.appendChild(createParagraph("Žádné modely pro tuto konfiguraci.", "text-muted mt-2"));
                    return;
                }

                const label = document.createElement("label");
                label.textContent = "Vyber model:";
                label.classList.add("form-label", "mt-3");
                modelList.appendChild(label);

                models.forEach(model => {
                    const wrapper = document.createElement("div");
                    wrapper.classList.add("form-check");

                    const input = document.createElement("input");
                    input.type = "radio";
                    input.name = "modelId";
                    input.value = String(model.id ?? "");
                    input.id = `model-${model.id}`;
                    input.required = true;
                    input.classList.add("form-check-input");

                    const lbl = document.createElement("label");
                    lbl.htmlFor = input.id;
                    lbl.classList.add("form-check-label");
                    lbl.textContent = model.name ?? "Model";

                    wrapper.appendChild(input);
                    wrapper.appendChild(lbl);
                    modelList.appendChild(wrapper);
                });

            } catch (e) {
                console.error(e);
                modelList.appendChild(createParagraph("Nepodařilo se načíst modely.", "text-danger mt-2"));
            }
        });
    }

    async function submitCalculationForm(formEl) {
        const action = formEl.getAttribute("action");
        if (!action) return;

        const formData = new FormData(formEl);

        try {
            const response = await AppHttp.apiFetch(action, {
                method: "POST",
                body: formData
            });

            if (!response) return;

            if (!response.ok) {
                let message = "Došlo k chybě při výpočtu.";
                try {
                    message = await response.text();
                } catch (_) {}
                window.AppModal?.showError(message);
                return;
            }

            const chartDto = await response.json();

            document.getElementById("resultSection")?.classList.remove("d-none");
            AppChart.renderChart("resultChart", chartDto, "output");

        } catch (error) {
            console.error("Chyba při spuštění výpočtu:", error);
            window.AppModal?.showError("Došlo k nečekané chybě při spuštění výpočtu.");
        }
    }

    function initCalculationSubmit() {
        const manualForm = document.querySelector(".form-manual");
        const configForm = document.querySelector(".form-config");

        if (manualForm) {
            manualForm.addEventListener("submit", async (e) => {
                e.preventDefault();
                await submitCalculationForm(manualForm);
            });
        }

        if (configForm) {
            configForm.addEventListener("submit", async (e) => {
                e.preventDefault();
                await submitCalculationForm(configForm);
            });
        }
    }

    (function () {
        // Last variances
        const lvList = document.getElementById('lastVarianceList');
        const lvTpl = document.getElementById('tpl-last-variance');
        const addLV = document.getElementById('addLastVariance');
        const removeLV = document.getElementById('removeLastVariance');

        if (lvList && lvTpl && addLV && removeLV) {
            addLV.addEventListener('click', () => {
                lvList.append(lvTpl.content.cloneNode(true));
            });
            removeLV.addEventListener('click', () => {
                const items = lvList.querySelectorAll('.row-item');
                if (items.length > 1) items[items.length - 1].remove();
            });
        }

        // Last shocks
        const lsList = document.getElementById('lastShockList');
        const lsTpl = document.getElementById('tpl-last-shock');
        const addLS = document.getElementById('addLastShock');
        const removeLS = document.getElementById('removeLastShock');

        if (lsList && lsTpl && addLS && removeLS) {
            addLS.addEventListener('click', () => {
                lsList.append(lsTpl.content.cloneNode(true));
            });
            removeLS.addEventListener('click', () => {
                const items = lsList.querySelectorAll('.row-item');
                if (items.length > 1) items[items.length - 1].remove();
            });
        }
    })();

    function initModeSwitcher() {
        const manualRadio = document.getElementById("mode-manual");
        const configRadio = document.getElementById("mode-config");

        const manualForm = document.querySelector(".form-manual");
        const configForm = document.querySelector(".form-config");

        function updateMode() {
            if (manualRadio.checked) {
                manualForm.style.display = "block";
                configForm.style.display = "none";
            } else {
                manualForm.style.display = "none";
                configForm.style.display = "block";
            }
        }

        manualRadio.addEventListener("change", updateMode);
        configRadio.addEventListener("change", updateMode);

        updateMode();
    }

    function initCalculationDescriptionToggle() {
        const button = document.getElementById("toggleCalculationDescription");
        const description = document.getElementById("calculationDescription");

        if (!button || !description) return;

        function updateButtonState() {
            const isOpen = description.classList.contains("show");
            button.setAttribute("aria-expanded", String(isOpen));
            button.textContent = isOpen ? "Skrýt popis" : "Zobrazit popis";
        }

        const collapseInstance = new bootstrap.Collapse(description, {
            toggle: false
        });

        button.addEventListener("click", () => {
            collapseInstance.toggle();
        });

        description.addEventListener("shown.bs.collapse", updateButtonState);
        description.addEventListener("hidden.bs.collapse", updateButtonState);
    }

    document.addEventListener("DOMContentLoaded", async () => {
        await init();
        initModelPickerForCalculation();
        initCalculationSubmit();
        initModeSwitcher();
        initCalculationDescriptionToggle();
    });
})();