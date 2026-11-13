package com.oslab.memory;

import java.util.List;

public record PageReplacementResult(
        String algorithmName,
        int frameCount,
        List<PageReplacementStep> steps,
        int pageFaults,
        int pageHits,
        double pageFaultRatio,
        double pageHitRatio
) {
    public PageReplacementResult {
        steps = List.copyOf(steps);
    }
}
