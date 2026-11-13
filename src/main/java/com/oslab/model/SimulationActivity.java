package com.oslab.model;

import java.time.LocalDateTime;

public record SimulationActivity(String name, String category, String status, LocalDateTime completedAt) {
}
