package com.oslab.memory;

import java.util.List;

public record MemoryAllocationResult(
        boolean allocated,
        String message,
        List<MemoryBlock> blocks,
        int freeMemory,
        int internalFragmentation,
        int externalFragmentation,
        double utilizationPercentage
) {
    public MemoryAllocationResult {
        blocks = blocks.stream().map(MemoryBlock::copy).toList();
    }
}
