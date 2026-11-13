package com.oslab.model.deadlock;

import java.util.List;

public record DeadlockDetectionResult(
        List<String> deadlockedProcesses,
        boolean deadlockDetected,
        List<List<Integer>> workProgression,
        List<List<Boolean>> finishProgression
) {
    public DeadlockDetectionResult {
        deadlockedProcesses = List.copyOf(deadlockedProcesses);
        workProgression = workProgression.stream().map(List::copyOf).toList();
        finishProgression = finishProgression.stream().map(List::copyOf).toList();
    }
}
