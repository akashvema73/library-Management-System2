(function () {
    const user = LMS.requireAuth(['STUDENT']);
    if (!user) return;

    LMS.renderSidebar('my-books');

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
        const body = document.getElementById('issues-body');
        try {
            const issues = await LMS.get('/api/student/issues');
            if (!issues.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="7">You haven\'t requested any books yet.</td></tr>';
                return;
            }
            issues.sort((a, b) => new Date(b.requestDate) - new Date(a.requestDate));
            body.innerHTML = issues.map(issue => `
                <tr>
                    <td><strong>${LMS.escapeHtml(issue.book.title)}</strong></td>
                    <td>${LMS.fmtDate(issue.requestDate)}</td>
                    <td>${LMS.fmtDate(issue.issueDate)}</td>
                    <td>${LMS.fmtDate(issue.dueDate)}</td>
                    <td>${LMS.fmtDate(issue.returnDate)}</td>
                    <td>${statusBadge(issue.status)}</td>
                    <td>${issue.fineAmount ? LMS.fmtMoney(issue.fineAmount) : '—'}</td>
                </tr>
            `).join('');
        } catch (err) {
            body.innerHTML = '';
            LMS.toast(err.message, 'error');
        }
    }

    load();
})();
