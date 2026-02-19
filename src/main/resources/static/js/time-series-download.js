document.addEventListener("DOMContentLoaded", function () {
    initTimeSeriesDownloadButtons();
});

function initTimeSeriesDownloadButtons() {
    const buttons = document.querySelectorAll(".download-time-series-btn");
    if (!buttons || buttons.length === 0) return;

    buttons.forEach(btn => {
        btn.addEventListener("click", function () {
            const timeSeriesId = this.getAttribute("data-time-series-id");
            if (!timeSeriesId) return;

            window.location.href = `/time-series/download/${timeSeriesId}`;
        });
    });
}
