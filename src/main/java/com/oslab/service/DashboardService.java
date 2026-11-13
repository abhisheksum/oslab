package com.oslab.service;

import com.oslab.model.SimulationActivity;
import com.oslab.model.SimulationSummary;
import com.oslab.repository.SimulationRepository;

import java.util.List;

public class DashboardService {
    private final SimulationRepository repository;

    public DashboardService(SimulationRepository repository) {
        this.repository = repository;
    }

    public SimulationSummary getSummary() {
        return repository.getSummary();
    }

    public List<SimulationActivity> getRecentActivity() {
        return repository.getRecentActivity();
    }
}
