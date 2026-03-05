(function () {
    "use strict";

    const SELECTORS = {
        fromDate: "#fromDate",
        toDate: "#toDate",
        filterBtn: "#filterBtn",
        auditTbody: "#auditTbody",
        pagination: "#pagination"
    };

    const pageSize = 50;
    let currentPage = 0;

    function getEl(selector) {
        return document.querySelector(selector);
    }

    function renderEmptyRow(tbody, message, className = "text-muted") {
        if (!tbody) return;

        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 7;
        td.className = className;
        td.textContent = message;
        tr.appendChild(td);

        tbody.replaceChildren(tr);
    }

    function renderRows(tbody, items) {
        if (!tbody) return;

        if (!items || items.length === 0) {
            renderEmptyRow(tbody, "Žádné záznamy pro daný interval.");
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
            tdOp.textContent = a.operationType ?? "";
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
            tdTime.textContent = window.AppFormatter?.formatInstant?.(a.occurredAt) ?? "";
            tr.appendChild(tdTime);

            fragment.appendChild(tr);
        }

        tbody.replaceChildren(fragment);
    }

    async function loadAuditLogs(ctx, page = 0) {
        const { fromEl, toEl, tbody, paginationEl } = ctx;

        const from = fromEl?.value;
        const to = toEl?.value;

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

        const url = `/log/data?${params.toString()}`;

        const res = await window.AppHttp?.apiFetch?.(url, { method: "GET" });
        if (!res) return;

        const pageData = await res.json();

        const pageNumber = pageData.page ?? 0;
        const totalPages = pageData.totalPages ?? 0;

        renderRows(tbody, pageData.content ?? []);

        window.AppPagination?.renderPagination?.(
            paginationEl,
            totalPages,
            pageNumber,
            (p) => loadAuditLogs(ctx, p)
        );
    }

    document.addEventListener("DOMContentLoaded", () => {
        const fromEl = getEl(SELECTORS.fromDate);
        const toEl = getEl(SELECTORS.toDate);
        const btn = getEl(SELECTORS.filterBtn);
        const tbody = getEl(SELECTORS.auditTbody);
        const paginationEl = getEl(SELECTORS.pagination);

        if (!fromEl || !toEl || !btn || !tbody || !paginationEl) return;

        const ctx = { fromEl, toEl, btn, tbody, paginationEl };

        btn.addEventListener("click", () => loadAuditLogs(ctx, 0));

        const today = new Date().toISOString().slice(0, 10);
        fromEl.value = today;
        toEl.value = today;
    });
})();