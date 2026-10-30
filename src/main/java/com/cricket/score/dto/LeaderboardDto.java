package com.cricket.score.dto;

import com.cricket.score.model.Player;
import java.util.List;

public class LeaderboardDto {
    private List<Player> topRunScorers;
    private List<Player> topWicketTakers;

    public LeaderboardDto() {}

    public LeaderboardDto(List<Player> topRunScorers, List<Player> topWicketTakers) {
        this.topRunScorers = topRunScorers;
        this.topWicketTakers = topWicketTakers;
    }

    public List<Player> getTopRunScorers() {
        return topRunScorers;
    }

    public void setTopRunScorers(List<Player> topRunScorers) {
        this.topRunScorers = topRunScorers;
    }

    public List<Player> getTopWicketTakers() {
        return topWicketTakers;
    }

    public void setTopWicketTakers(List<Player> topWicketTakers) {
        this.topWicketTakers = topWicketTakers;
    }
}
