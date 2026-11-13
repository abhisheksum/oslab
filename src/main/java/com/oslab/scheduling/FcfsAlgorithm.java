package com.oslab.scheduling;

import com.oslab.algorithms.AlgorithmSupport;
import com.oslab.algorithms.SchedulingAlgorithm;
import com.oslab.model.GanttChartEntry;
import com.oslab.model.Process;
import com.oslab.model.SchedulingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FcfsAlgorithm implements SchedulingAlgorithm {
    @Override public String name() { return "FCFS"; }

    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> ordered = AlgorithmSupport.validateAndSort(processes);
        List<GanttChartEntry> entries = new ArrayList<>();
        Map<String, Integer> completion = new HashMap<>();
        Map<String, Integer> response = new HashMap<>();
        int time = 0;
        for (Process process : ordered) {
            if (time < process.arrivalTime()) {
                AlgorithmSupport.addEntry(entries, "IDLE", time, process.arrivalTime());
                time = process.arrivalTime();
            }
            response.put(process.id(), time - process.arrivalTime());
            AlgorithmSupport.addEntry(entries, process.id(), time, time + process.burstTime());
            time += process.burstTime();
            completion.put(process.id(), time);
        }
        return AlgorithmSupport.result(name(), ordered, entries, completion, response);
    }
}
