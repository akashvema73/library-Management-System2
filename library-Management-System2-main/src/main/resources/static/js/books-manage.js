// Shared by librarian-books.html and admin-books.html
(function () {
    const user = LMS.requireAuth(['LIBRARIAN', 'ADMIN']);
    if (!user) return;

    LMS.renderSidebar('books');

    let categories = [];
    let currentTab = 'books';

    // ---------- Tabs ----------
    document.querySelectorAll('.pill-tabs button').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.pill-tabs button').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentTab = btn.getAttribute('data-tab');
            document.getElementById('books-panel').style.display = currentTab === 'books' ? 'block' : 'none';
            document.getElementById('categories-panel').style.display = currentTab === 'categories' ? 'block' : 'none';
        });
    });

    // ---------- Categories ----------
    async function loadCategories() {
        categories = await LMS.get('/api/categories');
        const filterSelect = document.getElementById('f-category');
        filterSelect.innerHTML = '<option value="">All categories</option>' +
            categories.map(c => `<option value="${c.id}">${LMS.escapeHtml(c.name)}</option>`).join('');

        const formSelect = document.getElementById('b-category');
        formSelect.innerHTML = categories.map(c => `<option value="${c.id}">${LMS.escapeHtml(c.name)}</option>`).join('');

        const body = document.getElementById('categories-body');
        if (!categories.length) {
            body.innerHTML = '<tr class="empty-row"><td colspan="3">No categories yet.</td></tr>';
            return;
        }
        body.innerHTML = categories.map(c => `
            <tr>
                <td><strong>${LMS.escapeHtml(c.name)}</strong></td>
                <td>${LMS.escapeHtml(c.description || '—')}</td>
                <td><button class="btn btn-outline btn-sm" data-del-cat="${c.id}">Delete</button></td>
            </tr>
        `).join('');
    }

    const categoryModal = document.getElementById('category-modal');
    document.getElementById('add-category-btn').addEventListener('click', () => categoryModal.classList.add('show'));
    document.getElementById('category-modal-close').addEventListener('click', () => categoryModal.classList.remove('show'));
    categoryModal.addEventListener('click', (e) => { if (e.target === categoryModal) categoryModal.classList.remove('show'); });

    document.getElementById('category-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await LMS.post('/api/categories', {
                name: document.getElementById('c-name').value.trim(),
                description: document.getElementById('c-description').value.trim()
            });
            LMS.toast('Category created', 'success');
            categoryModal.classList.remove('show');
            document.getElementById('category-form').reset();
            loadCategories();
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    });

    document.getElementById('categories-body').addEventListener('click', async (e) => {
        const id = e.target.getAttribute('data-del-cat');
        if (!id) return;
        if (!confirm('Delete this category? Books using it should be reassigned first.')) return;
        try {
            await LMS.del('/api/categories/' + id);
            LMS.toast('Category deleted', 'success');
            loadCategories();
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    });

    // ---------- Books ----------
    async function loadBooks() {
        const body = document.getElementById('books-body');
        const params = new URLSearchParams();
        const keyword = document.getElementById('f-keyword').value.trim();
        const categoryId = document.getElementById('f-category').value;
        if (keyword) params.set('keyword', keyword);
        if (categoryId) params.set('categoryId', categoryId);
        params.set('onlyAvailable', 'false');

        try {
            const books = await LMS.get('/api/books/search?' + params.toString());
            if (!books.length) {
                body.innerHTML = '<tr class="empty-row"><td colspan="6">No books found.</td></tr>';
                return;
            }
            body.innerHTML = books.map(b => `
                <tr>
                    <td><strong>${LMS.escapeHtml(b.title)}</strong></td>
                    <td>${LMS.escapeHtml(b.author || '—')}</td>
                    <td>${b.category ? LMS.escapeHtml(b.category.name) : '—'}</td>
                    <td>${LMS.escapeHtml(b.isbn)}</td>
                    <td>${b.availableCopies}/${b.totalCopies}</td>
                    <td>
                        <button class="btn btn-outline btn-sm" data-edit="${b.id}">Edit</button>
                        <button class="btn btn-outline btn-sm" data-del="${b.id}">Delete</button>
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            body.innerHTML = '';
            LMS.toast(err.message, 'error');
        }
    }

    document.getElementById('f-keyword').addEventListener('input', debounce(loadBooks, 300));
    document.getElementById('f-category').addEventListener('change', loadBooks);

    function debounce(fn, ms) {
        let t;
        return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
    }

    const bookModal = document.getElementById('book-modal');
    const bookForm = document.getElementById('book-form');

    function openBookModal(book) {
        document.getElementById('book-modal-title').textContent = book ? 'Edit book' : 'Add book';
        document.getElementById('b-id').value = book ? book.id : '';
        document.getElementById('b-title').value = book ? book.title : '';
        document.getElementById('b-isbn').value = book ? book.isbn : '';
        document.getElementById('b-author').value = book ? (book.author || '') : '';
        document.getElementById('b-publisher').value = book ? (book.publisher || '') : '';
        document.getElementById('b-category').value = book && book.category ? book.category.id : (categories[0] ? categories[0].id : '');
        document.getElementById('b-language').value = book ? (book.language || '') : '';
        document.getElementById('b-edition').value = book ? (book.edition || '') : '';
        document.getElementById('b-year').value = book ? (book.publicationYear || '') : '';
        document.getElementById('b-shelf').value = book ? (book.shelfNumber || '') : '';
        document.getElementById('b-rack').value = book ? (book.rackNumber || '') : '';
        document.getElementById('b-price').value = book ? (book.price || '') : '';
        document.getElementById('b-copies').value = book ? book.totalCopies : 1;
        bookModal.classList.add('show');
    }

    document.getElementById('add-book-btn').addEventListener('click', () => {
        if (!categories.length) {
            LMS.toast('Create a category first', 'error');
            return;
        }
        openBookModal(null);
    });
    document.getElementById('book-modal-close').addEventListener('click', () => bookModal.classList.remove('show'));
    bookModal.addEventListener('click', (e) => { if (e.target === bookModal) bookModal.classList.remove('show'); });

    bookForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('b-id').value;
        const payload = {
            title: document.getElementById('b-title').value.trim(),
            isbn: document.getElementById('b-isbn').value.trim(),
            author: document.getElementById('b-author').value.trim(),
            publisher: document.getElementById('b-publisher').value.trim(),
            categoryId: Number(document.getElementById('b-category').value),
            language: document.getElementById('b-language').value.trim(),
            edition: document.getElementById('b-edition').value.trim(),
            publicationYear: document.getElementById('b-year').value ? Number(document.getElementById('b-year').value) : null,
            shelfNumber: document.getElementById('b-shelf').value.trim(),
            rackNumber: document.getElementById('b-rack').value.trim(),
            price: document.getElementById('b-price').value ? Number(document.getElementById('b-price').value) : null,
            totalCopies: Number(document.getElementById('b-copies').value)
        };

        try {
            if (id) {
                await LMS.put('/api/books/' + id, payload);
                LMS.toast('Book updated', 'success');
            } else {
                await LMS.post('/api/books', payload);
                LMS.toast('Book added', 'success');
            }
            bookModal.classList.remove('show');
            loadBooks();
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    });

    document.getElementById('books-body').addEventListener('click', async (e) => {
        const editId = e.target.getAttribute('data-edit');
        const delId = e.target.getAttribute('data-del');

        if (editId) {
            try {
                const book = await LMS.get('/api/books/' + editId);
                openBookModal(book);
            } catch (err) {
                LMS.toast(err.message, 'error');
            }
        }

        if (delId) {
            if (!confirm('Delete this book and all its copies?')) return;
            try {
                await LMS.del('/api/books/' + delId);
                LMS.toast('Book deleted', 'success');
                loadBooks();
            } catch (err) {
                LMS.toast(err.message, 'error');
            }
        }
    });

    loadCategories().then(loadBooks);
})();
