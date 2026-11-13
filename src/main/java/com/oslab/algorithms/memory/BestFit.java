package com.oslab.algorithms.memory;

import com.oslab.memory.MemoryAllocationResult;
import com.oslab.memory.MemoryBlock;
import com.oslab.memory.MemoryProcess;

import java.util.List;
import java.util.Comparator;

public class BestFit implements MemoryAllocationAlgorithm {
    @Override public String name() { return "Best Fit"; }

    @Override
    public MemoryAllocationResult allocate(List<MemoryBlock> blocks, MemoryProcess process) {
        List<MemoryBlock> result = AllocationSupport.copies(blocks);
        int selected = -1;
        for (int i = 0; i < result.size(); i++) {
            MemoryBlock block = result.get(i);
            if (block.isFree() && block.size() >= process.requiredMemory()
                    && (selected < 0 || block.size() < result.get(selected).size())) {
                selected = i;
            }
        }
        if (selected >= 0) {
            AllocationSupport.allocateAndSplit(result, selected, process.id(), process.requiredMemory());
            return AllocationSupport.result(result, true, "Allocated using Best Fit");
        }
        return AllocationSupport.result(result, false, "Insufficient contiguous memory for " + process.id());
    }
}
