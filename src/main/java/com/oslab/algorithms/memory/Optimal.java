package com.oslab.algorithms.memory;

import com.oslab.memory.PageReference;
import com.oslab.memory.PageReplacementResult;
import com.oslab.memory.PageReplacementStep;

import java.util.ArrayList;
import java.util.List;

public class Optimal implements PageReplacementAlgorithm {
    @Override public String name() { return "Optimal"; }

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
            } else {
                faults++;
                if (frames.size() < frameCount) {
                    frames.add(page);
                } else {
                    int victim = farthestNextUse(frames, references, i + 1);
                    replaced = frames.set(victim, page);
                }
            }
            steps.add(new PageReplacementStep(i + 1, references.get(i),
                    PageReplacementSupport.displayFrames(frames, frameCount), hit, replaced));
        }
        return PageReplacementSupport.result(name(), frameCount, steps, faults, hits);
    }

    private int farthestNextUse(List<Integer> frames, List<PageReference> references, int from) {
        int victim = 0;
        int farthest = -1;
        for (int i = 0; i < frames.size(); i++) {
            int next = Integer.MAX_VALUE;
            for (int j = from; j < references.size(); j++) {
                if (references.get(j).pageNumber() == frames.get(i)) {
                    next = j;
                    break;
                }
            }
            if (next > farthest) {
                farthest = next;
                victim = i;
            }
        }
        return victim;
    }
}
