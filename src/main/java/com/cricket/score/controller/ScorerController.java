package com.cricket.score.controller;

import com.cricket.score.dto.LiveBallRequestDto;
import com.cricket.score.model.Match;
import com.cricket.score.service.ScoringEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scorer")
@CrossOrigin(origins = "*")
public class ScorerController {

    @Autowired
    private ScoringEngineService scoringEngineService;

    @PostMapping("/matches/{matchId}/ball")
    public ResponseEntity<Match> recordBall(@PathVariable Long matchId,
                                            @RequestBody LiveBallRequestDto request) {
        try {
            Match updated = scoringEngineService.processBall(matchId, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
