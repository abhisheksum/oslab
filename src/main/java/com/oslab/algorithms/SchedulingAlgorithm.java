package com.oslab.algorithms;

import com.oslab.model.Process;
import com.oslab.model.SchedulingResult;

import java.util.List;

public interface SchedulingAlgorithm {
    String name();

    SchedulingResult schedule(List<Process> processes);
}
