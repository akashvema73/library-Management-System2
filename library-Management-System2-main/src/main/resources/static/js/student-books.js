(function () {
    const user = LMS.requireAuth(['STUDENT']);
    if (!user) return;

    LMS.renderSidebar('books');

    const grid = document.getElementById('book-grid');
    const keywordEl = document.getElementById('f-keyword');
    const categoryEl = document.getElementById('f-category');
    const availableEl = document.getElementById('f-available');

    let debounceTimer = null;

    async function loadCategories() {
        try {
            const categories = await LMS.get('/api/categories');
            categoryEl.innerHTML = '<option value="">All categories</option>' +
                categories.map(c => `<option value="${c.id}">${LMS.escapeHtml(c.name)}</option>`).join('');
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    }

    function bookCard(book) {
        const available = book.availableCopies > 0;
        const availText = available
            ? `<span style="color:var(--success)">${book.availableCopies} of ${book.totalCopies} available</span>`
            : `<span style="color:var(--danger)">All ${book.totalCopies} copies issued</span>`;

        const actionBtn = available
            ? `<button class="btn btn-accent btn-sm" data-request="${book.id}">Request issue</button>`
            : `<button class="btn btn-outline btn-sm" data-reserve="${book.id}">Reserve</button>`;

        return `
            <div class="book-card">
                <div class="spine"></div>
                <h4>${LMS.escapeHtml(book.title)}</h4>
                <div class="meta">${LMS.escapeHtml(book.author || 'Unknown author')}</div>
                <div class="meta">${book.category ? LMS.escapeHtml(book.category.name) : ''}${book.rackNumber ? ' · Rack ' + LMS.escapeHtml(book.rackNumber) : ''}</div>
                <div class="avail">${availText}</div>
                <div class="actions">${actionBtn}</div>
            </div>
        `;
    }

    async function search() {
        grid.innerHTML = '<p class="loading-row">Searching…</p>';
        const params = new URLSearchParams();
        if (keywordEl.value.trim()) params.set('keyword', keywordEl.value.trim());
        if (categoryEl.value) params.set('categoryId', categoryEl.value);
        params.set('onlyAvailable', availableEl.value);

        try {
            const books = await LMS.get('/api/books/search?' + params.toString());
            if (!books.length) {
                grid.innerHTML = '<p class="loading-row">No books matched your search.</p>';
                return;
            }
            grid.innerHTML = books.map(bookCard).join('');
        } catch (err) {
            grid.innerHTML = '';
            LMS.toast(err.message, 'error');
        }
    }

    grid.addEventListener('click', async (e) => {
        const requestId = e.target.getAttribute('data-request');
        const reserveId = e.target.getAttribute('data-reserve');

        if (requestId) {
            e.target.disabled = true;
            try {
                await LMS.post('/api/student/issues/request', { bookId: Number(requestId) });
                LMS.toast('Issue request submitted. Waiting for librarian approval.', 'success');
                search();
            } catch (err) {
                LMS.toast(err.message, 'error');
                e.target.disabled = false;
            }
        }

        if (reserveId) {
            e.target.disabled = true;
            try {
                await LMS.post('/api/student/reservations?bookId=' + reserveId);
                LMS.toast('Book reserved. We will notify you when it is available.', 'success');
                search();
            } catch (err) {
                LMS.toast(err.message, 'error');
                e.target.disabled = false;
            }
        }
    });

    [keywordEl, categoryEl, availableEl].forEach(el => {
        el.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(search, 300);
        });
        el.addEventListener('change', search);
    });

    loadCategories().then(() => {
        const params = new URLSearchParams(window.location.search);
        const q = params.get('q');
        if (q) {
            keywordEl.value = q;
        }
        search();
    });
})();
