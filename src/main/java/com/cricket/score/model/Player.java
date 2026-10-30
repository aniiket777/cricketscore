package com.cricket.score.model;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRole role;

    private String battingStyle; // e.g. "Right-hand bat"
    private String bowlingStyle; // e.g. "Right-arm fast"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    private Team team;

    // Career / Aggregated stats for leaderboard
    private int totalRuns = 0;
    private int totalBallsFaced = 0;
    private int totalWickets = 0;
    private int totalBallsBowled = 0;
    private int totalRunsConceded = 0;
    private int matchesPlayed = 0;
    private int highestScore = 0;

    public Player() {}

    public Player(String name, PlayerRole role, String battingStyle, String bowlingStyle, Team team) {
        this.name = name;
        this.role = role;
        this.battingStyle = battingStyle;
        this.bowlingStyle = bowlingStyle;
        this.team = team;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public String getBattingStyle() {
        return battingStyle;
    }

    public void setBattingStyle(String battingStyle) {
        this.battingStyle = battingStyle;
    }

    public String getBowlingStyle() {
        return bowlingStyle;
    }

    public void setBowlingStyle(String bowlingStyle) {
        this.bowlingStyle = bowlingStyle;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public int getTotalBallsFaced() {
        return totalBallsFaced;
    }

    public void setTotalBallsFaced(int totalBallsFaced) {
        this.totalBallsFaced = totalBallsFaced;
    }

    public int getTotalWickets() {
        return totalWickets;
    }

    public void setTotalWickets(int totalWickets) {
        this.totalWickets = totalWickets;
    }

    public int getTotalBallsBowled() {
        return totalBallsBowled;
    }

    public void setTotalBallsBowled(int totalBallsBowled) {
        this.totalBallsBowled = totalBallsBowled;
    }

    public int getTotalRunsConceded() {
        return totalRunsConceded;
    }

    public void setTotalRunsConceded(int totalRunsConceded) {
        this.totalRunsConceded = totalRunsConceded;
    }

    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(int matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public double getBattingStrikeRate() {
        if (totalBallsFaced == 0) return 0.0;
        return Math.round(((double) totalRuns / totalBallsFaced) * 10000.0) / 100.0;
    }

    public double getBowlingEconomy() {
        if (totalBallsBowled == 0) return 0.0;
        double overs = totalBallsBowled / 6.0;
        return Math.round((totalRunsConceded / overs) * 100.0) / 100.0;
    }
}
