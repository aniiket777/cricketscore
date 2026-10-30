package com.cricket.score.controller;

import com.cricket.score.model.Match;
import com.cricket.score.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @PostMapping("/start/{matchId}")
    public ResponseEntity<Map<String, Object>> startSimulation(@PathVariable Long matchId,
                                                               @RequestParam(defaultValue = "1500") Long intervalMs) {
        boolean started = simulationService.startSimulation(matchId, intervalMs);
        Map<String, Object> res = new HashMap<>();
        res.put("matchId", matchId);
        res.put("status", started ? "RUNNING" : "FAILED");
        res.put("intervalMs", intervalMs);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/pause/{matchId}")
    public ResponseEntity<Map<String, Object>> pauseSimulation(@PathVariable Long matchId) {
        boolean stopped = simulationService.stopSimulation(matchId);
        Map<String, Object> res = new HashMap<>();
        res.put("matchId", matchId);
        res.put("status", stopped ? "PAUSED" : "NOT_RUNNING");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/step/{matchId}")
    public ResponseEntity<Match> stepSimulation(@PathVariable Long matchId) {
        try {
            Match updated = simulationService.stepSimulation(matchId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/speed/{matchId}")
    public ResponseEntity<Map<String, Object>> setSpeed(@PathVariable Long matchId,
                                                        @RequestParam Long intervalMs) {
        simulationService.setSpeed(matchId, intervalMs);
        Map<String, Object> res = new HashMap<>();
        res.put("matchId", matchId);
        res.put("intervalMs", intervalMs);
        res.put("isSimulating", simulationService.isSimulating(matchId));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/reset/{matchId}")
    public ResponseEntity<Match> resetMatch(@PathVariable Long matchId) {
        try {
            return ResponseEntity.ok(simulationService.resetMatch(matchId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{matchId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable Long matchId) {
        Map<String, Object> res = new HashMap<>();
        res.put("matchId", matchId);
        res.put("isSimulating", simulationService.isSimulating(matchId));
        return ResponseEntity.ok(res);
    }
}
