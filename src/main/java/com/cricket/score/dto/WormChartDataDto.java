package com.cricket.score.dto;

import java.util.List;

public class WormChartDataDto {
    private Long matchId;
    private String team1Name;
    private String team1Code;
    private List<OverRunPoint> team1Progression;

    private String team2Name;
    private String team2Code;
    private List<OverRunPoint> team2Progression;

    public static class OverRunPoint {
        private int over;
        private int cumulativeRuns;
        private int wickets;
        private int runsInOver;

        public OverRunPoint(int over, int cumulativeRuns, int wickets, int runsInOver) {
            this.over = over;
            this.cumulativeRuns = cumulativeRuns;
            this.wickets = wickets;
            this.runsInOver = runsInOver;
        }

        public int getOver() {
            return over;
        }

        public void setOver(int over) {
            this.over = over;
        }

        public int getCumulativeRuns() {
            return cumulativeRuns;
        }

        public void setCumulativeRuns(int cumulativeRuns) {
            this.cumulativeRuns = cumulativeRuns;
        }

        public int getWickets() {
            return wickets;
        }

        public void setWickets(int wickets) {
            this.wickets = wickets;
        }

        public int getRunsInOver() {
            return runsInOver;
        }

        public void setRunsInOver(int runsInOver) {
            this.runsInOver = runsInOver;
        }
    }

    public WormChartDataDto() {}

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public String getTeam1Name() {
        return team1Name;
    }

    public void setTeam1Name(String team1Name) {
        this.team1Name = team1Name;
    }

    public String getTeam1Code() {
        return team1Code;
    }

    public void setTeam1Code(String team1Code) {
        this.team1Code = team1Code;
    }

    public List<OverRunPoint> getTeam1Progression() {
        return team1Progression;
    }

    public void setTeam1Progression(List<OverRunPoint> team1Progression) {
        this.team1Progression = team1Progression;
    }

    public String getTeam2Name() {
        return team2Name;
    }

    public void setTeam2Name(String team2Name) {
        this.team2Name = team2Name;
    }

    public String getTeam2Code() {
        return team2Code;
    }

    public void setTeam2Code(String team2Code) {
        this.team2Code = team2Code;
    }

    public List<OverRunPoint> getTeam2Progression() {
        return team2Progression;
    }

    public void setTeam2Progression(List<OverRunPoint> team2Progression) {
        this.team2Progression = team2Progression;
    }
}
