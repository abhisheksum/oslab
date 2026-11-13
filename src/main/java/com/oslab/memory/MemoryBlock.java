package com.oslab.memory;

import java.util.Objects;

public final class MemoryBlock {
    private final int startAddress;
    private final int size;
    private String processId;

    public MemoryBlock(int startAddress, int size) {
        if (startAddress < 0 || size <= 0) {
            throw new IllegalArgumentException("Memory block address and size must be valid");
        }
        this.startAddress = startAddress;
        this.size = size;
    }

    private MemoryBlock(int startAddress, int size, String processId) {
        this(startAddress, size);
        this.processId = processId;
    }

    public int startAddress() { return startAddress; }
    public int size() { return size; }
    public String processId() { return processId; }
    public boolean isFree() { return processId == null; }

    public void allocate(String id) {
        if (!isFree()) {
            throw new IllegalStateException("Memory block is already allocated");
        }
        processId = Objects.requireNonNull(id);
    }

    public void free() {
        processId = null;
    }

    public MemoryBlock copy() {
        return new MemoryBlock(startAddress, size, processId);
    }
}
