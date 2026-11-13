package com.oslab.algorithms.memory;

import com.oslab.memory.PageReference;
import com.oslab.memory.PageReplacementResult;
import com.oslab.memory.PageReplacementStep;

import java.util.ArrayList;
import java.util.List;

public class LRU implements PageReplacementAlgorithm {
    @Override public String name() { return "LRU"; }

    @Override
    public PageReplacementResult simulate(List<PageReference> references, int frameCount) {
        PageReplacementSupport.validate(references, frameCount);
        List<Integer> frames = new ArrayList<>();
        List<PageReplacementStep> steps = new ArrayList<>();
        int faults = 0;
        int hits = 0;
        for (int i = 0; i < references.size(); i++) {
            int page = references.get(i).pageNumber();
            boolean hit = frames.contains(page);
            Integer replaced = null;
            if (hit) {
                hits++;
                frames.remove((Integer) page);
                frames.add(page);
            } else {
                faults++;
                if (frames.size() == frameCount) {
                    replaced = frames.remove(0);
                }
                frames.add(page);
            }
            steps.add(new PageReplacementStep(i + 1, references.get(i),
                    PageReplacementSupport.displayFrames(frames, frameCount), hit, replaced));
        }
        return PageReplacementSupport.result(name(), frameCount, steps, faults, hits);
    }
}
