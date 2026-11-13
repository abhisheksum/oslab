package com.oslab.model;

public record ProcessMetrics(
        Process process,
        int completionTime,
        int turnaroundTime,
        int waitingTime,
        int responseTime
) {
}
