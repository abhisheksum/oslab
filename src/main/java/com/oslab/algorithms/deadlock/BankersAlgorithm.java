package com.oslab.algorithms.deadlock;

import com.oslab.model.deadlock.BankersResult;
import com.oslab.model.deadlock.BankersStep;
import com.oslab.model.deadlock.ResourceState;

import java.util.ArrayList;
import java.util.List;

public class BankersAlgorithm {
    public BankersResult analyze(ResourceState state) {
        List<List<Integer>> need = new ArrayList<>();
        for (int process = 0; process < state.processCount(); process++) {
            List<Integer> row = new ArrayList<>();
            for (int resource = 0; resource < state.resourceCount(); resource++) {
                row.add(state.maximum().get(process).get(resource)
                        - state.allocation().get(process).get(resource));
            }
            need.add(List.copyOf(row));
        }

        List<Integer> work = new ArrayList<>(state.available());
        boolean[] finish = new boolean[state.processCount()];
        List<String> sequence = new ArrayList<>();
        List<BankersStep> steps = new ArrayList<>();
        boolean progress;
        do {
            progress = false;
            for (int process = 0; process < state.processCount(); process++) {
                if (finish[process] || !canRun(need.get(process), work)) {
                    continue;
                }
                List<Integer> before = List.copyOf(work);
                for (int resource = 0; resource < state.resourceCount(); resource++) {
                    work.set(resource, work.get(resource) + state.allocation().get(process).get(resource));
                }
                finish[process] = true;
                sequence.add("P" + (process + 1));
                steps.add(new BankersStep("P" + (process + 1), before, work, true, true));
                progress = true;
            }
        } while (progress);

        for (int process = 0; process < state.processCount(); process++) {
            if (!finish[process]) {
                steps.add(new BankersStep("P" + (process + 1), work, work, false, false));
            }
        }
        return new BankersResult(need, sequence, sequence.size() == state.processCount(), steps);
    }

    private boolean canRun(List<Integer> need, List<Integer> work) {
        for (int resource = 0; resource < need.size(); resource++) {
            if (need.get(resource) > work.get(resource)) return false;
        }
        return true;
    }
}
