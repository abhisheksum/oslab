package com.oslab.model.deadlock;

public record ResourceNode(String id, NodeType type, int instances) {
    public enum NodeType { PROCESS, RESOURCE }

    public ResourceNode {
        if (id == null || id.isBlank() || instances <= 0) {
            throw new IllegalArgumentException("Node ID and instance count must be valid");
        }
    }
}
