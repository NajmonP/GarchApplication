(function () {
    var modalEl = document.getElementById('errorModal');
    if (!modalEl || typeof bootstrap === 'undefined') return;
    var modal = new bootstrap.Modal(modalEl);
    modal.show();
})();