package com.oslab.scheduling;

import com.oslab.algorithms.AlgorithmSupport;
import com.oslab.algorithms.SchedulingAlgorithm;
import com.oslab.model.GanttChartEntry;
import com.oslab.model.Process;
import com.oslab.model.SchedulingResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class RoundRobinAlgorithm implements SchedulingAlgorithm {
    private final int quantum;

    public RoundRobinAlgorithm(int quantum) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be positive");
        }
        this.quantum = quantum;
    }

    public int quantum() {
        return quantum;
    }

    @Override public String name() { return "Round Robin (q=" + quantum + ")"; }

    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> ordered = AlgorithmSupport.validateAndSort(processes);
        List<GanttChartEntry> entries = new ArrayList<>();
        Map<String, Integer> remaining = new HashMap<>();
        Map<String, Integer> completion = new HashMap<>();
        Map<String, Integer> response = new HashMap<>();
        Queue<Process> ready = new ArrayDeque<>();
        int index = 0;
        int time = 0;
        while (completion.size() < ordered.size()) {
            while (index < ordered.size() && ordered.get(index).arrivalTime() <= time) {
                ready.add(ordered.get(index++));
            }
            if (ready.isEmpty()) {
                int next = ordered.get(index).arrivalTime();
                AlgorithmSupport.addEntry(entries, "IDLE", time, next);
                time = next;
                continue;
            }
            Process process = ready.remove();
            int remainingTime = remaining.computeIfAbsent(process.id(), ignored -> process.burstTime());
            response.putIfAbsent(process.id(), time - process.arrivalTime());
            int run = Math.min(quantum, remainingTime);
            AlgorithmSupport.addEntry(entries, process.id(), time, time + run);
            time += run;
            remainingTime -= run;
            while (index < ordered.size() && ordered.get(index).arrivalTime() <= time) {
                ready.add(ordered.get(index++));
            }
            if (remainingTime > 0) {
                remaining.put(process.id(), remainingTime);
                ready.add(process);
            } else {
                completion.put(process.id(), time);
            }
        }
        return AlgorithmSupport.result(name(), ordered, entries, completion, response);
    }
}
