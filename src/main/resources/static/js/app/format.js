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

    function formatValue(v, decimals = 6) {
        if (typeof v !== "number" || Number.isNaN(v)) return "-";

        if (Number.isInteger(v)) {
            return String(v);
        }

        return v
            .toFixed(decimals)
            .replace(/\.?0+$/, "");
    }

    window.AppFormatter = { formatInstant, formatValue  };
})();