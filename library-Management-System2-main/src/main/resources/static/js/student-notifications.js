(function () {
    const user = LMS.requireAuth(['STUDENT']);
    if (!user) return;

    LMS.renderSidebar('notifications');

    async function load() {
        const list = document.getElementById('notif-list');
        try {
            const notifications = await LMS.get('/api/notifications');
            if (!notifications.length) {
                list.innerHTML = '<p class="loading-row">No notifications yet.</p>';
                return;
            }
            list.innerHTML = notifications.map(n => `
                <div class="notif-item ${n.read ? 'read' : ''}">
                    <div class="dot"></div>
                    <div class="body">
                        <div class="msg">${LMS.escapeHtml(n.message)}</div>
                        <div class="time">${new Date(n.createdDate).toLocaleString('en-IN')}</div>
                    </div>
                </div>
            `).join('');
        } catch (err) {
            list.innerHTML = '';
            LMS.toast(err.message, 'error');
        }
    }

    document.getElementById('mark-read-btn').addEventListener('click', async () => {
        try {
            await LMS.post('/api/notifications/mark-all-read');
            LMS.toast('All notifications marked as read', 'success');
            load();
        } catch (err) {
            LMS.toast(err.message, 'error');
        }
    });

    load();
})();
