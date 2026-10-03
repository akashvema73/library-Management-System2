// Shared by librarian-students.html and admin-students.html (read-only browse/search)
(function () {
    const user = LMS.requireAuth(['LIBRARIAN', 'ADMIN']);
    if (!user) return;

    LMS.renderSidebar('students');

    function statusBadge(status) {
        const map = { ACTIVE: 'badge-success', INACTIVE: 'badge-muted', SUSPENDED: 'badge-danger' };
        return `<span class="badge ${map[status] || 'badge-muted'}">${LMS.escapeHtml(status)}</span>`;
    }

    async function load() {
        const body = document.getElementById('students-body');
        const keyword = document.getElementById('f-keyword').value.trim();
        try {
            const students = await LMS.get('/api/librarian/students' + (keyword ? '?keyword=' + encodeURIComponent(keyword) : ''));
            if (!students.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="6">No students found.</td></tr>';
                return;
            }
            body.innerHTML = students.map(s => `
                <tr>
                    <td><strong>${LMS.escapeHtml(s.name)}</strong></td>
                    <td>${LMS.escapeHtml(s.enrollmentNumber || '—')}</td>
                    <td>${LMS.escapeHtml(s.department || '—')}</td>
                    <td>${s.year ? s.year + ' / ' + (s.semester || '—') : '—'}</td>
                    <td>${LMS.escapeHtml(s.email)}</td>
                    <td>${statusBadge(s.accountStatus)}</td>
                </tr>
            `).join('');
        } catch (err) {
            body.innerHTML = '';
            LMS.toast(err.message, 'error');
        }
    }

    document.getElementById('f-keyword').addEventListener('input', (() => {
        let t;
        return () => { clearTimeout(t); t = setTimeout(load, 300); };
    })());

    load();
})();
