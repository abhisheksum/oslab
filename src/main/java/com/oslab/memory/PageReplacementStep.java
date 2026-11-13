package com.oslab.memory;

import java.util.List;

public record PageReplacementStep(
        int stepNumber,
        PageReference reference,
        List<Integer> frames,
        boolean hit,
        Integer replacedPage
) {
    public PageReplacementStep {
        frames = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(frames));
    }
}
