package com.oslab.model.synchronization;

import java.util.List;

public record PhilosopherSnapshot(List<ThreadState> states, List<Integer> forkOwners,
                                  List<String> operations) {
    public PhilosopherSnapshot {
        states = List.copyOf(states);
        forkOwners = List.copyOf(forkOwners);
        operations = List.copyOf(operations);
    }
}
