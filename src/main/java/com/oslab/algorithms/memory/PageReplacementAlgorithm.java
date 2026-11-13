package com.oslab.algorithms.memory;

import com.oslab.memory.PageReference;
import com.oslab.memory.PageReplacementResult;

import java.util.List;

public interface PageReplacementAlgorithm {
    String name();

    PageReplacementResult simulate(List<PageReference> references, int frameCount);
}
