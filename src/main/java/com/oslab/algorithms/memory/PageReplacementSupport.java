package com.oslab.algorithms.memory;

import com.oslab.memory.PageReference;
import com.oslab.memory.PageReplacementResult;
import com.oslab.memory.PageReplacementStep;

import java.util.ArrayList;
import java.util.List;

final class PageReplacementSupport {
    private PageReplacementSupport() {}

    static List<PageReference> validate(List<PageReference> references, int frameCount) {
        if (references == null) {
            throw new IllegalArgumentException("Page reference string is required");
        }
        if (frameCount <= 0) {
            throw new IllegalArgumentException("Frame count must be positive");
        }
        return List.copyOf(references);
    }

    static PageReplacementResult result(String name, int frames, List<PageReplacementStep> steps,
                                        int faults, int hits) {
        int total = faults + hits;
        return new PageReplacementResult(name, frames, steps, faults, hits,
                total == 0 ? 0 : faults * 100.0 / total,
                total == 0 ? 0 : hits * 100.0 / total);
    }

    static List<Integer> displayFrames(List<Integer> frames, int capacity) {
        List<Integer> display = new ArrayList<>(frames);
        while (display.size() < capacity) {
            display.add(null);
        }
        return display;
    }
}
