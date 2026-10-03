(function () {
    const user = LMS.requireAuth(['STUDENT']);
    if (!user) return;

    LMS.renderSidebar('reservations');

    function statusBadge(status) {
        const map = {
            PENDING: 'badge-warning',
            AVAILABLE: 'badge-success',
            FULFILLED: 'badge-muted',
            CANCELLED: 'badge-danger'
        };
        return `<span class="badge ${map[status] || 'badge-muted'}">${LMS.escapeHtml(status)}</span>`;
    }

    async function load() {
        const body = document.getElementById('res-body');
        try {
            const reservations = await LMS.get('/api/student/reservations');
            if (!reservations.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="5">No reservations yet.</td></tr>';
                return;
            }
            body.innerHTML = reservations.map(r => `
                <tr>
                    <td><strong>${LMS.escapeHtml(r.book.title)}</strong></td>
                    <td>${LMS.fmtDate(r.reservationDate)}</td>
                    <td>${r.queuePosition ? '#' + r.queuePosition : '—'}</td>
                    <td>${statusBadge(r.status)}</td>
                    <td>${r.status === 'PENDING' ? `<button class="btn btn-outline btn-sm" data-cancel="${r.id}">Cancel</button>` : ''}</td>
                </tr>
            `).join('');
        } catch (err) {
            body.innerHTML = '';
            LMS.toast(err.message, 'error');
        }
    }

    document.getElementById('res-body').addEventListener('click', async (e) => {
        const id = e.target.getAttribute('data-cancel');
        if (!id) return;
        e.target.disabled = true;
        try {
            await LMS.del('/api/student/reservations/' + id);
            LMS.toast('Reservation cancelled', 'success');
            load();
        } catch (err) {
            LMS.toast(err.message, 'error');
            e.target.disabled = false;
        }
    });

    load();
})();
