(function () {
    const user = LMS.requireAuth(['LIBRARIAN', 'ADMIN']);
    if (!user) return;

    LMS.renderSidebar('issues');

    let currentTab = 'all';

    function statusBadge(status) {
        const map = { ISSUED: 'badge-success', OVERDUE: 'badge-danger' };
        return `<span class="badge ${map[status] || 'badge-muted'}">${LMS.escapeHtml(status)}</span>`;
    }

    async function load() {
        const body = document.getElementById('issues-body');
        body.innerHTML = '<tr class="empty-row"><td colspan="6">Loading…</td></tr>';
        const endpoint = currentTab === 'overdue' ? '/api/librarian/issues/overdue' : '/api/librarian/issues';
        try {
            const issues = await LMS.get(endpoint);
            if (!issues.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="6">Nothing to show here.</td></tr>';
                return;
            }
            body.innerHTML = issues.map(i => `
                <tr>
                    <td>${LMS.escapeHtml(i.student.name)}</td>
                    <td>${LMS.escapeHtml(i.book.title)}</td>
                    <td>${LMS.fmtDate(i.issueDate)}</td>
                    <td>${LMS.fmtDate(i.dueDate)}</td>
                    <td>${statusBadge(i.status)}</td>
                    <td><button class="btn btn-accent btn-sm" data-return="${i.id}">Mark returned</button></td>
                </tr>
            `).join('');
        } catch (err) {
            body.innerHTML = '';
            LMS.toast(err.message, 'error');
        }
    }

    document.querySelectorAll('.pill-tabs button').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.pill-tabs button').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentTab = btn.getAttribute('data-tab');
            load();
        });
    });

    document.getElementById('issues-body').addEventListener('click', async (e) => {
        const id = e.target.getAttribute('data-return');
        if (!id) return;
        e.target.disabled = true;
        try {
            const result = await LMS.post('/api/librarian/issues/' + id + '/return');
            const fine = result.fineAmount;
            LMS.toast(fine > 0 ? `Book returned. Late fine of ${LMS.fmtMoney(fine)} generated.` : 'Book returned on time.', 'success');
            load();
        } catch (err) {
            LMS.toast(err.message, 'error');
            e.target.disabled = false;
        }
    });

    load();
})();
