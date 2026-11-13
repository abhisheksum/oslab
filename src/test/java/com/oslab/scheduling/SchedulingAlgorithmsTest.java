package com.oslab.scheduling;

import com.oslab.algorithms.SchedulingAlgorithm;
import com.oslab.model.GanttChartEntry;
import com.oslab.model.Process;
import com.oslab.model.SchedulingResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SchedulingAlgorithmsTest {
    private final List<Process> standard = List.of(
            new Process("P1", 0, 7, 2),
            new Process("P2", 2, 4, 1),
            new Process("P3", 4, 1, 3));

    @Test
    void fcfsHandlesIdleAndDifferentArrivals() {
        SchedulingResult result = new FcfsAlgorithm().schedule(List.of(
                new Process("P1", 2, 3, 1), new Process("P2", 6, 2, 1)));
        assertEquals(List.of("IDLE", "P1", "IDLE", "P2"),
                result.ganttEntries().stream().map(GanttChartEntry::processId).toList());
        assertEquals(8, result.processMetrics().get("P2").completionTime());
    }

    @Test
    void sjfChoosesShortestReadyProcess() {
        SchedulingResult result = new SjfNonPreemptiveAlgorithm().schedule(standard);
        assertEquals(List.of("P1", "P3", "P2"),
                result.ganttEntries().stream().map(GanttChartEntry::processId).toList());
    }

    @Test
    void srtfPreemptsWhenShorterProcessArrives() {
        SchedulingResult result = new SrtfAlgorithm().schedule(List.of(
                new Process("P1", 0, 8, 1), new Process("P2", 1, 2, 1)));
        assertEquals(List.of("P1", "P2", "P1"),
                result.ganttEntries().stream().map(GanttChartEntry::processId).toList());
        assertEquals(10, result.processMetrics().get("P1").completionTime());
    }

    @Test
    void priorityAlgorithmsPreferLowerPriorityNumber() {
        List<Process> input = List.of(
                new Process("P1", 0, 4, 3), new Process("P2", 1, 2, 1));
        assertEquals("P2", new PriorityNonPreemptiveAlgorithm().schedule(input)
                .ganttEntries().get(1).processId());
        assertEquals("P2", new PriorityPreemptiveAlgorithm().schedule(input)
                .ganttEntries().get(1).processId());
    }

    @Test
    void roundRobinHonorsTimeQuantum() {
        SchedulingResult result = new RoundRobinAlgorithm(2).schedule(List.of(
                new Process("P1", 0, 5, 1), new Process("P2", 0, 3, 1)));
        assertEquals(List.of("P1", "P2", "P1", "P2", "P1"),
                result.ganttEntries().stream().map(GanttChartEntry::processId).toList());
        assertEquals(2, result.ganttEntries().get(0).duration());
    }

    @Test
    void equalPriorityUsesStableArrivalAndIdOrder() {
        SchedulingResult result = new PriorityNonPreemptiveAlgorithm().schedule(List.of(
                new Process("P2", 0, 2, 1), new Process("P1", 0, 1, 1)));
        assertEquals("P1", result.ganttEntries().get(0).processId());
    }

    @Test
    void singleProcessMetricsAreAccurate() {
        SchedulingResult result = new FcfsAlgorithm().schedule(List.of(new Process("P1", 0, 5, 1)));
        var metrics = result.processMetrics().get("P1");
        assertEquals(5, metrics.completionTime());
        assertEquals(5, metrics.turnaroundTime());
        assertEquals(0, metrics.waitingTime());
        assertEquals(0, metrics.responseTime());
        assertEquals(100.0, result.cpuUtilization());
    }

    @Test
    void allAlgorithmsRejectEmptyInputsAndInvalidQuantums() {
        List<SchedulingAlgorithm> algorithms = List.of(new FcfsAlgorithm(),
                new SjfNonPreemptiveAlgorithm(), new SrtfAlgorithm(),
                new PriorityNonPreemptiveAlgorithm(), new PriorityPreemptiveAlgorithm());
        algorithms.forEach(algorithm -> assertThrows(IllegalArgumentException.class,
                () -> algorithm.schedule(List.of())));
        assertThrows(IllegalArgumentException.class, () -> new RoundRobinAlgorithm(0));
    }
}
