(function () {
    const user = LMS.requireAuth(['STUDENT']);
    if (!user) return;

    LMS.renderSidebar('dashboard');
    document.getElementById('welcome-name').textContent = ', ' + user.name.split(' ')[0];

    function statusBadge(status) {
        const map = {
            ISSUED: 'badge-success',
            OVERDUE: 'badge-danger',
            RETURNED: 'badge-muted',
            REQUESTED: 'badge-warning',
            REJECTED: 'badge-danger'
        };
        return `<span class="badge ${map[status] || 'badge-muted'}">${LMS.escapeHtml(status)}</span>`;
    }

    async function load() {
        try {
            const [stats, active] = await Promise.all([
                LMS.get('/api/dashboard/student'),
                LMS.get('/api/student/issues/active')
            ]);

            document.getElementById('stat-issued').textContent = stats.stats.booksIssued;
            document.getElementById('stat-due-soon').textContent = stats.stats.dueSoon;
            document.getElementById('stat-fine').textContent = LMS.fmtMoney(stats.stats.pendingFine);

            const body = document.getElementById('recent-books-body');
            if (!active.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="4">No books currently issued</td></tr>';
                return;
            }
            body.innerHTML = active.slice(0, 6).map(issue => `
                <tr>
                    <td><strong>${LMS.escapeHtml(issue.book.title)}</strong></td>
                    <td>${LMS.fmtDate(issue.issueDate)}</td>
                    <td>${LMS.fmtDate(issue.dueDate)}</td>
                    <td>${statusBadge(issue.status)}</td>
                </tr>
            `).join('');
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    }

    load();
})();
