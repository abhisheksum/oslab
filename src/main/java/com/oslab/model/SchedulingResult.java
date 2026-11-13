package com.oslab.model;

import java.util.List;
import java.util.Map;

public record SchedulingResult(
        String algorithmName,
        List<GanttChartEntry> ganttEntries,
        Map<String, ProcessMetrics> processMetrics,
        double averageWaitingTime,
        double averageTurnaroundTime,
        double cpuUtilization,
        double throughput
) {
    public SchedulingResult {
        ganttEntries = List.copyOf(ganttEntries);
        processMetrics = Map.copyOf(processMetrics);
    }
}
