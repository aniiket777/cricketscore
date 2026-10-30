package com.cricket.score.controller;

import com.cricket.score.dto.LeaderboardDto;
import com.cricket.score.model.Player;
import com.cricket.score.model.Team;
import com.cricket.score.repository.MatchRepository;
import com.cricket.score.repository.PlayerRepository;
import com.cricket.score.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardDto> getLeaderboard() {
        List<Player> topBatsmen = playerRepository.findTopRunScorers();
        if (topBatsmen.size() > 10) topBatsmen = topBatsmen.subList(0, 10);

        List<Player> topBowlers = playerRepository.findTopWicketTakers();
        if (topBowlers.size() > 10) topBowlers = topBowlers.subList(0, 10);

        return ResponseEntity.ok(new LeaderboardDto(topBatsmen, topBowlers));
    }

    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamRepository.findAll());
    }

    @GetMapping("/teams/{id}/players")
    public ResponseEntity<List<Player>> getPlayersByTeam(@PathVariable Long id) {
        return ResponseEntity.ok(playerRepository.findByTeamId(id));
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalMatches", matchRepository.count());
        map.put("totalTeams", teamRepository.count());
        map.put("totalPlayers", playerRepository.count());
        map.put("serverStatus", "UP");
        return ResponseEntity.ok(map);
    }
}
