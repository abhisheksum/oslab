package com.oslab.service;

import com.oslab.algorithms.deadlock.BankersAlgorithm;
import com.oslab.algorithms.deadlock.DeadlockDetectionAlgorithm;
import com.oslab.algorithms.deadlock.ResourceAllocationGraphAnalyzer;
import com.oslab.model.deadlock.BankersResult;
import com.oslab.model.deadlock.DeadlockDetectionResult;
import com.oslab.model.deadlock.ResourceEdge;
import com.oslab.model.deadlock.ResourceGraphResult;
import com.oslab.model.deadlock.ResourceNode;
import com.oslab.model.deadlock.ResourceState;

import java.util.List;

public class DeadlockService {
    private final BankersAlgorithm bankers = new BankersAlgorithm();
    private final DeadlockDetectionAlgorithm detector = new DeadlockDetectionAlgorithm();
    private final ResourceAllocationGraphAnalyzer graphAnalyzer = new ResourceAllocationGraphAnalyzer();

    public BankersResult analyzeBankers(ResourceState state) { return bankers.analyze(state); }

    public DeadlockDetectionResult detect(List<Integer> available, List<List<Integer>> allocation,
                                          List<List<Integer>> request) {
        return detector.detect(available, allocation, request);
    }

    public ResourceGraphResult analyzeGraph(List<ResourceNode> nodes, List<ResourceEdge> edges) {
        return graphAnalyzer.analyze(nodes, edges);
    }
}
