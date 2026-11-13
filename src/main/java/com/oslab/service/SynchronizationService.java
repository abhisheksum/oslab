package com.oslab.service;

import com.oslab.algorithms.synchronization.DiningPhilosophersSimulation;
import com.oslab.algorithms.synchronization.ProducerConsumerSimulation;
import com.oslab.algorithms.synchronization.ReaderWriterSimulation;

public class SynchronizationService {
    public ProducerConsumerSimulation producerConsumer(int capacity, boolean synchronizedMode) {
        return new ProducerConsumerSimulation(capacity, synchronizedMode);
    }
    public DiningPhilosophersSimulation philosophers(boolean deadlockFree) {
        return new DiningPhilosophersSimulation(deadlockFree);
    }
    public ReaderWriterSimulation readersWriters(int readers, int writers, boolean writerPreference) {
        return new ReaderWriterSimulation(readers, writers, writerPreference);
    }
}
