/**
 * DualTrack Enterprise dashboard controller.
 * Server-side filtering, pagination and statistics; CRUD, edit and status history.
 */
const state = {
  page: 0, size: 10, totalElements: 0, totalPages: 0,
  activeFilter: 'ALL', searchQuery: '', editingId: null,
  applications: [], deleteTarget: null
};

document.addEventListener('DOMContentLoaded', () => {
  const byId = id => document.getElementById(id);
  const authScreen = byId('auth-screen');
  const authForm = byId('auth-form');
  const authError = byId('auth-error');
  let authMode = 'login';

  const form = byId('application-form');
  const submitBtn = byId('submit-btn');
  const submitText = byId('submit-text');
  const modal = byId('delete-modal');

  function setAuthMode(mode) {
    authMode = mode;
    byId('auth-heading').textContent = mode === 'login' ? 'Welcome back' : 'Create your account';
    byId('auth-submit').textContent = mode === 'login' ? 'Sign in' : 'Create account';
    byId('auth-toggle').textContent = mode === 'login' ? 'Create an account' : 'Sign in instead';
    byId('auth-toggle-label').textContent = mode === 'login' ? 'New here?' : 'Already registered?';
    byId('auth-password').autocomplete = mode === 'login' ? 'current-password' : 'new-password';
  }

  async function showWorkspace(user) {
    byId('signed-in-email').textContent = user.email;
    byId('sign-out').style.display = 'inline-flex';
    authScreen.style.display = 'none';
    await refreshDashboard();
  }

  authForm.addEventListener('submit', async event => {
    event.preventDefault();
    authError.textContent = '';
    const payload = {
      email: byId('auth-email').value.trim(),
      password: byId('auth-password').value
    };
    try {
      if (authMode === 'register') await api.register(payload);
      const user = await api.login(payload);
      await showWorkspace(user);
    } catch (error) { authError.textContent = error.message; }
  });
  byId('auth-toggle').addEventListener('click', () => setAuthMode(authMode === 'login' ? 'register' : 'login'));
  byId('sign-out').style.display = 'none';
  byId('sign-out').addEventListener('click', async () => {
    try { await api.logout(); location.reload(); }
    catch (error) { showToast(error.message, 'error'); }
  });

  function readForm() {
    return {
      companyName: byId('company').value.trim(),
      positionTitle: byId('position').value.trim(),
      status: byId('status').value,
      applicationDate: byId('applicationDate').value,
      notes: byId('notes').value.trim()
    };
  }

  function resetForm() {
    state.editingId = null;
    form.reset();
    byId('applicationDate').value = new Date().toISOString().slice(0, 10);
    byId('form-heading').textContent = 'New Application';
    submitText.textContent = 'Add Application';
    byId('cancel-edit').classList.add('hidden');
  }

  async function refreshDashboard() {
    const refreshIcon = byId('refresh-icon');
    refreshIcon.style.animation = 'spin 0.7s linear infinite';
    try {
      const [pageData, stats] = await Promise.all([
        api.list({ page: state.page, size: state.size, status: state.activeFilter, q: state.searchQuery }),
        api.stats()
      ]);
      state.applications = pageData.content || [];
      state.totalElements = pageData.totalElements || 0;
      state.totalPages = pageData.totalPages || 0;
      renderTable(state.applications);
      renderStats(stats);
      renderPagination(pageData);
      byId('last-updated').textContent = `Updated ${new Date().toLocaleTimeString('en-GB')}`;
    } catch (error) {
      console.error(error);
      showToast(error.message || 'Could not load your applications.', 'error');
      byId('empty-title').textContent = 'Could not load applications';
      byId('empty-sub').textContent = 'Check your connection and try refreshing.';
      byId('empty-state').classList.remove('hidden');
    } finally { refreshIcon.style.animation = ''; }
  }

  function renderStats(stats) {
    setText('stat-total', stats.total);
    setText('stat-applied', stats.applied);
    setText('stat-interviewing', stats.interviewing);
    setText('stat-offered', stats.offered);
    setText('stat-rejected', stats.rejected);
  }

  function renderPagination(pageData) {
    let nav = byId('pagination');
    if (!nav) {
      nav = document.createElement('div');
      nav.id = 'pagination';
      nav.style.cssText = 'display:flex;align-items:center;justify-content:space-between;gap:12px;padding:14px 18px;border-top:1px solid rgba(51,65,85,.35);color:#94a3b8;font-size:12px;flex-wrap:wrap;';
      byId('empty-state').before(nav);
    }
    const first = pageData.totalElements === 0 ? 0 : pageData.page * pageData.size + 1;
    const last = Math.min((pageData.page + 1) * pageData.size, pageData.totalElements);
    nav.replaceChildren();
    const info = document.createElement('span');
    info.textContent = `Showing ${first}–${last} of ${pageData.totalElements}`;
    const controls = document.createElement('div');
    controls.style.cssText = 'display:flex;align-items:center;gap:8px;';
    const sizeLabel = document.createElement('label');
    sizeLabel.textContent = 'Rows ';
    const sizeSelect = document.createElement('select');
    sizeSelect.style.cssText = 'padding:5px;background:#0f172a;color:#cbd5e1;border:1px solid #334155;border-radius:6px;';
    [10, 20, 50].forEach(size => {
      const option = document.createElement('option');
      option.value = size; option.textContent = size; option.selected = state.size === size;
      sizeSelect.appendChild(option);
    });
    sizeSelect.addEventListener('change', () => { state.size = Number(sizeSelect.value); state.page = 0; refreshDashboard(); });
    sizeLabel.appendChild(sizeSelect);
    const prev = paginationButton('Previous', state.page === 0, () => { state.page--; refreshDashboard(); });
    const pageText = document.createElement('span');
    pageText.textContent = `Page ${pageData.totalPages ? pageData.page + 1 : 0} of ${pageData.totalPages}`;
    const next = paginationButton('Next', pageData.last || pageData.totalPages === 0, () => { state.page++; refreshDashboard(); });
    controls.append(sizeLabel, prev, pageText, next);
    nav.append(info, controls);
  }

  function paginationButton(label, disabled, onClick) {
    const button = document.createElement('button');
    button.type = 'button'; button.textContent = label; button.disabled = disabled;
    button.style.cssText = 'padding:6px 10px;border:1px solid #334155;border-radius:6px;background:#0f172a;color:#cbd5e1;cursor:pointer;';
    if (disabled) button.style.opacity = '.4';
    button.addEventListener('click', onClick);
    return button;
  }

  form.addEventListener('submit', async event => {
    event.preventDefault();
    const payload = readForm();
    if (!payload.companyName || !payload.positionTitle || !payload.applicationDate) {
      showToast('Company, position and application date are required.', 'warn'); return;
    }
    submitBtn.disabled = true;
    submitText.textContent = state.editingId ? 'Updating…' : 'Saving…';
    try {
      if (state.editingId) {
        await api.update(state.editingId, payload);
        showToast('Application updated.', 'success');
      } else {
        await api.create(payload);
        showToast('Application added.', 'success');
      }
      resetForm();
      state.page = 0;
      await refreshDashboard();
    } catch (error) { showToast(error.message, 'error'); }
    finally { submitBtn.disabled = false; submitText.textContent = state.editingId ? 'Update Application' : 'Add Application'; }
  });

  byId('cancel-edit').addEventListener('click', resetForm);

  byId('applications-table-body').addEventListener('click', async event => {
    const button = event.target.closest('button[data-id]');
    if (!button) return;
    const id = Number(button.dataset.id);
    const app = state.applications.find(item => item.id === id);
    if (!app) return;

    if (button.classList.contains('edit-btn')) {
      state.editingId = id;
      byId('company').value = app.companyName;
      byId('position').value = app.positionTitle;
      byId('status').value = app.status;
      byId('applicationDate').value = app.applicationDate;
      byId('notes').value = app.notes || '';
      byId('form-heading').textContent = 'Edit Application';
      submitText.textContent = 'Update Application';
      byId('cancel-edit').classList.remove('hidden');
      byId('company').focus();
      return;
    }
    if (button.classList.contains('history-btn')) {
      await showHistory(app);
      return;
    }
    if (button.classList.contains('delete-btn')) {
      state.deleteTarget = app;
      byId('modal-company').textContent = app.companyName;
      modal.classList.remove('hidden'); modal.classList.add('flex');
    }
  });

  async function showHistory(app) {
    const dialog = byId('history-modal');
    const content = byId('history-content');
    content.replaceChildren();
    const heading = document.createElement('h3');
    heading.textContent = `Status history — ${app.companyName}`;
    heading.style.cssText = 'font-size:16px;font-weight:700;margin-bottom:14px;color:#e2e8f0;';
    content.appendChild(heading);
    try {
      const entries = await api.history(app.id);
      if (!entries.length) {
        const empty = document.createElement('p'); empty.textContent = 'No status changes recorded yet.'; content.appendChild(empty);
      } else {
        entries.forEach(entry => {
          const item = document.createElement('p');
          const from = entry.oldStatus ? STATUS_CFG[entry.oldStatus]?.label || entry.oldStatus : 'Created';
          const to = STATUS_CFG[entry.newStatus]?.label || entry.newStatus;
          item.textContent = `${from} → ${to} · ${new Date(entry.changedAt).toLocaleString()}`;
          item.style.cssText = 'padding:10px 0;border-bottom:1px solid #334155;color:#cbd5e1;font-size:13px;';
          content.appendChild(item);
        });
      }
      dialog.classList.remove('hidden'); dialog.classList.add('flex');
    } catch (error) { showToast(error.message, 'error'); }
  }

  byId('history-close').addEventListener('click', () => {
    byId('history-modal').classList.add('hidden');
    byId('history-modal').classList.remove('flex');
  });

  byId('modal-cancel').addEventListener('click', closeDeleteModal);
  function closeDeleteModal() { modal.classList.add('hidden'); modal.classList.remove('flex'); state.deleteTarget = null; }
  byId('modal-confirm').addEventListener('click', async () => {
    if (!state.deleteTarget) return;
    const target = state.deleteTarget;
    try {
      await api.remove(target.id);
      showToast(`${target.companyName} deleted.`, 'info');
      closeDeleteModal();
      if (state.applications.length === 1 && state.page > 0) state.page--;
      await refreshDashboard();
    } catch (error) { showToast(error.message, 'error'); }
  });

  byId('filter-bar').addEventListener('click', event => {
    const button = event.target.closest('[data-filter]');
    if (!button) return;
    state.activeFilter = button.dataset.filter;
    state.page = 0;
    byId('filter-bar').querySelectorAll('[data-filter]').forEach(item => {
      item.classList.toggle('filter-active', item === button);
      item.classList.toggle('filter-inactive', item !== button);
    });
    refreshDashboard();
  });

  let searchTimer;
  byId('search-input').addEventListener('input', event => {
    clearTimeout(searchTimer);
    state.searchQuery = event.target.value;
    searchTimer = setTimeout(() => { state.page = 0; refreshDashboard(); }, 250);
  });

  byId('refresh-btn').addEventListener('click', refreshDashboard);
  byId('applicationDate').value = new Date().toISOString().slice(0, 10);

  // Restore an existing session without exposing protected endpoints to anonymous users.
  api.me().then(showWorkspace).catch(() => { authScreen.style.display = 'flex'; });
});
