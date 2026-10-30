package com.cricket.score.model;

import jakarta.persistence.*;

@Entity
@Table(name = "innings")
public class Innings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long matchId;

    @Column(nullable = false)
    private int inningsNumber; // 1 or 2

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batting_team_id")
    private Team battingTeam;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bowling_team_id")
    private Team bowlingTeam;

    private int totalRuns = 0;
    private int totalWickets = 0;
    private int totalBalls = 0;
    private String oversFormatted = "0.0";

    // Extras
    private int wides = 0;
    private int noBalls = 0;
    private int byes = 0;
    private int legByes = 0;
    private int penaltyRuns = 0;

    private boolean isCompleted = false;

    public Innings() {}

    public Innings(Long matchId, int inningsNumber, Team battingTeam, Team bowlingTeam) {
        this.matchId = matchId;
        this.inningsNumber = inningsNumber;
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public int getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(int inningsNumber) {
        this.inningsNumber = inningsNumber;
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

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public int getTotalWickets() {
        return totalWickets;
    }

    public void setTotalWickets(int totalWickets) {
        this.totalWickets = totalWickets;
    }

    public int getTotalBalls() {
        return totalBalls;
    }

    public void setTotalBalls(int totalBalls) {
        this.totalBalls = totalBalls;
        int overs = totalBalls / 6;
        int balls = totalBalls % 6;
        this.oversFormatted = overs + "." + balls;
    }

    public String getOversFormatted() {
        return oversFormatted;
    }

    public void setOversFormatted(String oversFormatted) {
        this.oversFormatted = oversFormatted;
    }

    public int getWides() {
        return wides;
    }

    public void setWides(int wides) {
        this.wides = wides;
    }

    public int getNoBalls() {
        return noBalls;
    }

    public void setNoBalls(int noBalls) {
        this.noBalls = noBalls;
    }

    public int getByes() {
        return byes;
    }

    public void setByes(int byes) {
        this.byes = byes;
    }

    public int getLegByes() {
        return legByes;
    }

    public void setLegByes(int legByes) {
        this.legByes = legByes;
    }

    public int getPenaltyRuns() {
        return penaltyRuns;
    }

    public void setPenaltyRuns(int penaltyRuns) {
        this.penaltyRuns = penaltyRuns;
    }

    public int getTotalExtras() {
        return wides + noBalls + byes + legByes + penaltyRuns;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
