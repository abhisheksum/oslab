package com.oslab.repository;

import com.oslab.model.SimulationActivity;
import com.oslab.model.SimulationSummary;

import java.time.LocalDateTime;
import java.util.List;

public class InMemorySimulationRepository implements SimulationRepository {
    @Override
    public SimulationSummary getSummary() {
        return new SimulationSummary(24, 86, 12);
    }

    @Override
    public List<SimulationActivity> getRecentActivity() {
        return List.of(
                new SimulationActivity("Round Robin Analysis", "CPU Scheduling", "Completed",
                        LocalDateTime.now().minusMinutes(18)),
                new SimulationActivity("Banker's Algorithm", "Deadlock Detection", "Completed",
                        LocalDateTime.now().minusHours(2)),
                new SimulationActivity("Paging Visualization", "Memory Management", "In progress",
                        LocalDateTime.now().minusHours(5)),
                new SimulationActivity("Producer Consumer", "Synchronization", "Completed",
                        LocalDateTime.now().minusDays(1))
        );
    }
}
