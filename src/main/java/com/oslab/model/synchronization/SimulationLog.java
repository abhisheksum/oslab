package com.oslab.model.synchronization;

import java.time.LocalTime;

public record SimulationLog(LocalTime time, String message) {
    @Override
    public String toString() {
        return time + "  " + message;
    }
}
