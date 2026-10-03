(function () {
    const user = LMS.requireAuth(['STUDENT']);
    if (!user) return;

    LMS.renderSidebar('fines');

    function statusBadge(status) {
        const map = { PENDING: 'badge-warning', PAID: 'badge-success', WAIVED: 'badge-muted' };
        return `<span class="badge ${map[status] || 'badge-muted'}">${LMS.escapeHtml(status)}</span>`;
    }

    async function load() {
        const body = document.getElementById('fines-body');
        try {
            const fines = await LMS.get('/api/student/fines');
            const pendingTotal = fines.filter(f => f.status === 'PENDING').reduce((s, f) => s + f.amount, 0);
            document.getElementById('pending-total').textContent = LMS.fmtMoney(pendingTotal);

            if (!fines.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="5">No fines on your account. Keep it up!</td></tr>';
                return;
            }
            fines.sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate));
            body.innerHTML = fines.map(f => `
                <tr>
                    <td>${f.issue && f.issue.book ? LMS.escapeHtml(f.issue.book.title) : '—'}</td>
                    <td>${LMS.fmtDate(f.createdDate)}</td>
                    <td>${LMS.fmtMoney(f.amount)}</td>
                    <td>${statusBadge(f.status)}</td>
                    <td>${f.status === 'PENDING' ? `<button class="btn btn-accent btn-sm" data-pay="${f.id}">Pay fine</button>` : ''}</td>
                </tr>
            `).join('');
        } catch (err) {
            body.innerHTML = '';
            LMS.toast(err.message, 'error');
        }
    }

    document.getElementById('fines-body').addEventListener('click', async (e) => {
        const id = e.target.getAttribute('data-pay');
        if (!id) return;
        if (!confirm('Confirm payment of this fine? (Demo: marks it paid immediately.)')) return;
        e.target.disabled = true;
        try {
            await LMS.post('/api/student/fines/' + id + '/pay');
            LMS.toast('Fine paid successfully', 'success');
            load();
        } catch (err) {
            LMS.toast(err.message, 'error');
            e.target.disabled = false;
        }
    });

    load();
})();
