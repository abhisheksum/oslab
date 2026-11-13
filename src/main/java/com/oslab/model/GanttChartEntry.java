package com.oslab.model;

public record GanttChartEntry(String processId, int startTime, int endTime) {
    public GanttChartEntry {
        if (processId == null || processId.isBlank() || startTime < 0 || endTime <= startTime) {
            throw new IllegalArgumentException("Invalid Gantt chart entry");
        }
    }

    public int duration() {
        return endTime - startTime;
    }

    public boolean isIdle() {
        return "IDLE".equals(processId);
    }
}
