package com.oslab.model.deadlock;

import java.util.List;

public record BankersStep(
        String processId,
        List<Integer> availableBefore,
        List<Integer> availableAfter,
        boolean executable,
        boolean completed
) {
    public BankersStep {
        availableBefore = List.copyOf(availableBefore);
        availableAfter = List.copyOf(availableAfter);
    }
}
