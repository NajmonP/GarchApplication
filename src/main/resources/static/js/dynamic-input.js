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
