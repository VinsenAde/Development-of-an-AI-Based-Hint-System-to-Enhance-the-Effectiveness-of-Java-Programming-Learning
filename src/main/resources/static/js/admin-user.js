// File: src/main/resources/static/js/admin-user.js

/** Fetch all users from the admin API **/
async function fetchUsers() {
  const res = await fetch('/api/admin/users');
  if (!res.ok) throw new Error('Cannot load users');
  return await res.json(); // returns [ { id, username, role, fullName }, … ]
}
/** Render table rows for each non-admin user **/
function renderUsers(users) {
  const tbody = document.getElementById('students-body');
  tbody.innerHTML = '';

  users
    .filter(u => u.role !== 'ADMIN')
    .forEach(u => {
      const tr = document.createElement('tr');
      tr.className = 'border-b hover:bg-gray-50';
      tr.innerHTML = `
        <td class="px-4 py-2">${u.id}</td>
        <td class="px-4 py-2">${u.fullName || '–'}</td>
        <td class="px-4 py-2">${u.username}</td>
        <td class="px-4 py-2">${u.role}</td>
        <td class="px-4 py-2 space-x-2">
          <button data-id="${u.id}" class="summary-btn bg-blue-500 text-white px-2 py-1 rounded">
            Summary
          </button>
          <button data-id="${u.id}" class="print-btn bg-indigo-500 text-white px-2 py-1 rounded">
            Print
          </button>
          <button data-id="${u.id}" class="delete-btn bg-red-500 text-white px-2 py-1 rounded">
            Delete
          </button>
        </td>
      `;
      tbody.append(tr);
    });

  // Export all users as CSV
  const exportAllBtn = document.createElement('button');
  exportAllBtn.textContent = 'Export All Users CSV';
  exportAllBtn.className = 'bg-green-500 text-white px-4 py-2 rounded my-4';
  exportAllBtn.addEventListener('click', () => {
    window.open('/api/admin/summary/users?export=true', '_blank');
  });

  document.body.appendChild(exportAllBtn);

  // 1) Summary → redirect into summary.html with ?userId=…
  document.querySelectorAll('.summary-btn').forEach(btn =>
    btn.addEventListener('click', () => {
      const id = btn.dataset.id;
      // Redirect to the static summary.html, passing the userId in the query string
      window.location.href = `/summary.html?userId=${id}`;
    })
  );

  // 2) Print → open CSV export in a new tab for individual users
  document.querySelectorAll('.print-btn').forEach(btn =>
    btn.addEventListener('click', () => {
      const id = btn.dataset.id;
      window.open(
        `/api/admin/summary/users?export=true&search=&userId=${id}`,
        '_blank'
      );
    })
  );

  // 3) Delete → call DELETE, then reload the list
  document.querySelectorAll('.delete-btn').forEach(btn =>
    btn.addEventListener('click', async () => {
      if (!confirm('Really delete this user?')) return;
      const res = await fetch(`/api/admin/users/${btn.dataset.id}`, {
        method: 'DELETE'
      });
      if (res.ok) {
        load();
      } else {
        alert('Failed to delete user');
      }
    })
  );
}


/** Bootstraps the page on DOMContentLoaded **/
async function load() {
  try {
    const users = await fetchUsers();
    renderUsers(users);
  } catch (e) {
    console.error(e);
    alert('Error loading users: ' + e.message);
  }
}

document.addEventListener('DOMContentLoaded', load);
