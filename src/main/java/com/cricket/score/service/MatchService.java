package com.cricket.score.service;

import com.cricket.score.dto.MatchCreateRequestDto;
import com.cricket.score.dto.ScorecardDto;
import com.cricket.score.dto.WormChartDataDto;
import com.cricket.score.model.*;
import com.cricket.score.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private InningsRepository inningsRepository;

    @Autowired
    private BatsmanScoreRepository batsmanScoreRepository;

    @Autowired
    private BowlerScoreRepository bowlerScoreRepository;

    @Autowired
    private BallEventRepository ballEventRepository;

    // SSE Emitters for real-time fallback streaming
    private final Map<Long, List<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();

    public List<Match> getAllMatches(MatchStatus status) {
        if (status != null) {
            return matchRepository.findByStatus(status);
        }
        return matchRepository.findAllByOrderByStartTimeDesc();
    }

    public Optional<Match> getMatchById(Long id) {
        return matchRepository.findById(id);
    }

    public List<BallEvent> getCommentary(Long matchId, int limit) {
        return ballEventRepository.findByMatchIdOrderByTimestampDesc(matchId, PageRequest.of(0, limit));
    }

    public ScorecardDto getScorecard(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));

        ScorecardDto.InningsSection section1 = null;
        ScorecardDto.InningsSection section2 = null;

        Optional<Innings> inn1Opt = inningsRepository.findByMatchIdAndInningsNumber(matchId, 1);
        if (inn1Opt.isPresent()) {
            Innings inn1 = inn1Opt.get();
            List<BatsmanScore> bat1 = batsmanScoreRepository.findByMatchIdAndInningsNumberOrderByBattingPositionAsc(matchId, 1);
            List<BowlerScore> bowl1 = bowlerScoreRepository.findByMatchIdAndInningsNumberOrderByBowlingPositionAsc(matchId, 1);
            section1 = new ScorecardDto.InningsSection(inn1, bat1, bowl1);
        }

        Optional<Innings> inn2Opt = inningsRepository.findByMatchIdAndInningsNumber(matchId, 2);
        if (inn2Opt.isPresent()) {
            Innings inn2 = inn2Opt.get();
            List<BatsmanScore> bat2 = batsmanScoreRepository.findByMatchIdAndInningsNumberOrderByBattingPositionAsc(matchId, 2);
            List<BowlerScore> bowl2 = bowlerScoreRepository.findByMatchIdAndInningsNumberOrderByBowlingPositionAsc(matchId, 2);
            section2 = new ScorecardDto.InningsSection(inn2, bat2, bowl2);
        }

        return new ScorecardDto(match, section1, section2);
    }

    public WormChartDataDto getWormChartData(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        WormChartDataDto dto = new WormChartDataDto();
        dto.setMatchId(matchId);
        dto.setTeam1Name(match.getTeam1().getName());
        dto.setTeam1Code(match.getTeam1().getShortCode());
        dto.setTeam2Name(match.getTeam2().getName());
        dto.setTeam2Code(match.getTeam2().getShortCode());

        // Process Innings 1 over-by-over
        dto.setTeam1Progression(computeProgression(matchId, 1));
        // Process Innings 2 over-by-over
        dto.setTeam2Progression(computeProgression(matchId, 2));

        return dto;
    }

    private List<WormChartDataDto.OverRunPoint> computeProgression(Long matchId, int inningsNumber) {
        List<BallEvent> balls = ballEventRepository.findByMatchIdAndInningsNumberOrderByTimestampAsc(matchId, inningsNumber);
        Map<Integer, WormChartDataDto.OverRunPoint> map = new LinkedHashMap<>();

        int runningRuns = 0;
        int runningWickets = 0;
        int currentOverRuns = 0;
        int lastOverNumber = 0;

        for (BallEvent b : balls) {
            int over = b.getOverNumber();
            runningRuns = b.getScoreAfterBall();
            runningWickets = b.getWicketsAfterBall();

            if (over != lastOverNumber && lastOverNumber != 0) {
                map.put(lastOverNumber, new WormChartDataDto.OverRunPoint(lastOverNumber, runningRuns, runningWickets, currentOverRuns));
                currentOverRuns = 0;
            }
            lastOverNumber = over;
            currentOverRuns += b.getTotalRuns();
        }

        if (lastOverNumber > 0) {
            map.put(lastOverNumber, new WormChartDataDto.OverRunPoint(lastOverNumber, runningRuns, runningWickets, currentOverRuns));
        }

        return new ArrayList<>(map.values());
    }

    @Transactional
    public Match createMatch(MatchCreateRequestDto request) {
        Team team1 = teamRepository.findById(request.getTeam1Id())
                .orElseThrow(() -> new IllegalArgumentException("Invalid team1 ID"));
        Team team2 = teamRepository.findById(request.getTeam2Id())
                .orElseThrow(() -> new IllegalArgumentException("Invalid team2 ID"));

        Match match = new Match();
        match.setTitle(request.getTitle() != null ? request.getTitle() : team1.getName() + " vs " + team2.getName());
        match.setSeries(request.getSeries() != null ? request.getSeries() : "T20 Championship");
        match.setVenue(request.getVenue() != null ? request.getVenue() : "International Stadium");
        match.setMatchType(request.getMatchType() != null ? request.getMatchType() : MatchType.T20);
        match.setTotalOvers(request.getTotalOvers() > 0 ? request.getTotalOvers() : 20);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setStatus(MatchStatus.LIVE);

        Team tossWinner = request.getTossWinnerId() != null
                ? teamRepository.findById(request.getTossWinnerId()).orElse(team1)
                : team1;
        TossDecision tossDecision = request.getTossDecision() != null ? request.getTossDecision() : TossDecision.BAT;

        match.setTossWinner(tossWinner);
        match.setTossDecision(tossDecision);

        Team battingTeam = (tossDecision == TossDecision.BAT) ? tossWinner : (tossWinner.getId().equals(team1.getId()) ? team2 : team1);
        Team bowlingTeam = battingTeam.getId().equals(team1.getId()) ? team2 : team1;

        match.setBattingTeam(battingTeam);
        match.setBowlingTeam(bowlingTeam);
        match.setCurrentInningsNumber(1);
        match.setStartTime(LocalDateTime.now());
        match.setLastUpdated(LocalDateTime.now());

        Match savedMatch = matchRepository.save(match);

        // Initialize Innings 1
        Innings inn1 = new Innings(savedMatch.getId(), 1, battingTeam, bowlingTeam);
        inningsRepository.save(inn1);

        // Populate BatsmanScores for Batting Team
        List<Player> batPlayers = playerRepository.findByTeamId(battingTeam.getId());
        for (int i = 0; i < batPlayers.size(); i++) {
            BatsmanScore bs = new BatsmanScore(savedMatch.getId(), 1, batPlayers.get(i), i + 1);
            if (i == 0) {
                bs.setOnStrike(true);
                bs.setHasBatted(true);
                savedMatch.setCurrentStrikerName(batPlayers.get(i).getName());
            } else if (i == 1) {
                bs.setNonStriker(true);
                bs.setHasBatted(true);
                savedMatch.setCurrentNonStrikerName(batPlayers.get(i).getName());
            }
            batsmanScoreRepository.save(bs);
        }

        // Populate BowlerScores for Bowling Team
        List<Player> bowlPlayers = playerRepository.findByTeamId(bowlingTeam.getId());
        List<Player> bowlers = bowlPlayers.stream()
                .filter(p -> p.getRole() == PlayerRole.BOWLER || p.getRole() == PlayerRole.ALL_ROUNDER)
                .toList();

        if (bowlers.isEmpty()) bowlers = bowlPlayers;

        for (int i = 0; i < bowlers.size(); i++) {
            BowlerScore bscore = new BowlerScore(savedMatch.getId(), 1, bowlers.get(i), i + 1);
            if (i == 0) {
                bscore.setCurrentBowler(true);
                savedMatch.setCurrentBowlerName(bowlers.get(i).getName());
                savedMatch.setCurrentBowlerFigures("0.0-0-0-0");
            }
            bowlerScoreRepository.save(bscore);
        }

        return matchRepository.save(savedMatch);
    }

    public SseEmitter registerSseEmitter(Long matchId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1 hour timeout
        sseEmitters.computeIfAbsent(matchId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeSseEmitter(matchId, emitter));
        emitter.onTimeout(() -> removeSseEmitter(matchId, emitter));
        emitter.onError((e) -> removeSseEmitter(matchId, emitter));

        return emitter;
    }

    private void removeSseEmitter(Long matchId, SseEmitter emitter) {
        List<SseEmitter> list = sseEmitters.get(matchId);
        if (list != null) {
            list.remove(emitter);
        }
    }

    public void broadcastSse(Long matchId, Object data) {
        List<SseEmitter> list = sseEmitters.get(matchId);
        if (list != null && !list.isEmpty()) {
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("match-update").data(data));
                } catch (IOException e) {
                    removeSseEmitter(matchId, emitter);
                }
            }
        }
    }
}
