package com.oslab.algorithms.deadlock;

import com.oslab.model.deadlock.DeadlockDetectionResult;

import java.util.ArrayList;
import java.util.List;

public class DeadlockDetectionAlgorithm {
    public DeadlockDetectionResult detect(List<Integer> available, List<List<Integer>> allocation,
                                           List<List<Integer>> request) {
        validate(available, allocation, request);
        List<Integer> work = new ArrayList<>(available);
        boolean[] finish = new boolean[allocation.size()];
        List<List<Integer>> workProgression = new ArrayList<>();
        List<List<Boolean>> finishProgression = new ArrayList<>();
        workProgression.add(List.copyOf(work));
        finishProgression.add(toList(finish));
        boolean progress;
        do {
            progress = false;
            for (int process = 0; process < allocation.size(); process++) {
                if (finish[process] || !canRun(request.get(process), work)) continue;
                for (int resource = 0; resource < work.size(); resource++) {
                    work.set(resource, work.get(resource) + allocation.get(process).get(resource));
                }
                finish[process] = true;
                workProgression.add(List.copyOf(work));
                finishProgression.add(toList(finish));
                progress = true;
            }
        } while (progress);
        List<String> deadlocked = new ArrayList<>();
        for (int process = 0; process < finish.length; process++) {
            if (!finish[process]) deadlocked.add("P" + (process + 1));
        }
        return new DeadlockDetectionResult(deadlocked, !deadlocked.isEmpty(),
                workProgression, finishProgression);
    }

    private void validate(List<Integer> available, List<List<Integer>> allocation,
                          List<List<Integer>> request) {
        if (available == null || available.isEmpty() || allocation == null || allocation.isEmpty()
                || request == null || request.size() != allocation.size()) {
            throw new IllegalArgumentException("Invalid detection matrices or available vector");
        }
        int resources = available.size();
        for (List<Integer> row : allocation) {
            if (row == null || row.size() != resources || row.stream().anyMatch(value -> value < 0)) {
                throw new IllegalArgumentException("Invalid allocation matrix");
            }
        }
        for (List<Integer> row : request) {
            if (row == null || row.size() != resources || row.stream().anyMatch(value -> value < 0)) {
                throw new IllegalArgumentException("Invalid request matrix");
            }
        }
        if (available.stream().anyMatch(value -> value == null || value < 0)) {
            throw new IllegalArgumentException("Available resources must be non-negative");
        }
    }

    private boolean canRun(List<Integer> request, List<Integer> work) {
        for (int resource = 0; resource < work.size(); resource++) {
            if (request.get(resource) > work.get(resource)) return false;
        }
        return true;
    }

    private List<Boolean> toList(boolean[] values) {
        List<Boolean> result = new ArrayList<>();
        for (boolean value : values) result.add(value);
        return result;
    }
}
