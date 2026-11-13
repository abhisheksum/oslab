package com.oslab.algorithms.memory;

import com.oslab.memory.MemoryBlock;
import com.oslab.memory.MemoryProcess;
import com.oslab.memory.MemoryAllocationResult;

import java.util.List;

public interface MemoryAllocationAlgorithm {
    String name();

    MemoryAllocationResult allocate(List<MemoryBlock> blocks, MemoryProcess process);
}
