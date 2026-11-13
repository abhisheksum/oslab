package com.oslab.model.synchronization;

import java.util.List;

public record BufferSnapshot(List<Integer> items, int capacity, int emptySlots, int fullSlots,
                             int emptySemaphore, int fullSemaphore, ThreadState producerState,
                             ThreadState consumerState, List<String> operations) {
    public BufferSnapshot {
        items = List.copyOf(items);
        operations = List.copyOf(operations);
    }
}
