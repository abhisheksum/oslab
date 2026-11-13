package com.oslab.scheduling;

import com.oslab.model.Process;
import java.util.Comparator;
import java.util.Map;

public class SrtfAlgorithm extends PreemptiveScheduling {
    @Override public String name() { return "SRTF (Preemptive)"; }
    @Override protected Comparator<Process> comparator(Map<String, Integer> remaining) {
        return Comparator.comparingInt(p -> remaining.get(p.id()));
    }
}
