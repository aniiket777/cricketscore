package com.cricket.score.dto;

import com.cricket.score.model.ExtraType;
import com.cricket.score.model.WicketType;

public class LiveBallRequestDto {
    private int runsScored = 0;
    private ExtraType extraType = ExtraType.NONE;
    private int extraRuns = 0;
    private boolean isWicket = false;
    private WicketType wicketType = WicketType.NONE;
    private String fielderName;
    private String commentary;

    public LiveBallRequestDto() {}

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
}
