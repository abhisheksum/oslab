package com.oslab.scheduling;

import com.oslab.algorithms.AlgorithmSupport;
import com.oslab.algorithms.SchedulingAlgorithm;
import com.oslab.model.GanttChartEntry;
import com.oslab.model.Process;
import com.oslab.model.SchedulingResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriorityNonPreemptiveAlgorithm implements SchedulingAlgorithm {
    @Override public String name() { return "Priority (Non-preemptive)"; }

    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> pending = new ArrayList<>(AlgorithmSupport.validateAndSort(processes));
        List<GanttChartEntry> entries = new ArrayList<>();
        Map<String, Integer> completion = new HashMap<>();
        Map<String, Integer> response = new HashMap<>();
        int time = 0;
        while (!pending.isEmpty()) {
            int currentTime = time;
            List<Process> ready = pending.stream().filter(p -> p.arrivalTime() <= currentTime).toList();
            if (ready.isEmpty()) {
                int next = pending.get(0).arrivalTime();
                AlgorithmSupport.addEntry(entries, "IDLE", time, next);
                time = next;
                continue;
            }
            Process process = ready.stream().min(Comparator.comparingInt(Process::priority)
                    .thenComparingInt(Process::arrivalTime).thenComparing(Process::id)).orElseThrow();
            pending.remove(process);
            response.put(process.id(), time - process.arrivalTime());
            AlgorithmSupport.addEntry(entries, process.id(), time, time + process.burstTime());
            time += process.burstTime();
            completion.put(process.id(), time);
        }
        return AlgorithmSupport.result(name(), processes, entries, completion, response);
    }
}
