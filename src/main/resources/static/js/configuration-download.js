document.addEventListener("DOMContentLoaded", function () {
    initConfigurationDownloadButtons();
});

function initConfigurationDownloadButtons() {
    const buttons = document.querySelectorAll(".download-config-btn");
    if (!buttons || buttons.length === 0) return;

    buttons.forEach(btn => {
        btn.addEventListener("click", function () {
            const configurationId = this.getAttribute("data-configuration-id");
            if (!configurationId) return;

            window.location.href = `/configuration/download/${configurationId}`;
        });
    });
}
