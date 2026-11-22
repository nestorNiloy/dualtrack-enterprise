/**
 * app.js — DualTrack v2
 * Main orchestrator: state, events, lifecycle.
 */

document.addEventListener('DOMContentLoaded', () => {

  /* ── Authentication gate ───────────────────────── */
  let authMode = 'login';
  const authScreen = document.getElementById('auth-screen');
  const authForm = document.getElementById('auth-form');
  const authError = document.getElementById('auth-error');
  const authHeading = document.getElementById('auth-heading');
  const authSubmit = document.getElementById('auth-submit');
  const authToggle = document.getElementById('auth-toggle');
  function setAuthMode(mode) {
    authMode = mode;
    authHeading.textContent = mode === 'login' ? 'Welcome back' : 'Create your account';
    authSubmit.textContent = mode === 'login' ? 'Sign in' : 'Create account';
    authToggle.textContent = mode === 'login' ? 'Create an account' : 'Sign in instead';
    document.getElementById('auth-toggle-label').textContent = mode === 'login' ? 'New here?' : 'Already registered?';
    document.getElementById('auth-password').autocomplete = mode === 'login' ? 'current-password' : 'new-password';
  }
  authToggle.addEventListener('click', () => setAuthMode(authMode === 'login' ? 'register' : 'login'));
  async function showWorkspace(user) {
    document.getElementById('signed-in-email').textContent = user.email;
    document.getElementById('sign-out').style.display = 'inline-flex';
    authScreen.style.display = 'none';
    await loadApplications();
  }
  document.getElementById('sign-out').addEventListener('click', async () => {
    try { await api.logout(); location.reload(); } catch (e) { showToast(e.message, 'error'); }
  });
  authForm.addEventListener('submit', async (e) => {
    e.preventDefault(); authError.textContent = '';
    const payload = { email: document.getElementById('auth-email').value.trim(), password: document.getElementById('auth-password').value };
    try {
      if (authMode === 'register') await api.register(payload);
      const user = await api.login(payload);
      await showWorkspace(user);
    } catch (err) { authError.textContent = err.message; }
  });
  document.getElementById('sign-out').style.display = 'none';

  /* ── State ──────────────────────────────────────── */
  let allApplications = [];
  let activeFilter    = 'ALL';
  let searchQuery     = '';
  let deleteTarget    = null; // { id, companyName }

  /* ── DOM refs ───────────────────────────────────── */
  const form          = document.getElementById('application-form');
  const submitBtn     = document.getElementById('submit-btn');
  const submitText    = document.getElementById('submit-text');
  const submitSpinner = document.getElementById('submit-spinner');
  const filterBar     = document.getElementById('filter-bar');
  const searchInput   = document.getElementById('search-input');
  const lastUpdated   = document.getElementById('last-updated');
  const refreshIcon   = document.getElementById('refresh-icon');
  const modal         = document.getElementById('delete-modal');
  const modalCompany  = document.getElementById('modal-company');
  const modalCancel   = document.getElementById('modal-cancel');
  const modalConfirm  = document.getElementById('modal-confirm');

  /* ── Load applications ──────────────────────────── */
  async function loadApplications() {
    refreshIcon.style.animation = 'spin 0.7s linear infinite';
    try {
      allApplications = await api.getAll();
      updateStats(allApplications);
      renderTable(allApplications, activeFilter, searchQuery);
      const now = new Date();
      lastUpdated.textContent = `Updated ${now.toLocaleTimeString('en-GB', { hour:'2-digit', minute:'2-digit', second:'2-digit' })}`;
    } catch (err) {
      console.error('Load error:', err);
      document.getElementById('empty-state').classList.remove('hidden');
      document.getElementById('empty-title').textContent = 'Could not reach backend';
      document.getElementById('empty-sub').textContent   = 'Check server is running at Railway';
      showToast('Cannot connect to the backend. Check Railway deployment.', 'error');
      ['stat-total','stat-applied','stat-interviewing','stat-offered'].forEach(id => setText(id, '—'));
    } finally {
      refreshIcon.style.animation = '';
    }
  }

  /* ── Submit form ────────────────────────────────── */
  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const companyName    = document.getElementById('company').value.trim();
    const positionTitle  = document.getElementById('position').value.trim();
    const status         = document.getElementById('status').value;
    const applicationDate = document.getElementById('applicationDate').value;
    const notes          = document.getElementById('notes').value.trim();

    if (!companyName)     { showToast('Company name is required.', 'warn'); document.getElementById('company').focus(); return; }
    if (!positionTitle)   { showToast('Position is required.', 'warn'); document.getElementById('position').focus(); return; }
    if (!applicationDate) { showToast('Application date is required.', 'warn'); document.getElementById('applicationDate').focus(); return; }

    submitBtn.disabled     = true;
    submitText.textContent = 'Saving…';
    submitSpinner.style.display = 'inline-block';

    try {
      await api.create({ companyName, positionTitle, status, applicationDate, notes });
      form.reset();
      document.getElementById('applicationDate').value = new Date().toISOString().split('T')[0];
      showToast(`${companyName} added successfully.`, 'success');
      await loadApplications();
    } catch (err) {
      console.error('Create error:', err);
      showToast(`Failed to save: ${err.message}`, 'error');
    } finally {
      submitBtn.disabled     = false;
      submitText.textContent = 'Add Application';
      submitSpinner.style.display = 'none';
    }
  });

  /* ── Delete: open confirm modal ─────────────────── */
  document.getElementById('applications-table-body').addEventListener('click', (e) => {
    const btn = e.target.closest('.delete-btn');
    if (!btn) return;
    const id  = btn.dataset.id;
    const app = allApplications.find(a => String(a.id) === String(id));
    deleteTarget = { id, companyName: app ? app.companyName : `#${id}` };
    modalCompany.textContent = deleteTarget.companyName;
    modal.classList.remove('hidden');
    modal.classList.add('flex');
  });

  /* ── Delete: cancel ─────────────────────────────── */
  modalCancel.addEventListener('click', () => {
    modal.classList.add('hidden');
    modal.classList.remove('flex');
    deleteTarget = null;
  });

  /* ── Delete: confirm ────────────────────────────── */
  modalConfirm.addEventListener('click', async () => {
    if (!deleteTarget) return;
    modal.classList.add('hidden');
    modal.classList.remove('flex');

    try {
      await api.remove(deleteTarget.id);
      showToast(`${deleteTarget.companyName} deleted.`, 'info');
      allApplications = allApplications.filter(a => String(a.id) !== String(deleteTarget.id));
      updateStats(allApplications);
      renderTable(allApplications, activeFilter, searchQuery);
    } catch (err) {
      showToast(`Delete failed: ${err.message}`, 'error');
    } finally {
      deleteTarget = null;
    }
  });

  /* ── Filter bar ─────────────────────────────────── */
  filterBar.addEventListener('click', (e) => {
    const btn = e.target.closest('[data-filter]');
    if (!btn) return;
    activeFilter = btn.dataset.filter;
    filterBar.querySelectorAll('[data-filter]').forEach(b => {
      b.classList.toggle('filter-active', b.dataset.filter === activeFilter);
      b.classList.toggle('filter-inactive', b.dataset.filter !== activeFilter);
    });
    renderTable(allApplications, activeFilter, searchQuery);
  });

  /* ── Search ─────────────────────────────────────── */
  searchInput.addEventListener('input', (e) => {
    searchQuery = e.target.value.trim();
    renderTable(allApplications, activeFilter, searchQuery);
  });

  /* ── Refresh button ─────────────────────────────── */
  document.getElementById('refresh-btn').addEventListener('click', loadApplications);

  /* ── Set today as default date ──────────────────── */
  document.getElementById('applicationDate').value = new Date().toISOString().split('T')[0];

  /* ── Initial load ───────────────────────────────── */
  loadApplications();

});
