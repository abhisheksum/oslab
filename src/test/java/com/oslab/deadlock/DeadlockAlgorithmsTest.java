package com.oslab.deadlock;

import com.oslab.algorithms.deadlock.BankersAlgorithm;
import com.oslab.algorithms.deadlock.DeadlockDetectionAlgorithm;
import com.oslab.algorithms.deadlock.ResourceAllocationGraphAnalyzer;
import com.oslab.model.deadlock.BankersResult;
import com.oslab.model.deadlock.DeadlockDetectionResult;
import com.oslab.model.deadlock.ResourceEdge;
import com.oslab.model.deadlock.ResourceGraphResult;
import com.oslab.model.deadlock.ResourceNode;
import com.oslab.model.deadlock.ResourceState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeadlockAlgorithmsTest {
    @Test
    void bankersFindsKnownSafeSequence() {
        ResourceState state = new ResourceState(
                List.of(3, 3, 2),
                matrix("0 1 0;2 0 0;3 0 2;2 1 1;0 0 2"),
                matrix("7 5 3;3 2 2;9 0 2;2 2 2;4 3 3"));
        BankersResult result = new BankersAlgorithm().analyze(state);
        assertTrue(result.safe());
        assertEquals(List.of("P2", "P4", "P5", "P1", "P3"), result.safeSequence());
        assertEquals(List.of(7, 4, 3), result.need().get(0));
    }

    @Test
    void bankersIdentifiesUnsafeAllocation() {
        ResourceState state = new ResourceState(List.of(0, 0),
                matrix("1 0;0 1"), matrix("2 1;1 2"));
        BankersResult result = new BankersAlgorithm().analyze(state);
        assertFalse(result.safe());
        assertTrue(result.safeSequence().isEmpty());
    }

    @Test
    void bankersRejectsInvalidMatrices() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceState(List.of(1, 1),
                matrix("1"), matrix("1 1")));
        assertThrows(IllegalArgumentException.class, () -> new ResourceState(List.of(1),
                matrix("2"), matrix("1")));
    }

    @Test
    void bankersSupportsMultipleResourceTypes() {
        ResourceState state = new ResourceState(List.of(1, 1, 1),
                matrix("0 0 0;1 0 0"), matrix("1 1 1;1 1 1"));
        assertTrue(new BankersAlgorithm().analyze(state).safe());
    }

    @Test
    void detectionFindsNoDeadlock() {
        DeadlockDetectionResult result = new DeadlockDetectionAlgorithm().detect(
                List.of(1, 0), matrix("0 1;0 0"), matrix("1 0;0 0"));
        assertFalse(result.deadlockDetected());
        assertTrue(result.deadlockedProcesses().isEmpty());
    }

    @Test
    void detectionFindsActualDeadlock() {
        DeadlockDetectionResult result = new DeadlockDetectionAlgorithm().detect(
                List.of(0, 0), matrix("1 0;0 1"), matrix("0 1;1 0"));
        assertTrue(result.deadlockDetected());
        assertEquals(List.of("P1", "P2"), result.deadlockedProcesses());
    }

    @Test
    void detectionSupportsMultipleInstances() {
        DeadlockDetectionResult result = new DeadlockDetectionAlgorithm().detect(
                List.of(1), matrix("1;0;0"), matrix("0;1;1"));
        assertFalse(result.deadlockDetected());
        assertEquals(List.of(true, true, true), result.finishProgression().get(3));
    }

    @Test
    void resourceGraphDetectsCycle() {
        List<ResourceNode> nodes = List.of(
                new ResourceNode("P1", ResourceNode.NodeType.PROCESS, 1),
                new ResourceNode("P2", ResourceNode.NodeType.PROCESS, 1),
                new ResourceNode("R1", ResourceNode.NodeType.RESOURCE, 1),
                new ResourceNode("R2", ResourceNode.NodeType.RESOURCE, 1));
        List<ResourceEdge> edges = List.of(
                new ResourceEdge("P1", "R2", ResourceEdge.EdgeType.REQUEST),
                new ResourceEdge("R2", "P2", ResourceEdge.EdgeType.ALLOCATION),
                new ResourceEdge("P2", "R1", ResourceEdge.EdgeType.REQUEST),
                new ResourceEdge("R1", "P1", ResourceEdge.EdgeType.ALLOCATION));
        ResourceGraphResult result = new ResourceAllocationGraphAnalyzer().analyze(nodes, edges);
        assertTrue(result.cycleDetected());
        assertTrue(result.cycleNodes().contains("P1"));
    }

    @Test
    void resourceGraphCanBeAcyclic() {
        ResourceGraphResult result = new ResourceAllocationGraphAnalyzer().analyze(
                List.of(new ResourceNode("P1", ResourceNode.NodeType.PROCESS, 1),
                        new ResourceNode("R1", ResourceNode.NodeType.RESOURCE, 2)),
                List.of(new ResourceEdge("P1", "R1", ResourceEdge.EdgeType.REQUEST)));
        assertFalse(result.cycleDetected());
    }

    private static List<List<Integer>> matrix(String value) {
        return java.util.Arrays.stream(value.split(";")).map(row ->
                java.util.Arrays.stream(row.trim().split("\\s+")).map(Integer::parseInt).toList()).toList();
    }
}
