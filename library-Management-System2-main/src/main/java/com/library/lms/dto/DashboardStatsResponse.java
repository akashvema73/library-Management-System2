package com.library.lms.dto;

import java.util.Map;

public class DashboardStatsResponse {

    private Map<String, Object> stats;

    public DashboardStatsResponse(Map<String, Object> stats) {
        this.stats = stats;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }
}
