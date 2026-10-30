package com.cricket.score.controller;

import com.cricket.score.dto.MatchCreateRequestDto;
import com.cricket.score.dto.ScorecardDto;
import com.cricket.score.dto.WormChartDataDto;
import com.cricket.score.model.BallEvent;
import com.cricket.score.model.Match;
import com.cricket.score.model.MatchStatus;
import com.cricket.score.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches(@RequestParam(required = false) MatchStatus status) {
        return ResponseEntity.ok(matchService.getAllMatches(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatchById(@PathVariable Long id) {
        return matchService.getMatchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/scorecard")
    public ResponseEntity<ScorecardDto> getScorecard(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(matchService.getScorecard(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/commentary")
    public ResponseEntity<List<BallEvent>> getCommentary(@PathVariable Long id,
                                                         @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(matchService.getCommentary(id, limit));
    }

    @GetMapping("/{id}/worm")
    public ResponseEntity<WormChartDataDto> getWormChartData(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(matchService.getWormChartData(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody MatchCreateRequestDto request) {
        return ResponseEntity.ok(matchService.createMatch(request));
    }

    @GetMapping(value = "/{id}/stream", produces = "text/event-stream")
    public SseEmitter streamLiveUpdates(@PathVariable Long id) {
        return matchService.registerSseEmitter(id);
    }
}
