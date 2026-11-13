package com.oslab.model.analytics;

import java.time.LocalDateTime;

public record SimulationRecord(
        long id,
        String module,
        String algorithm,
        String inputParameters,
        String results,
        LocalDateTime executedAt
) {
    public SimulationRecord {
        if (module == null || module.isBlank() || algorithm == null || algorithm.isBlank()
                || inputParameters == null || results == null || executedAt == null) {
            throw new IllegalArgumentException("Simulation record fields are required");
        }
    }

    public SimulationRecord(String module, String algorithm, String inputParameters,
                            String results, LocalDateTime executedAt) {
        this(0, module, algorithm, inputParameters, results, executedAt);
    }
}
