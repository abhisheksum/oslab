package com.oslab.repository;

import com.oslab.model.SimulationActivity;
import com.oslab.model.SimulationSummary;

import java.util.List;

public interface SimulationRepository {
    SimulationSummary getSummary();

    List<SimulationActivity> getRecentActivity();
}
