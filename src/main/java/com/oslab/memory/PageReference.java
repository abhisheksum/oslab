package com.oslab.memory;

public record PageReference(int pageNumber) {
    public PageReference {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
    }
}
