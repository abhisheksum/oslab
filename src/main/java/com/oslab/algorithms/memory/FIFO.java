package com.oslab.algorithms.memory;

import com.oslab.memory.PageReference;
import com.oslab.memory.PageReplacementResult;
import com.oslab.memory.PageReplacementStep;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FIFO implements PageReplacementAlgorithm {
    @Override public String name() { return "FIFO"; }

    @Override
    public PageReplacementResult simulate(List<PageReference> references, int frameCount) {
        PageReplacementSupport.validate(references, frameCount);
        List<Integer> frames = new ArrayList<>();
        List<PageReplacementStep> steps = new ArrayList<>();
        int pointer = 0;
        int faults = 0;
        int hits = 0;
        for (int i = 0; i < references.size(); i++) {
            int page = references.get(i).pageNumber();
            int index = frames.indexOf(page);
            boolean hit = index >= 0;
            Integer replaced = null;
            if (hit) {
                hits++;
            } else {
                faults++;
                if (frames.size() < frameCount) {
                    frames.add(page);
                } else {
                    replaced = frames.set(pointer, page);
                    pointer = (pointer + 1) % frameCount;
                }
            }
            steps.add(new PageReplacementStep(i + 1, references.get(i),
                    PageReplacementSupport.displayFrames(frames, frameCount), hit, replaced));
        }
        return PageReplacementSupport.result(name(), frameCount, steps, faults, hits);
    }
}
