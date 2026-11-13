package com.oslab.service;

import com.oslab.algorithms.memory.PageReplacementAlgorithm;
import com.oslab.memory.PageReference;
import com.oslab.memory.PageReplacementResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PageReplacementService {
    public PageReplacementResult run(PageReplacementAlgorithm algorithm, List<PageReference> references,
                                     int frameCount) {
        return algorithm.simulate(references, frameCount);
    }

    public Map<String, PageReplacementResult> compare(List<PageReplacementAlgorithm> algorithms,
                                                       List<PageReference> references, int frameCount) {
        return algorithms.stream().collect(Collectors.toMap(PageReplacementAlgorithm::name,
                algorithm -> run(algorithm, references, frameCount)));
    }
}
