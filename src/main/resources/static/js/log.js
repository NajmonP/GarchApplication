document.addEventListener("DOMContentLoaded", () => {

    const fromEl = document.getElementById("fromDate");
    const toEl   = document.getElementById("toDate");
    const btn    = document.getElementById("filterBtn");
    const tbody  = document.getElementById("auditTbody");

    const paginationEl = document.getElementById("pagination");

    let currentPage = 0;
    const pageSize = 50;

    function formatInstant(isoInstant) {
        if (!isoInstant) return "";
        const d = new Date(isoInstant);
        return new Intl.DateTimeFormat("cs-CZ", {
            timeZone: "Europe/Prague",
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit"
        }).format(d);
    }

    function clearChildren(el) {
        if (!el) return;
        el.replaceChildren();
    }

    function renderEmptyRow(message, className = "text-muted") {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 7;
        td.className = className;
        td.textContent = message;
        tr.appendChild(td);
        tbody.replaceChildren(tr);
    }

    function renderRows(items) {
        if (!items || items.length === 0) {
            renderEmptyRow("Žádné záznamy pro daný interval.");
            return;
        }

        const fragment = document.createDocumentFragment();

        for (const a of items) {
            const tr = document.createElement("tr");

            const tdUserId = document.createElement("td");
            tdUserId.textContent = a.userId ?? "";
            tr.appendChild(tdUserId);

            const tdUsername = document.createElement("td");
            tdUsername.textContent = a.username ?? "";
            tr.appendChild(tdUsername);

            const tdOp = document.createElement("td");
            tdOp.textContent = a.operationType ?? ""; // nebo a.operation (podle DTO)
            tr.appendChild(tdOp);

            const tdEntityType = document.createElement("td");
            tdEntityType.textContent = a.entityType ?? "";
            tr.appendChild(tdEntityType);

            const tdEntityId = document.createElement("td");
            tdEntityId.textContent = a.entityId ?? "";
            tr.appendChild(tdEntityId);

            const tdEntityName = document.createElement("td");
            tdEntityName.textContent = a.entityName ?? "";
            tr.appendChild(tdEntityName);

            const tdTime = document.createElement("td");
            tdTime.textContent = formatInstant(a.occurredAt);
            tr.appendChild(tdTime);

            fragment.appendChild(tr);
        }

        tbody.replaceChildren(fragment);
    }

    function createPageItem({ label, page, disabled, active }) {
        const li = document.createElement("li");
        li.classList.add("page-item");
        if (disabled) li.classList.add("disabled");
        if (active) li.classList.add("active");

        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "page-link";
        btn.textContent = label;

        if (!disabled) {
            btn.addEventListener("click", () => loadAuditLogs(page));
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

    function renderPagination(totalPages, pageNumber) {
        if (!paginationEl) return;

        if (!totalPages || totalPages <= 1) {
            clearChildren(paginationEl);
            return;
        }

        const windowSize = 7;
        const start = Math.max(0, pageNumber - Math.floor(windowSize / 2));
        const end = Math.min(totalPages - 1, start + windowSize - 1);
        const realStart = Math.max(0, end - windowSize + 1);

        const fragment = document.createDocumentFragment();

        // «
        fragment.appendChild(createPageItem({
            label: "«",
            page: pageNumber - 1,
            disabled: pageNumber === 0,
            active: false
        }));

        // 1 + …
        if (realStart > 0) {
            fragment.appendChild(createPageItem({
                label: "1",
                page: 0,
                disabled: false,
                active: pageNumber === 0
            }));
            if (realStart > 1) fragment.appendChild(createEllipsisItem());
        }

        // okno stránek
        for (let i = realStart; i <= end; i++) {
            fragment.appendChild(createPageItem({
                label: String(i + 1),
                page: i,
                disabled: false,
                active: i === pageNumber
            }));
        }

        if (end < totalPages - 1) {
            if (end < totalPages - 2) fragment.appendChild(createEllipsisItem());
            fragment.appendChild(createPageItem({
                label: String(totalPages),
                page: totalPages - 1,
                disabled: false,
                active: pageNumber === totalPages - 1
            }));
        }

        fragment.appendChild(createPageItem({
            label: "»",
            page: pageNumber + 1,
            disabled: pageNumber === totalPages - 1,
            active: false
        }));

        paginationEl.replaceChildren(fragment);
    }

    async function loadAuditLogs(page = 0) {
        const from = fromEl.value;
        const to   = toEl.value;

        if (!from || !to) {
            alert("Vyplň Od i Do.");
            return;
        }

        currentPage = page;

        const params = new URLSearchParams({
            from,
            to,
            page: String(currentPage),
            size: String(pageSize)
        });

        const res = await fetch(`/log/data?${params.toString()}`, {
            method: "GET",
            headers: { "Accept": "application/json" }
        });

        if (!res.ok) {
            const text = await res.text();
            renderEmptyRow(`Chyba: HTTP ${res.status} – ${text}`, "text-danger");
            clearChildren(paginationEl);
            return;
        }

        const pageData = await res.json();

        // PageResponse => pageData.page
        const pageNumber = pageData.page ?? 0;
        const totalPages = pageData.totalPages ?? 0;

        renderRows(pageData.content ?? []);
        renderPagination(totalPages, pageNumber);
    }

    btn.addEventListener("click", () => loadAuditLogs(0));

    const today = new Date().toISOString().slice(0, 10);
    fromEl.value = today;
    toEl.value = today;
});