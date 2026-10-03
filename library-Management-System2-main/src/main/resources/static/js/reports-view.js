// Shared by librarian-reports.html and admin-reports.html
(function () {
    const user = LMS.requireAuth(['LIBRARIAN', 'ADMIN']);
    if (!user) return;

    LMS.renderSidebar('reports');

    function fillTable(id, rows, labelKey, colspan) {
        const body = document.getElementById(id);
        if (!rows.length) {
            body.innerHTML = `<tr class="empty-row"><td colspan="${colspan}">No data yet.</td></tr>`;
            return;
        }
        body.innerHTML = rows.slice(0, 10).map(r => `
            <tr>
                <td>${LMS.escapeHtml(r[labelKey] || 'Unspecified')}</td>
                <td>${r.count}</td>
            </tr>
        `).join('');
    }

    async function load() {
        try {
            const [summary, mostBorrowed, activeStudents, categoryWise, departmentWise] = await Promise.all([
                LMS.get('/api/reports/summary'),
                LMS.get('/api/reports/most-borrowed-books'),
                LMS.get('/api/reports/most-active-students'),
                LMS.get('/api/reports/category-wise'),
                LMS.get('/api/reports/department-wise')
            ]);

            document.getElementById('r-total-books').textContent = summary.totalBooks;
            document.getElementById('r-issued').textContent = summary.issuedBooks;
            document.getElementById('r-returned').textContent = summary.returnedBooks;
            document.getElementById('r-overdue').textContent = summary.overdueBooks;
            document.getElementById('r-students').textContent = summary.totalStudents;
            document.getElementById('r-fine').textContent = LMS.fmtMoney(summary.totalFineCollected);

            fillTable('report-books', mostBorrowed, 'title', 2);
            fillTable('report-students', activeStudents, 'name', 2);
            fillTable('report-category', categoryWise, 'category', 2);
            fillTable('report-department', departmentWise, 'department', 2);
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    }

    load();
})();
