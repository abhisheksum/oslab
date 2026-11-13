package com.oslab.model.deadlock;

public record ResourceEdge(String from, String to, EdgeType type) {
    public enum EdgeType { REQUEST, ALLOCATION }

    public ResourceEdge {
        if (from == null || from.isBlank() || to == null || to.isBlank() || type == null) {
            throw new IllegalArgumentException("Resource edge must have endpoints and a type");
        }
    }
}
