package com.cricket.score.service;

import com.cricket.score.dto.LiveBallRequestDto;
import com.cricket.score.model.*;
import com.cricket.score.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ScoringEngineService {

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
    private PlayerRepository playerRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MatchService matchService;

    @Transactional
    public Match processBall(Long matchId, LiveBallRequestDto request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.ABANDONED) {
            return match;
        }

        int inningsNum = match.getCurrentInningsNumber();
        Innings innings = inningsRepository.findByMatchIdAndInningsNumber(matchId, inningsNum)
                .orElseThrow(() -> new IllegalStateException("Innings " + inningsNum + " not found for match " + matchId));

        // Get Current Striker, Non-striker, and Bowler
        BatsmanScore striker = batsmanScoreRepository.findByMatchIdAndInningsNumberAndIsOnStrikeTrue(matchId, inningsNum)
                .orElse(null);
        BatsmanScore nonStriker = batsmanScoreRepository.findByMatchIdAndInningsNumberAndIsNonStrikerTrue(matchId, inningsNum)
                .orElse(null);
        BowlerScore bowler = bowlerScoreRepository.findByMatchIdAndInningsNumberAndIsCurrentBowlerTrue(matchId, inningsNum)
                .orElse(null);

        // Fallbacks if not initialized
        if (striker == null || nonStriker == null || bowler == null) {
            initParticipantsIfMissing(matchId, inningsNum);
            striker = batsmanScoreRepository.findByMatchIdAndInningsNumberAndIsOnStrikeTrue(matchId, inningsNum).orElse(null);
            nonStriker = batsmanScoreRepository.findByMatchIdAndInningsNumberAndIsNonStrikerTrue(matchId, inningsNum).orElse(null);
            bowler = bowlerScoreRepository.findByMatchIdAndInningsNumberAndIsCurrentBowlerTrue(matchId, inningsNum).orElse(null);
        }

        ExtraType extraType = request.getExtraType() != null ? request.getExtraType() : ExtraType.NONE;
        int runsScored = request.getRunsScored();
        int extraRuns = request.getExtraRuns();

        // Calculate if delivery is legal ball
        boolean isLegalBall = (extraType != ExtraType.WIDE && extraType != ExtraType.NO_BALL);

        // Adjust extra runs if default
        if (extraType == ExtraType.WIDE && extraRuns == 0) extraRuns = 1;
        if (extraType == ExtraType.NO_BALL && extraRuns == 0) extraRuns = 1;

        int totalBallRuns = runsScored + extraRuns;

        // 1. Update Innings Score & Extras
        innings.setTotalRuns(innings.getTotalRuns() + totalBallRuns);
        if (extraType == ExtraType.WIDE) innings.setWides(innings.getWides() + extraRuns);
        if (extraType == ExtraType.NO_BALL) innings.setNoBalls(innings.getNoBalls() + extraRuns);
        if (extraType == ExtraType.BYE) innings.setByes(innings.getByes() + extraRuns);
        if (extraType == ExtraType.LEG_BYE) innings.setLegByes(innings.getLegByes() + extraRuns);

        if (isLegalBall) {
            innings.setTotalBalls(innings.getTotalBalls() + 1);
        }

        // 2. Update Batsman Score
        if (striker != null) {
            if (isLegalBall || extraType == ExtraType.NO_BALL) {
                // Batsman faces ball on legal deliveries and no-balls
                striker.setBalls(striker.getBalls() + 1);
            }
            if (runsScored > 0) {
                striker.setRuns(striker.getRuns() + runsScored);
                if (runsScored == 4) striker.setFours(striker.getFours() + 1);
                if (runsScored == 6) striker.setSixes(striker.getSixes() + 1);
            }
            striker.calculateStrikeRate();
            batsmanScoreRepository.save(striker);
        }

        // 3. Update Bowler Figures
        if (bowler != null) {
            if (isLegalBall) {
                bowler.setBallsBowled(bowler.getBallsBowled() + 1);
                if (totalBallRuns == 0) {
                    bowler.setDotBalls(bowler.getDotBalls() + 1);
                }
            }
            // Byes and leg-byes are not conceded by bowler
            if (extraType != ExtraType.BYE && extraType != ExtraType.LEG_BYE) {
                bowler.setRunsConceded(bowler.getRunsConceded() + totalBallRuns);
            }
            if (extraType == ExtraType.WIDE) bowler.setWides(bowler.getWides() + extraRuns);
            if (extraType == ExtraType.NO_BALL) bowler.setNoBalls(bowler.getNoBalls() + extraRuns);

            if (request.isWicket() && request.getWicketType() != WicketType.RUN_OUT) {
                bowler.setWickets(bowler.getWickets() + 1);
            }
            bowler.calculateEconomy();
            bowlerScoreRepository.save(bowler);
        }

        // 4. Handle Wicket
        boolean isWicket = request.isWicket();
        WicketType wicketType = request.getWicketType() != null ? request.getWicketType() : WicketType.NONE;
        String dismissedPlayerName = null;

        if (isWicket) {
            innings.setTotalWickets(innings.getTotalWickets() + 1);
            if (striker != null) {
                striker.setOut(true);
                striker.setDismissalType(wicketType != WicketType.NONE ? wicketType : WicketType.BOWLED);
                striker.setBowlerName(bowler != null ? bowler.getPlayer().getName() : "Bowler");
                striker.setFielderName(request.getFielderName());
                striker.setOnStrike(false);
                dismissedPlayerName = striker.getPlayer().getName();
                batsmanScoreRepository.save(striker);

                // Update player career stats
                Player player = striker.getPlayer();
                player.setTotalRuns(player.getTotalRuns() + striker.getRuns());
                player.setTotalBallsFaced(player.getTotalBallsFaced() + striker.getBalls());
                if (striker.getRuns() > player.getHighestScore()) player.setHighestScore(striker.getRuns());
                playerRepository.save(player);

                // Bring next batsman in order
                bringNextBatsman(matchId, inningsNum);
            }
        }

        // 5. Strike Rotation Logic
        // On odd runs scored, rotate strike
        boolean rotateDueToRuns = (runsScored % 2 != 0);
        if (rotateDueToRuns && !isWicket) {
            swapStrike(matchId, inningsNum);
        }

        // Check Over Completion (6 legal balls)
        int currentBalls = innings.getTotalBalls();
        boolean isOverCompleted = isLegalBall && (currentBalls % 6 == 0);
        int overNumber = (currentBalls == 0) ? 1 : ((currentBalls - 1) / 6) + 1;
        int ballInOver = (currentBalls == 0) ? 1 : ((currentBalls - 1) % 6) + 1;

        if (isOverCompleted) {
            // Check for maiden over: if bowler conceded 0 runs in this over
            // Swap strike at the end of the over
            swapStrike(matchId, inningsNum);
            // Rotate bowler
            rotateBowler(matchId, inningsNum, bowler != null ? bowler.getId() : null);
        }

        inningsRepository.save(innings);

        // 6. Generate Realistic Commentary if not provided
        String commentary = request.getCommentary();
        if (commentary == null || commentary.trim().isEmpty()) {
            commentary = generateCommentary(
                    striker != null ? striker.getPlayer().getName() : "Striker",
                    bowler != null ? bowler.getPlayer().getName() : "Bowler",
                    runsScored, extraType, extraRuns, isWicket, wicketType, request.getFielderName()
            );
        }

        // 7. Record Ball Event
        BallEvent ballEvent = new BallEvent();
        ballEvent.setMatchId(matchId);
        ballEvent.setInningsNumber(inningsNum);
        ballEvent.setOverNumber(overNumber);
        ballEvent.setBallInOver(ballInOver);
        ballEvent.setStrikerName(striker != null ? striker.getPlayer().getName() : "");
        ballEvent.setNonStrikerName(nonStriker != null ? nonStriker.getPlayer().getName() : "");
        ballEvent.setBowlerName(bowler != null ? bowler.getPlayer().getName() : "");
        ballEvent.setRunsScored(runsScored);
        ballEvent.setExtraType(extraType);
        ballEvent.setExtraRuns(extraRuns);
        ballEvent.setTotalRuns(totalBallRuns);
        ballEvent.setWicket(isWicket);
        ballEvent.setWicketType(wicketType);
        ballEvent.setDismissedPlayerName(dismissedPlayerName);
        ballEvent.setFielderName(request.getFielderName());
        ballEvent.setCommentary(commentary);
        ballEvent.setScoreAfterBall(innings.getTotalRuns());
        ballEvent.setWicketsAfterBall(innings.getTotalWickets());
        ballEvent.setOversAfterBall(innings.getOversFormatted());
        ballEvent.setTimestamp(LocalDateTime.now());
        ballEventRepository.save(ballEvent);

        // 8. Update Match Live Snapshot
        match.setCurrentScore(innings.getTotalRuns());
        match.setCurrentWickets(innings.getTotalWickets());
        match.setCurrentBalls(innings.getTotalBalls());
        match.setCurrentOversFormatted(innings.getOversFormatted());

        // Update Over summary ticker (e.g. "1 4 0 W 6")
        updateLastBallsSummary(match, ballEvent);

        // Calculate CRR
        double totalOversDec = innings.getTotalBalls() / 6.0;
        if (totalOversDec > 0) {
            match.setCurrentRunRate(Math.round((innings.getTotalRuns() / totalOversDec) * 100.0) / 100.0);
        }

        // Reload striker & non-striker & bowler for match snapshot
        BatsmanScore currentStriker = batsmanScoreRepository.findByMatchIdAndInningsNumberAndIsOnStrikeTrue(matchId, inningsNum).orElse(null);
        BatsmanScore currentNonStriker = batsmanScoreRepository.findByMatchIdAndInningsNumberAndIsNonStrikerTrue(matchId, inningsNum).orElse(null);
        BowlerScore currentBowler = bowlerScoreRepository.findByMatchIdAndInningsNumberAndIsCurrentBowlerTrue(matchId, inningsNum).orElse(null);

        if (currentStriker != null) {
            match.setCurrentStrikerName(currentStriker.getPlayer().getName());
            match.setCurrentStrikerRuns(currentStriker.getRuns());
            match.setCurrentStrikerBalls(currentStriker.getBalls());
        }
        if (currentNonStriker != null) {
            match.setCurrentNonStrikerName(currentNonStriker.getPlayer().getName());
            match.setCurrentNonStrikerRuns(currentNonStriker.getRuns());
            match.setCurrentNonStrikerBalls(currentNonStriker.getBalls());
        }
        if (currentBowler != null) {
            match.setCurrentBowlerName(currentBowler.getPlayer().getName());
            match.setCurrentBowlerFigures(currentBowler.getFiguresFormatted());
        }

        // 9. Innings & Match Completion Evaluation
        checkMatchProgress(match, innings);

        match.setLastUpdated(LocalDateTime.now());
        Match updatedMatch = matchRepository.save(match);

        // 10. Real-time Broadcasting via WebSocket and SSE
        broadcastUpdates(updatedMatch, ballEvent);

        return updatedMatch;
    }

    private void updateLastBallsSummary(Match match, BallEvent ball) {
        String ballSymbol;
        if (ball.isWicket()) {
            ballSymbol = "W";
        } else if (ball.getExtraType() == ExtraType.WIDE) {
            ballSymbol = "Wd" + (ball.getExtraRuns() > 1 ? "+" + (ball.getExtraRuns() - 1) : "");
        } else if (ball.getExtraType() == ExtraType.NO_BALL) {
            ballSymbol = "Nb" + (ball.getRunsScored() > 0 ? "+" + ball.getRunsScored() : "");
        } else if (ball.getRunsScored() == 0) {
            ballSymbol = "•";
        } else {
            ballSymbol = String.valueOf(ball.getRunsScored());
        }

        String existing = match.getLastBallsSummary();
        if (existing == null || existing.isEmpty() || (ball.getBallInOver() == 1 && ball.getExtraType() == ExtraType.NONE)) {
            match.setLastBallsSummary(ballSymbol);
        } else {
            String[] parts = existing.split(" ");
            if (parts.length >= 6 && ball.getBallInOver() == 1) {
                match.setLastBallsSummary(ballSymbol);
            } else {
                match.setLastBallsSummary(existing + " " + ballSymbol);
            }
        }
    }

    private void checkMatchProgress(Match match, Innings innings) {
        int maxLegalBalls = match.getTotalOvers() * 6;
        boolean allOut = (innings.getTotalWickets() >= 10);
        boolean oversFinished = (innings.getTotalBalls() >= maxLegalBalls);

        if (match.getCurrentInningsNumber() == 1) {
            match.setTeam1Score(innings.getTotalRuns());
            match.setTeam1Wickets(innings.getTotalWickets());
            match.setTeam1Overs(innings.getOversFormatted());

            if (allOut || oversFinished) {
                innings.setCompleted(true);
                inningsRepository.save(innings);

                // Switch to Innings 2
                match.setCurrentInningsNumber(2);
                match.setStatus(MatchStatus.INNINGS_BREAK);
                match.setTargetRuns(innings.getTotalRuns() + 1);

                // Swap Batting and Bowling teams
                Team temp = match.getBattingTeam();
                match.setBattingTeam(match.getBowlingTeam());
                match.setBowlingTeam(temp);

                // Initialize 2nd Innings
                initSecondInnings(match);
            }
        } else {
            // Chasing in 2nd Innings
            match.setTeam2Score(innings.getTotalRuns());
            match.setTeam2Wickets(innings.getTotalWickets());
            match.setTeam2Overs(innings.getOversFormatted());

            // Compute Required Run Rate (RRR)
            int runsNeeded = match.getTargetRuns() - innings.getTotalRuns();
            int ballsRemaining = maxLegalBalls - innings.getTotalBalls();
            if (ballsRemaining > 0 && runsNeeded > 0) {
                double oversRemaining = ballsRemaining / 6.0;
                match.setRequiredRunRate(Math.round((runsNeeded / oversRemaining) * 100.0) / 100.0);
            } else {
                match.setRequiredRunRate(0.0);
            }

            if (innings.getTotalRuns() >= match.getTargetRuns()) {
                // Batting team won
                innings.setCompleted(true);
                inningsRepository.save(innings);
                match.setStatus(MatchStatus.COMPLETED);
                match.setWinnerTeam(match.getBattingTeam());
                int wicketsLeft = 10 - innings.getTotalWickets();
                match.setWinDescription(match.getBattingTeam().getName() + " won by " + wicketsLeft + " wicket" + (wicketsLeft > 1 ? "s" : "") + "!");
                awardPlayerOfTheMatch(match);
            } else if (allOut || oversFinished) {
                innings.setCompleted(true);
                inningsRepository.save(innings);
                match.setStatus(MatchStatus.COMPLETED);

                if (innings.getTotalRuns() == match.getTargetRuns() - 1) {
                    match.setWinDescription("Match Tied! Dramatic finish!");
                } else {
                    int margin = (match.getTargetRuns() - 1) - innings.getTotalRuns();
                    match.setWinnerTeam(match.getBowlingTeam());
                    match.setWinDescription(match.getBowlingTeam().getName() + " won by " + margin + " run" + (margin > 1 ? "s" : "") + "!");
                    awardPlayerOfTheMatch(match);
                }
            }
        }
    }

    private void awardPlayerOfTheMatch(Match match) {
        // Choose best performer
        List<BatsmanScore> bats = batsmanScoreRepository.findByMatchIdAndInningsNumberOrderByBattingPositionAsc(match.getId(), 1);
        bats.addAll(batsmanScoreRepository.findByMatchIdAndInningsNumberOrderByBattingPositionAsc(match.getId(), 2));

        BatsmanScore bestBat = bats.stream().max(Comparator.comparingInt(BatsmanScore::getRuns)).orElse(null);
        if (bestBat != null && bestBat.getRuns() >= 50) {
            match.setPlayerOfTheMatch(bestBat.getPlayer().getName() + " (" + bestBat.getRuns() + " off " + bestBat.getBalls() + ")");
            return;
        }

        List<BowlerScore> bowls = bowlerScoreRepository.findByMatchIdAndInningsNumberOrderByBowlingPositionAsc(match.getId(), 1);
        bowls.addAll(bowlerScoreRepository.findByMatchIdAndInningsNumberOrderByBowlingPositionAsc(match.getId(), 2));

        BowlerScore bestBowl = bowls.stream().max(Comparator.comparingInt(BowlerScore::getWickets)).orElse(null);
        if (bestBowl != null && bestBowl.getWickets() >= 3) {
            match.setPlayerOfTheMatch(bestBowl.getPlayer().getName() + " (" + bestBowl.getFiguresFormatted() + ")");
            return;
        }

        if (bestBat != null) {
            match.setPlayerOfTheMatch(bestBat.getPlayer().getName() + " (" + bestBat.getRuns() + " runs)");
        }
    }

    private void initSecondInnings(Match match) {
        Innings inn2 = new Innings(match.getId(), 2, match.getBattingTeam(), match.getBowlingTeam());
        inningsRepository.save(inn2);

        match.setCurrentScore(0);
        match.setCurrentWickets(0);
        match.setCurrentBalls(0);
        match.setCurrentOversFormatted("0.0");
        match.setLastBallsSummary("");
        match.setStatus(MatchStatus.LIVE);

        // Batting order
        List<Player> batPlayers = playerRepository.findByTeamId(match.getBattingTeam().getId());
        for (int i = 0; i < batPlayers.size(); i++) {
            BatsmanScore bs = new BatsmanScore(match.getId(), 2, batPlayers.get(i), i + 1);
            if (i == 0) {
                bs.setOnStrike(true);
                bs.setHasBatted(true);
                match.setCurrentStrikerName(batPlayers.get(i).getName());
            } else if (i == 1) {
                bs.setNonStriker(true);
                bs.setHasBatted(true);
                match.setCurrentNonStrikerName(batPlayers.get(i).getName());
            }
            batsmanScoreRepository.save(bs);
        }

        // Bowling order
        List<Player> bowlPlayers = playerRepository.findByTeamId(match.getBowlingTeam().getId());
        List<Player> bowlers = bowlPlayers.stream()
                .filter(p -> p.getRole() == PlayerRole.BOWLER || p.getRole() == PlayerRole.ALL_ROUNDER)
                .toList();
        if (bowlers.isEmpty()) bowlers = bowlPlayers;

        for (int i = 0; i < bowlers.size(); i++) {
            BowlerScore bscore = new BowlerScore(match.getId(), 2, bowlers.get(i), i + 1);
            if (i == 0) {
                bscore.setCurrentBowler(true);
                match.setCurrentBowlerName(bowlers.get(i).getName());
                match.setCurrentBowlerFigures("0.0-0-0-0");
            }
            bowlerScoreRepository.save(bscore);
        }
    }

    private void swapStrike(Long matchId, int inningsNum) {
        BatsmanScore s = batsmanScoreRepository.findByMatchIdAndInningsNumberAndIsOnStrikeTrue(matchId, inningsNum).orElse(null);
        BatsmanScore ns = batsmanScoreRepository.findByMatchIdAndInningsNumberAndIsNonStrikerTrue(matchId, inningsNum).orElse(null);

        if (s != null && ns != null) {
            s.setOnStrike(false);
            s.setNonStriker(true);
            ns.setOnStrike(true);
            ns.setNonStriker(false);
            batsmanScoreRepository.save(s);
            batsmanScoreRepository.save(ns);
        }
    }

    private void bringNextBatsman(Long matchId, int inningsNum) {
        List<BatsmanScore> allBatters = batsmanScoreRepository.findByMatchIdAndInningsNumberOrderByBattingPositionAsc(matchId, inningsNum);
        for (BatsmanScore b : allBatters) {
            if (!b.isHasBatted() && !b.isOut()) {
                b.setHasBatted(true);
                b.setOnStrike(true);
                batsmanScoreRepository.save(b);
                break;
            }
        }
    }

    private void rotateBowler(Long matchId, int inningsNum, Long currentBowlerScoreId) {
        List<BowlerScore> bowlers = bowlerScoreRepository.findByMatchIdAndInningsNumberOrderByBowlingPositionAsc(matchId, inningsNum);
        if (bowlers.isEmpty()) return;

        int currentIndex = 0;
        for (int i = 0; i < bowlers.size(); i++) {
            if (bowlers.get(i).getId().equals(currentBowlerScoreId)) {
                currentIndex = i;
                bowlers.get(i).setCurrentBowler(false);
                bowlerScoreRepository.save(bowlers.get(i));
                break;
            }
        }

        // Pick next bowler in spell
        int nextIndex = (currentIndex + 1) % bowlers.size();
        BowlerScore nextBowler = bowlers.get(nextIndex);
        nextBowler.setCurrentBowler(true);
        bowlerScoreRepository.save(nextBowler);
    }

    private void initParticipantsIfMissing(Long matchId, int inningsNum) {
        List<BatsmanScore> bat = batsmanScoreRepository.findByMatchIdAndInningsNumberOrderByBattingPositionAsc(matchId, inningsNum);
        if (!bat.isEmpty()) {
            boolean hasStriker = bat.stream().anyMatch(BatsmanScore::isOnStrike);
            boolean hasNonStriker = bat.stream().anyMatch(BatsmanScore::isNonStriker);
            if (!hasStriker && !bat.isEmpty()) {
                bat.get(0).setOnStrike(true);
                bat.get(0).setHasBatted(true);
                batsmanScoreRepository.save(bat.get(0));
            }
            if (!hasNonStriker && bat.size() > 1) {
                bat.get(1).setNonStriker(true);
                bat.get(1).setHasBatted(true);
                batsmanScoreRepository.save(bat.get(1));
            }
        }

        List<BowlerScore> bowl = bowlerScoreRepository.findByMatchIdAndInningsNumberOrderByBowlingPositionAsc(matchId, inningsNum);
        if (!bowl.isEmpty()) {
            boolean hasCurrentBowler = bowl.stream().anyMatch(BowlerScore::isCurrentBowler);
            if (!hasCurrentBowler) {
                bowl.get(0).setCurrentBowler(true);
                bowlerScoreRepository.save(bowl.get(0));
            }
        }
    }

    private String generateCommentary(String striker, String bowler, int runs, ExtraType extra, int extraRuns, boolean wicket, WicketType wType, String fielder) {
        if (wicket) {
            switch (wType) {
                case BOWLED:
                    return "OUT! Clean bowled! " + bowler + " produces an absolute jaffa that crashes straight into the stumps! " + striker + " is stunned!";
                case CAUGHT:
                    return "OUT! In the air and TAKEN! " + bowler + " forces the false shot, and " + (fielder != null ? fielder : "the fielder") + " takes a well-judged catch! Huge wicket!";
                case LBW:
                    return "OUT! L.B.W.! Struck right in front of middle! " + bowler + " appeals frantically, and the umpire's finger goes straight up! " + striker + " must depart.";
                case RUN_OUT:
                    return "OUT! RUN OUT! Chaos in the middle! Direct hit by " + (fielder != null ? fielder : "the fielder") + " and " + striker + " is well short of his crease!";
                case STUMPED:
                    return "OUT! Stumped! Beaten in flight by " + bowler + ", and the keeper whips off the bails in a flash!";
                default:
                    return "OUT! Wicket falls! " + bowler + " strikes for the bowling team!";
            }
        }

        if (extra == ExtraType.WIDE) {
            return "Wide ball signaled! " + bowler + " errs down the leg side, extra runs added to the total.";
        }
        if (extra == ExtraType.NO_BALL) {
            return "NO BALL called! " + bowler + " oversteps the crease! Free hit coming up!";
        }

        switch (runs) {
            case 0:
                String[] dots = {
                        bowler + " fires in a tidy length ball, defended solidly back to the bowler.",
                        "Good length delivery outside off, left alone by " + striker + ". No run.",
                        "Beaten for pace! " + bowler + " zips it past the outside edge into the gloves.",
                        "Tapped gently towards point, eager for a single but sent back safely."
                };
                return dots[new Random().nextInt(dots.length)];
            case 1:
                return bowler + " bowls full, " + striker + " works it through mid-wicket for a sharp single. Strike rotated.";
            case 2:
                return "Neatly clipped into the vacant gap at deep square leg. Good running between the wickets converts one into a comfortable two!";
            case 3:
                return "Driven beautifully through cover! The outfield is slightly sluggish, allowing the batsmen to scamper back for three!";
            case 4:
                String[] fours = {
                        "FOUR! Magnificent shot! " + striker + " leans into a gorgeous cover drive, piercing the infield with pure timing!",
                        "FOUR! Hammered through mid-wicket! Short and punished with disdain into the boundary ropes!",
                        "FOUR! Slashed over backward point! Width offered by " + bowler + " and taken full toll of!"
                };
                return fours[new Random().nextInt(fours.length)];
            case 6:
                String[] sixes = {
                        "SIX! Out of the park! " + striker + " launches into a colossal stroke high over long-on! What an enormous hit!",
                        "SIX! Picked up effortlessly! Into the crowd! Sensational striking by " + striker + "!",
                        "SIX! Maximum! In the slot and dispatched miles into the second tier! The stadium erupts!"
                };
                return sixes[new Random().nextInt(sixes.length)];
            default:
                return bowler + " to " + striker + ", " + runs + " run(s) taken.";
        }
    }

    private void broadcastUpdates(Match match, BallEvent latestBall) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("match", match);
        payload.put("latestBall", latestBall);

        // STOMP WebSocket
        messagingTemplate.convertAndSend("/topic/match/" + match.getId(), (Object) payload);
        messagingTemplate.convertAndSend("/topic/matches", (Object) match);

        // Server-Sent Events Fallback
        matchService.broadcastSse(match.getId(), payload);
    }
}
