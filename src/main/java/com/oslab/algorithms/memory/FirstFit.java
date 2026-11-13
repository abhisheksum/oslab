package com.oslab.algorithms.memory;

import com.oslab.memory.MemoryAllocationResult;
import com.oslab.memory.MemoryBlock;
import com.oslab.memory.MemoryProcess;

import java.util.List;

public class FirstFit implements MemoryAllocationAlgorithm {
    @Override public String name() { return "First Fit"; }

    @Override
    public MemoryAllocationResult allocate(List<MemoryBlock> blocks, MemoryProcess process) {
        List<MemoryBlock> result = AllocationSupport.copies(blocks);
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).isFree() && result.get(i).size() >= process.requiredMemory()) {
                AllocationSupport.allocateAndSplit(result, i, process.id(), process.requiredMemory());
                return AllocationSupport.result(result, true, "Allocated using First Fit");
            }
        }
        return AllocationSupport.result(result, false, "Insufficient contiguous memory for " + process.id());
    }
}
