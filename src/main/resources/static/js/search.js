document.addEventListener("DOMContentLoaded", () => {

    const searchInput = document.getElementById("searchInput");
    const nameCards = document.querySelectorAll(".card[data-name]");

    if (searchInput && nameCards.length) {
        searchInput.addEventListener("input", () => {
            const value = searchInput.value.toLowerCase().trim();

            nameCards.forEach(card => {
                const name = (card.dataset.name || "").toLowerCase();
                card.style.display = name.includes(value) ? "" : "none";
            });
        });
    }

    const inEl = document.getElementById("inputTsSearch");
    const outEl = document.getElementById("outputTsSearch");
    const calculationCards = document.querySelectorAll(".calculation-card");

    if (calculationCards.length && (inEl || outEl)) {

        function applyFilter() {
            const inQ = inEl ? inEl.value.toLowerCase().trim() : "";
            const outQ = outEl ? outEl.value.toLowerCase().trim() : "";

            calculationCards.forEach(card => {
                const inVal = (card.dataset.input || "").toLowerCase();
                const outVal = (card.dataset.output || "").toLowerCase();

                const matchIn = !inQ || inVal.includes(inQ);
                const matchOut = !outQ || outVal.includes(outQ);

                card.style.display = (matchIn && matchOut) ? "" : "none";
            });
        }

        if (inEl) inEl.addEventListener("input", applyFilter);
        if (outEl) outEl.addEventListener("input", applyFilter);
    }

});