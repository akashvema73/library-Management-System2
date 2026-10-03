(function () {
    const user = LMS.requireAuth(['ADMIN']);
    if (!user) return;

    LMS.renderSidebar('dashboard');

    async function load() {
        try {
            const res = await LMS.get('/api/dashboard/admin');
            const s = res.stats;
            document.getElementById('s-students').textContent = s.totalStudents;
            document.getElementById('s-librarians').textContent = s.totalLibrarians;
            document.getElementById('s-books').textContent = s.totalBooks;
            document.getElementById('s-copies').textContent = s.totalCopies;
            document.getElementById('s-issued').textContent = s.booksIssued;
            document.getElementById('s-overdue').textContent = s.overdueBooks;
            document.getElementById('s-pending').textContent = s.pendingRequests;
            document.getElementById('s-fine').textContent = LMS.fmtMoney(s.totalFineCollected);
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    }

    load();
})();
