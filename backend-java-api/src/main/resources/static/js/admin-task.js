// Single module for both list and form
const apiBase = '/api/problems';

// ————————————————— LISTING —————————————————

export async function fetchTasks() {
  const res = await fetch(apiBase);
  if (!res.ok) throw new Error('Failed to load tasks');
  return await res.json();
}

export function renderTasks(list) {
  const tbody = document.getElementById('tasks-body');
  tbody.innerHTML = '';
  list.forEach(p => {
    const tr = document.createElement('tr');
    tr.className = 'border-b';
    tr.innerHTML = `
      <td class="px-4 py-2">${p.id}</td>
      <td class="px-4 py-2">${p.title}</td>
      <td class="px-4 py-2">${p.description}</td>
      <td class="px-4 py-2">${p.expectedOutput}</td>
      <td class="px-4 py-2">${p.category}</td>
      <td class="px-4 py-2">${p.subTopic}</td>
      <td class="px-4 py-2 space-x-2">
        <button data-id="${p.id}" class="edit-btn bg-yellow-500 text-white px-3 py-1 rounded">Edit</button>
        <button data-id="${p.id}" class="del-btn bg-red-500 text-white px-3 py-1 rounded">Delete</button>
      </td>`;
    tbody.append(tr);
  });

  document.querySelectorAll('.edit-btn').forEach(btn =>
    btn.addEventListener('click', () => {
      window.location.href = `/admin/task-form?id=${btn.dataset.id}`;
    })
  );
  document.querySelectorAll('.del-btn').forEach(btn =>
    btn.addEventListener('click', async () => {
      if (!confirm('Really delete this task?')) return;
      const res = await fetch(`${apiBase}/${btn.dataset.id}`, { method: 'DELETE' });
      if (!res.ok) return alert('Delete failed');
      loadTasks();
    })
  );
}

async function loadTasks() {
  try {
    const list = await fetchTasks();
    renderTasks(list);
  } catch (e) {
    console.error(e);
    alert('Could not load tasks');
  }
}

// ————————————————— FORM —————————————————

async function loadForm() {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');
  if (!id) return;

  document.getElementById('form-title').textContent = 'Edit Task';
  const res = await fetch(`${apiBase}/${id}`);
  if (!res.ok) return alert('Failed to load task');
  const t = await res.json();

  document.getElementById('taskId').value           = t.id;
  document.getElementById('title').value            = t.title;
  document.getElementById('description').value      = t.description;
  document.getElementById('expectedOutput').value   = t.expectedOutput;
  document.getElementById('category').value         = t.category;
  document.getElementById('subTopic').value         = t.subTopic;
}

async function handleFormSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('taskId').value;

  const payload = {
    title:          document.getElementById('title').value,
    description:    document.getElementById('description').value,
    expectedOutput: document.getElementById('expectedOutput').value,
    category:       document.getElementById('category').value,
    subTopic:       document.getElementById('subTopic').value
  };

  const url    = id ? `${apiBase}/${id}` : apiBase;
  const method = id ? 'PUT' : 'POST';

  const res = await fetch(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify(id ? { id: Number(id), ...payload } : payload)
  });

  if (!res.ok) {
    alert('Save failed');
  } else {
    window.location.href = '/admin/task-management';
  }
}

// ————————————— BOOTSTRAP —————————————

document.addEventListener('DOMContentLoaded', () => {
  // Listing page?
  if (document.getElementById('tasks-body')) {
    loadTasks();
    document.getElementById('new-task-btn')
            .addEventListener('click', () => {
      window.location.href = '/admin/task-form';
    });
  }

  // Form page?
  if (document.getElementById('taskForm')) {
    loadForm();
    document.getElementById('taskForm')
            .addEventListener('submit', handleFormSubmit);
    document.getElementById('cancelBtn')
            .addEventListener('click', () => {
      window.location.href = '/admin/task-management';
    });
  }
});
