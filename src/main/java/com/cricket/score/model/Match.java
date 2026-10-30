package com.cricket.score.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // e.g. "India vs Australia"

    private String series; // e.g. "ICC Men's T20 World Cup Final"
    private String venue; // e.g. "Narendra Modi Stadium, Ahmedabad"

    @Enumerated(EnumType.STRING)
    private MatchType matchType = MatchType.T20;

    private int totalOvers = 20;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.UPCOMING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team1_id")
    private Team team1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team2_id")
    private Team team2;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "toss_winner_id")
    private Team tossWinner;

    @Enumerated(EnumType.STRING)
    private TossDecision tossDecision;

    private int currentInningsNumber = 1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batting_team_id")
    private Team battingTeam;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bowling_team_id")
    private Team bowlingTeam;

    // Live match score state for quick retrieval
    private int currentScore = 0;
    private int currentWickets = 0;
    private int currentBalls = 0; // Total legal balls in current innings
    private String currentOversFormatted = "0.0";

    // 1st Innings summary stored on Match
    private int team1Score = 0;
    private int team1Wickets = 0;
    private String team1Overs = "0.0";

    // 2nd Innings summary stored on Match
    private int team2Score = 0;
    private int team2Wickets = 0;
    private String team2Overs = "0.0";

    private int targetRuns = 0; // Set for innings 2
    private double currentRunRate = 0.0;
    private double requiredRunRate = 0.0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_id")
    private Team winnerTeam;

    private String winDescription;
    private String playerOfTheMatch;

    // Active live participants for display
    private String currentStrikerName;
    private int currentStrikerRuns;
    private int currentStrikerBalls;

    private String currentNonStrikerName;
    private int currentNonStrikerRuns;
    private int currentNonStrikerBalls;

    private String currentBowlerName;
    private String currentBowlerFigures; // e.g. "3.2-0-24-2"

    private String lastBallsSummary = ""; // e.g. "1 4 0 W 6 1"

    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public Match() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public int getTotalOvers() {
        return totalOvers;
    }

    public void setTotalOvers(int totalOvers) {
        this.totalOvers = totalOvers;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    public Team getTossWinner() {
        return tossWinner;
    }

    public void setTossWinner(Team tossWinner) {
        this.tossWinner = tossWinner;
    }

    public TossDecision getTossDecision() {
        return tossDecision;
    }

    public void setTossDecision(TossDecision tossDecision) {
        this.tossDecision = tossDecision;
    }

    public int getCurrentInningsNumber() {
        return currentInningsNumber;
    }

    public void setCurrentInningsNumber(int currentInningsNumber) {
        this.currentInningsNumber = currentInningsNumber;
    }

    public Team getBattingTeam() {
        return battingTeam;
    }

    public void setBattingTeam(Team battingTeam) {
        this.battingTeam = battingTeam;
    }

    public Team getBowlingTeam() {
        return bowlingTeam;
    }

    public void setBowlingTeam(Team bowlingTeam) {
        this.bowlingTeam = bowlingTeam;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public int getCurrentWickets() {
        return currentWickets;
    }

    public void setCurrentWickets(int currentWickets) {
        this.currentWickets = currentWickets;
    }

    public int getCurrentBalls() {
        return currentBalls;
    }

    public void setCurrentBalls(int currentBalls) {
        this.currentBalls = currentBalls;
    }

    public String getCurrentOversFormatted() {
        return currentOversFormatted;
    }

    public void setCurrentOversFormatted(String currentOversFormatted) {
        this.currentOversFormatted = currentOversFormatted;
    }

    public int getTeam1Score() {
        return team1Score;
    }

    public void setTeam1Score(int team1Score) {
        this.team1Score = team1Score;
    }

    public int getTeam1Wickets() {
        return team1Wickets;
    }

    public void setTeam1Wickets(int team1Wickets) {
        this.team1Wickets = team1Wickets;
    }

    public String getTeam1Overs() {
        return team1Overs;
    }

    public void setTeam1Overs(String team1Overs) {
        this.team1Overs = team1Overs;
    }

    public int getTeam2Score() {
        return team2Score;
    }

    public void setTeam2Score(int team2Score) {
        this.team2Score = team2Score;
    }

    public int getTeam2Wickets() {
        return team2Wickets;
    }

    public void setTeam2Wickets(int team2Wickets) {
        this.team2Wickets = team2Wickets;
    }

    public String getTeam2Overs() {
        return team2Overs;
    }

    public void setTeam2Overs(String team2Overs) {
        this.team2Overs = team2Overs;
    }

    public int getTargetRuns() {
        return targetRuns;
    }

    public void setTargetRuns(int targetRuns) {
        this.targetRuns = targetRuns;
    }

    public double getCurrentRunRate() {
        return currentRunRate;
    }

    public void setCurrentRunRate(double currentRunRate) {
        this.currentRunRate = currentRunRate;
    }

    public double getRequiredRunRate() {
        return requiredRunRate;
    }

    public void setRequiredRunRate(double requiredRunRate) {
        this.requiredRunRate = requiredRunRate;
    }

    public Team getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(Team winnerTeam) {
        this.winnerTeam = winnerTeam;
    }

    public String getWinDescription() {
        return winDescription;
    }

    public void setWinDescription(String winDescription) {
        this.winDescription = winDescription;
    }

    public String getPlayerOfTheMatch() {
        return playerOfTheMatch;
    }

    public void setPlayerOfTheMatch(String playerOfTheMatch) {
        this.playerOfTheMatch = playerOfTheMatch;
    }

    public String getCurrentStrikerName() {
        return currentStrikerName;
    }

    public void setCurrentStrikerName(String currentStrikerName) {
        this.currentStrikerName = currentStrikerName;
    }

    public int getCurrentStrikerRuns() {
        return currentStrikerRuns;
    }

    public void setCurrentStrikerRuns(int currentStrikerRuns) {
        this.currentStrikerRuns = currentStrikerRuns;
    }

    public int getCurrentStrikerBalls() {
        return currentStrikerBalls;
    }

    public void setCurrentStrikerBalls(int currentStrikerBalls) {
        this.currentStrikerBalls = currentStrikerBalls;
    }

    public String getCurrentNonStrikerName() {
        return currentNonStrikerName;
    }

    public void setCurrentNonStrikerName(String currentNonStrikerName) {
        this.currentNonStrikerName = currentNonStrikerName;
    }

    public int getCurrentNonStrikerRuns() {
        return currentNonStrikerRuns;
    }

    public void setCurrentNonStrikerRuns(int currentNonStrikerRuns) {
        this.currentNonStrikerRuns = currentNonStrikerRuns;
    }

    public int getCurrentNonStrikerBalls() {
        return currentNonStrikerBalls;
    }

    public void setCurrentNonStrikerBalls(int currentNonStrikerBalls) {
        this.currentNonStrikerBalls = currentNonStrikerBalls;
    }

    public String getCurrentBowlerName() {
        return currentBowlerName;
    }

    public void setCurrentBowlerName(String currentBowlerName) {
        this.currentBowlerName = currentBowlerName;
    }

    public String getCurrentBowlerFigures() {
        return currentBowlerFigures;
    }

    public void setCurrentBowlerFigures(String currentBowlerFigures) {
        this.currentBowlerFigures = currentBowlerFigures;
    }

    public String getLastBallsSummary() {
        return lastBallsSummary;
    }

    public void setLastBallsSummary(String lastBallsSummary) {
        this.lastBallsSummary = lastBallsSummary;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
