package com.oslab.model;

import java.util.Objects;

public record Process(String id, int arrivalTime, int burstTime, int priority) {
    public Process {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Process ID is required");
        }
        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Arrival time cannot be negative");
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException("Burst time must be positive");
        }
    }

    public Process copy() {
        return new Process(id, arrivalTime, burstTime, priority);
    }
}
