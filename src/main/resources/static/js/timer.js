//let taskStartTime = null;
//let editorFocusTime = 0;
//let hintOpenTime = 0;
//let elapsedSeconds = 0;
//
//let editorTimerInterval;
//let hintTimerInterval;
//let taskTimerInterval;
//
//export function startTimer() {
//  const start = Date.now();
//  taskStartTime = start;
//
//  taskTimerInterval = setInterval(() => {
//    elapsedSeconds = Math.floor((Date.now() - start) / 1000);
//    const min = Math.floor(elapsedSeconds / 60);
//    const sec = elapsedSeconds % 60;
//    const timerEl = document.getElementById("timer");
//    if (timerEl) {
//      timerEl.innerText = `🕒 Time Elapsed: ${min}m ${sec}s`;
//    }
//  }, 1000);
//}
//
//export function stopTimer() {
//  clearInterval(taskTimerInterval);
//  return elapsedSeconds;
//}
//
//export function startEditorTimer() {
//  editorTimerInterval = setInterval(() => {
//    editorFocusTime += 1;
//  }, 1000);
//}
//
//export function stopEditorTimer() {
//  clearInterval(editorTimerInterval);
//}
//
//export function startHintTimer() {
//  hintTimerInterval = setInterval(() => {
//    hintOpenTime += 1;
//  }, 1000);
//}
//
//export function stopHintTimer() {
//  clearInterval(hintTimerInterval);
//}
//
//export function getAllTimes() {
//  return {
//    totalTaskTime: elapsedSeconds,
//    activeEditorTime: editorFocusTime,
//    hintUsageTime: hintOpenTime
//  };
//}
//
//export function resetAllTimers() {
//  clearInterval(taskTimerInterval);
//  clearInterval(editorTimerInterval);
//  clearInterval(hintTimerInterval);
//  elapsedSeconds = 0;
//  editorFocusTime = 0;
//  hintOpenTime = 0;
//  taskStartTime = null;
//}
import { renderAllCharts } from './chart-helpers.js';

// ———————————————————————————————————————————————————————————————
//  Parse optional userId, sessionNumber, and admin mode
// ———————————————————————————————————————————————————————————————
const params        = new URLSearchParams(window.location.search);
let   userId        = params.get('userId');
const sessionNumber = parseInt(params.get('sessionNumber') || '1', 10);
const isAdminView   = params.has('userId');

// ———————————————————————————————————————————————————————————————
//  Grab UI elements
// ———————————————————————————————————————————————————————————————
const statPanel           = document.getElementById('statPanel');
const tableBody           = document.getElementById('tableBody');
const aiFeedbackText      = document.getElementById('aiFeedbackText');
const generateFeedbackBtn = document.getElementById('generateFeedbackBtn');
const saveFeedbackBtn     = document.getElementById('saveFeedbackBtn');
const deleteFeedbackBtn   = document.getElementById('deleteFeedbackBtn');
const printSummaryBtn     = document.getElementById('printSummaryBtn');

// New UI elements for sliding
const contentWrapper      = document.getElementById('contentWrapper');
const viewChartsBtn       = document.getElementById('viewChartsBtn');
const viewSummaryBtn      = document.getElementById('viewSummaryBtn');


let lastStats       = null;
let lastSubmissions = [];
let lastFeedback    = '';

// ———————————————————————————————————————————————————————————————
//  Helpers
// ———————————————————————————————————————————————————————————————
function formatTimeSec(sec) {
  const m = Math.floor((sec || 0) / 60);
  const s = Math.floor(sec || 0) % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
}

// Add this helper function
function getGrade(score) {
  if (score >= 90) return 'A';
  if (score >= 80) return 'B';
  if (score >= 70) return 'C';
  if (score >= 60) return 'D';
  return 'E';
}

async function resolveUserId() {
  if (userId) return;
  const res = await fetch('/api/users/me');
  if (!res.ok) {
    statPanel.innerHTML = '<div class="text-red-600">Error: Cannot determine current user.</div>';
    throw new Error('Cannot fetch /api/users/me');
  }
  const me = await res.json();
  userId = me.id;
}

// ———————————————————————————————————————————————————————————————
//  Load previously saved AI feedback for this view
// ———————————————————————————————————————————————————————————————
async function fetchSavedFeedback() {
  await resolveUserId();
  const url = isAdminView
    ? `/api/users/${userId}/feedback`
    : `/api/users/me/feedback`;
  const res = await fetch(url);
  if (!res.ok) return '';
  const { feedback } = await res.json();
  return feedback || '';
}

// ———————————————————————————————————————————————————————————————
//  Fetch summary stats & raw submissions
// ———————————————————————————————————————————————————————————————
async function fetchStats() {
  await resolveUserId();
  const res = await fetch(`/api/admin/users/${userId}/summary`);
  if (!res.ok) throw new Error(`Failed to fetch stats (${res.status})`);
  const { data } = await res.json();
  return data;
}

async function fetchSubmissions() {
  await resolveUserId();
  const url = isAdminView
    ? `/api/admin/submissions?userId=${userId}&sessionNumber=${sessionNumber}`
    : `/api/submissions?userId=${userId}&sessionNumber=${sessionNumber}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error(`Failed to fetch submissions (${res.status})`);
  return await res.json();
}

function renderTable(subs) {
  tableBody.innerHTML = '';
  subs.forEach(sub => {
    tableBody.innerHTML += `
      <tr class="border-b">
        <td class="p-2">${sub.problem.title}</td>
        <td class="p-2">${sub.score}</td>
        <td class="p-2">${sub.hintsUsed}</td>
        <td class="p-2">${sub.failedRuns}</td>
        <td class="p-2">${formatTimeSec(sub.onTaskTime)}</td>
        <td class="p-2">${formatTimeSec(sub.offTaskTime)}</td>
        <td class="p-2">${sub.hintLevelCap ?? '-'}</td>
        <td class="p-2">${sub.output || ''}</td>
        <td class="p-2">${sub.submittedAt?.replace('T',' ').slice(0,19) || ''}</td>
      </tr>`;
  });
}

function renderStats(stats, submissions) {
  const total         = stats.totalSubmissions  ?? 0;
  const avgScore      = stats.avgScore          ?? 0;
  const noHintCount   = submissions.filter(s => s.hintsUsed === 0).length;
  const usedHintCount = submissions.length - noHintCount;
  const avgFailed     = total
    ? (stats.totalFailedRuns / total).toFixed(2)
    : '0.00';

  const totalOnSec    = submissions.reduce((sum, s) => sum + (s.onTaskTime || 0), 0);
  const totalOffSec   = submissions.reduce((sum, s) => sum + (s.offTaskTime|| 0), 0);
  const avgOnSec      = total ? totalOnSec  / total : 0;
  const avgOffSec     = total ? totalOffSec / total : 0;

  const capBadges     = Object.entries(stats.capMap || {})
    .map(([cap, cnt]) =>
      `<span class="inline-block px-2 py-1 bg-gray-100 rounded mr-1">
         ${cap}: <span class="font-bold">${cnt}</span>
       </span>`
    ).join('');

  // Calculate the grade
  const grade = getGrade(avgScore);

  statPanel.innerHTML = `
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-2">
      <div class="p-3 bg-indigo-50 rounded">
        <div class="text-sm text-gray-500">Total Submissions</div>
        <div class="text-xl font-bold">${total}</div>
      </div>
      <div class="p-3 bg-indigo-50 rounded">
        <div class="text-sm text-gray-500">Average Score</div>
        <div class="text-xl font-bold">${avgScore}</div>
      </div>
      <div class="p-3 bg-indigo-50 rounded">
        <div class="text-sm text-gray-500">Grade</div>
        <div class="text-xl font-bold">${grade}</div>
      </div>
      <div class="p-3 bg-indigo-50 rounded">
        <div class="text-sm text-gray-500">No Hint Used</div>
        <div class="text-xl font-bold">${noHintCount}</div>
      </div>
      <div class="p-3 bg-indigo-50 rounded">
        <div class="text-sm text-gray-500">Used Hint(s)</div>
        <div class="text-xl font-bold">${usedHintCount}</div>
      </div>
    </div>
    <div class="flex flex-wrap mb-2 text-xs">${capBadges}</div>
    <div class="grid grid-cols-2 md:grid-cols-3 gap-4 text-sm">
      <div><strong>Avg Failed Runs:</strong> ${avgFailed}</div>
      <div><strong>Avg On-Task:</strong> ${formatTimeSec(avgOnSec)}</div>
      <div><strong>Avg Off-Task:</strong> ${formatTimeSec(avgOffSec)}</div>
    </div>
  `;
}

// ———————————————————————————————————————————————————————————————
//  Generate new AI feedback (no auto-save)
// ———————————————————————————————————————————————————————————————
async function generateAIFeedback() {
  if (!lastStats) return;
  aiFeedbackText.innerText      = 'Generating feedback…';
  saveFeedbackBtn.disabled      = true;
  deleteFeedbackBtn.disabled    = true;

  try {
    const total       = lastStats.totalSubmissions;
    const sumFails    = lastStats.totalFailedRuns;
    const sumHints    = lastStats.totalHints;
    const totalOnSec  = lastSubmissions.reduce((a,b)=>a+b.onTaskTime, 0);
    const totalOffSec = lastSubmissions.reduce((a,b)=>a+b.offTaskTime,0);

    const payload = {
      totalSubmissions: total,
      runsToSuccess:    total ? Math.round(sumFails / total) : 0,
      hintUsage:        sumHints,
      avgScore:         lastStats.avgScore  || 0,
      totalOnTaskTime:  totalOnSec,
      totalOffTaskTime: totalOffSec,
      hintDistribution: lastStats.capMap     || {}
    };

    const aiResp = await fetch('/api/feedback/generate', {
      method: 'POST', headers:{ 'Content-Type':'application/json' }, body: JSON.stringify(payload)
    });
    if (!aiResp.ok) throw new Error(`AI error (${aiResp.status})`);

    lastFeedback = await aiResp.text();
    aiFeedbackText.innerText = lastFeedback;
    saveFeedbackBtn.disabled = false;

  } catch(err) {
    aiFeedbackText.innerText = 'Error: ' + err.message;
    saveFeedbackBtn.disabled = false;
  }
}

// ———————————————————————————————————————————————————————————————
//  Save the generated feedback
// ———————————————————————————————————————————————————————————————
async function saveAIFeedback() {
  if (!lastFeedback) return;
  saveFeedbackBtn.disabled   = true;
  deleteFeedbackBtn.disabled = true;

  const url = isAdminView
    ? `/api/users/${userId}/feedback`
    : `/api/users/me/feedback`;

  try {
    const resp = await fetch(url, {
      method: 'POST', headers:{ 'Content-Type':'application/json' }, body: JSON.stringify({ feedback: lastFeedback })
    });
    if (!resp.ok) throw new Error(`Save failed (${resp.status})`);

    deleteFeedbackBtn.disabled = false;
    alert('Feedback saved!');

  } catch(err) {
    alert('Save error: ' + err.message);
    saveFeedbackBtn.disabled = false;
  }
}

// ———————————————————————————————————————————————————————————————
//  Delete any saved feedback
// ———————————————————————————————————————————————————————————————
async function deleteAIFeedback() {
  deleteFeedbackBtn.disabled = true;

  const url = isAdminView
    ? `/api/users/${userId}/feedback`
    : `/api/users/me/feedback`;

  try {
    const resp = await fetch(url, { method:'DELETE' });
    if (!resp.ok) throw new Error(`Delete failed (${resp.status})`);

    lastFeedback = '';
    aiFeedbackText.innerText = 'Feedback deleted.';
    saveFeedbackBtn.disabled = true;

  } catch(err) {
    alert('Delete error: ' + err.message);
    deleteFeedbackBtn.disabled = false;
  }
}

// ———————————————————————————————————————————————————————————————
//  Print to PDF
// ———————————————————————————————————————————————————————————————
function printAsPdf() {
  window.print();
}

// ———————————————————————————————————————————————————————————————
//  Sliding functionality
// ———————————————————————————————————————————————————————————————
function showChartsPanel() {
  contentWrapper.classList.add('slide-left');
}

function showSummaryPanel() {
  contentWrapper.classList.remove('slide-left');
}

// ———————————————————————————————————————————————————————————————
//  Bootstrap: fetch → render → charts → load saved feedback
// ———————————————————————————————————————————————————————————————
async function renderAll() {
  try {
    const stats       = await fetchStats();
    const submissions = await fetchSubmissions();
    lastStats         = stats;
    lastSubmissions   = submissions;

    renderStats(stats, submissions);
    renderTable(submissions);
    renderAllCharts(submissions, stats);

    // load existing feedback
    lastFeedback = await fetchSavedFeedback();
    if (lastFeedback) {
      aiFeedbackText.innerText = lastFeedback;
      deleteFeedbackBtn.disabled = false;
      saveFeedbackBtn.disabled   = true;
    } else {
      aiFeedbackText.innerText = 'Press “Generate Feedback” to get AI insights.';
      saveFeedbackBtn.disabled   = true;
      deleteFeedbackBtn.disabled = true;
    }

  } catch(e) {
    statPanel.innerHTML   = `<div class="text-red-600">Failed to load summary: ${e.message}</div>`;
    aiFeedbackText.innerText = 'Unable to fetch data.';
  }
}

// ———————————————————————————————————————————————————————————————
//  Wire up event handlers
// ———————————————————————————————————————————————————————————————
generateFeedbackBtn.addEventListener('click', generateAIFeedback);
saveFeedbackBtn    .addEventListener('click', saveAIFeedback);
deleteFeedbackBtn  .addEventListener('click', deleteAIFeedback);
printSummaryBtn    .addEventListener('click', printAsPdf);

// Wire up sliding functionality
viewChartsBtn      .addEventListener('click', showChartsPanel);
viewSummaryBtn     .addEventListener('click', showSummaryPanel);

// Kick it off
renderAll().catch(err => console.error('Init error:', err));