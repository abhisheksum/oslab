package com.oslab.model.analytics;

public record AnalyticsSummary(
        long totalSimulations,
        long cpuSimulations,
        long memorySimulations,
        long deadlockSimulations,
        long synchronizationSimulations
) {
}
