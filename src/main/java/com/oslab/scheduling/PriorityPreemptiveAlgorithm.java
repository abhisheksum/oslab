package com.oslab.scheduling;

import com.oslab.model.Process;
import java.util.Comparator;
import java.util.Map;

public class PriorityPreemptiveAlgorithm extends PreemptiveScheduling {
    @Override public String name() { return "Priority (Preemptive)"; }
    @Override protected Comparator<Process> comparator(Map<String, Integer> remaining) {
        return Comparator.comparingInt(Process::priority);
    }
}
