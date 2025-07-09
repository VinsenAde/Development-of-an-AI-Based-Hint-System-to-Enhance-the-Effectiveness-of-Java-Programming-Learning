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
const summaryTable        = document.getElementById('summaryTable'); // Get reference to the table

// New UI elements for sliding
const contentWrapper      = document.getElementById('contentWrapper');
const viewChartsBtn       = document.getElementById('viewChartsBtn');
const viewSummaryBtn      = document.getElementById('viewSummaryBtn');

// Add an element to display the user's full name
const userNameDisplay     = document.getElementById('userNameDisplay');

// Pagination elements
const prevPageBtn           = document.getElementById('prevPageBtn');
const nextPageBtn           = document.getElementById('nextPageBtn');
const pageNumbersContainer  = document.getElementById('pageNumbers');


let lastStats       = null;
let lastSubmissions = []; // This will always hold the FULL, POTENTIALLY SORTED data
let lastFeedback    = '';
let currentUserName = 'Guest'; // Default value

// ———————————————————————————————————————————————————————————————
//  Sorting State (for multi-column sort)
// ———————————————————————————————————————————————————————————————
let sortCriteria = []; // Array of { column: string, direction: 'asc' | 'desc' }
const MAX_SORT_CRITERIA = 3; // Limit the number of columns to sort by

// ———————————————————————————————————————————————————————————————
//  Pagination State
// ———————————————————————————————————————————————————————————————
const rowsPerPage = 7;
let currentPage = 1;

// ———————————————————————————————————————————————————————————————
//  Helpers
// ———————————————————————————————————————————————————————————————
function formatTimeSec(sec) {
  const m = Math.floor((sec || 0) / 60);
  const s = Math.floor(sec || 0) % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
}

function getGrade(score) {
  if (score >= 90) return 'A';
  if (score >= 80) return 'B';
  if (score >= 70) return 'C';
  return 'D';
}

// Helper to get nested object properties using a string path (e.g., 'problem.title')
function getNestedValue(obj, path) {
  return path.split('.').reduce((acc, part) => acc && acc[part], obj);
}

// ———————————————————————————————————————————————————————————————
//  Fetch user data, summary stats & raw submissions
// ———————————————————————————————————————————————————————————————
async function resolveUserId() {
  if (userId) {
      return;
  }
  const res = await fetch('/api/users/me');
  if (!res.ok) {
    if (statPanel) {
      statPanel.innerHTML = '<div class="text-red-600">Error: Cannot determine current user.</div>';
    }
    throw new Error('Cannot fetch /api/users/me');
  }
  const me = await res.json();
  userId = me.id;
  currentUserName = me.fullName || me.username || 'User';
  if (userNameDisplay) {
    userNameDisplay.textContent = currentUserName;
  }
}

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

// ———————————————————————————————————————————————————————————————
//  Table Rendering, Sorting, and Pagination
// ———————————————————————————————————————————————————————————————

// Renders the current page of the table
function renderTable() { // No longer takes 'subs' as argument, uses 'lastSubmissions' global
  if (!tableBody) {
    console.error('Table body element not found.');
    return;
  }

  const startIndex = (currentPage - 1) * rowsPerPage;
  const endIndex = startIndex + rowsPerPage;
  const pagedSubmissions = lastSubmissions.slice(startIndex, endIndex); // Slice the (potentially sorted) array

  tableBody.innerHTML = ''; // Clear existing rows
  if (pagedSubmissions.length === 0) {
    tableBody.innerHTML = '<tr><td colspan="10" class="p-4 text-center text-gray-500">No submissions found.</td></tr>';
  } else {
    pagedSubmissions.forEach(sub => {
      tableBody.innerHTML += `
        <tr class="border-b">
          <td class="p-2">${sub.problem.title}</td>
          <td class="p-2">${sub.problem.subTopic || '-'}</td> <td class="p-2">${sub.score}</td>
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

  updatePaginationControls(); // Update buttons and page numbers
  updateSortIcons(); // Update sort arrows after rendering
}

// Sorts the lastSubmissions array based on sortCriteria and re-renders the table
function sortSubmissions() {
  if (!lastSubmissions || lastSubmissions.length === 0 || sortCriteria.length === 0) {
      renderTable(); // Still render, even if unsorted or empty
      return;
  }

  // Create a copy to sort, so we don't mutate the original array if it's referenced elsewhere
  const sortedSubmissions = [...lastSubmissions].sort((a, b) => {
      for (const criterion of sortCriteria) {
          const { column, direction } = criterion;
          let valA = getNestedValue(a, column);
          let valB = getNestedValue(b, column);

          // Handle null/undefined values by pushing them to the end (or start based on direction)
          const isANull = valA === null || valA === undefined;
          const isBNull = valB === null || valB === undefined;

          if (isANull && isBNull) continue; // Both null, move to next criterion
          if (isANull) return direction === 'asc' ? 1 : -1; // A is null, B is not (A goes after B for asc)
          if (isBNull) return direction === 'asc' ? -1 : 1; // B is null, A is not (B goes after A for asc)

          // Handle string comparison (case-insensitive for text fields)
          if (typeof valA === 'string' && typeof valB === 'string') {
              valA = valA.toLowerCase();
              valB = valB.toLowerCase();
          }

          let comparison = 0;
          if (valA > valB) {
              comparison = 1;
          } else if (valA < valB) {
              comparison = -1;
          }

          if (comparison !== 0) {
              return direction === 'desc' ? comparison * -1 : comparison;
          }
      }
      return 0; // All criteria are equal
  });

  lastSubmissions = sortedSubmissions; // Update the global array with the sorted version
  renderTable(); // Re-render with sorted data
}

// Handles click events on table headers for sorting
function handleHeaderClick(event) {
  const th = event.currentTarget;
  const column = th.dataset.sortBy;

  if (!column) return; // Not a sortable column

  let existingCriterionIndex = sortCriteria.findIndex(c => c.column === column);

  if (existingCriterionIndex !== -1) {
    // Column already in sort criteria
    let existingCriterion = sortCriteria[existingCriterionIndex];
    existingCriterion.direction = existingCriterion.direction === 'asc' ? 'desc' : 'asc';
    // Move to front (primary sort key)
    sortCriteria.splice(existingCriterionIndex, 1);
    sortCriteria.unshift(existingCriterion);
  } else {
    // New column, add to front, default to ascending
    sortCriteria.unshift({ column: column, direction: 'asc' });
  }

  // Limit number of sort criteria
  if (sortCriteria.length > MAX_SORT_CRITERIA) {
    sortCriteria.pop(); // Remove the oldest criterion
  }

  currentPage = 1; // Reset to first page after sorting
  sortSubmissions(); // Re-sort and re-render
}

// Updates the visual sort icons in the table headers
function updateSortIcons() {
  if (!summaryTable) return;
  const headers = summaryTable.querySelectorAll('thead th[data-sort-by]');
  headers.forEach(header => {
    const icon = header.querySelector('.sort-icon');
    const column = header.dataset.sortBy;
    header.classList.remove('bg-gray-200'); // Remove active style background

    if (icon) {
      icon.innerHTML = ''; // Clear previous icon content
    }

    const criterion = sortCriteria.find(c => c.column === column);
    if (criterion) {
      if (icon) {
        icon.innerHTML = criterion.direction === 'asc' ? ' &#9650;' : ' &#9660;'; // Up or Down arrow
      }
      header.classList.add('bg-gray-200'); // Add active style background
      // Optional: Add priority number
      // const priority = sortCriteria.indexOf(criterion) + 1;
      // if (priority > 1) { /* add priority number to a span next to icon */ }
    }
  });
}

// Updates the pagination controls (buttons and page numbers)
function updatePaginationControls() {
  const totalPages = Math.ceil(lastSubmissions.length / rowsPerPage);

  // Update Prev/Next button states
  if (prevPageBtn) prevPageBtn.disabled = currentPage === 1;
  if (nextPageBtn) nextPageBtn.disabled = currentPage === totalPages || totalPages === 0;

  // Update page numbers
  if (pageNumbersContainer) {
    pageNumbersContainer.innerHTML = '';
    const maxPagesToShow = 5; // Show up to 5 page numbers (e.g., 1 2 [3] 4 5)
    let startPage = Math.max(1, currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(totalPages, startPage + maxPagesToShow - 1);

    // Adjust startPage if endPage is limited by totalPages
    if (endPage - startPage + 1 < maxPagesToShow) {
        startPage = Math.max(1, endPage - maxPagesToShow + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      const pageButton = document.createElement('button');
      pageButton.innerText = i;
      pageButton.classList.add('px-3', 'py-1', 'rounded', 'text-sm');
      if (i === currentPage) {
        pageButton.classList.add('bg-blue-600', 'text-white');
      } else {
        pageButton.classList.add('bg-gray-100', 'text-gray-700', 'hover:bg-gray-200');
      }
      pageButton.addEventListener('click', () => {
        currentPage = i;
        renderTable(); // Re-render table with current page
      });
      pageNumbersContainer.appendChild(pageButton);
    }
    if (totalPages === 0) {
        pageNumbersContainer.innerHTML = '<span class="text-gray-500 text-sm">No pages</span>';
    }
  }
}

function goToNextPage() {
  const totalPages = Math.ceil(lastSubmissions.length / rowsPerPage);
  if (currentPage < totalPages) {
    currentPage++;
    renderTable();
  }
}

function goToPrevPage() {
  if (currentPage > 1) {
    currentPage--;
    renderTable();
  }
}


// ———————————————————————————————————————————————————————————————
//  Other UI Rendering & Functionality
// ———————————————————————————————————————————————————————————————
function renderStats(stats, submissions) {
  if (!statPanel) {
    console.error('Stat panel element not found.');
    return;
  }
  const total = stats.totalSubmissions ?? 0;
  const avgScore = (stats.avgScore ?? 0).toFixed(2); // Format to 2 decimal places
  const noHintCount = submissions.filter(s => s.hintsUsed === 0).length;
  const usedHintCount = submissions.length - noHintCount;
  const avgFailed = total
    ? (stats.totalFailedRuns / total).toFixed(2)
    : '0.00';

  const totalOnSec = submissions.reduce((sum, s) => sum + (s.onTaskTime || 0), 0);
  const totalOffSec = submissions.reduce((sum, s) => sum + (s.offTaskTime || 0), 0);
  const avgOnSec = total ? totalOnSec / total : 0;
  const avgOffSec = total ? totalOffSec / total : 0;

  const capBadges = Object.entries(stats.capMap || {})
    .map(([cap, cnt]) =>
      `<span class="inline-block px-2 py-1 bg-gray-100 rounded mr-1">
          ${cap}: <span class="font-bold">${cnt}</span>
        </span>`
    ).join('');

  const grade = getGrade(avgScore); // Assuming getGrade function is defined elsewhere

  statPanel.innerHTML = `
    <div class="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 mb-4">
        <div class="bg-indigo-100 p-4 rounded-lg shadow-sm border border-indigo-200">
            <div class="text-sm text-indigo-700 font-medium">Average Score</div>
            <div class="text-3xl font-extrabold text-indigo-800 mt-1">${avgScore}</div>
        </div>

        <div class="bg-purple-100 p-4 rounded-lg shadow-sm border border-purple-200">
            <div class="text-sm text-purple-700 font-medium">Grade</div>
            <div class="text-3xl font-extrabold text-purple-800 mt-1">${grade}</div>
        </div>

        <div class="bg-blue-50 p-4 rounded-lg shadow-sm border border-blue-100">
            <div class="text-sm text-blue-700">Total Submissions</div>
            <div class="text-2xl font-bold text-blue-800 mt-1">${total}</div>
        </div>

        <div class="bg-green-50 p-4 rounded-lg shadow-sm border border-green-100">
            <div class="text-sm text-green-700">Avg On-Task Time</div>
            <div class="text-2xl font-bold text-green-800 mt-1">${formatTimeSec(avgOnSec)}</div>
        </div>

        <div class="bg-yellow-50 p-4 rounded-lg shadow-sm border border-yellow-100">
            <div class="text-sm text-yellow-700">No Hint Used</div>
            <div class="text-2xl font-bold text-yellow-800 mt-1">${noHintCount}</div>
        </div>

        <div class="bg-red-50 p-4 rounded-lg shadow-sm border border-red-100">
            <div class="text-sm text-red-700">Used Hint(s)</div>
            <div class="text-2xl font-bold text-red-800 mt-1">${usedHintCount}</div>
        </div>

        <div class="bg-gray-50 p-4 rounded-lg shadow-sm border border-gray-100">
            <div class="text-sm text-gray-700">Avg Failed Runs</div>
            <div class="text-2xl font-bold text-gray-800 mt-1">${avgFailed}</div>
        </div>

        <div class="bg-orange-50 p-4 rounded-lg shadow-sm border border-orange-100">
            <div class="text-sm text-orange-700">Avg Off-Task Time</div>
            <div class="text-2xl font-bold text-orange-800 mt-1">${formatTimeSec(avgOffSec)}</div>
        </div>
    </div>

    <div class="flex flex-wrap gap-2 text-xs mb-4">
        <span class="text-sm font-semibold text-gray-700 mr-1">Hint Caps:</span>
        ${capBadges || '<span class="text-gray-500">No specific caps recorded</span>'}
    </div>
  `;
}

// Generate new AI feedback (no auto-save)
async function generateAIFeedback() {
  if (!lastStats) return;
  if (aiFeedbackText) {
    aiFeedbackText.innerText = 'Generating feedback…';
  }
  if (saveFeedbackBtn) {
    saveFeedbackBtn.disabled = true;
  }
  if (deleteFeedbackBtn) {
    deleteFeedbackBtn.disabled = true;
  }

  try {
    const total         = lastStats.totalSubmissions;
    const sumFails      = lastStats.totalFailedRuns;
    const sumHints      = lastStats.totalHints;
    const totalOnSec    = lastSubmissions.reduce((a,b)=>a+b.onTaskTime, 0);
    const totalOffSec = lastSubmissions.reduce((a,b)=>a+b.offTaskTime,0);

    const payload = {
      totalSubmissions: total,
      runsToSuccess:    total ? Math.round(sumFails / total) : 0,
      hintUsage:        sumHints,
      avgScore:         lastStats.avgScore  || 0,
      totalOnTaskTime:  totalOnSec,
      totalOffTaskTime: totalOffSec,
      hintDistribution: lastStats.capMap    || {}
    };

    const aiResp = await fetch('/api/feedback/generate', {
      method: 'POST', headers:{ 'Content-Type':'application/json' }, body: JSON.stringify(payload)
    });
    if (!aiResp.ok) throw new Error(`AI error (${aiResp.status})`);

    lastFeedback = await aiResp.text();
    if (aiFeedbackText) {
      aiFeedbackText.innerText = lastFeedback;
    }
    if (saveFeedbackBtn) {
      saveFeedbackBtn.disabled = false;
    }

  } catch(err) {
    if (aiFeedbackText) {
      aiFeedbackText.innerText = 'Error: ' + err.message;
    }
    if (saveFeedbackBtn) {
      saveFeedbackBtn.disabled = false;
    }
  }
}

// Save the generated feedback
async function saveAIFeedback() {
  if (!lastFeedback) return;
  if (saveFeedbackBtn) {
    saveFeedbackBtn.disabled = true;
  }
  if (deleteFeedbackBtn) {
    deleteFeedbackBtn.disabled = true;
  }

  const url = isAdminView
    ? `/api/users/${userId}/feedback`
    : `/api/users/me/feedback`;

  try {
    const resp = await fetch(url, {
      method: 'POST', headers:{ 'Content-Type':'application/json' }, body: JSON.stringify({ feedback: lastFeedback })
    });
    if (!resp.ok) throw new Error(`Save failed (${resp.status})`);

    if (deleteFeedbackBtn) {
      deleteFeedbackBtn.disabled = false;
    }
    alert('Feedback saved!');

  } catch(err) {
    alert('Save error: ' + err.message);
    if (saveFeedbackBtn) {
      saveFeedbackBtn.disabled = false;
    }
  }
}

// Delete any saved feedback
async function deleteAIFeedback() {
  if (deleteFeedbackBtn) {
    deleteFeedbackBtn.disabled = true;
  }

  const url = isAdminView
    ? `/api/users/${userId}/feedback`
    : `/api/users/me/feedback`;

  try {
    const resp = await fetch(url, { method:'DELETE' });
    if (!resp.ok) throw new Error(`Delete failed (${resp.status})`);

    lastFeedback = '';
    if (aiFeedbackText) {
      aiFeedbackText.innerText = 'Feedback deleted.';
    }
    if (saveFeedbackBtn) {
      saveFeedbackBtn.disabled = true;
    }

  } catch(err) {
    alert('Delete error: ' + err.message);
    if (deleteFeedbackBtn) {
      deleteFeedbackBtn.disabled = false;
    }
  }
}

// Print to PDF
function printAsPdf() {
  window.print();
}

// Sliding functionality
function showChartsPanel() {
  console.log('Attempting to show charts panel...');
  if (contentWrapper) {
    contentWrapper.classList.add('slide-left');
    console.log('slide-left class added.');
  } else {
    console.error('contentWrapper not found!');
  }
}

function showSummaryPanel() {
  console.log('Attempting to show summary panel...');
  if (contentWrapper) {
    contentWrapper.classList.remove('slide-left');
    console.log('slide-left class removed.');
  } else {
    console.error('contentWrapper not found!');
  }
}

// ———————————————————————————————————————————————————————————————
//  Bootstrap: fetch → render → charts → load saved feedback
// ———————————————————————————————————————————————————————————————
async function renderAll() {
  try {
    // resolveUserId is called to set 'userId' if not in admin view,
    // and also implicitly sets currentUserName for non-admin view.
    // For admin view, it does nothing as userId is already set.
    await resolveUserId();

    const stats       = await fetchStats();
    const submissions = await fetchSubmissions();
    lastStats         = stats;
    lastSubmissions   = submissions;

    // --- ADDED THIS BLOCK TO UPDATE userNameDisplay ---
    if (userNameDisplay && lastStats) {
      userNameDisplay.textContent = lastStats.fullName || lastStats.username || 'User';
    }
    // --------------------------------------------------

    renderStats(stats, submissions);

    // Initial sort state: Sort by 'submittedAt' descending by default
    sortCriteria = [{ column: 'submittedAt', direction: 'desc' }];
    currentPage = 1; // Ensure we start on the first page
    sortSubmissions(); // This will sort `lastSubmissions` and call renderTable()

    renderAllCharts(submissions, stats); // Charts still use the full list

    lastFeedback = await fetchSavedFeedback();
    if (lastFeedback) {
      if (aiFeedbackText) {
        aiFeedbackText.innerText = lastFeedback;
      }
      if (deleteFeedbackBtn) {
        deleteFeedbackBtn.disabled = false;
      }
      if (saveFeedbackBtn) {
        saveFeedbackBtn.disabled = true;
      }
    } else {
      if (aiFeedbackText) {
        aiFeedbackText.innerText = 'Press “Generate Feedback” to get AI insights.';
      }
      if (saveFeedbackBtn) {
        saveFeedbackBtn.disabled = true;
      }
      if (deleteFeedbackBtn) {
        deleteFeedbackBtn.disabled = true;
      }
    }

  } catch(e) {
    if (statPanel) {
      statPanel.innerHTML   = `<div class="text-red-600">Failed to load summary: ${e.message}</div>`;
    }
    if (aiFeedbackText) {
      aiFeedbackText.innerText = 'Unable to fetch data.';
    }
    console.error('Error in renderAll:', e);
  }
}

// ———————————————————————————————————————————————————————————————
//  Wire up event handlers
// ———————————————————————————————————————————————————————————————
if (generateFeedbackBtn) generateFeedbackBtn.addEventListener('click', generateAIFeedback);
if (saveFeedbackBtn)     saveFeedbackBtn.addEventListener('click', saveAIFeedback);
if (deleteFeedbackBtn)   deleteFeedbackBtn.addEventListener('click', deleteAIFeedback);
if (printSummaryBtn)     printSummaryBtn.addEventListener('click', printAsPdf);
if (viewChartsBtn)       viewChartsBtn.addEventListener('click', showChartsPanel);
if (viewSummaryBtn)      viewSummaryBtn.addEventListener('click', showSummaryPanel);

// Wire up sorting headers
if (summaryTable) {
  const sortableHeaders = summaryTable.querySelectorAll('thead th[data-sort-by]');
  sortableHeaders.forEach(header => {
    header.addEventListener('click', handleHeaderClick);
  });
}

// Wire up pagination buttons
if (prevPageBtn) prevPageBtn.addEventListener('click', goToPrevPage);
if (nextPageBtn) nextPageBtn.addEventListener('click', goToNextPage);


// Kick it off
renderAll().catch(err => console.error('Init error:', err));
