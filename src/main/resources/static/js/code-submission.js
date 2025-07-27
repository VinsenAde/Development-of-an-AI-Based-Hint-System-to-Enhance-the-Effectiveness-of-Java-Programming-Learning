// File: src/main/resources/static/js/code-submission.js
import { getHintCounts, resetHintCounts, getHintCapLevel } from './hint-modal.js';
import {
  updateTimers,
  getOnTaskTimeSeconds,
  getOffTaskTimeSeconds,
  resetActivityTracking, // Also ensure resetActivityTracking is imported
  stopActivityTrackingAndReserveTime // NEW: Import this function
} from './activity-tracker.js';
import { getEditorCode } from './editor-init.js';


export async function runCode(task) {
  const code = getEditorCode();

  const response = await fetch('/api/submissions/run', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code, problemId: task.id })
  });
  if (!response.ok) {
    return { error: await response.text() };
  }

  const result = await response.json();

 // 📈 track the number of failed runs
 if (result && result.success === false) {
  const failed = parseInt(localStorage.getItem('failedRuns') || '0', 10) + 1;
   localStorage.setItem('failedRuns', failed);
 }

  return result;
}


export async function submitCode(task) {
  // push any pending timing updates
  if (updateTimers) updateTimers();

  const code = getEditorCode();

  const userId        = parseInt(localStorage.getItem('loggedInUserId')   || '1', 10);
  const sessionNumber = parseInt(localStorage.getItem('sessionNumber')    || '1', 10);
  const hintCounts    = getHintCounts();
  const hintsUsed     = Object.values(hintCounts).reduce((sum, v) => sum + v, 0);
  const failedRuns    = parseInt(localStorage.getItem('failedRuns')       || '0', 10);

  // Directly get the latest on-task and off-task times from activity-tracker
  const onTaskTime    = getOnTaskTimeSeconds();
  const offTaskTime   = getOffTaskTimeSeconds();

  const hintLevelCap = getHintCapLevel();

  const payload = {
    problemId:       task.id,
    userId,
    sessionNumber,
    code,
    hintsUsed,
    hintCounts,
    failedRuns,
    timeLimitSeconds: 300,  // e.g. 5 minutes
    onTaskTime,
    offTaskTime,
    hintLevelCap
  };

  const response = await fetch('/api/submissions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    return { error: await response.text() };
  }
  const result = await response.json();

  // NEW: If submission is successful, stop activity tracking permanently
  if (result.success) {
    stopActivityTrackingAndReserveTime(); // Call the new function
  }

  return result;
}

export function resetSubmissionTracking() {
  localStorage.setItem('hintsUsed',  '0');
  resetHintCounts();
  localStorage.setItem('failedRuns', '0');
  // Reset activity tracking as well when submission tracking is reset
  resetActivityTracking(); // Ensure this is called here if you want to reset timers on "Clear"
}
//import { getHintCounts, resetHintCounts, getHintCapLevel } from './hint-modal.js';
//import {
//  updateTimers,
//  getOnTaskTimeSeconds,
//  getOffTaskTimeSeconds,
//} from './activity-tracker.js';
//// ✂️ Replace your old import with this:
//import { getEditorCode } from './editor-init.js';
//
////export async function runCode(task) {
////  // ✅ grab exactly what the user has in Monaco
////  const code = getEditorCode();
////
////  const response = await fetch('/api/submissions/run', {
////    method: 'POST',
////    headers: { 'Content-Type': 'application/json' },
////    body: JSON.stringify({ code, problemId: task.id })
////  });
////
////  if (!response.ok) {
////    return { error: await response.text() };
////  }
////  return await response.json();
////}
//
//export async function runCode(task) {
//  const code = getEditorCode();
//
//  const response = await fetch('/api/submissions/run', {
//    method: 'POST',
//    headers: { 'Content-Type': 'application/json' },
//    body: JSON.stringify({ code, problemId: task.id })
//  });
//  if (!response.ok) {
//    return { error: await response.text() };
//  }
//
//  const result = await response.json();
//
// // 📈 track the number of failed runs
// if (result && result.success === false) {
//  const failed = parseInt(localStorage.getItem('failedRuns') || '0', 10) + 1;
//   localStorage.setItem('failedRuns', failed);
// }
//
//  return result;
//}
//
//
//export async function submitCode(task) {
//  // push any pending timing updates
//  if (updateTimers) updateTimers();
//
//  // ✅ again, pull from Monaco instead of .value on a DIV
//  const code = getEditorCode();
//
//  const userId        = parseInt(localStorage.getItem('loggedInUserId')   || '1', 10);
//  const sessionNumber = parseInt(localStorage.getItem('sessionNumber')    || '1', 10);
//  const hintCounts    = getHintCounts();
//  const hintsUsed     = Object.values(hintCounts).reduce((sum, v) => sum + v, 0);
//  const failedRuns    = parseInt(localStorage.getItem('failedRuns')       || '0', 10);
//
//  const onTaskTime    = getOnTaskTimeSeconds();
//  const offTaskTime   = getOffTaskTimeSeconds();
//  const nowSeconds    = Math.trunc(Date.now() / 1000);
//  const elapsedSeconds = window.totalStart
//    ? nowSeconds - Math.trunc(window.totalStart / 1000)
//    : 0;
//
//  const hintLevelCap = getHintCapLevel();
//
//  const payload = {
//    problemId:       task.id,
//    userId,
//    sessionNumber,
//    code,
//    hintsUsed,
//    hintCounts,
//    failedRuns,
//    elapsedSeconds,         // in seconds
//    timeLimitSeconds: 300,  // e.g. 5 minutes
//    onTaskTime,             // Added to payload
//    offTaskTime,            // Added to payload
//    hintLevelCap
//  };
//
//  const response = await fetch('/api/submissions', {
//    method: 'POST',
//    headers: { 'Content-Type': 'application/json' },
//    body: JSON.stringify(payload)
//  });
//
//  if (!response.ok) {
//    return { error: await response.text() };
//  }
//  return await response.json();
//}
//
//export function resetSubmissionTracking() {
//  localStorage.setItem('hintsUsed',  '0');
//  resetHintCounts();
//  localStorage.setItem('failedRuns', '0');
//  // reset the global start timestamp:
//  window.totalStart = Date.now();
//}

//
//import { getHintCounts, resetHintCounts, getHintCapLevel } from './hint-modal.js';
//import {
//  updateTimers,
//  getOnTaskTimeSeconds,
//  getOffTaskTimeSeconds,
//} from './activity-tracker.js';
//// ✂️ Replace your old import with this:
//import { getEditorCode } from './editor-init.js';
//
//export async function runCode(task) {
//  // ✅ grab exactly what the user has in Monaco
//  const code = getEditorCode();
//
//  const response = await fetch('/api/submissions/run', {
//    method: 'POST',
//    headers: { 'Content-Type': 'application/json' },
//    body: JSON.stringify({ code, problemId: task.id })
//  });
//
//  if (!response.ok) {
//    return { error: await response.text() };
//  }
//  return await response.json();
//}
//
//export async function submitCode(task) {
//  // push any pending timing updates
//  if (updateTimers) updateTimers();
//
//  // ✅ again, pull from Monaco instead of .value on a DIV
//  const code = getEditorCode();
//
//  const userId        = parseInt(localStorage.getItem('loggedInUserId')   || '1', 10);
//  const sessionNumber = parseInt(localStorage.getItem('sessionNumber')    || '1', 10);
//  const hintCounts    = getHintCounts();
//  const hintsUsed     = Object.values(hintCounts).reduce((sum, v) => sum + v, 0);
//  const failedRuns    = parseInt(localStorage.getItem('failedRuns')       || '0', 10);
//
//  const onTaskTime    = getOnTaskTimeSeconds();
//  const offTaskTime   = getOffTaskTimeSeconds();
//  const nowSeconds    = Math.trunc(Date.now() / 1000);
//  const elapsedSeconds = window.totalStart
//    ? nowSeconds - Math.trunc(window.totalStart / 1000)
//    : 0;
//
//  const hintLevelCap = getHintCapLevel();
//
//  const payload = {
//    problemId:       task.id,
//    userId,
//    sessionNumber,
//    code,
//    hintsUsed,
//    hintCounts,
//    failedRuns,
//    elapsedSeconds,         // in seconds
//    timeLimitSeconds: 300,  // e.g. 5 minutes
//    onTaskTime,
//    offTaskTime,
//    hintLevelCap
//  };
//
//  const response = await fetch('/api/submissions', {
//    method: 'POST',
//    headers: { 'Content-Type': 'application/json' },
//    body: JSON.stringify(payload)
//  });
//
//  if (!response.ok) {
//    return { error: await response.text() };
//  }
//  return await response.json();
//}
//
//export function resetSubmissionTracking() {
//  localStorage.setItem('hintsUsed',  '0');
//  resetHintCounts();
//  localStorage.setItem('failedRuns', '0');
//  // reset the global start timestamp:
//  window.totalStart = Date.now();
//}
