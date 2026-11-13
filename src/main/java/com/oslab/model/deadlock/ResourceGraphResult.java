package com.oslab.model.deadlock;

import java.util.List;

public record ResourceGraphResult(boolean cycleDetected, List<String> cycleNodes) {
    public ResourceGraphResult {
        cycleNodes = List.copyOf(cycleNodes);
    }
}
