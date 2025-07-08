// File: src/main/resources/static/js/activity-tracker.js

let totalStart    = Date.now();
let onTaskStart   = Date.now();
let offTaskStart  = null;
let timerInterval = null;  // <-- track our interval ID

export let onTaskTime  = 0;
export let offTaskTime = 0;

// ==== IDLE DETECTION ====
let idleTimeout = null;
const idleThreshold = 30 * 1000; // 30 detik idle = off task

function startIdleTracking() {
  function resetIdle() {
    if (idleTimeout) clearTimeout(idleTimeout);
    // jika hint modal terbuka, jangan markOnTask
    if (!window.isHintModalOpen) {
      markOnTask();
    }
    idleTimeout = setTimeout(markOffTask, idleThreshold);
  }
  ['mousemove','keydown','mousedown','touchstart'].forEach(ev =>
    window.addEventListener(ev, resetIdle, true)
  );
  resetIdle();
}

// ==== PAGE VISIBILITY (switch tab/minimize) ====
document.addEventListener("visibilitychange", () => {
  document.hidden ? markOffTask() : markOnTask();
});

// konversi ms ke mm:ss
function format(ms) {
  const s  = Math.floor(ms / 1000);
  const m  = Math.floor(s / 60);
  const ss = s % 60;
  return String(m).padStart(2, '0') + ':' + String(ss).padStart(2, '0');
}

export function markOnTask() {
  if (!onTaskStart) {
    onTaskStart = Date.now();
    if (offTaskStart) {
      offTaskTime += Date.now() - offTaskStart;
      offTaskStart = null;
    }
  }
}

export function markOffTask() {
  if (!offTaskStart) {
    offTaskStart = Date.now();
    if (onTaskStart) {
      onTaskTime += Date.now() - onTaskStart;
      onTaskStart = null;
    }
  }
}

export function updateTimers() {
  const now   = Date.now();
  const total = now - totalStart;
  const on    = onTaskTime + (onTaskStart ? (now - onTaskStart) : 0);
  const off   = offTaskTime + (offTaskStart ? (now - offTaskStart) : 0);

  document.getElementById('totalTimer').innerText  = format(total);
  document.getElementById('onTaskTimer').innerText = format(on);
  document.getElementById('offTaskTimer').innerText = format(off);
}

export function startActivityTracking() {
  const editor = document.getElementById('code-editor');
  editor.addEventListener('focus', markOnTask);
  editor.addEventListener('blur',  markOffTask);

  // store interval ID so we can clear it later
  timerInterval = setInterval(updateTimers, 1000);

  startIdleTracking();
}

/** Stops the recurring timer updates */
export function stopActivityTracking() {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }
}

// untuk backend: ambil detik, bukan ms
export function getOnTaskTimeSeconds() {
  return Math.floor(onTaskTime / 1000);
}
export function getOffTaskTimeSeconds() {
  return Math.floor(offTaskTime / 1000);
}
