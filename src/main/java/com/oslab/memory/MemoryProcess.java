package com.oslab.memory;

public record MemoryProcess(String id, int requiredMemory) {
    public MemoryProcess {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Process ID is required");
        }
        if (requiredMemory <= 0) {
            throw new IllegalArgumentException("Required memory must be positive");
        }
    }
}
