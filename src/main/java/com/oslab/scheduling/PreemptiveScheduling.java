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

abstract class PreemptiveScheduling implements SchedulingAlgorithm {
    protected abstract Comparator<Process> comparator(Map<String, Integer> remaining);

    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> ordered = AlgorithmSupport.validateAndSort(processes);
        List<GanttChartEntry> entries = new ArrayList<>();
        Map<String, Integer> remaining = new HashMap<>();
        Map<String, Integer> completion = new HashMap<>();
        Map<String, Integer> response = new HashMap<>();
        ordered.forEach(p -> remaining.put(p.id(), p.burstTime()));
        int completed = 0;
        int time = 0;
        while (completed < ordered.size()) {
            int currentTime = time;
            List<Process> ready = ordered.stream().filter(p -> p.arrivalTime() <= currentTime
                    && remaining.get(p.id()) > 0).toList();
            if (ready.isEmpty()) {
                int next = ordered.stream().filter(p -> remaining.get(p.id()) > 0)
                        .mapToInt(Process::arrivalTime).filter(a -> a > currentTime).min().orElse(time + 1);
                AlgorithmSupport.addEntry(entries, "IDLE", time, next);
                time = next;
                continue;
            }
            Process process = ready.stream().min(comparator(remaining)
                    .thenComparingInt(Process::arrivalTime).thenComparing(Process::id)).orElseThrow();
            response.putIfAbsent(process.id(), time - process.arrivalTime());
            AlgorithmSupport.addEntry(entries, process.id(), time, time + 1);
            remaining.put(process.id(), remaining.get(process.id()) - 1);
            time++;
            if (remaining.get(process.id()) == 0) {
                completion.put(process.id(), time);
                completed++;
            }
        }
        return AlgorithmSupport.result(name(), ordered, entries, completion, response);
    }
}
