package com.oslab.algorithms.deadlock;

import com.oslab.model.deadlock.ResourceEdge;
import com.oslab.model.deadlock.ResourceGraphResult;
import com.oslab.model.deadlock.ResourceNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceAllocationGraphAnalyzer {
    public ResourceGraphResult analyze(List<ResourceNode> nodes, List<ResourceEdge> edges) {
        if (nodes == null || edges == null) throw new IllegalArgumentException("Graph data is required");
        Set<String> nodeIds = nodes.stream().map(ResourceNode::id).collect(java.util.stream.Collectors.toSet());
        Map<String, List<String>> adjacency = new HashMap<>();
        for (String id : nodeIds) adjacency.put(id, new ArrayList<>());
        for (ResourceEdge edge : edges) {
            if (!nodeIds.contains(edge.from()) || !nodeIds.contains(edge.to())) {
                throw new IllegalArgumentException("Edge endpoint does not exist");
            }
            adjacency.get(edge.from()).add(edge.to());
        }
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        for (String node : nodeIds) {
            List<String> cycle = findCycle(node, adjacency, visiting, visited, path);
            if (cycle != null) return new ResourceGraphResult(true, cycle);
        }
        return new ResourceGraphResult(false, List.of());
    }

    private List<String> findCycle(String node, Map<String, List<String>> adjacency,
                                   Set<String> visiting, Set<String> visited, List<String> path) {
        if (visiting.contains(node)) {
            int start = path.indexOf(node);
            return new ArrayList<>(path.subList(Math.max(0, start), path.size()));
        }
        if (visited.contains(node)) return null;
        visiting.add(node);
        path.add(node);
        for (String next : adjacency.get(node)) {
            List<String> cycle = findCycle(next, adjacency, visiting, visited, path);
            if (cycle != null) return cycle;
        }
        path.remove(path.size() - 1);
        visiting.remove(node);
        visited.add(node);
        return null;
    }
}
