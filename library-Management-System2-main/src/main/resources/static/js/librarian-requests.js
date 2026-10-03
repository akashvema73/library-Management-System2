(function () {
    const user = LMS.requireAuth(['LIBRARIAN', 'ADMIN']);
    if (!user) return;

    LMS.renderSidebar('requests');

    async function load() {
        const body = document.getElementById('requests-body');
        try {
            const requests = await LMS.get('/api/librarian/issues/requests');
            if (!requests.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="5">No pending requests.</td></tr>';
                return;
            }
            body.innerHTML = requests.map(r => `
                <tr>
                    <td>${LMS.escapeHtml(r.student.name)}</td>
                    <td>${LMS.escapeHtml(r.student.enrollmentNumber || '—')}</td>
                    <td>${LMS.escapeHtml(r.book.title)}</td>
                    <td>${LMS.fmtDate(r.requestDate)}</td>
                    <td>
                        <button class="btn btn-accent btn-sm" data-approve="${r.id}">Approve</button>
                        <button class="btn btn-outline btn-sm" data-reject="${r.id}">Reject</button>
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            body.innerHTML = '';
            LMS.toast(err.message, 'error');
        }
    }

    document.getElementById('requests-body').addEventListener('click', async (e) => {
        const approveId = e.target.getAttribute('data-approve');
        const rejectId = e.target.getAttribute('data-reject');
        if (!approveId && !rejectId) return;

        e.target.disabled = true;
        try {
            if (approveId) {
                await LMS.post('/api/librarian/issues/' + approveId + '/approve');
                LMS.toast('Issue approved', 'success');
            } else {
                await LMS.post('/api/librarian/issues/' + rejectId + '/reject');
                LMS.toast('Request rejected', 'success');
            }
            load();
        } catch (err) {
            LMS.toast(err.message, 'error');
            e.target.disabled = false;
        }
    });

    // ---------- Direct issue modal ----------
    const modal = document.getElementById('direct-modal');
    const studentSelect = document.getElementById('d-student');
    const bookSelect = document.getElementById('d-book');

    async function openModal() {
        try {
            const [students, books] = await Promise.all([
                LMS.get('/api/librarian/students'),
                LMS.get('/api/books')
            ]);
            studentSelect.innerHTML = students.map(s =>
                `<option value="${s.id}">${LMS.escapeHtml(s.name)} (${LMS.escapeHtml(s.enrollmentNumber || s.email)})</option>`
            ).join('');
            bookSelect.innerHTML = books.map(b =>
                `<option value="${b.id}" ${b.availableCopies <= 0 ? 'disabled' : ''}>${LMS.escapeHtml(b.title)} — ${b.availableCopies}/${b.totalCopies} available</option>`
            ).join('');
            modal.classList.add('show');
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    }

    document.getElementById('direct-issue-btn').addEventListener('click', openModal);
    document.getElementById('close-modal').addEventListener('click', () => modal.classList.remove('show'));
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.classList.remove('show'); });

    document.getElementById('direct-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await LMS.post('/api/librarian/issues/direct?studentId=' + studentSelect.value + '&bookId=' + bookSelect.value);
            LMS.toast('Book issued directly to student', 'success');
            modal.classList.remove('show');
            load();
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    });

    load();
})();
