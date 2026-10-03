(function () {
    const user = LMS.requireAuth(['ADMIN']);
    if (!user) return;

    LMS.renderSidebar('librarians');

    function statusBadge(status) {
        const map = { ACTIVE: 'badge-success', INACTIVE: 'badge-muted', SUSPENDED: 'badge-danger' };
        return `<span class="badge ${map[status] || 'badge-muted'}">${LMS.escapeHtml(status)}</span>`;
    }

    async function load() {
        const body = document.getElementById('librarians-body');
        try {
            const librarians = await LMS.get('/api/admin/librarians');
            if (!librarians.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="4">No librarians yet.</td></tr>';
                return;
            }
            body.innerHTML = librarians.map(l => `
                <tr>
                    <td><strong>${LMS.escapeHtml(l.name)}</strong></td>
                    <td>${LMS.escapeHtml(l.email)}</td>
                    <td>${LMS.escapeHtml(l.mobile || '—')}</td>
                    <td>${statusBadge(l.accountStatus)}</td>
                </tr>
            `).join('');
        } catch (err) {
            body.innerHTML = '';
            LMS.toast(err.message, 'error');
        }
    }

    const modal = document.getElementById('librarian-modal');
    document.getElementById('add-librarian-btn').addEventListener('click', () => modal.classList.add('show'));
    document.getElementById('librarian-modal-close').addEventListener('click', () => modal.classList.remove('show'));
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.classList.remove('show'); });

    document.getElementById('librarian-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await LMS.post('/api/admin/librarians', {
                name: document.getElementById('l-name').value.trim(),
                email: document.getElementById('l-email').value.trim(),
                password: document.getElementById('l-password').value,
                mobile: document.getElementById('l-mobile').value.trim()
            });
            LMS.toast('Librarian account created', 'success');
            modal.classList.remove('show');
            document.getElementById('librarian-form').reset();
            load();
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    });

    load();
})();
