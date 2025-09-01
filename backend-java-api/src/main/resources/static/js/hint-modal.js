import { markOffTask, markOnTask } from './activity-tracker.js';
import { getEditorCode } from './editor-init.js';

const HINT_HISTORY_KEY = "hintHistory";
const HINT_COUNTS_KEY  = "hintCounts";
const HINT_CAP_KEY     = "hintLevelCap";

const HINT_PRIORITY = ['conceptual', 'syntax', 'logic', 'step', 'reveal'];
const HINT_CAP = {
  conceptual: 100,
  syntax:    90,
  logic:     80,
  step:      70,
  reveal:    55,
};

// ========================
// TRACKING & HISTORY
// ========================
export function getHintCounts() {
  return JSON.parse(localStorage.getItem(HINT_COUNTS_KEY) || "{}");
}

export function getHintCapLevel() {
  return localStorage.getItem(HINT_CAP_KEY) || "";
}

function trackHintUsage(type) {
  const counts = getHintCounts();
  counts[type] = (counts[type] || 0) + 1;
  localStorage.setItem(HINT_COUNTS_KEY, JSON.stringify(counts));

  const prevCapPriority = HINT_PRIORITY.indexOf(getHintCapLevel());
  const nowPriority     = HINT_PRIORITY.indexOf(type);
  const newPriority     = Math.max(prevCapPriority, nowPriority);
  localStorage.setItem(HINT_CAP_KEY, HINT_PRIORITY[newPriority] || "");
}

export function resetHintCounts() {
  localStorage.setItem(HINT_COUNTS_KEY, "{}");
  localStorage.setItem(HINT_CAP_KEY, "");
}

function saveHintToHistory(type, hint) {
  const task    = JSON.parse(localStorage.getItem('selectedTask') || '{}');
  const history = JSON.parse(localStorage.getItem(HINT_HISTORY_KEY) || "[]");
  history.unshift({
    ts:        new Date().toISOString(),
    type,
    hint,
    taskTitle: task.title || '-'
  });
  if (history.length > 10) history.length = 10;
  localStorage.setItem(HINT_HISTORY_KEY, JSON.stringify(history));
}

function renderHintHistory() {
  const ul = document.getElementById('hintHistoryList');
  if (!ul) return;                         // guard: list may not exist
  ul.innerHTML = '';

  const history = JSON.parse(localStorage.getItem(HINT_HISTORY_KEY) || "[]");
  if (!history.length) {
    ul.innerHTML = "<li>no hints yet…</li>";
    return;
  }

  history.forEach(h => {
    const li = document.createElement('li');
    li.innerHTML = `
      <span class="font-bold text-indigo-700">${h.type}</span>
      <span class="text-gray-400 text-xs">${h.taskTitle}</span><br>
      <span class="font-mono">${h.hint}</span>
      <span class="block text-xs text-gray-400 mt-1">${new Date(h.ts).toLocaleString()}</span>
    `;
    ul.appendChild(li);
  });
}



// File: hint-modal.js

// File: src/main/resources/static/js/hint-modal.js
export function initHintModal() {
  const modal           = document.getElementById('hintModal');
  const closeBtn        = document.getElementById('closeHint');
  const showHistoryBtn  = document.getElementById('showHistoryBtn');
  const historyList     = document.getElementById('historyList');
  if (!modal || !closeBtn || !showHistoryBtn || !historyList) return;

  // start with history hidden
  historyList.classList.add('hidden');
  showHistoryBtn.textContent = 'Show History';

  // close the modal
  closeBtn.addEventListener('click', () => {
    modal.classList.add('hidden');
    window.isHintModalOpen = false;
    markOnTask();
  });

  // toggle the history panel
  showHistoryBtn.addEventListener('click', () => {
    const hidden = historyList.classList.toggle('hidden');
    showHistoryBtn.textContent = hidden ? 'Show History' : 'Hide History';
  });

  // populate history list
  renderHintHistory();
}




// ========================
// REQUEST HINT LOGIC
// ========================
export async function requestHintByType(type) {
  const modal    = document.getElementById('hintModal');
  const loader   = document.getElementById('hintLoader');
  const terminal = document.getElementById('hintTerminal');
  if (!modal || !loader || !terminal) return;  // guard: elements must exist

  const code = getEditorCode().trim();
  if (!code) {
    modal.classList.remove('hidden');
    loader.classList.add('hidden');
    terminal.innerText = "Editor cannot be empty. Please write your code before asking for a hint.";
    markOffTask();
    return;
  }

  trackHintUsage(type);
  window.isHintModalOpen = true;
  markOffTask();
  modal.classList.remove('hidden');
  loader.classList.remove('hidden');
  terminal.innerText = "Loading hint…";

  try {
    const task          = JSON.parse(localStorage.getItem('selectedTask') || '{}');
    const userId        = parseInt(localStorage.getItem('loggedInUserId') || "1", 10);
    const sessionNumber = parseInt(localStorage.getItem('sessionNumber')  || "1", 10);

    const res = await fetch('/api/hints', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, problemId: task.id, sessionNumber, type, userId })
    });

    if (!res.ok) throw new Error('Hint fetch failed');
    const contentType = res.headers.get('content-type') || '';
    let hintText = '';
    if (contentType.includes('application/json')) {
      const data = await res.json();
      hintText = data.hint || JSON.stringify(data) || 'No hint found.';
    } else {
      hintText = await res.text() || 'No hint found.';
    }

    loader.classList.add('hidden');
    terminal.innerText = hintText;
    saveHintToHistory(type, hintText);
    renderHintHistory();
  } catch (e) {
    loader.classList.add('hidden');
    terminal.innerText = "Failed to fetch hint.";
  }
}

// ========================
// BUTTON LOCK/UNLOCK
// ========================
export function lockHintsByTime() {
  document.querySelectorAll('.hint-btn').forEach(btn => {
    btn.disabled = (btn.dataset.type !== 'conceptual');
  });
}

export function unlockAllHints() {
  document.querySelectorAll('.hint-btn').forEach(btn => {
    btn.disabled = false;
  });
}

export function resetHintTracking() {
  resetHintCounts();
  unlockAllHints();
  renderHintHistory();
}
