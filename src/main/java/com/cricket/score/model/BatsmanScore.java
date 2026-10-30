package com.cricket.score.model;

import jakarta.persistence.*;

@Entity
@Table(name = "batsman_scores")
public class BatsmanScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long matchId;

    @Column(nullable = false)
    private int inningsNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    private int battingPosition = 1;

    private int runs = 0;
    private int balls = 0;
    private int fours = 0;
    private int sixes = 0;
    private double strikeRate = 0.0;

    private boolean isOut = false;

    @Enumerated(EnumType.STRING)
    private WicketType dismissalType = WicketType.NONE;

    private String bowlerName;
    private String fielderName;

    private boolean isOnStrike = false;
    private boolean isNonStriker = false;
    private boolean hasBatted = false;

    public BatsmanScore() {}

    public BatsmanScore(Long matchId, int inningsNumber, Player player, int battingPosition) {
        this.matchId = matchId;
        this.inningsNumber = inningsNumber;
        this.player = player;
        this.battingPosition = battingPosition;
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

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getBattingPosition() {
        return battingPosition;
    }

    public void setBattingPosition(int battingPosition) {
        this.battingPosition = battingPosition;
    }

    public int getRuns() {
        return runs;
    }

    public void setRuns(int runs) {
        this.runs = runs;
        calculateStrikeRate();
    }

    public int getBalls() {
        return balls;
    }

    public void setBalls(int balls) {
        this.balls = balls;
        calculateStrikeRate();
    }

    public int getFours() {
        return fours;
    }

    public void setFours(int fours) {
        this.fours = fours;
    }

    public int getSixes() {
        return sixes;
    }

    public void setSixes(int sixes) {
        this.sixes = sixes;
    }

    public double getStrikeRate() {
        return strikeRate;
    }

    public void setStrikeRate(double strikeRate) {
        this.strikeRate = strikeRate;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut(boolean out) {
        isOut = out;
    }

    public WicketType getDismissalType() {
        return dismissalType;
    }

    public void setDismissalType(WicketType dismissalType) {
        this.dismissalType = dismissalType;
    }

    public String getBowlerName() {
        return bowlerName;
    }

    public void setBowlerName(String bowlerName) {
        this.bowlerName = bowlerName;
    }

    public String getFielderName() {
        return fielderName;
    }

    public void setFielderName(String fielderName) {
        this.fielderName = fielderName;
    }

    public boolean isOnStrike() {
        return isOnStrike;
    }

    public void setOnStrike(boolean onStrike) {
        isOnStrike = onStrike;
    }

    public boolean isNonStriker() {
        return isNonStriker;
    }

    public void setNonStriker(boolean nonStriker) {
        isNonStriker = nonStriker;
    }

    public boolean isHasBatted() {
        return hasBatted;
    }

    public void setHasBatted(boolean hasBatted) {
        this.hasBatted = hasBatted;
    }

    public void calculateStrikeRate() {
        if (balls == 0) {
            this.strikeRate = 0.0;
        } else {
            this.strikeRate = Math.round(((double) runs / balls) * 10000.0) / 100.0;
        }
    }

    public String getDismissalDescription() {
        if (!hasBatted) return "Did not bat";
        if (!isOut) return "not out";
        switch (dismissalType) {
            case BOWLED:
                return "b " + bowlerName;
            case CAUGHT:
                return (fielderName != null && !fielderName.isEmpty() ? "c " + fielderName + " " : "c & ") + "b " + bowlerName;
            case LBW:
                return "lbw b " + bowlerName;
            case RUN_OUT:
                return "run out (" + (fielderName != null ? fielderName : "") + ")";
            case STUMPED:
                return "st " + (fielderName != null ? fielderName : "") + " b " + bowlerName;
            case HIT_WICKET:
                return "hit wicket b " + bowlerName;
            default:
                return "out";
        }
    }
}
