(function () {
    const user = LMS.requireAuth(['LIBRARIAN', 'ADMIN']);
    if (!user) return;

    LMS.renderSidebar('dashboard');

    async function approve(id, btn) {
        btn.disabled = true;
        try {
            await LMS.post('/api/librarian/issues/' + id + '/approve');
            LMS.toast('Issue approved', 'success');
            load();
        } catch (err) {
            LMS.toast(err.message, 'error');
            btn.disabled = false;
        }
    }

    async function reject(id, btn) {
        btn.disabled = true;
        try {
            await LMS.post('/api/librarian/issues/' + id + '/reject');
            LMS.toast('Request rejected', 'success');
            load();
        } catch (err) {
            LMS.toast(err.message, 'error');
            btn.disabled = false;
        }
    }

    async function load() {
        try {
            const [stats, requests] = await Promise.all([
                LMS.get('/api/dashboard/librarian'),
                LMS.get('/api/librarian/issues/requests')
            ]);

            document.getElementById('s-books').textContent = stats.stats.totalBooks;
            document.getElementById('s-students').textContent = stats.stats.totalStudents;
            document.getElementById('s-issued').textContent = stats.stats.booksIssued;
            document.getElementById('s-overdue').textContent = stats.stats.overdueBooks;
            document.getElementById('s-pending').textContent = stats.stats.pendingRequests;
            document.getElementById('s-fine').textContent = LMS.fmtMoney(stats.stats.collectedFine);

            const body = document.getElementById('requests-body');
            if (!requests.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="4">No pending requests right now.</td></tr>';
                return;
            }
            body.innerHTML = requests.slice(0, 8).map(r => `
                <tr>
                    <td>${LMS.escapeHtml(r.student.name)}</td>
                    <td>${LMS.escapeHtml(r.book.title)}</td>
                    <td>${LMS.fmtDate(r.requestDate)}</td>
                    <td>
                        <button class="btn btn-accent btn-sm" data-approve="${r.id}">Approve</button>
                        <button class="btn btn-outline btn-sm" data-reject="${r.id}">Reject</button>
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    }

    document.getElementById('requests-body').addEventListener('click', (e) => {
        const approveId = e.target.getAttribute('data-approve');
        const rejectId = e.target.getAttribute('data-reject');
        if (approveId) approve(approveId, e.target);
        if (rejectId) reject(rejectId, e.target);
    });

    load();
})();
