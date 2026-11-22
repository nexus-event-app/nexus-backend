package com.nexus.backend.enums;

public enum ImportSourceType {

    RSS_NEWS(true, 30, 1),
    REMOTIVE_JOBS(false, 360, 360),
    ARBEITNOW_JOBS(false, 60, 5),
    JOBICY_JOBS(false, 360, 60),
    GETONBRD_JOBS(false, 120, 5),
    ADZUNA_JOBS(false, 360, 60);

    private final boolean news;
    private final int minIntervalMinutes;
    private final int manualCooldownMinutes;

    ImportSourceType(boolean news, int minIntervalMinutes, int manualCooldownMinutes) {
        this.news = news;
        this.minIntervalMinutes = minIntervalMinutes;
        this.manualCooldownMinutes = manualCooldownMinutes;
    }

    public boolean isNews() {
        return news;
    }

    public int getMinIntervalMinutes() {
        return minIntervalMinutes;
    }

    public int getManualCooldownMinutes() {
        return manualCooldownMinutes;
    }
}
