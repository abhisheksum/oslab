package com.oslab.service;

import com.oslab.algorithms.memory.MemoryAllocationAlgorithm;
import com.oslab.memory.MemoryAllocationResult;
import com.oslab.memory.MemoryBlock;
import com.oslab.memory.MemoryProcess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MemoryAllocationService {
    private List<MemoryBlock> blocks = new ArrayList<>();
    private int totalMemory;

    public void configure(int totalMemory, List<Integer> partitionSizes) {
        if (totalMemory <= 0) {
            throw new IllegalArgumentException("Total memory must be positive");
        }
        if (partitionSizes == null || partitionSizes.isEmpty()
                || partitionSizes.stream().anyMatch(size -> size == null || size <= 0)) {
            throw new IllegalArgumentException("At least one positive partition is required");
        }
        int configured = partitionSizes.stream().mapToInt(Integer::intValue).sum();
        if (configured > totalMemory) {
            throw new IllegalArgumentException("Partition sizes cannot exceed total memory");
        }
        blocks = new ArrayList<>();
        int address = 0;
        for (int size : partitionSizes) {
            blocks.add(new MemoryBlock(address, size));
            address += size;
        }
        if (address < totalMemory) {
            blocks.add(new MemoryBlock(address, totalMemory - address));
        }
        this.totalMemory = totalMemory;
    }

    public MemoryAllocationResult allocate(MemoryAllocationAlgorithm algorithm, MemoryProcess process) {
        ensureConfigured();
        if (blocks.stream().anyMatch(block -> process.id().equals(block.processId()))) {
            throw new IllegalArgumentException("Process is already allocated");
        }
        MemoryAllocationResult result = algorithm.allocate(blocks, process);
        if (result.allocated()) {
            blocks = result.blocks().stream().map(MemoryBlock::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        }
        return result;
    }

    public MemoryAllocationResult deallocate(String processId) {
        ensureConfigured();
        if (processId == null || processId.isBlank()) {
            throw new IllegalArgumentException("Process ID is required");
        }
        boolean found = false;
        for (MemoryBlock block : blocks) {
            if (processId.equals(block.processId())) {
                block.free();
                found = true;
            }
        }
        if (!found) {
            return metrics(false, "Process was not found");
        }
        coalesceFreeBlocks();
        return metrics(true, "Deallocated " + processId);
    }

    public List<MemoryBlock> blocks() {
        ensureConfigured();
        return blocks.stream().map(MemoryBlock::copy).toList();
    }

    public int totalMemory() {
        return totalMemory;
    }

    private void coalesceFreeBlocks() {
        List<MemoryBlock> merged = new ArrayList<>();
        for (MemoryBlock block : blocks) {
            if (!merged.isEmpty() && merged.get(merged.size() - 1).isFree() && block.isFree()) {
                MemoryBlock previous = merged.remove(merged.size() - 1);
                merged.add(new MemoryBlock(previous.startAddress(), previous.size() + block.size()));
            } else {
                merged.add(block.copy());
            }
        }
        blocks = merged;
    }

    private MemoryAllocationResult metrics(boolean success, String message) {
        int free = blocks.stream().filter(MemoryBlock::isFree).mapToInt(MemoryBlock::size).sum();
        int largest = blocks.stream().filter(MemoryBlock::isFree).mapToInt(MemoryBlock::size).max().orElse(0);
        return new MemoryAllocationResult(success, message, blocks, free, 0,
                Math.max(0, free - largest), totalMemory == 0 ? 0 : (totalMemory - free) * 100.0 / totalMemory);
    }

    private void ensureConfigured() {
        if (blocks.isEmpty()) {
            throw new IllegalStateException("Configure memory before using it");
        }
    }
}
