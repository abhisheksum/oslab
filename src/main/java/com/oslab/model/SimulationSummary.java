package com.oslab.model;

public record SimulationSummary(
        int totalSimulations,
        int totalProcesses,
        int availableAlgorithms
) {
}
