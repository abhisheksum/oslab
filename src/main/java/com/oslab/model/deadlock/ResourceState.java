package com.oslab.model.deadlock;

import java.util.ArrayList;
import java.util.List;

public record ResourceState(
        List<Integer> available,
        List<List<Integer>> allocation,
        List<List<Integer>> maximum
) {
    public ResourceState {
        List<Integer> normalizedAvailable = immutableVector(available);
        List<List<Integer>> normalizedAllocation = immutableMatrix(allocation);
        List<List<Integer>> normalizedMaximum = immutableMatrix(maximum);
        validateDimensions(normalizedAvailable, normalizedAllocation, normalizedMaximum);
        available = normalizedAvailable;
        allocation = normalizedAllocation;
        maximum = normalizedMaximum;
    }

    public int processCount() { return allocation.size(); }
    public int resourceCount() { return available.size(); }

    private static void validateDimensions(List<Integer> available, List<List<Integer>> allocation,
                                           List<List<Integer>> maximum) {
        if (available.isEmpty() || allocation.isEmpty() || maximum.size() != allocation.size()) {
            throw new IllegalArgumentException("Resource state must contain processes and resources");
        }
        for (List<Integer> row : allocation) {
            if (row.size() != available.size() || row.stream().anyMatch(value -> value < 0)) {
                throw new IllegalArgumentException("Allocation matrix has invalid dimensions or values");
            }
        }
        for (List<Integer> row : maximum) {
            if (row.size() != available.size() || row.stream().anyMatch(value -> value < 0)) {
                throw new IllegalArgumentException("Maximum matrix has invalid dimensions or values");
            }
        }
        for (int process = 0; process < allocation.size(); process++) {
            for (int resource = 0; resource < available.size(); resource++) {
                if (maximum.get(process).get(resource) < allocation.get(process).get(resource)) {
                    throw new IllegalArgumentException("Maximum demand cannot be less than allocation");
                }
            }
        }
    }

    private static List<Integer> immutableVector(List<Integer> source) {
        if (source == null || source.isEmpty() || source.stream().anyMatch(value -> value == null || value < 0)) {
            throw new IllegalArgumentException("Resource vector must contain non-negative values");
        }
        return List.copyOf(source);
    }

    private static List<List<Integer>> immutableMatrix(List<List<Integer>> source) {
        if (source == null || source.isEmpty() || source.stream().anyMatch(row -> row == null)) {
            throw new IllegalArgumentException("Matrix must contain rows");
        }
        return source.stream().map(row -> List.copyOf(new ArrayList<>(row))).toList();
    }
}
