(function () {
    const user = LMS.requireAuth(['LIBRARIAN', 'ADMIN']);
    if (!user) return;

    LMS.renderSidebar('fines');

    let currentTab = 'pending';

    function statusBadge(status) {
        const map = { PENDING: 'badge-warning', PAID: 'badge-success', WAIVED: 'badge-muted' };
        return `<span class="badge ${map[status] || 'badge-muted'}">${LMS.escapeHtml(status)}</span>`;
    }

    async function load() {
        const body = document.getElementById('fines-body');
        body.innerHTML = '<tr class="empty-row"><td colspan="6">Loading…</td></tr>';
        const endpoint = currentTab === 'pending' ? '/api/librarian/fines/pending' : '/api/librarian/fines';
        try {
            const fines = await LMS.get(endpoint);
            if (!fines.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="6">Nothing to show here.</td></tr>';
                return;
            }
            fines.sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate));
            body.innerHTML = fines.map(f => `
                <tr>
                    <td>${LMS.escapeHtml(f.student.name)}</td>
                    <td>${f.issue && f.issue.book ? LMS.escapeHtml(f.issue.book.title) : '—'}</td>
                    <td>${LMS.fmtDate(f.createdDate)}</td>
                    <td>${LMS.fmtMoney(f.amount)}</td>
                    <td>${statusBadge(f.status)}</td>
                    <td>${f.status === 'PENDING' ? `
                        <button class="btn btn-accent btn-sm" data-collect="${f.id}">Collect</button>
                        <button class="btn btn-outline btn-sm" data-waive="${f.id}">Waive</button>
                    ` : ''}</td>
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

    document.getElementById('fines-body').addEventListener('click', async (e) => {
        const collectId = e.target.getAttribute('data-collect');
        const waiveId = e.target.getAttribute('data-waive');
        if (!collectId && !waiveId) return;

        e.target.disabled = true;
        try {
            if (collectId) {
                await LMS.post('/api/librarian/fines/' + collectId + '/collect');
                LMS.toast('Fine collected', 'success');
            } else {
                await LMS.post('/api/librarian/fines/' + waiveId + '/waive');
                LMS.toast('Fine waived', 'success');
            }
            load();
        } catch (err) {
            LMS.toast(err.message, 'error');
            e.target.disabled = false;
        }
    });

    load();
})();
