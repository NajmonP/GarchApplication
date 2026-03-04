(function () {
    "use strict";

    function filterByDatasetContains({
                                         input,
                                         itemsSelector,
                                         itemDatasetKey,
                                         emptyDisplay = "none",
                                         hiddenDisplay = "none"
                                     }) {
        if (!input) return;

        const apply = () => {
            const q = (input.value || "").toLowerCase().trim();
            const items = document.querySelectorAll(itemsSelector);

            items.forEach((el) => {
                const val = (el.dataset?.[itemDatasetKey] || "").toLowerCase();
                const show = !q || val.includes(q);
                el.style.display = show ? emptyDisplay : hiddenDisplay;
            });
        };

        input.addEventListener("input", apply);
        apply();
    }

    function filterByMultipleDatasetContains({
                                                 inputs,        // [{ el, key }]
                                                 itemsSelector,
                                                 emptyDisplay = "",
                                                 hiddenDisplay = "none"
                                             }) {
        const valid = (inputs || []).filter(x => x?.el);
        if (!valid.length) return;

        const apply = () => {
            const queries = valid.map(x => ({
                key: x.key,
                q: (x.el.value || "").toLowerCase().trim()
            }));

            const items = document.querySelectorAll(itemsSelector);

            items.forEach((el) => {
                const ok = queries.every(({ key, q }) => {
                    if (!q) return true;
                    const val = (el.dataset?.[key] || "").toLowerCase();
                    return val.includes(q);
                });

                el.style.display = ok ? emptyDisplay : hiddenDisplay;
            });
        };

        valid.forEach(x => x.el.addEventListener("input", apply));
        apply();
    }

    window.AppSearch = {
        filterByDatasetContains,
        filterByMultipleDatasetContains
    };
})();