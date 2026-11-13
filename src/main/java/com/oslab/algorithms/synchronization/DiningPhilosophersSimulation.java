package com.oslab.algorithms.synchronization;

import com.oslab.model.synchronization.PhilosopherSnapshot;
import com.oslab.model.synchronization.ThreadState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class DiningPhilosophersSimulation implements AutoCloseable {
    private final boolean deadlockFree;
    private final ReentrantLock[] forks = {new ReentrantLock(), new ReentrantLock(),
            new ReentrantLock(), new ReentrantLock(), new ReentrantLock()};
    private final List<ThreadState> states = new CopyOnWriteArrayList<>(
            List.of(ThreadState.THINKING, ThreadState.THINKING, ThreadState.THINKING,
                    ThreadState.THINKING, ThreadState.THINKING));
    private final int[] owners = {-1, -1, -1, -1, -1};
    private final List<String> operations = new CopyOnWriteArrayList<>();
    private volatile boolean running;
    private volatile boolean paused;
    private ExecutorService executor;

    public DiningPhilosophersSimulation(boolean deadlockFree) {
        this.deadlockFree = deadlockFree;
    }

    public synchronized void start(long delayMillis, Consumer<PhilosopherSnapshot> listener) {
        stop();
        running = true;
        paused = false;
        executor = Executors.newFixedThreadPool(5, runnable -> {
            Thread thread = new Thread(runnable, "oslab-philosopher");
            thread.setDaemon(true);
            return thread;
        });
        for (int philosopher = 0; philosopher < 5; philosopher++) {
            int id = philosopher;
            executor.submit(() -> dine(id, delayMillis, listener));
        }
    }

    private void dine(int id, long delay, Consumer<PhilosopherSnapshot> listener) {
        int left = id;
        int right = (id + 1) % 5;
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                if (paused) { states.set(id, ThreadState.WAITING); Thread.sleep(20); continue; }
                states.set(id, ThreadState.THINKING);
                publish(listener);
                Thread.sleep(Math.max(1, delay));
                states.set(id, ThreadState.HUNGRY);
                publish(listener);
                int first = deadlockFree ? Math.min(left, right) : left;
                int second = deadlockFree ? Math.max(left, right) : right;
                forks[first].lockInterruptibly();
                synchronized (owners) { owners[first] = id; }
                forks[second].lockInterruptibly();
                synchronized (owners) { owners[second] = id; }
                states.set(id, ThreadState.EATING);
                operations.add("Philosopher " + id + " acquired forks and is eating");
                publish(listener);
                Thread.sleep(Math.max(1, delay));
                synchronized (owners) { owners[first] = -1; owners[second] = -1; }
                forks[second].unlock();
                forks[first].unlock();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                releaseOwned(id, left, right);
                break;
            }
        }
        states.set(id, ThreadState.STOPPED);
        publish(listener);
    }

    private void releaseOwned(int id, int left, int right) {
        for (int fork : new int[]{left, right}) {
            synchronized (owners) { if (owners[fork] == id) owners[fork] = -1; }
            if (forks[fork].isHeldByCurrentThread()) forks[fork].unlock();
        }
    }

    public synchronized void stop() {
        running = false;
        if (executor != null) {
            ExecutorService current = executor;
            current.shutdownNow();
            try {
                current.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            synchronized (owners) {
                java.util.Arrays.fill(owners, -1);
            }
            executor = null;
        }
    }

    public PhilosopherSnapshot snapshot() {
        synchronized (owners) {
            return new PhilosopherSnapshot(new ArrayList<>(states),
                    java.util.Arrays.stream(owners).boxed().toList(), new ArrayList<>(operations));
        }
    }

    private void publish(Consumer<PhilosopherSnapshot> listener) {
        if (listener != null) listener.accept(snapshot());
    }

    public boolean isDeadlockFree() { return deadlockFree; }
    public void pause() { paused = true; }
    public void resume() { paused = false; }
    @Override public void close() { stop(); }
}
