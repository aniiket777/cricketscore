package com.cricket.score.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ball_events")
public class BallEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long matchId;

    @Column(nullable = false)
    private int inningsNumber;

    private int overNumber; // 0-indexed or 1-indexed (e.g. over 1 to 20)
    private int ballInOver; // 1 to 6

    private String strikerName;
    private String nonStrikerName;
    private String bowlerName;

    private int runsScored = 0; // Off the bat

    @Enumerated(EnumType.STRING)
    private ExtraType extraType = ExtraType.NONE;

    private int extraRuns = 0;
    private int totalRuns = 0; // runsScored + extraRuns

    private boolean isWicket = false;

    @Enumerated(EnumType.STRING)
    private WicketType wicketType = WicketType.NONE;

    private String dismissedPlayerName;
    private String fielderName;

    @Column(length = 1000)
    private String commentary;

    private int scoreAfterBall;
    private int wicketsAfterBall;
    private String oversAfterBall;

    private LocalDateTime timestamp = LocalDateTime.now();

    public BallEvent() {}

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

    public int getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(int overNumber) {
        this.overNumber = overNumber;
    }

    public int getBallInOver() {
        return ballInOver;
    }

    public void setBallInOver(int ballInOver) {
        this.ballInOver = ballInOver;
    }

    public String getStrikerName() {
        return strikerName;
    }

    public void setStrikerName(String strikerName) {
        this.strikerName = strikerName;
    }

    public String getNonStrikerName() {
        return nonStrikerName;
    }

    public void setNonStrikerName(String nonStrikerName) {
        this.nonStrikerName = nonStrikerName;
    }

    public String getBowlerName() {
        return bowlerName;
    }

    public void setBowlerName(String bowlerName) {
        this.bowlerName = bowlerName;
    }

    public int getRunsScored() {
        return runsScored;
    }

    public void setRunsScored(int runsScored) {
        this.runsScored = runsScored;
    }

    public ExtraType getExtraType() {
        return extraType;
    }

    public void setExtraType(ExtraType extraType) {
        this.extraType = extraType;
    }

    public int getExtraRuns() {
        return extraRuns;
    }

    public void setExtraRuns(int extraRuns) {
        this.extraRuns = extraRuns;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public boolean isWicket() {
        return isWicket;
    }

    public void setWicket(boolean wicket) {
        isWicket = wicket;
    }

    public WicketType getWicketType() {
        return wicketType;
    }

    public void setWicketType(WicketType wicketType) {
        this.wicketType = wicketType;
    }

    public String getDismissedPlayerName() {
        return dismissedPlayerName;
    }

    public void setDismissedPlayerName(String dismissedPlayerName) {
        this.dismissedPlayerName = dismissedPlayerName;
    }

    public String getFielderName() {
        return fielderName;
    }

    public void setFielderName(String fielderName) {
        this.fielderName = fielderName;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public int getScoreAfterBall() {
        return scoreAfterBall;
    }

    public void setScoreAfterBall(int scoreAfterBall) {
        this.scoreAfterBall = scoreAfterBall;
    }

    public int getWicketsAfterBall() {
        return wicketsAfterBall;
    }

    public void setWicketsAfterBall(int wicketsAfterBall) {
        this.wicketsAfterBall = wicketsAfterBall;
    }

    public String getOversAfterBall() {
        return oversAfterBall;
    }

    public void setOversAfterBall(String oversAfterBall) {
        this.oversAfterBall = oversAfterBall;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
