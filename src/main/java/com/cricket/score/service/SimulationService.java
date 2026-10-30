package com.cricket.score.service;

import com.cricket.score.dto.LiveBallRequestDto;
import com.cricket.score.model.*;
import com.cricket.score.repository.BallEventRepository;
import com.cricket.score.repository.BatsmanScoreRepository;
import com.cricket.score.repository.BowlerScoreRepository;
import com.cricket.score.repository.InningsRepository;
import com.cricket.score.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;

@Service
public class SimulationService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private InningsRepository inningsRepository;

    @Autowired
    private BatsmanScoreRepository batsmanScoreRepository;

    @Autowired
    private BowlerScoreRepository bowlerScoreRepository;

    @Autowired
    private BallEventRepository ballEventRepository;

    @Autowired
    private ScoringEngineService scoringEngineService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<Long, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();
    private final Map<Long, Long> matchIntervals = new ConcurrentHashMap<>();

    private final Random random = new Random();

    public synchronized boolean startSimulation(Long matchId, Long intervalMs) {
        if (runningTasks.containsKey(matchId) && !runningTasks.get(matchId).isCancelled()) {
            return true; // Already running
        }

        long delay = (intervalMs != null && intervalMs >= 300) ? intervalMs : 1500L;
        matchIntervals.put(matchId, delay);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                Match match = matchRepository.findById(matchId).orElse(null);
                if (match == null || match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.ABANDONED) {
                    stopSimulation(matchId);
                    return;
                }

                stepSimulation(matchId);
            } catch (Exception e) {
                // Keep simulation robust against sporadic errors
                e.printStackTrace();
            }
        }, 500, delay, TimeUnit.MILLISECONDS);

        runningTasks.put(matchId, future);
        return true;
    }

    public synchronized boolean stopSimulation(Long matchId) {
        ScheduledFuture<?> future = runningTasks.remove(matchId);
        if (future != null) {
            future.cancel(false);
            return true;
        }
        return false;
    }

    public synchronized boolean isSimulating(Long matchId) {
        ScheduledFuture<?> future = runningTasks.get(matchId);
        return future != null && !future.isCancelled() && !future.isDone();
    }

    public synchronized boolean setSpeed(Long matchId, Long intervalMs) {
        if (isSimulating(matchId)) {
            stopSimulation(matchId);
            startSimulation(matchId, intervalMs);
            return true;
        }
        matchIntervals.put(matchId, intervalMs);
        return false;
    }

    public Match stepSimulation(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.ABANDONED) {
            stopSimulation(matchId);
            return match;
        }

        // Generate probabilistic realistic ball outcome
        LiveBallRequestDto ballReq = generateRealisticBall(match);
        return scoringEngineService.processBall(matchId, ballReq);
    }

    private LiveBallRequestDto generateRealisticBall(Match match) {
        LiveBallRequestDto req = new LiveBallRequestDto();
        int over = (match.getCurrentBalls() / 6) + 1;
        boolean isPowerplay = (over <= 6);
        boolean isDeathOvers = (over >= 16);

        // Extras chance (3% total: 2% wide, 1% no-ball)
        int extraRoll = random.nextInt(100);
        if (extraRoll < 2) {
            req.setExtraType(ExtraType.WIDE);
            req.setExtraRuns(1);
            req.setRunsScored(0);
            return req;
        } else if (extraRoll == 2) {
            req.setExtraType(ExtraType.NO_BALL);
            req.setExtraRuns(1);
            req.setRunsScored(random.nextInt(3)); // Batsman can score off no-ball
            return req;
        }

        // Wicket probability
        int wicketThreshold = isDeathOvers ? 8 : (isPowerplay ? 5 : 4); // percentage
        int roll = random.nextInt(100);

        if (roll < wicketThreshold && match.getCurrentWickets() < 10) {
            req.setWicket(true);
            int wTypeRoll = random.nextInt(100);
            if (wTypeRoll < 45) {
                req.setWicketType(WicketType.CAUGHT);
                req.setFielderName(pickRandomFielder());
            } else if (wTypeRoll < 75) {
                req.setWicketType(WicketType.BOWLED);
            } else if (wTypeRoll < 90) {
                req.setWicketType(WicketType.LBW);
            } else {
                req.setWicketType(WicketType.RUN_OUT);
                req.setFielderName(pickRandomFielder());
            }
            req.setRunsScored(0);
            return req;
        }

        // Normal run distribution based on match situation
        int runRoll = random.nextInt(100);
        if (isDeathOvers) {
            if (runRoll < 25) req.setRunsScored(0);
            else if (runRoll < 55) req.setRunsScored(1);
            else if (runRoll < 70) req.setRunsScored(2);
            else if (runRoll < 85) req.setRunsScored(4);
            else req.setRunsScored(6);
        } else if (isPowerplay) {
            if (runRoll < 35) req.setRunsScored(0);
            else if (runRoll < 65) req.setRunsScored(1);
            else if (runRoll < 75) req.setRunsScored(2);
            else if (runRoll < 93) req.setRunsScored(4);
            else req.setRunsScored(6);
        } else {
            // Middle overs
            if (runRoll < 40) req.setRunsScored(0);
            else if (runRoll < 75) req.setRunsScored(1);
            else if (runRoll < 87) req.setRunsScored(2);
            else if (runRoll < 89) req.setRunsScored(3);
            else if (runRoll < 97) req.setRunsScored(4);
            else req.setRunsScored(6);
        }

        return req;
    }

    private String pickRandomFielder() {
        String[] fielders = {"Deep Cover", "Mid-off", "Long-on", "Slip", "Point", "Square Leg", "Wicketkeeper"};
        return fielders[random.nextInt(fielders.length)];
    }

    @Transactional
    public Match resetMatch(Long matchId) {
        stopSimulation(matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        // Delete all ball events for this match
        List<BallEvent> balls = ballEventRepository.findByMatchIdOrderByTimestampDesc(matchId);
        ballEventRepository.deleteAll(balls);

        // Delete innings 2 if exists
        inningsRepository.findByMatchIdAndInningsNumber(matchId, 2).ifPresent(inningsRepository::delete);
        List<BatsmanScore> bat2 = batsmanScoreRepository.findByMatchIdAndInningsNumberOrderByBattingPositionAsc(matchId, 2);
        batsmanScoreRepository.deleteAll(bat2);
        List<BowlerScore> bowl2 = bowlerScoreRepository.findByMatchIdAndInningsNumberOrderByBowlingPositionAsc(matchId, 2);
        bowlerScoreRepository.deleteAll(bowl2);

        // Reset Innings 1
        Innings inn1 = inningsRepository.findByMatchIdAndInningsNumber(matchId, 1).orElse(null);
        if (inn1 != null) {
            inn1.setTotalRuns(0);
            inn1.setTotalWickets(0);
            inn1.setTotalBalls(0);
            inn1.setOversFormatted("0.0");
            inn1.setWides(0);
            inn1.setNoBalls(0);
            inn1.setByes(0);
            inn1.setLegByes(0);
            inn1.setCompleted(false);
            inningsRepository.save(inn1);
        }

        // Reset Batsmen 1
        List<BatsmanScore> bat1 = batsmanScoreRepository.findByMatchIdAndInningsNumberOrderByBattingPositionAsc(matchId, 1);
        for (int i = 0; i < bat1.size(); i++) {
            BatsmanScore b = bat1.get(i);
            b.setRuns(0);
            b.setBalls(0);
            b.setFours(0);
            b.setSixes(0);
            b.setStrikeRate(0.0);
            b.setOut(false);
            b.setDismissalType(WicketType.NONE);
            b.setBowlerName(null);
            b.setFielderName(null);
            b.setHasBatted(i < 2);
            b.setOnStrike(i == 0);
            b.setNonStriker(i == 1);
            batsmanScoreRepository.save(b);
        }

        // Reset Bowlers 1
        List<BowlerScore> bowl1 = bowlerScoreRepository.findByMatchIdAndInningsNumberOrderByBowlingPositionAsc(matchId, 1);
        for (int i = 0; i < bowl1.size(); i++) {
            BowlerScore b = bowl1.get(i);
            b.setBallsBowled(0);
            b.setOversBowled("0.0");
            b.setRunsConceded(0);
            b.setWickets(0);
            b.setMaidens(0);
            b.setEconomyRate(0.0);
            b.setDotBalls(0);
            b.setCurrentBowler(i == 0);
            bowlerScoreRepository.save(b);
        }

        // Reset Match state
        match.setStatus(MatchStatus.LIVE);
        match.setCurrentInningsNumber(1);
        match.setCurrentScore(0);
        match.setCurrentWickets(0);
        match.setCurrentBalls(0);
        match.setCurrentOversFormatted("0.0");
        match.setTeam1Score(0);
        match.setTeam1Wickets(0);
        match.setTeam1Overs("0.0");
        match.setTeam2Score(0);
        match.setTeam2Wickets(0);
        match.setTeam2Overs("0.0");
        match.setTargetRuns(0);
        match.setCurrentRunRate(0.0);
        match.setRequiredRunRate(0.0);
        match.setWinnerTeam(null);
        match.setWinDescription(null);
        match.setPlayerOfTheMatch(null);
        match.setLastBallsSummary("");

        if (!bat1.isEmpty()) match.setCurrentStrikerName(bat1.get(0).getPlayer().getName());
        if (bat1.size() > 1) match.setCurrentNonStrikerName(bat1.get(1).getPlayer().getName());
        if (!bowl1.isEmpty()) {
            match.setCurrentBowlerName(bowl1.get(0).getPlayer().getName());
            match.setCurrentBowlerFigures("0.0-0-0-0");
        }

        return matchRepository.save(match);
    }
}
