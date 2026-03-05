(function () {
    "use strict";

    function createPageItem({ label, page, disabled, active, onClick }) {
        const li = document.createElement("li");
        li.classList.add("page-item");
        if (disabled) li.classList.add("disabled");
        if (active) li.classList.add("active");

        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "page-link";
        btn.textContent = label;

        if (!disabled && typeof onClick === "function") {
            btn.addEventListener("click", () => onClick(page));
        }

        li.appendChild(btn);
        return li;
    }

    function createEllipsisItem() {
        const li = document.createElement("li");
        li.className = "page-item disabled";

        const span = document.createElement("span");
        span.className = "page-link";
        span.textContent = "…";

        li.appendChild(span);
        return li;
    }

    function renderPagination(container, totalPages, pageNumber, onPageClick) {

        if (!container) return;

        if (!totalPages || totalPages <= 1) {
            container.replaceChildren();
            return;
        }

        const windowSize = 7;

        const start = Math.max(0, pageNumber - Math.floor(windowSize / 2));
        const end = Math.min(totalPages - 1, start + windowSize - 1);
        const realStart = Math.max(0, end - windowSize + 1);

        const fragment = document.createDocumentFragment();

        fragment.appendChild(createPageItem({
            label: "«",
            page: pageNumber - 1,
            disabled: pageNumber === 0,
            active: false,
            onClick: onPageClick
        }));

        if (realStart > 0) {
            fragment.appendChild(createPageItem({
                label: "1",
                page: 0,
                disabled: false,
                active: pageNumber === 0,
                onClick: onPageClick
            }));

            if (realStart > 1) {
                fragment.appendChild(createEllipsisItem());
            }
        }

        for (let i = realStart; i <= end; i++) {
            fragment.appendChild(createPageItem({
                label: String(i + 1),
                page: i,
                disabled: false,
                active: i === pageNumber,
                onClick: onPageClick
            }));
        }

        if (end < totalPages - 1) {

            if (end < totalPages - 2) {
                fragment.appendChild(createEllipsisItem());
            }

            fragment.appendChild(createPageItem({
                label: String(totalPages),
                page: totalPages - 1,
                disabled: false,
                active: pageNumber === totalPages - 1,
                onClick: onPageClick
            }));
        }

        fragment.appendChild(createPageItem({
            label: "»",
            page: pageNumber + 1,
            disabled: pageNumber === totalPages - 1,
            active: false,
            onClick: onPageClick
        }));

        container.replaceChildren(fragment);
    }

    window.AppPagination = { renderPagination };
})();