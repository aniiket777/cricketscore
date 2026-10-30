package com.cricket.score.dto;

import com.cricket.score.model.BatsmanScore;
import com.cricket.score.model.BowlerScore;
import com.cricket.score.model.Innings;
import com.cricket.score.model.Match;

import java.util.List;

public class ScorecardDto {
    private Match match;
    private InningsSection innings1;
    private InningsSection innings2;

    public static class InningsSection {
        private Innings innings;
        private List<BatsmanScore> batting;
        private List<BowlerScore> bowling;

        public InningsSection(Innings innings, List<BatsmanScore> batting, List<BowlerScore> bowling) {
            this.innings = innings;
            this.batting = batting;
            this.bowling = bowling;
        }

        public Innings getInnings() {
            return innings;
        }

        public void setInnings(Innings innings) {
            this.innings = innings;
        }

        public List<BatsmanScore> getBatting() {
            return batting;
        }

        public void setBatting(List<BatsmanScore> batting) {
            this.batting = batting;
        }

        public List<BowlerScore> getBowling() {
            return bowling;
        }

        public void setBowling(List<BowlerScore> bowling) {
            this.bowling = bowling;
        }
    }

    public ScorecardDto() {}

    public ScorecardDto(Match match, InningsSection innings1, InningsSection innings2) {
        this.match = match;
        this.innings1 = innings1;
        this.innings2 = innings2;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public InningsSection getInnings1() {
        return innings1;
    }

    public void setInnings1(InningsSection innings1) {
        this.innings1 = innings1;
    }

    public InningsSection getInnings2() {
        return innings2;
    }

    public void setInnings2(InningsSection innings2) {
        this.innings2 = innings2;
    }
}
