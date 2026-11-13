package com.oslab.service;

import com.oslab.memory.MemoryAllocationResult;
import com.oslab.memory.PageReplacementResult;
import com.oslab.model.SchedulingResult;
import com.oslab.model.analytics.AnalyticsSummary;
import com.oslab.model.analytics.SimulationRecord;
import com.oslab.model.deadlock.BankersResult;
import com.oslab.model.deadlock.DeadlockDetectionResult;
import com.oslab.model.synchronization.BufferSnapshot;
import com.oslab.model.synchronization.PhilosopherSnapshot;
import com.oslab.model.synchronization.ReaderWriterSnapshot;
import com.oslab.repository.AnalyticsRepository;

import java.time.LocalDateTime;
import java.util.List;

public class AnalyticsService {
    private final AnalyticsRepository repository;

    public AnalyticsService(AnalyticsRepository repository) {
        this.repository = repository;
    }

    public SimulationRecord recordScheduling(String input, SchedulingResult result) {
        return save("CPU Scheduling", result.algorithmName(), input,
                "avgWaiting=" + result.averageWaitingTime() + ",avgTurnaround=" + result.averageTurnaroundTime()
                        + ",cpuUtilization=" + result.cpuUtilization() + ",throughput=" + result.throughput());
    }

    public SimulationRecord recordMemoryAllocation(String input, String algorithm, MemoryAllocationResult result) {
        return save("Memory Management", algorithm, input,
                "allocated=" + result.allocated() + ",freeMemory=" + result.freeMemory()
                        + ",externalFragmentation=" + result.externalFragmentation()
                        + ",utilization=" + result.utilizationPercentage());
    }

    public SimulationRecord recordPageReplacement(String input, PageReplacementResult result) {
        return save("Memory Management", result.algorithmName(), input,
                "faults=" + result.pageFaults() + ",hits=" + result.pageHits()
                        + ",faultRatio=" + result.pageFaultRatio() + ",hitRatio=" + result.pageHitRatio());
    }

    public SimulationRecord recordBankers(String input, BankersResult result) {
        return save("Deadlock Management", "Banker's Algorithm", input,
                "safe=" + result.safe() + ",safeSequence=" + result.safeSequence());
    }

    public SimulationRecord recordDetection(String input, DeadlockDetectionResult result) {
        return save("Deadlock Management", "Deadlock Detection", input,
                "deadlockDetected=" + result.deadlockDetected()
                        + ",deadlockedProcesses=" + result.deadlockedProcesses());
    }

    public SimulationRecord recordSynchronization(String input, String algorithm, BufferSnapshot result) {
        return save("Process Synchronization", algorithm, input,
                "producedOrConsumedOperations=" + result.operations().size()
                        + ",bufferUtilization=" + (result.capacity() == 0 ? 0 : result.fullSlots() * 100.0 / result.capacity()));
    }

    public SimulationRecord recordPhilosophers(String input, PhilosopherSnapshot result) {
        return save("Process Synchronization", "Dining Philosophers", input,
                "operations=" + result.operations().size() + ",forkOwners=" + result.forkOwners());
    }

    public SimulationRecord recordReadersWriters(String input, ReaderWriterSnapshot result) {
        return save("Process Synchronization", "Reader Writer", input,
                "operations=" + result.operations().size() + ",sharedValue=" + result.sharedValue()
                        + ",activeReaders=" + result.activeReaders());
    }

    public List<SimulationRecord> history(String module, String algorithm) {
        return repository.find(module, algorithm);
    }

    public void delete(long id) { repository.deleteById(id); }

    public AnalyticsSummary summary() {
        List<SimulationRecord> all = repository.find("All", "All");
        return new AnalyticsSummary(all.size(),
                all.stream().filter(record -> record.module().equals("CPU Scheduling")).count(),
                all.stream().filter(record -> record.module().equals("Memory Management")).count(),
                all.stream().filter(record -> record.module().equals("Deadlock Management")).count(),
                all.stream().filter(record -> record.module().equals("Process Synchronization")).count());
    }

    private SimulationRecord save(String module, String algorithm, String input, String results) {
        return repository.save(new SimulationRecord(module, algorithm, input, results, LocalDateTime.now()));
    }
}
