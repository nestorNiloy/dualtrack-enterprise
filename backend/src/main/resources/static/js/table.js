/**
 * Safe DOM rendering for the applications table and server-side pagination.
 */
function renderTable(apps) {
  const tbody = document.getElementById('applications-table-body');
  const emptyState = document.getElementById('empty-state');
  const countEl = document.getElementById('record-count');
  tbody.replaceChildren();
  countEl.textContent = `(${state.totalElements})`;

  if (!apps.length) {
    emptyState.classList.remove('hidden');
    document.getElementById('empty-title').textContent = state.totalElements ? 'No matches on this page' : 'No applications yet';
    document.getElementById('empty-sub').textContent = state.totalElements ? 'Try another filter or search term.' : 'Add your first application using the form.';
    return;
  }
  emptyState.classList.add('hidden');

  apps.forEach(app => {
    const cfg = STATUS_CFG[app.status] || STATUS_CFG.APPLIED;
    const tr = document.createElement('tr');
    tr.className = 'app-row';
    tr.style.borderBottom = '1px solid rgba(51,65,85,0.25)';

    const cell = (value, css = '') => {
      const td = document.createElement('td');
      td.style.cssText = `padding:14px 16px;${css}`;
      td.textContent = value ?? '—';
      tr.appendChild(td);
      return td;
    };

    cell(String(app.id), 'font-family:"DM Mono",monospace;font-size:11px;color:#64748b;');
    cell(app.companyName, 'font-weight:600;color:#e2e8f0;');
    cell(app.positionTitle, 'color:#cbd5e1;');

    const statusCell = cell('');
    const badge = document.createElement('span');
    badge.textContent = cfg.label;
    badge.style.cssText = `display:inline-flex;border-radius:999px;padding:3px 10px;font:11px "DM Mono",monospace;background:${cfg.bg};border:1px solid ${cfg.border};color:${cfg.text};`;
    statusCell.appendChild(badge);

    cell(formatDate(app.applicationDate), 'font:11px "DM Mono",monospace;color:#94a3b8;white-space:nowrap;');
    const notes = cell(app.notes || '', 'font-size:12px;color:#64748b;max-width:180px;');
    notes.style.overflowWrap = 'anywhere';

    const actions = cell('');
    actions.style.whiteSpace = 'nowrap';
    actions.style.textAlign = 'center';
    actions.append(
      actionButton('Edit', 'edit-btn', app.id),
      actionButton('History', 'history-btn', app.id),
      actionButton('Delete', 'delete-btn', app.id)
    );
    tbody.appendChild(tr);
  });
}

function actionButton(label, className, id) {
  const button = document.createElement('button');
  button.type = 'button';
  button.className = className;
  button.dataset.id = id;
  button.textContent = label;
  button.style.cssText = 'margin:2px;padding:5px 7px;border:1px solid rgba(100,116,139,.35);border-radius:6px;background:rgba(30,41,59,.65);color:#cbd5e1;font-size:11px;cursor:pointer;';
  if (className === 'delete-btn') button.style.color = '#fb7185';
  return button;
}
