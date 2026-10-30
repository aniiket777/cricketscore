// Cricket Live Score Management System - Frontend App
let stompClient = null;
let activeMatchId = 1;
let matchesList = [];
let activeMatch = null;
let isSimulating = false;
let commentaryFilter = 'all';
let manualBallState = { runs: 0, extraType: 'NONE', extraRuns: 0, isWicket: false, wicketType: 'NONE' };

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

async function initApp() {
    setupTabs();
    setupEventListeners();
    await loadMatches();
    connectWebSocket();
    loadLeaderboard();
}

// Setup Tab Navigation
function setupTabs() {
    const tabBtns = document.querySelectorAll('.tab-btn');
    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            tabBtns.forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content-panel').forEach(p => p.classList.remove('active'));

            btn.classList.add('active');
            const target = btn.getAttribute('data-tab');
            const panel = document.getElementById(target);
            if (panel) {
                panel.classList.add('active');
                if (target === 'tab-scorecard') loadScorecard();
                if (target === 'tab-worm') loadWormChart();
                if (target === 'tab-leaderboard') loadLeaderboard();
                if (target === 'tab-squads') loadSquads();
            }
        });
    });
}

// Setup Event Listeners
function setupEventListeners() {
    // Sim controls
    document.getElementById('btn-sim-toggle').addEventListener('click', toggleSimulation);
    document.getElementById('btn-sim-step').addEventListener('click', stepSimulation);
    document.getElementById('btn-sim-reset').addEventListener('click', resetMatch);
    document.getElementById('sim-speed-select').addEventListener('change', changeSimSpeed);

    // Manual scoring modal
    document.getElementById('btn-open-scorer').addEventListener('click', () => {
        openModal('modal-scorer');
    });
    document.getElementById('btn-open-create-match').addEventListener('click', () => {
        populateCreateMatchTeams();
        openModal('modal-create-match');
    });

    // Commentary filter chips
    document.querySelectorAll('.filter-chip').forEach(chip => {
        chip.addEventListener('click', () => {
            document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            commentaryFilter = chip.getAttribute('data-filter');
            loadCommentary();
        });
    });

    // Keypad runs selection
    document.querySelectorAll('.keypad-run-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.keypad-run-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            manualBallState.runs = parseInt(btn.getAttribute('data-val'));
        });
    });

    // Extras checkbox/select
    document.getElementById('scorer-extra-type').addEventListener('change', (e) => {
        manualBallState.extraType = e.target.value;
    });

    // Wicket checkbox
    document.getElementById('scorer-is-wicket').addEventListener('change', (e) => {
        manualBallState.isWicket = e.target.checked;
        document.getElementById('wicket-details-row').style.display = e.target.checked ? 'block' : 'none';
    });

    document.getElementById('btn-submit-ball').addEventListener('click', submitManualBall);
    document.getElementById('btn-submit-create-match').addEventListener('click', submitCreateMatch);
}

// WebSocket STOMP Connection
function connectWebSocket() {
    const badge = document.getElementById('ws-status-badge');
    const badgeText = document.getElementById('ws-status-text');

    try {
        const socket = new SockJS('/ws-cricket');
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // Suppress verbose console logs

        stompClient.connect({}, frame => {
            badge.style.borderColor = 'rgba(16, 185, 129, 0.4)';
            badge.style.color = '#34d399';
            badgeText.textContent = 'WEBSOCKET: LIVE';

            // Subscribe to all matches ticker updates
            stompClient.subscribe('/topic/matches', message => {
                const matchUpdate = JSON.parse(message.body);
                handleMatchListUpdate(matchUpdate);
            });

            // Subscribe to active match updates
            subscribeToActiveMatch();
        }, error => {
            badge.style.borderColor = 'rgba(245, 158, 11, 0.4)';
            badge.style.color = '#f59e0b';
            badgeText.textContent = 'WS: RECONNECTING';
            setTimeout(connectWebSocket, 4000);
        });
    } catch (e) {
        console.warn('SockJS failed, fallback to polling', e);
        badgeText.textContent = 'POLLING ACTIVE';
        setInterval(() => {
            if (activeMatchId) loadMatchDetails(activeMatchId);
        }, 3000);
    }
}

let activeMatchSubscription = null;
function subscribeToActiveMatch() {
    if (!stompClient || !stompClient.connected) return;
    if (activeMatchSubscription) {
        activeMatchSubscription.unsubscribe();
    }

    activeMatchSubscription = stompClient.subscribe('/topic/match/' + activeMatchId, message => {
        const data = JSON.parse(message.body);
        if (data.match) {
            updateMatchUI(data.match);
            if (data.latestBall) {
                prependCommentary(data.latestBall);
            }
        }
    });
}

// Load All Matches
async function loadMatches() {
    try {
        const res = await fetch('/api/matches');
        matchesList = await res.json();
        renderTicker();

        if (matchesList.length > 0) {
            // Find first live match or default to first match
            const liveMatch = matchesList.find(m => m.status === 'LIVE') || matchesList[0];
            setActiveMatch(liveMatch.id);
        }
    } catch (err) {
        console.error('Error loading matches:', err);
    }
}

function renderTicker() {
    const tickerContainer = document.getElementById('matches-ticker-list');
    tickerContainer.innerHTML = '';

    matchesList.forEach(m => {
        const card = document.createElement('div');
        card.className = `ticker-card ${m.id === activeMatchId ? 'active' : ''}`;
        card.setAttribute('id', `ticker-match-${m.id}`);

        let badgeClass = 'upcoming';
        if (m.status === 'LIVE') badgeClass = 'live';
        if (m.status === 'COMPLETED') badgeClass = 'completed';

        let scoreText = 'Upcoming';
        if (m.status === 'LIVE' || m.status === 'COMPLETED') {
            const batCode = m.battingTeam ? m.battingTeam.shortCode : m.team1.shortCode;
            scoreText = `${batCode} ${m.currentScore}/${m.currentWickets} (${m.currentOversFormatted})`;
        }

        card.innerHTML = `
            <span class="ticker-badge ${badgeClass}">${m.status}</span>
            <span class="ticker-teams">${m.team1.shortCode} vs ${m.team2.shortCode}</span>
            <span class="ticker-score">${scoreText}</span>
        `;

        card.addEventListener('click', () => {
            setActiveMatch(m.id);
        });

        tickerContainer.appendChild(card);
    });
}

function handleMatchListUpdate(updatedMatch) {
    const idx = matchesList.findIndex(m => m.id === updatedMatch.id);
    if (idx !== -1) {
        matchesList[idx] = updatedMatch;
    } else {
        matchesList.unshift(updatedMatch);
    }
    renderTicker();

    if (updatedMatch.id === activeMatchId) {
        updateMatchUI(updatedMatch);
    }
}

async function setActiveMatch(matchId) {
    activeMatchId = matchId;
    document.querySelectorAll('.ticker-card').forEach(c => c.classList.remove('active'));
    const activeCard = document.getElementById(`ticker-match-${matchId}`);
    if (activeCard) activeCard.classList.add('active');

    subscribeToActiveMatch();
    await loadMatchDetails(matchId);
    checkSimStatus(matchId);
    loadCommentary();
    loadScorecard();
    loadWormChart();
    loadSquads();
}

async function loadMatchDetails(matchId) {
    try {
        const res = await fetch(`/api/matches/${matchId}`);
        if (res.ok) {
            activeMatch = await res.json();
            updateMatchUI(activeMatch);
        }
    } catch (e) {
        console.error('Failed to load match details:', e);
    }
}

// Update Main Scoreboard UI
function updateMatchUI(m) {
    activeMatch = m;

    // Header info
    document.getElementById('match-series').textContent = m.series || 'Match';
    document.getElementById('match-venue').textContent = m.venue || 'Stadium';
    
    const stateBadge = document.getElementById('match-state-pill');
    stateBadge.textContent = m.status;
    stateBadge.className = `match-state-pill ${m.status.toLowerCase()}`;

    // Team 1
    document.getElementById('t1-flag').textContent = m.team1.flagEmoji || '🏏';
    document.getElementById('t1-name').textContent = m.team1.name;
    document.getElementById('t1-code').textContent = m.team1.shortCode;
    
    // Team 2
    document.getElementById('t2-flag').textContent = m.team2.flagEmoji || '🏏';
    document.getElementById('t2-name').textContent = m.team2.name;
    document.getElementById('t2-code').textContent = m.team2.shortCode;

    // Scores
    if (m.currentInningsNumber === 1) {
        document.getElementById('t1-score').textContent = `${m.currentScore}/${m.currentWickets}`;
        document.getElementById('t1-overs').textContent = `(${m.currentOversFormatted} / ${m.totalOvers} ov)`;
        document.getElementById('t2-score').textContent = 'Yet to bat';
        document.getElementById('t2-overs').textContent = '';
    } else {
        // Australia / Team 1 already batted
        document.getElementById('t1-score').textContent = `${m.team1Score}/${m.team1Wickets}`;
        document.getElementById('t1-overs').textContent = `(${m.team1Overs} ov)`;
        document.getElementById('t2-score').textContent = `${m.currentScore}/${m.currentWickets}`;
        document.getElementById('t2-overs').textContent = `(${m.currentOversFormatted} / ${m.totalOvers} ov)`;
    }

    // Equation / Status description
    const equationText = document.getElementById('match-equation-text');
    if (m.status === 'COMPLETED') {
        equationText.innerHTML = `🏆 <b>${m.winDescription || 'Match Finished'}</b>`;
        if (m.playerOfTheMatch) {
            equationText.innerHTML += ` &bull; Player of the Match: <span style="color:var(--accent-cyan);">${m.playerOfTheMatch}</span>`;
        }
    } else if (m.currentInningsNumber === 2 && m.targetRuns > 0) {
        const runsNeeded = m.targetRuns - m.currentScore;
        const totalBalls = m.totalOvers * 6;
        const ballsRemaining = Math.max(0, totalBalls - m.currentBalls);
        const batTeamName = m.battingTeam ? m.battingTeam.name : 'Batting Team';
        if (runsNeeded > 0) {
            equationText.innerHTML = `🎯 <b>${batTeamName}</b> need <b>${runsNeeded} runs</b> in <b>${ballsRemaining} balls</b> (Target: ${m.targetRuns})`;
        } else {
            equationText.innerHTML = `🎯 Target reached!`;
        }
    } else {
        const tossWinnerName = m.tossWinner ? m.tossWinner.name : 'Team';
        equationText.innerHTML = `🪙 ${tossWinnerName} won the toss and opted to ${m.tossDecision ? m.tossDecision.toLowerCase() : 'bat'}`;
    }

    document.getElementById('crr-val').textContent = m.currentRunRate || '0.00';
    document.getElementById('rrr-val').textContent = m.requiredRunRate > 0 ? m.requiredRunRate : 'N/A';

    // Ball Ticker Pills
    renderBallPills(m.lastBallsSummary);

    // Striker & Non-Striker
    renderBatsmen(m);

    // Bowler
    renderBowler(m);
}

function renderBallPills(summary) {
    const list = document.getElementById('current-over-pills');
    list.innerHTML = '';
    if (!summary || summary.trim() === '') {
        list.innerHTML = '<span style="color:var(--text-muted); font-size:0.8rem;">Waiting for delivery...</span>';
        return;
    }

    const balls = summary.split(' ');
    balls.forEach(b => {
        const pill = document.createElement('div');
        pill.className = 'ball-pill';
        pill.textContent = b;

        if (b === '•' || b === '0') pill.classList.add('b-dot');
        else if (b === '4') pill.classList.add('b-four');
        else if (b === '6') pill.classList.add('b-six');
        else if (b === 'W') pill.classList.add('b-wicket');
        else if (b.startsWith('Wd') || b.startsWith('Nb')) pill.classList.add('b-extra');
        else pill.classList.add('b-one');

        list.appendChild(pill);
    });
}

function renderBatsmen(m) {
    const strikerName = m.currentStrikerName || 'Striker';
    const strikerRuns = m.currentStrikerRuns || 0;
    const strikerBalls = m.currentStrikerBalls || 0;
    const strikerSr = strikerBalls > 0 ? ((strikerRuns / strikerBalls) * 100).toFixed(1) : '0.0';

    const nonStrikerName = m.currentNonStrikerName || 'Non-Striker';
    const nonStrikerRuns = m.currentNonStrikerRuns || 0;
    const nonStrikerBalls = m.currentNonStrikerBalls || 0;
    const nonStrikerSr = nonStrikerBalls > 0 ? ((nonStrikerRuns / nonStrikerBalls) * 100).toFixed(1) : '0.0';

    document.getElementById('striker-name').innerHTML = `${strikerName} <span class="strike-indicator">🏏*</span>`;
    document.getElementById('striker-stats').textContent = `${strikerRuns} (${strikerBalls})`;
    document.getElementById('striker-sr').textContent = `SR: ${strikerSr}`;

    document.getElementById('nonstriker-name').textContent = nonStrikerName;
    document.getElementById('nonstriker-stats').textContent = `${nonStrikerRuns} (${nonStrikerBalls})`;
    document.getElementById('nonstriker-sr').textContent = `SR: ${nonStrikerSr}`;
}

function renderBowler(m) {
    document.getElementById('bowler-name').textContent = m.currentBowlerName || 'Current Bowler';
    document.getElementById('bowler-figures').textContent = m.currentBowlerFigures || '0.0-0-0-0';
}

// Commentary
async function loadCommentary() {
    try {
        const res = await fetch(`/api/matches/${activeMatchId}/commentary?limit=40`);
        if (res.ok) {
            const list = await res.json();
            renderCommentaryList(list);
        }
    } catch (e) {
        console.error('Failed to load commentary:', e);
    }
}

function renderCommentaryList(balls) {
    const container = document.getElementById('commentary-feed');
    container.innerHTML = '';

    const filtered = balls.filter(b => {
        if (commentaryFilter === 'boundaries') return b.runsScored === 4 || b.runsScored === 6;
        if (commentaryFilter === 'wickets') return b.wicket;
        return true;
    });

    if (filtered.length === 0) {
        container.innerHTML = '<div style="color:var(--text-muted); padding:1rem; text-align:center;">No commentary events matching filter.</div>';
        return;
    }

    filtered.forEach(b => {
        const item = createCommentaryElement(b);
        container.appendChild(item);
    });
}

function prependCommentary(ball) {
    const container = document.getElementById('commentary-feed');
    const item = createCommentaryElement(ball);
    container.insertBefore(item, container.firstChild);
}

function createCommentaryElement(b) {
    const item = document.createElement('div');
    item.className = 'commentary-item';

    if (b.runsScored === 4) item.classList.add('boundary-four');
    else if (b.runsScored === 6) item.classList.add('boundary-six');
    else if (b.wicket) item.classList.add('wicket-ball');

    const overText = b.oversAfterBall || `${b.overNumber}.${b.ballInOver}`;

    item.innerHTML = `
        <div class="comm-over-col">${overText}</div>
        <div class="comm-text-col">${b.commentary || `${b.bowlerName} to ${b.strikerName}, ${b.totalRuns} runs.`}</div>
    `;
    return item;
}

// Scorecard
async function loadScorecard() {
    try {
        const res = await fetch(`/api/matches/${activeMatchId}/scorecard`);
        if (res.ok) {
            const data = await res.json();
            renderScorecard(data);
        }
    } catch (e) {
        console.error('Failed to load scorecard:', e);
    }
}

function renderScorecard(data) {
    const container = document.getElementById('scorecard-wrapper');
    container.innerHTML = '';

    if (!data.innings1 && !data.innings2) {
        container.innerHTML = '<div style="color:var(--text-muted); text-align:center;">Scorecard not available yet.</div>';
        return;
    }

    if (data.innings1) {
        container.appendChild(createInningsSection(data.innings1, 1));
    }
    if (data.innings2) {
        container.appendChild(createInningsSection(data.innings2, 2));
    }
}

function createInningsSection(sec, num) {
    const card = document.createElement('div');
    card.className = 'innings-card';

    const batTeam = sec.innings.battingTeam ? sec.innings.battingTeam.name : `Innings ${num}`;
    const extrasTotal = sec.innings.wides + sec.innings.noBalls + sec.innings.byes + sec.innings.legByes;

    let batRows = '';
    sec.batting.forEach(b => {
        let nameDisplay = b.player.name;
        if (b.onStrike) nameDisplay += ' <span style="color:var(--accent-emerald)">*</span>';
        batRows += `
            <tr>
                <td class="font-bold">${nameDisplay}</td>
                <td style="color:var(--text-muted)">${b.dismissalDescription || (b.out ? 'out' : 'not out')}</td>
                <td class="text-right font-bold">${b.runs}</td>
                <td class="text-right">${b.balls}</td>
                <td class="text-right">${b.fours}</td>
                <td class="text-right">${b.sixes}</td>
                <td class="text-right">${b.strikeRate.toFixed(1)}</td>
            </tr>
        `;
    });

    let bowlRows = '';
    sec.bowling.forEach(bw => {
        bowlRows += `
            <tr>
                <td class="font-bold">${bw.player.name}</td>
                <td class="text-right">${bw.oversBowled}</td>
                <td class="text-right">${bw.maidens}</td>
                <td class="text-right">${bw.runsConceded}</td>
                <td class="text-right font-bold" style="color:var(--accent-emerald)">${bw.wickets}</td>
                <td class="text-right">${bw.economyRate.toFixed(2)}</td>
                <td class="text-right">${bw.dotBalls}</td>
            </tr>
        `;
    });

    card.innerHTML = `
        <div class="innings-title-row">
            <h3>${batTeam} Innings</h3>
            <div class="innings-total-badge">${sec.innings.totalRuns} / ${sec.innings.totalWickets} <span style="font-size:0.85rem; color:var(--text-secondary);">(${sec.innings.oversFormatted} ov)</span></div>
        </div>

        <table class="custom-table">
            <thead>
                <tr>
                    <th>Batter</th>
                    <th>Dismissal</th>
                    <th class="text-right">R</th>
                    <th class="text-right">B</th>
                    <th class="text-right">4s</th>
                    <th class="text-right">6s</th>
                    <th class="text-right">SR</th>
                </tr>
            </thead>
            <tbody>${batRows}</tbody>
        </table>

        <div class="extras-row">
            <span>Extras: <b>${extrasTotal}</b> (b ${sec.innings.byes}, lb ${sec.innings.legByes}, w ${sec.innings.wides}, nb ${sec.innings.noBalls})</span>
            <span>Total: <b>${sec.innings.totalRuns}/${sec.innings.totalWickets}</b> in ${sec.innings.oversFormatted} ov</span>
        </div>

        <h4 style="margin: 1.25rem 0 0.5rem 0; font-size:0.95rem; color:var(--text-secondary)">Bowling</h4>
        <table class="custom-table">
            <thead>
                <tr>
                    <th>Bowler</th>
                    <th class="text-right">O</th>
                    <th class="text-right">M</th>
                    <th class="text-right">R</th>
                    <th class="text-right">W</th>
                    <th class="text-right">ECON</th>
                    <th class="text-right">DOTS</th>
                </tr>
            </thead>
            <tbody>${bowlRows}</tbody>
        </table>
    `;

    return card;
}

// Worm Chart Canvas
async function loadWormChart() {
    try {
        const res = await fetch(`/api/matches/${activeMatchId}/worm`);
        if (res.ok) {
            const data = await res.json();
            drawWormChart(data);
        }
    } catch (e) {
        console.error('Failed to load worm chart:', e);
    }
}

function drawWormChart(data) {
    const canvas = document.getElementById('worm-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const width = canvas.width = canvas.parentElement.clientWidth;
    const height = canvas.height = 300;

    ctx.clearRect(0, 0, width, height);

    const padding = { top: 30, right: 30, bottom: 40, left: 50 };
    const chartW = width - padding.left - padding.right;
    const chartH = height - padding.top - padding.bottom;

    const maxOvers = 20;
    const maxRuns = Math.max(
        220,
        ...((data.team1Progression || []).map(p => p.cumulativeRuns)),
        ...((data.team2Progression || []).map(p => p.cumulativeRuns))
    );

    // Draw grid lines
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.06)';
    ctx.lineWidth = 1;
    ctx.fillStyle = '#64748b';
    ctx.font = '11px sans-serif';

    // Horizontal run grids (every 50 runs)
    for (let r = 0; r <= maxRuns; r += 50) {
        const y = padding.top + chartH - (r / maxRuns) * chartH;
        ctx.beginPath();
        ctx.moveTo(padding.left, y);
        ctx.lineTo(padding.left + chartW, y);
        ctx.stroke();
        ctx.fillText(r, 15, y + 4);
    }

    // Vertical over grids (every 5 overs)
    for (let o = 0; o <= maxOvers; o += 5) {
        const x = padding.left + (o / maxOvers) * chartW;
        ctx.beginPath();
        ctx.moveTo(x, padding.top);
        ctx.lineTo(x, padding.top + chartH);
        ctx.stroke();
        ctx.fillText(`${o} ov`, x - 12, height - 15);
    }

    // Draw Team 1 Line (Blue)
    drawLine(ctx, data.team1Progression, '#38bdf8', maxOvers, maxRuns, padding, chartW, chartH);
    // Draw Team 2 Line (Emerald Green)
    drawLine(ctx, data.team2Progression, '#10b981', maxOvers, maxRuns, padding, chartW, chartH);

    // Update legend names
    document.getElementById('legend-t1-name').textContent = data.team1Name || 'Team 1';
    document.getElementById('legend-t2-name').textContent = data.team2Name || 'Team 2';
}

function drawLine(ctx, points, color, maxOvers, maxRuns, padding, chartW, chartH) {
    if (!points || points.length === 0) return;

    ctx.strokeStyle = color;
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(padding.left, padding.top + chartH);

    points.forEach(p => {
        const x = padding.left + (p.over / maxOvers) * chartW;
        const y = padding.top + chartH - (p.cumulativeRuns / maxRuns) * chartH;
        ctx.lineTo(x, y);
    });
    ctx.stroke();

    // Wicket markers
    points.forEach(p => {
        if (p.wickets > 0) {
            const x = padding.left + (p.over / maxOvers) * chartW;
            const y = padding.top + chartH - (p.cumulativeRuns / maxRuns) * chartH;
            ctx.fillStyle = '#ef4444';
            ctx.beginPath();
            ctx.arc(x, y, 4, 0, Math.PI * 2);
            ctx.fill();
        }
    });
}

// Leaderboard
async function loadLeaderboard() {
    try {
        const res = await fetch('/api/stats/leaderboard');
        if (res.ok) {
            const data = await res.json();
            renderLeaderboard(data);
        }
    } catch (e) {
        console.error('Failed to load leaderboard:', e);
    }
}

function renderLeaderboard(data) {
    const batContainer = document.getElementById('leaderboard-batsmen');
    const bowlContainer = document.getElementById('leaderboard-bowlers');

    let batHtml = `
        <table class="custom-table">
            <thead>
                <tr>
                    <th>Player</th>
                    <th>Team</th>
                    <th class="text-right">Runs</th>
                    <th class="text-right">HS</th>
                    <th class="text-right">SR</th>
                </tr>
            </thead>
            <tbody>
    `;
    data.topRunScorers.forEach(p => {
        batHtml += `
            <tr>
                <td class="font-bold">${p.name}</td>
                <td><span class="ticker-badge" style="background:rgba(255,255,255,0.08);">${p.team ? p.team.shortCode : ''}</span></td>
                <td class="text-right font-bold" style="color:var(--accent-gold);">${p.totalRuns}</td>
                <td class="text-right">${p.highestScore}</td>
                <td class="text-right">${p.battingStrikeRate.toFixed(1)}</td>
            </tr>
        `;
    });
    batHtml += '</tbody></table>';
    batContainer.innerHTML = batHtml;

    let bowlHtml = `
        <table class="custom-table">
            <thead>
                <tr>
                    <th>Player</th>
                    <th>Team</th>
                    <th class="text-right">Wickets</th>
                    <th class="text-right">Econ</th>
                </tr>
            </thead>
            <tbody>
    `;
    data.topWicketTakers.forEach(p => {
        bowlHtml += `
            <tr>
                <td class="font-bold">${p.name}</td>
                <td><span class="ticker-badge" style="background:rgba(255,255,255,0.08);">${p.team ? p.team.shortCode : ''}</span></td>
                <td class="text-right font-bold" style="color:var(--accent-purple);">${p.totalWickets}</td>
                <td class="text-right">${p.bowlingEconomy.toFixed(2)}</td>
            </tr>
        `;
    });
    bowlHtml += '</tbody></table>';
    bowlContainer.innerHTML = bowlHtml;
}

// Squads
async function loadSquads() {
    if (!activeMatch) return;
    try {
        const [p1, p2] = await Promise.all([
            fetch(`/api/stats/teams/${activeMatch.team1.id}/players`).then(r => r.json()),
            fetch(`/api/stats/teams/${activeMatch.team2.id}/players`).then(r => r.json())
        ]);

        const container = document.getElementById('squads-wrapper');
        container.innerHTML = `
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:1.5rem;">
                <div class="in-play-card">
                    <h3 style="margin-bottom:1rem; color:var(--accent-cyan);">${activeMatch.team1.name} Playing XI</h3>
                    ${renderPlayerList(p1)}
                </div>
                <div class="in-play-card">
                    <h3 style="margin-bottom:1rem; color:var(--accent-emerald);">${activeMatch.team2.name} Playing XI</h3>
                    ${renderPlayerList(p2)}
                </div>
            </div>
        `;
    } catch (e) {
        console.error('Failed to load squads:', e);
    }
}

function renderPlayerList(players) {
    let html = '<div style="display:flex; flex-direction:column; gap:0.5rem;">';
    players.forEach(p => {
        html += `
            <div style="display:flex; justify-content:space-between; align-items:center; padding:0.4rem 0; border-bottom:1px solid rgba(255,255,255,0.04);">
                <div>
                    <span class="font-bold">${p.name}</span>
                    <div style="font-size:0.75rem; color:var(--text-muted);">${p.battingStyle || ''} &bull; ${p.bowlingStyle || ''}</div>
                </div>
                <span class="ticker-badge" style="background:rgba(255,255,255,0.05); color:var(--text-secondary);">${p.role}</span>
            </div>
        `;
    });
    html += '</div>';
    return html;
}

// Simulation Controls
async function toggleSimulation() {
    const btn = document.getElementById('btn-sim-toggle');
    const indicator = document.getElementById('sim-indicator');
    const interval = document.getElementById('sim-speed-select').value;

    if (isSimulating) {
        const res = await fetch(`/api/simulation/pause/${activeMatchId}`, { method: 'POST' });
        if (res.ok) {
            isSimulating = false;
            btn.innerHTML = '▶ Start Auto Simulation';
            btn.className = 'btn btn-success';
            indicator.textContent = 'SIMULATION: PAUSED';
            indicator.className = 'sim-indicator paused';
        }
    } else {
        const res = await fetch(`/api/simulation/start/${activeMatchId}?intervalMs=${interval}`, { method: 'POST' });
        if (res.ok) {
            isSimulating = true;
            btn.innerHTML = '⏸ Pause Simulation';
            btn.className = 'btn btn-warning';
            indicator.textContent = 'SIMULATION: RUNNING';
            indicator.className = 'sim-indicator active';
        }
    }
}

async function stepSimulation() {
    try {
        const res = await fetch(`/api/simulation/step/${activeMatchId}`, { method: 'POST' });
        if (res.ok) {
            const m = await res.json();
            updateMatchUI(m);
            loadCommentary();
        }
    } catch (e) {
        console.error('Step simulation error:', e);
    }
}

async function changeSimSpeed(e) {
    const interval = e.target.value;
    await fetch(`/api/simulation/speed/${activeMatchId}?intervalMs=${interval}`, { method: 'POST' });
}

async function resetMatch() {
    if (!confirm('Are you sure you want to reset this match back to 0.0 overs?')) return;
    try {
        const res = await fetch(`/api/simulation/reset/${activeMatchId}`, { method: 'POST' });
        if (res.ok) {
            const m = await res.json();
            updateMatchUI(m);
            loadCommentary();
            loadScorecard();
            loadWormChart();
        }
    } catch (e) {
        console.error('Reset error:', e);
    }
}

async function checkSimStatus(matchId) {
    try {
        const res = await fetch(`/api/simulation/status/${matchId}`);
        if (res.ok) {
            const data = await res.json();
            isSimulating = data.isSimulating;
            const btn = document.getElementById('btn-sim-toggle');
            const indicator = document.getElementById('sim-indicator');
            if (isSimulating) {
                btn.innerHTML = '⏸ Pause Simulation';
                btn.className = 'btn btn-warning';
                indicator.textContent = 'SIMULATION: RUNNING';
                indicator.className = 'sim-indicator active';
            } else {
                btn.innerHTML = '▶ Start Auto Simulation';
                btn.className = 'btn btn-success';
                indicator.textContent = 'SIMULATION: PAUSED';
                indicator.className = 'sim-indicator paused';
            }
        }
    } catch (e) {}
}

// Manual Scoring Modal Handlers
function openModal(id) {
    document.getElementById(id).classList.add('active');
}
function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

async function submitManualBall() {
    const commentaryCustom = document.getElementById('scorer-commentary').value;
    const wicketType = document.getElementById('scorer-wicket-type').value;

    const payload = {
        runsScored: manualBallState.runs,
        extraType: manualBallState.extraType,
        extraRuns: (manualBallState.extraType === 'WIDE' || manualBallState.extraType === 'NO_BALL') ? 1 : 0,
        isWicket: manualBallState.isWicket,
        wicketType: manualBallState.isWicket ? wicketType : 'NONE',
        commentary: commentaryCustom
    };

    try {
        const res = await fetch(`/api/scorer/matches/${activeMatchId}/ball`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            closeModal('modal-scorer');
            document.getElementById('scorer-commentary').value = '';
            document.getElementById('scorer-is-wicket').checked = false;
            document.getElementById('wicket-details-row').style.display = 'none';
            const m = await res.json();
            updateMatchUI(m);
            loadCommentary();
        }
    } catch (e) {
        console.error('Manual ball submit failed:', e);
    }
}

// Create Custom Match
async function populateCreateMatchTeams() {
    try {
        const res = await fetch('/api/stats/teams');
        const teams = await res.json();
        const t1Select = document.getElementById('new-match-t1');
        const t2Select = document.getElementById('new-match-t2');
        t1Select.innerHTML = '';
        t2Select.innerHTML = '';

        teams.forEach(t => {
            t1Select.innerHTML += `<option value="${t.id}">${t.name} (${t.shortCode})</option>`;
            t2Select.innerHTML += `<option value="${t.id}">${t.name} (${t.shortCode})</option>`;
        });
        if (teams.length > 1) t2Select.selectedIndex = 1;
    } catch (e) {}
}

async function submitCreateMatch() {
    const title = document.getElementById('new-match-title').value;
    const series = document.getElementById('new-match-series').value;
    const venue = document.getElementById('new-match-venue').value;
    const overs = parseInt(document.getElementById('new-match-overs').value);
    const team1Id = parseInt(document.getElementById('new-match-t1').value);
    const team2Id = parseInt(document.getElementById('new-match-t2').value);

    if (team1Id === team2Id) {
        alert('Please select two distinct teams.');
        return;
    }

    const payload = {
        title: title || 'Custom Match',
        series: series || 'T20 Series',
        venue: venue || 'International Stadium',
        matchType: 'T20',
        totalOvers: overs || 20,
        team1Id: team1Id,
        team2Id: team2Id,
        tossWinnerId: team1Id,
        tossDecision: 'BAT'
    };

    try {
        const res = await fetch('/api/matches', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            closeModal('modal-create-match');
            await loadMatches();
            const created = await res.json();
            setActiveMatch(created.id);
        }
    } catch (e) {
        console.error('Failed to create match:', e);
    }
}
