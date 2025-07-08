// File: static/js/main.js

document.addEventListener('DOMContentLoaded', () => {
  // 1) Fetch & render group summary
  fetch('/api/admin/summary/group')
    .then(res => res.json())
    .then(json => renderGroupSummary(json.data));

  // 2) Setup user‐summary: search & export
  const searchInput = document.getElementById('userSearch');
  const exportBtn   = document.getElementById('exportCsvBtn');

  async function loadUserSummary() {
    const q = searchInput.value.trim();
    const params = new URLSearchParams();
    if (q) params.set('search', q);
    const url = '/api/admin/summary/users' + (params.toString() ? `?${params}` : '');
    const res = await fetch(url);
    const json = await res.json();
    renderUserTable(json.data);
  }

  // initial load
  loadUserSummary();

  // reload on search input
  searchInput.addEventListener('input', loadUserSummary);

  // export CSV, preserving current search
  exportBtn.addEventListener('click', () => {
    const q = searchInput.value.trim();
    const params = new URLSearchParams({ export: 'true' });
    if (q) params.set('search', q);
    window.location.href = `/api/admin/summary/users?${params}`;
  });
});

/**
 * Renders the group‐level summary cards.
 * Expects an object: { overallAvgScore:…, totalSubmissions:…, … }
 */
function renderGroupSummary(data) {
  const container = document.getElementById('group-summary');
  container.innerHTML = '';
  Object.entries(data).forEach(([key, val]) => {
    const card = document.createElement('div');
    card.className = 'bg-white p-4 rounded shadow';
    card.innerHTML = `
      <div class="text-gray-500">${key}</div>
      <div class="text-2xl font-bold">${Number(val).toFixed(1)}</div>
    `;
    container.append(card);
  });
}

/**
 * Populates the per‐user summary table.
 * Expects an array of UserSummaryDto:
 * [{ userId, username, totalSubmissions, avgScore, totalHints, totalFailedRuns }, …]
 */
function renderUserTable(list) {
  const tbody = document.getElementById('user-summary-body');
  tbody.innerHTML = '';
  list.forEach(u => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td class="p-2">${u.userId}</td>
      <td class="p-2">${u.username}</td>
      <td class="p-2">${u.totalSubmissions}</td>
      <td class="p-2">${u.avgScore.toFixed(1)}</td>
      <td class="p-2">${u.totalHints}</td>
      <td class="p-2">${u.totalFailedRuns}</td>
    `;
    tbody.append(tr);
  });
}
