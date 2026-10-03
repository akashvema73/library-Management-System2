/* ==========================================================================
   api.js — shared fetch wrapper, auth/session helpers, small UI utilities
   ========================================================================== */

const LMS = (() => {
    const TOKEN_KEY = 'lms_token';
    const USER_KEY = 'lms_user';

    function saveSession(authResponse) {
        localStorage.setItem(TOKEN_KEY, authResponse.token);
        localStorage.setItem(USER_KEY, JSON.stringify({
            id: authResponse.userId,
            name: authResponse.name,
            email: authResponse.email,
            role: authResponse.role
        }));
    }

    function getToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function getUser() {
        const raw = localStorage.getItem(USER_KEY);
        return raw ? JSON.parse(raw) : null;
    }

    function logout() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        window.location.href = '/login.html';
    }

    function homeForRole(role) {
        if (role === 'ADMIN') return 'admin-dashboard.html';
        if (role === 'LIBRARIAN') return 'librarian-dashboard.html';
        return 'student-dashboard.html';
    }

    /**
     * Redirects to login if not authenticated, or if the current user's role
     * isn't in allowedRoles (when provided). Returns the current user.
     */
    function requireAuth(allowedRoles) {
        const user = getUser();
        const token = getToken();
        if (!user || !token) {
            window.location.href = '/login.html';
            return null;
        }
        if (allowedRoles && !allowedRoles.includes(user.role)) {
            window.location.href = homeForRole(user.role);
            return null;
        }
        return user;
    }

    async function request(path, options = {}) {
        const headers = Object.assign(
            { 'Content-Type': 'application/json' },
            options.headers || {}
        );
        const token = getToken();
        if (token) headers['Authorization'] = 'Bearer ' + token;

        const res = await fetch(path, Object.assign({}, options, { headers }));

        if (res.status === 401) {
            logout();
            throw new Error('Session expired. Please log in again.');
        }

        let body = null;
        const text = await res.text();
        if (text) {
            try { body = JSON.parse(text); } catch (e) { body = text; }
        }

        if (!res.ok) {
            const message = (body && body.message) ? body.message :
                (typeof body === 'string' ? body : 'Something went wrong (' + res.status + ')');
            throw new Error(message);
        }

        return body;
    }

    function get(path) { return request(path, { method: 'GET' }); }
    function del(path) { return request(path, { method: 'DELETE' }); }
    function post(path, data) {
        return request(path, { method: 'POST', body: data !== undefined ? JSON.stringify(data) : undefined });
    }
    function put(path, data) {
        return request(path, { method: 'PUT', body: data !== undefined ? JSON.stringify(data) : undefined });
    }
    function patch(path, data) {
        return request(path, { method: 'PATCH', body: data !== undefined ? JSON.stringify(data) : undefined });
    }

    // ---------------- UI helpers ----------------

    function toast(message, type) {
        let el = document.getElementById('lms-toast');
        if (!el) {
            el = document.createElement('div');
            el.id = 'lms-toast';
            el.className = 'toast';
            document.body.appendChild(el);
        }
        el.textContent = message;
        el.className = 'toast show' + (type ? ' toast-' + type : '');
        clearTimeout(el._timer);
        el._timer = setTimeout(() => { el.classList.remove('show'); }, 3200);
    }

    function fmtDate(d) {
        if (!d) return '—';
        const date = new Date(d);
        if (isNaN(date.getTime())) return d;
        return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    function fmtMoney(n) {
        if (n === null || n === undefined) return '₹0';
        return '₹' + Number(n).toFixed(2).replace(/\.00$/, '');
    }

    function initials(name) {
        if (!name) return '?';
        const parts = name.trim().split(/\s+/);
        return (parts[0][0] + (parts[1] ? parts[1][0] : '')).toUpperCase();
    }

    function escapeHtml(str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replaceAll('&', '&amp;').replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;').replaceAll('"', '&quot;');
    }

    /** Renders the sidebar shell into any element with id="lms-sidebar-slot". */
    function renderSidebar(activeKey) {
        const slot = document.getElementById('lms-sidebar-slot');
        if (!slot) return;
        const user = getUser();
        if (!user) return;

        const links = {
            ADMIN: [
                ['admin-dashboard.html', 'dashboard', 'Dashboard'],
                ['admin-books.html', 'books', 'Books & Categories'],
                ['admin-students.html', 'students', 'Students'],
                ['admin-librarians.html', 'librarians', 'Librarians'],
                ['admin-issues.html', 'issues', 'Issues & Returns'],
                ['admin-fines.html', 'fines', 'Fines'],
                ['admin-reports.html', 'reports', 'Reports']
            ],
            LIBRARIAN: [
                ['librarian-dashboard.html', 'dashboard', 'Dashboard'],
                ['librarian-requests.html', 'requests', 'Issue Requests'],
                ['librarian-issues.html', 'issues', 'Issued Books'],
                ['librarian-books.html', 'books', 'Books & Categories'],
                ['librarian-students.html', 'students', 'Students'],
                ['librarian-fines.html', 'fines', 'Fine Management'],
                ['librarian-reports.html', 'reports', 'Reports']
            ],
            STUDENT: [
                ['student-dashboard.html', 'dashboard', 'Dashboard'],
                ['student-books.html', 'books', 'Search Books'],
                ['student-my-books.html', 'my-books', 'My Books'],
                ['student-reservations.html', 'reservations', 'Reservations'],
                ['student-fines.html', 'fines', 'Fine'],
                ['student-notifications.html', 'notifications', 'Notifications']
            ]
        };

        const items = links[user.role] || [];
        const navHtml = items.map(([href, key, label]) =>
            `<a href="${href}" class="${key === activeKey ? 'active' : ''}">${escapeHtml(label)}</a>`
        ).join('');

        slot.innerHTML = `
            <div class="brand">
                <div class="mark">L</div>
                <div class="name">Harvard International Library <br><small>Library System</small></div>
            </div>
            <nav>${navHtml}</nav>
            <div class="sidebar-foot">
                <div class="user-chip">
                    <div class="avatar">${initials(user.name)}</div>
                    <div class="who">
                        <div class="n">${escapeHtml(user.name)}</div>
                        <div class="r">${escapeHtml(user.role)}</div>
                    </div>
                </div>
                <button class="logout-btn" onclick="LMS.logout()">Log out</button>
            </div>
        `;
    }

    return {
        saveSession, getToken, getUser, logout, requireAuth, homeForRole,
        get, post, put, patch, del,
        toast, fmtDate, fmtMoney, initials, escapeHtml, renderSidebar
    };
})();
