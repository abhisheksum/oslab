package com.oslab.algorithms;

import com.oslab.model.GanttChartEntry;
import com.oslab.model.Process;
import com.oslab.model.ProcessMetrics;
import com.oslab.model.SchedulingResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AlgorithmSupport {
    private AlgorithmSupport() {
    }

    public static List<Process> validateAndSort(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            throw new IllegalArgumentException("At least one process is required");
        }
        Map<String, Boolean> ids = new HashMap<>();
        for (Process process : processes) {
            if (ids.put(process.id(), Boolean.TRUE) != null) {
                throw new IllegalArgumentException("Process IDs must be unique");
            }
        }
        return processes.stream()
                .map(Process::copy)
                .sorted(Comparator.comparingInt(Process::arrivalTime).thenComparing(Process::id))
                .toList();
    }

    public static SchedulingResult result(String name, List<Process> processes,
                                          List<GanttChartEntry> entries,
                                          Map<String, Integer> completion,
                                          Map<String, Integer> response) {
        List<Process> ordered = validateAndSort(processes);
        Map<String, ProcessMetrics> metrics = new LinkedHashMap<>();
        double waiting = 0;
        double turnaround = 0;
        int busy = 0;
        int first = ordered.stream().mapToInt(Process::arrivalTime).min().orElse(0);
        int last = entries.stream().mapToInt(GanttChartEntry::endTime).max().orElse(first);
        for (Process process : ordered) {
            int complete = completion.get(process.id());
            int turn = complete - process.arrivalTime();
            int wait = turn - process.burstTime();
            metrics.put(process.id(), new ProcessMetrics(process, complete, turn, wait,
                    response.getOrDefault(process.id(), complete - process.arrivalTime())));
            waiting += wait;
            turnaround += turn;
            busy += process.burstTime();
        }
        double span = last - first;
        return new SchedulingResult(name, mergeEntries(entries), metrics,
                waiting / ordered.size(), turnaround / ordered.size(),
                span == 0 ? 0 : busy * 100.0 / span,
                span == 0 ? 0 : ordered.size() / span);
    }

    public static void addEntry(List<GanttChartEntry> entries, String id, int start, int end) {
        if (end <= start) {
            return;
        }
        if (!entries.isEmpty()) {
            GanttChartEntry previous = entries.get(entries.size() - 1);
            if (previous.processId().equals(id) && previous.endTime() == start) {
                entries.set(entries.size() - 1, new GanttChartEntry(id, previous.startTime(), end));
                return;
            }
        }
        entries.add(new GanttChartEntry(id, start, end));
    }

    private static List<GanttChartEntry> mergeEntries(List<GanttChartEntry> entries) {
        List<GanttChartEntry> merged = new ArrayList<>();
        for (GanttChartEntry entry : entries) {
            addEntry(merged, entry.processId(), entry.startTime(), entry.endTime());
        }
        return merged;
    }
}
