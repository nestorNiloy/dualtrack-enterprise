// Same-origin API client using the Spring Security session and CSRF cookie.
const API_BASE = '/api';

function csrfToken() {
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : '';
}

async function request(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const headers = {
    Accept: 'application/json',
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(options.headers || {})
  };

  if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
    const token = csrfToken();
    if (token) headers['X-XSRF-TOKEN'] = token;
  }

  const response = await fetch(`${API_BASE}${path}`, {
    credentials: 'same-origin',
    ...options,
    method,
    headers
  });

  if (response.status === 204) return null;

  const raw = await response.text();
  let data = null;
  try { data = raw ? JSON.parse(raw) : null; }
  catch { data = raw; }

  if (!response.ok) {
    const fieldMessages = data?.fields ? Object.entries(data.fields).map(([field, message]) => `${field}: ${message}`).join('; ') : '';
    const message = data?.message || fieldMessages || data?.error || `Request failed (${response.status})`;
    throw new Error(message);
  }
  return data;
}

const api = {
  csrf: () => request('/auth/csrf'),
  me: () => request('/auth/me'),
  async login(payload) {
    await api.csrf();
    return request('/auth/login', { method: 'POST', body: JSON.stringify(payload) });
  },
  async register(payload) {
    await api.csrf();
    return request('/auth/register', { method: 'POST', body: JSON.stringify(payload) });
  },
  logout: () => request('/auth/logout', { method: 'POST' }),
  list({ page = 0, size = 10, status = '', q = '' } = {}) {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (status && status !== 'ALL') params.set('status', status);
    if (q.trim()) params.set('q', q.trim());
    return request(`/applications?${params.toString()}`);
  },
  create: payload => request('/applications', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id, payload) => request(`/applications/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  remove: id => request(`/applications/${id}`, { method: 'DELETE' }),
  stats: () => request('/applications/stats'),
  history: id => request(`/applications/${id}/history`)
};
