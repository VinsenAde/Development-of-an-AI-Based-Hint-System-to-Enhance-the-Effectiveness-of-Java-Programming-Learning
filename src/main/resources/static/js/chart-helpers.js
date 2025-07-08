// chart.js
let hintCapChart, avgScoreChart, avgTimeChart, failedRunsChart, hintScoreScatter, hintTypePie;

// CHART 1: Distribusi Hint Cap
function renderHintCapChart(submissions, stats) {
  const ctx = document.getElementById('hintCapChart').getContext('2d');
  if (hintCapChart instanceof Chart) hintCapChart.destroy();
  hintCapChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: Object.keys(stats.capMap),
      datasets: [{
        label: 'Jumlah Task',
        data: Object.values(stats.capMap),
        borderWidth: 1
      }]
    },
    options: {
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true } }
    }
  });
}

// CHART 2: Skor Rata-rata Per Task
function renderAvgScoreChart(submissions) {
  const ctx = document.getElementById('avgScoreChart').getContext('2d');
  if (avgScoreChart instanceof Chart) avgScoreChart.destroy();
  avgScoreChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: submissions.map(sub => sub.problem.title),
      datasets: [{
        label: 'Score',
        data: submissions.map(sub => sub.score),
        borderWidth: 1
      }]
    },
    options: {
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true } }
    }
  });
}

// CHART 3: Waktu On/Off Task Per Task
function renderAvgTimeChart(submissions) {
  const ctx = document.getElementById('avgTimeChart').getContext('2d');
  if (avgTimeChart instanceof Chart) avgTimeChart.destroy();
  avgTimeChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: submissions.map(sub => sub.problem.title),
      datasets: [
        {
          label: 'On-Task',
          data: submissions.map(sub => sub.onTaskTime),
          backgroundColor: 'rgba(99,102,241,0.5)'
        },
        {
          label: 'Off-Task',
          data: submissions.map(sub => sub.offTaskTime),
          backgroundColor: 'rgba(239,68,68,0.5)'
        }
      ]
    },
    options: {
      scales: { y: { beginAtZero: true } }
    }
  });
}

// CHART 4: Failed Runs Per Task
function renderFailedRunsChart(submissions) {
  const ctx = document.getElementById('failedRunsChart').getContext('2d');
  if (failedRunsChart instanceof Chart) failedRunsChart.destroy();
  failedRunsChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: submissions.map(sub => sub.problem.title),
      datasets: [{
        label: 'Failed Runs',
        data: submissions.map(sub => sub.failedRuns),
        backgroundColor: 'rgba(239,68,68,0.7)'
      }]
    },
    options: {
      scales: { y: { beginAtZero: true } }
    }
  });
}

// CHART 5: Score vs Hint Used (Scatter)
function renderHintScoreScatter(submissions) {
  const scatterData = submissions.map(sub => ({
    x: sub.hintsUsed,
    y: sub.score,
    label: sub.problem.title,
  }));

  const ctx = document.getElementById('hintScoreScatter').getContext('2d');
  if (hintScoreScatter instanceof Chart) hintScoreScatter.destroy();
  hintScoreScatter = new Chart(ctx, {
    type: 'scatter',
    data: {
      datasets: [{
        label: 'Score vs. Hint Used',
        data: scatterData,
        pointBackgroundColor: 'rgba(99,102,241,1)'
      }]
    },
    options: {
      plugins: {
        tooltip: {
          callbacks: {
            label: (c) => `${scatterData[c.dataIndex].label}: Hint ${c.parsed.x}, Score ${c.parsed.y}`
          }
        }
      },
      scales: {
        x: { title: { display: true, text: 'Hints Used' }, beginAtZero: true },
        y: { title: { display: true, text: 'Score' }, beginAtZero: true }
      }
    }
  });
}

// CHART 6: Hint Type Frequency (Pie)
function renderHintTypePie(submissions) {
  const hintTypeKeys = ['conceptual','syntax','logic','step','reveal'];
  const hintTypeCounts = hintTypeKeys.map(k => submissions.reduce((sum, sub) => (sub.hintCounts && sub.hintCounts[k]) ? sum + sub.hintCounts[k] : sum, 0));
  const ctx = document.getElementById('hintTypePie').getContext('2d');
  if (hintTypePie instanceof Chart) hintTypePie.destroy();
  hintTypePie = new Chart(ctx, {
    type: 'pie',
    data: {
      labels: hintTypeKeys,
      datasets: [{
        data: hintTypeCounts,
        backgroundColor: [
          'rgba(99,102,241,0.7)',   // conceptual
          'rgba(251,191,36,0.7)',   // syntax
          'rgba(59,130,246,0.7)',   // logic
          'rgba(16,185,129,0.7)',   // step
          'rgba(75,85,99,0.7)'      // reveal
        ]
      }]
    }
  });
}
// Helper: grup submissions berdasarkan kategori/topik
function groupByTopic(submissions) {
  return submissions.reduce((map, sub) => {
    const topic = sub.problem.category || 'Unknown';
    if (!map[topic]) {
      map[topic] = { count:0, scoreSum:0, hintSum:0, failSum:0 };
    }
    map[topic].count++;
    map[topic].scoreSum   += sub.score;
    map[topic].hintSum    += (sub.hintCounts 
                               ? Object.values(sub.hintCounts).reduce((a,b)=>a+b,0) 
                               : 0);
    map[topic].failSum    += sub.failedRuns;
    return map;
  }, {});
}

// Grafik 7: Skor Rata-rata per Topik
function renderAvgScoreTopicChart(subs) {
  const byTopic = groupByTopic(subs);
  const labels  = Object.keys(byTopic);
  const data    = labels.map(t => (byTopic[t].scoreSum / byTopic[t].count).toFixed(1));
  const ctx     = document.getElementById('avgScoreTopicChart').getContext('2d');
  new Chart(ctx, {
    type: 'bar',
    data: { labels, datasets: [{ label:'Avg Score', data, borderWidth:1 }] },
    options: { plugins:{ legend:{display:false} }, scales:{ y:{beginAtZero:true} } }
  });
}

// Grafik 8: Total Hint Used per Topik
function renderHintUsageTopicChart(subs) {
  const byTopic = groupByTopic(subs);
  const labels  = Object.keys(byTopic);
  const data    = labels.map(t => byTopic[t].hintSum);
  const ctx     = document.getElementById('hintUsageTopicChart').getContext('2d');
  new Chart(ctx, {
    type:'bar',
    data:{ labels, datasets:[{ label:'Total Hints', data, borderWidth:1 }] },
    options:{ plugins:{ legend:{display:false} }, scales:{ y:{beginAtZero:true} } }
  });
}

// Grafik 9: Failed Runs per Topik
function renderFailedRunsTopicChart(subs) {
  const byTopic = groupByTopic(subs);
  const labels  = Object.keys(byTopic);
  const data    = labels.map(t => byTopic[t].failSum);
  const ctx     = document.getElementById('failedRunsTopicChart').getContext('2d');
  new Chart(ctx, {
    type:'bar',
    data:{ labels, datasets:[{ label:'Failed Runs', data, backgroundColor:'rgba(239,68,68,0.7)' }] },
    options:{ scales:{ y:{beginAtZero:true} } }
  });
}

// Panggil semua chart, termasuk yg baru
export function renderAllCharts(submissions, stats) {
  renderHintCapChart(submissions, stats);
  renderAvgScoreChart(submissions);
  renderAvgTimeChart(submissions);
  renderFailedRunsChart(submissions);
  renderHintScoreScatter(submissions);
  renderHintTypePie(submissions);
  // tambahan per-topik:
  renderAvgScoreTopicChart(submissions);
  renderHintUsageTopicChart(submissions);
  renderFailedRunsTopicChart(submissions);
}