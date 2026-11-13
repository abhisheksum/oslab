package com.oslab.synchronization;

import com.oslab.algorithms.synchronization.BoundedBuffer;
import com.oslab.algorithms.synchronization.DiningPhilosophersSimulation;
import com.oslab.algorithms.synchronization.ProducerConsumerSimulation;
import com.oslab.algorithms.synchronization.ReaderWriterSimulation;
import com.oslab.model.synchronization.BufferSnapshot;
import com.oslab.model.synchronization.ReaderWriterSnapshot;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynchronizationSimulationTest {
    @Test
    void boundedBufferNeverExceedsCapacity() throws Exception {
        BoundedBuffer buffer = new BoundedBuffer(2);
        buffer.put(1, true);
        buffer.put(2, true);
        assertEquals(2, buffer.size());
        assertEquals(0, buffer.emptyPermits());
        assertEquals(1, buffer.take(true));
        assertEquals(1, buffer.size());
    }

    @Test
    void synchronizedProducerConsumerMaintainsSemaphoreInvariant() throws Exception {
        try (ProducerConsumerSimulation simulation = new ProducerConsumerSimulation(3, true)) {
            simulation.start(10, null);
            TimeUnit.MILLISECONDS.sleep(100);
            BufferSnapshot snapshot = simulation.snapshot();
            assertTrue(snapshot.items().size() <= snapshot.capacity());
            assertEquals(snapshot.capacity() - snapshot.items().size(), snapshot.emptySemaphore());
            assertEquals(snapshot.items().size(), snapshot.fullSemaphore());
        }
    }

    @Test
    void producerConsumerStopsAndResetClearsState() throws Exception {
        try (ProducerConsumerSimulation simulation = new ProducerConsumerSimulation(2, true)) {
            simulation.start(10, null);
            TimeUnit.MILLISECONDS.sleep(30);
            simulation.stop();
            assertFalse(simulation.isRunning());
            simulation.reset();
            assertEquals(0, simulation.snapshot().items().size());
            assertFalse(simulation.isRunning());
        }
    }

    @Test
    void deadlockFreePhilosophersUseOrderedForkStrategy() throws Exception {
        try (DiningPhilosophersSimulation simulation = new DiningPhilosophersSimulation(true)) {
            assertTrue(simulation.isDeadlockFree());
            simulation.start(10, null);
            TimeUnit.MILLISECONDS.sleep(100);
            simulation.stop();
            assertFalse(simulation.snapshot().states().stream()
                    .anyMatch(state -> state.name().equals("RUNNING")));
        }
    }

    @Test
    void philosopherForksAreReleasedWhenStopped() throws Exception {
        try (DiningPhilosophersSimulation simulation = new DiningPhilosophersSimulation(true)) {
            simulation.start(5, null);
            TimeUnit.MILLISECONDS.sleep(50);
            simulation.stop();
            assertTrue(simulation.snapshot().forkOwners().stream().allMatch(owner -> owner == -1));
        }
    }

    @Test
    void readerWriterHasAtMostOneWriterAndReset() throws Exception {
        try (ReaderWriterSimulation simulation = new ReaderWriterSimulation(3, 2, true)) {
            simulation.start(10, null);
            TimeUnit.MILLISECONDS.sleep(100);
            ReaderWriterSnapshot snapshot = simulation.snapshot();
            assertTrue(snapshot.currentWriter() < 2);
            simulation.reset();
            assertEquals(0, simulation.snapshot().sharedValue());
            assertEquals(0, simulation.snapshot().activeReaders());
            assertFalse(simulation.isRunning());
        }
    }

    @Test
    void readerWriterThreadsTerminateOnStop() throws Exception {
        ReaderWriterSimulation simulation = new ReaderWriterSimulation(2, 1, false);
        simulation.start(5, null);
        TimeUnit.MILLISECONDS.sleep(30);
        simulation.stop();
        assertFalse(simulation.isRunning());
    }
}
