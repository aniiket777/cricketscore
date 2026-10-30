package com.cricket.score.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bowler_scores")
public class BowlerScore {

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

    private int bowlingPosition = 1;

    private int ballsBowled = 0;
    private String oversBowled = "0.0";
    private int maidens = 0;
    private int runsConceded = 0;
    private int wickets = 0;
    private double economyRate = 0.0;
    private int wides = 0;
    private int noBalls = 0;
    private int dotBalls = 0;

    private boolean isCurrentBowler = false;

    public BowlerScore() {}

    public BowlerScore(Long matchId, int inningsNumber, Player player, int bowlingPosition) {
        this.matchId = matchId;
        this.inningsNumber = inningsNumber;
        this.player = player;
        this.bowlingPosition = bowlingPosition;
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

    public int getBowlingPosition() {
        return bowlingPosition;
    }

    public void setBowlingPosition(int bowlingPosition) {
        this.bowlingPosition = bowlingPosition;
    }

    public int getBallsBowled() {
        return ballsBowled;
    }

    public void setBallsBowled(int ballsBowled) {
        this.ballsBowled = ballsBowled;
        int overs = ballsBowled / 6;
        int balls = ballsBowled % 6;
        this.oversBowled = overs + "." + balls;
        calculateEconomy();
    }

    public String getOversBowled() {
        return oversBowled;
    }

    public void setOversBowled(String oversBowled) {
        this.oversBowled = oversBowled;
    }

    public int getMaidens() {
        return maidens;
    }

    public void setMaidens(int maidens) {
        this.maidens = maidens;
    }

    public int getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(int runsConceded) {
        this.runsConceded = runsConceded;
        calculateEconomy();
    }

    public int getWickets() {
        return wickets;
    }

    public void setWickets(int wickets) {
        this.wickets = wickets;
    }

    public double getEconomyRate() {
        return economyRate;
    }

    public void setEconomyRate(double economyRate) {
        this.economyRate = economyRate;
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

    public int getDotBalls() {
        return dotBalls;
    }

    public void setDotBalls(int dotBalls) {
        this.dotBalls = dotBalls;
    }

    public boolean isCurrentBowler() {
        return isCurrentBowler;
    }

    public void setCurrentBowler(boolean currentBowler) {
        isCurrentBowler = currentBowler;
    }

    public void calculateEconomy() {
        if (ballsBowled == 0) {
            this.economyRate = 0.0;
        } else {
            double totalOversDec = ballsBowled / 6.0;
            this.economyRate = Math.round((runsConceded / totalOversDec) * 100.0) / 100.0;
        }
    }

    public String getFiguresFormatted() {
        return oversBowled + "-" + maidens + "-" + runsConceded + "-" + wickets;
    }
}
