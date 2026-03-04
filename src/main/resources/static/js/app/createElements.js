(function () {
    "use strict";

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

    window.AppElManager = { clear, createEl, createInfoRow };
})();