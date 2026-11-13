package com.oslab.algorithms.memory;

import com.oslab.memory.MemoryAllocationResult;
import com.oslab.memory.MemoryBlock;

import java.util.ArrayList;
import java.util.List;

final class AllocationSupport {
    private AllocationSupport() {}

    static List<MemoryBlock> copies(List<MemoryBlock> source) {
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("At least one memory block is required");
        }
        return new ArrayList<>(source.stream().map(MemoryBlock::copy).toList());
    }

    static MemoryAllocationResult result(List<MemoryBlock> blocks, boolean allocated, String message) {
        int free = blocks.stream().filter(MemoryBlock::isFree).mapToInt(MemoryBlock::size).sum();
        int largest = blocks.stream().filter(MemoryBlock::isFree)
                .mapToInt(MemoryBlock::size).max().orElse(0);
        int total = blocks.stream().mapToInt(MemoryBlock::size).sum();
        double utilization = total == 0 ? 0 : (total - free) * 100.0 / total;
        return new MemoryAllocationResult(allocated, message, blocks, free, 0,
                Math.max(0, free - largest), utilization);
    }

    static void allocateAndSplit(List<MemoryBlock> blocks, int index, String processId, int required) {
        MemoryBlock selected = blocks.get(index);
        int remainder = selected.size() - required;
        int start = selected.startAddress();
        blocks.remove(index);
        MemoryBlock allocated = new MemoryBlock(start, required);
        allocated.allocate(processId);
        blocks.add(index, allocated);
        if (remainder > 0) {
            blocks.add(index + 1, new MemoryBlock(start + required, remainder));
        }
    }
}
