package com.oslab.model.deadlock;

import java.util.List;

public record BankersResult(
        List<List<Integer>> need,
        List<String> safeSequence,
        boolean safe,
        List<BankersStep> steps
) {
    public BankersResult {
        need = need.stream().map(List::copyOf).toList();
        safeSequence = List.copyOf(safeSequence);
        steps = List.copyOf(steps);
    }
}
