package com.oslab.repository;

import com.oslab.model.analytics.SimulationRecord;

import java.util.List;
import java.util.Optional;

public interface AnalyticsRepository extends AutoCloseable {
    SimulationRecord save(SimulationRecord record);
    List<SimulationRecord> find(String module, String algorithm);
    Optional<SimulationRecord> findById(long id);
    void deleteById(long id);
    @Override void close();
}
