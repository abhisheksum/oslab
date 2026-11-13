package com.oslab.service;

import com.oslab.algorithms.SchedulingAlgorithm;
import com.oslab.model.Process;
import com.oslab.model.SchedulingResult;

import java.util.List;
import java.util.Map;

public class SchedulingService {
    public SchedulingResult run(SchedulingAlgorithm algorithm, List<Process> processes) {
        return algorithm.schedule(processes);
    }

    public Map<String, SchedulingResult> compare(List<SchedulingAlgorithm> algorithms, List<Process> processes) {
        return algorithms.stream().collect(java.util.stream.Collectors.toMap(
                SchedulingAlgorithm::name, algorithm -> run(algorithm, processes)));
    }
}
