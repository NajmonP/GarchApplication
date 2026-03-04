(function () {
    "use strict";

    function formatInstant(isoInstant, options = {}) {
        if (!isoInstant) return "";

        const d = new Date(isoInstant);
        if (Number.isNaN(d.getTime())) return "";

        const {
            locale = "cs-CZ",
            timeZone = "Europe/Prague",
            year = "numeric",
            month = "2-digit",
            day = "2-digit",
            hour = "2-digit",
            minute = "2-digit",
            second = "2-digit"
        } = options;

        return new Intl.DateTimeFormat(locale, {
            timeZone,
            year,
            month,
            day,
            hour,
            minute,
            second
        }).format(d);
    }

    function clear(el) {
        if (el) el.replaceChildren();
    }

    function createEl(tag, className, text) {
        const el = document.createElement(tag);
        if (className) el.className = className;
        if (text != null) el.textContent = text;
        return el;
    }

    function createInfoRow(label, value) {
        const p = document.createElement("p");
        const strong = document.createElement("strong");
        strong.textContent = label;

        const span = document.createElement("span");
        span.textContent = value ?? "";

        p.appendChild(strong);
        p.append(" ");
        p.appendChild(span);
        return p;
    }

    window.AppElManager = { formatInstant, clear, createEl, createInfoRow };
})();