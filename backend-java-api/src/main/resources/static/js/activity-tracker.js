// File: src/main/resources/static/js/activity-tracker.js

let totalStart = Date.now();
let onTaskStart = Date.now();
let offTaskStart = null;
let timerInterval = null; // <-- track our interval ID

// Exported variables to hold accumulated times (in milliseconds)
export let onTaskTime = 0;
export let offTaskTime = 0;

let stoppedPermanently = false; // NEW: Flag to stop all tracking after successful submission

// ==== IDLE DETECTION ====
let idleTimeout = null;
const idleThreshold = 30 * 1000; // 30 detik idle = off task

function startIdleTracking() {
    function resetIdle() {
        if (idleTimeout) clearTimeout(idleTimeout);
        // NEW: Don't mark on-task if permanently stopped
        if (!stoppedPermanently && !window.isHintModalOpen) {
            markOnTask();
        }
        // NEW: Don't set new timeout if permanently stopped
        if (!stoppedPermanently) {
            idleTimeout = setTimeout(markOffTask, idleThreshold);
        }
    }

    // NEW: Add/remove listeners based on stoppedPermanently flag
    const events = ['mousemove', 'keydown', 'mousedown', 'touchstart'];
    events.forEach(ev => {
        window.addEventListener(ev, resetIdle, true);
    });
    // Store event listeners for later removal
    window.activityListeners = events.map(ev => ({ event: ev, handler: resetIdle, useCapture: true }));

    resetIdle();
}

// NEW: Function to stop idle tracking
function stopIdleTracking() {
    if (idleTimeout) {
        clearTimeout(idleTimeout);
        idleTimeout = null;
    }
    if (window.activityListeners) {
        window.activityListeners.forEach(({ event, handler, useCapture }) => {
            window.removeEventListener(event, handler, useCapture);
        });
        window.activityListeners = null;
    }
}

// ==== PAGE VISIBILITY (switch tab/minimize) ====
document.addEventListener("visibilitychange", handleVisibilityChange);

function handleVisibilityChange() {
    // NEW: Only react to visibility changes if not permanently stopped
    if (!stoppedPermanently) {
        document.hidden ? markOffTask() : markOnTask();
    }
}

// konversi ms ke mm:ss
function format(ms) {
    const s = Math.floor(ms / 1000);
    const m = Math.floor(s / 60);
    const ss = s % 60;
    return String(m).padStart(2, '0') + ':' + String(ss).padStart(2, '0');
}

export function markOnTask() {
    // NEW: Don't mark on-task if permanently stopped
    if (stoppedPermanently) return;

    if (!onTaskStart) {
        onTaskStart = Date.now();
        if (offTaskStart) {
            offTaskTime += Date.now() - offTaskStart;
            offTaskStart = null;
        }
    }
}

export function markOffTask() {
    // NEW: Don't mark off-task if permanently stopped
    if (stoppedPermanently) return;

    if (!offTaskStart) {
        offTaskStart = Date.now();
        if (onTaskStart) {
            onTaskTime += Date.now() - onTaskStart;
            onTaskStart = null;
        }
    }
}

export function updateTimers() {
    // NEW: If permanently stopped, just clear interval and return
    if (stoppedPermanently) {
        if (timerInterval) {
            clearInterval(timerInterval);
            timerInterval = null;
        }
        return;
    }

    const now = Date.now();
    const total = now - totalStart;
    const on = onTaskTime + (onTaskStart ? (now - onTaskStart) : 0);
    const off = offTaskTime + (offTaskStart ? (now - offTaskStart) : 0);

    document.getElementById('totalTimer').innerText = format(total);
    document.getElementById('onTaskTimer').innerText = format(on);
    document.getElementById('offTaskTimer').innerText = format(off);
}

// NEW: Store editor event listeners for later removal
let editorFocusListener = null;
let editorBlurListener = null;

export function startActivityTracking() {
    // NEW: Only start if not permanently stopped and not already running
    if (stoppedPermanently || timerInterval) {
        return;
    }

    const editor = document.getElementById('code-editor');

    // NEW: Assign and store listeners
    editorFocusListener = markOnTask;
    editorBlurListener = markOffTask;

    editor.addEventListener('focus', editorFocusListener);
    editor.addEventListener('blur', editorBlurListener);

    timerInterval = setInterval(updateTimers, 1000);
    startIdleTracking();
}

/** Stops the recurring timer updates (used for temporary pauses, like before final calculation) */
export function stopActivityTracking() {
    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
    }
}

// NEW: Function to stop all timers and freeze time values
export function stopActivityTrackingAndReserveTime() {
    if (stoppedPermanently) return; // Already stopped

    // Ensure all current pending time is added to accumulated totals
    if (onTaskStart) {
        onTaskTime += Date.now() - onTaskStart;
        onTaskStart = null;
    }
    if (offTaskStart) {
        offTaskTime += Date.now() - offTaskStart;
        offTaskStart = null;
    }

    // Stop all intervals and clear flags
    stopActivityTracking(); // Clears the main update interval
    stopIdleTracking(); // Clears idle timeout and event listeners for activity
    stoppedPermanently = true; // Set the permanent stop flag

    // Remove editor listeners to prevent further time accumulation
    const editor = document.getElementById('code-editor');
    if (editor && editorFocusListener) {
        editor.removeEventListener('focus', editorFocusListener);
        editorFocusListener = null;
    }
    if (editor && editorBlurListener) {
        editor.removeEventListener('blur', editorBlurListener);
        editorBlurListener = null;
    }
    document.removeEventListener("visibilitychange", handleVisibilityChange); // Remove visibility listener

    console.log('Activity tracking permanently stopped and time reserved.');
    console.log(`Final On-Task Time: ${getOnTaskTimeSeconds()}s`);
    console.log(`Final Off-Task Time: ${getOffTaskTimeSeconds()}s`);
    // Optional: Update UI one last time to reflect final state
    updateTimers();
}


// Reset all tracking to 0 and allow restart (e.g., for "Clear" button)
export function resetActivityTracking() {
    stopActivityTracking(); // Stop main timer
    stopIdleTracking(); // Stop idle tracking
    stoppedPermanently = false; // Allow tracking to restart

    totalStart = Date.now();
    onTaskStart = Date.now();
    offTaskStart = null;

    onTaskTime = 0;
    offTaskTime = 0;

    // Reset UI
    document.getElementById('totalTimer').innerText = format(0);
    document.getElementById('onTaskTimer').innerText = format(0);
    document.getElementById('offTaskTimer').innerText = format(0);

    // Re-initialize tracking (re-add listeners, start interval)
    startActivityTracking();
}


// untuk backend: ambil detik, bukan ms
export function getOnTaskTimeSeconds() {
    // Ensure current pending time is accounted for before returning
    const now = Date.now();
    const currentOn = onTaskTime + (onTaskStart ? (now - onTaskStart) : 0);
    return Math.floor(currentOn / 1000);
}

export function getOffTaskTimeSeconds() {
    // Ensure current pending time is accounted for before returning
    const now = Date.now();
    const currentOff = offTaskTime + (offTaskStart ? (now - offTaskStart) : 0);
    return Math.floor(currentOff / 1000);
}