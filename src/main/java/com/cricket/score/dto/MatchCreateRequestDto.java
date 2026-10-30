package com.cricket.score.dto;

import com.cricket.score.model.MatchType;
import com.cricket.score.model.TossDecision;

public class MatchCreateRequestDto {
    private String title;
    private String series;
    private String venue;
    private MatchType matchType = MatchType.T20;
    private int totalOvers = 20;
    private Long team1Id;
    private Long team2Id;
    private Long tossWinnerId;
    private TossDecision tossDecision;

    public MatchCreateRequestDto() {}

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

    public Long getTeam1Id() {
        return team1Id;
    }

    public void setTeam1Id(Long team1Id) {
        this.team1Id = team1Id;
    }

    public Long getTeam2Id() {
        return team2Id;
    }

    public void setTeam2Id(Long team2Id) {
        this.team2Id = team2Id;
    }

    public Long getTossWinnerId() {
        return tossWinnerId;
    }

    public void setTossWinnerId(Long tossWinnerId) {
        this.tossWinnerId = tossWinnerId;
    }

    public TossDecision getTossDecision() {
        return tossDecision;
    }

    public void setTossDecision(TossDecision tossDecision) {
        this.tossDecision = tossDecision;
    }
}
