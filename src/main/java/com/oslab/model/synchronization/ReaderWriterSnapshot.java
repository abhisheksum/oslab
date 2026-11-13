package com.oslab.model.synchronization;

import java.util.List;

public record ReaderWriterSnapshot(List<ThreadState> readerStates, List<ThreadState> writerStates,
                                   int activeReaders, int currentWriter, int sharedValue,
                                   List<String> operations) {
    public ReaderWriterSnapshot {
        readerStates = List.copyOf(readerStates);
        writerStates = List.copyOf(writerStates);
        operations = List.copyOf(operations);
    }
}
