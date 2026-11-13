package com.oslab.algorithms.synchronization;

import com.oslab.model.synchronization.BufferSnapshot;
import com.oslab.model.synchronization.ThreadState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ProducerConsumerSimulation implements AutoCloseable {
    private final boolean synchronizedMode;
    private final int capacity;
    private final List<String> operations = new CopyOnWriteArrayList<>();
    private volatile ThreadState producerState = ThreadState.IDLE;
    private volatile ThreadState consumerState = ThreadState.IDLE;
    private volatile boolean running;
    private volatile boolean paused;
    private volatile int nextItem;
    private BoundedBuffer buffer;
    private ExecutorService executor;

    public ProducerConsumerSimulation(int capacity, boolean synchronizedMode) {
        this.capacity = capacity;
        this.synchronizedMode = synchronizedMode;
        buffer = new BoundedBuffer(capacity);
    }

    public synchronized void start(long delayMillis, Consumer<BufferSnapshot> listener) {
        stop();
        running = true;
        paused = false;
        executor = Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "oslab-producer-consumer");
            thread.setDaemon(true);
            return thread;
        });
        executor.submit(() -> produce(delayMillis, listener));
        executor.submit(() -> consume(delayMillis, listener));
    }

    private void produce(long delay, Consumer<BufferSnapshot> listener) {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                if (paused) { producerState = ThreadState.WAITING; Thread.sleep(20); continue; }
                producerState = ThreadState.RUNNING;
                int item = nextItem++;
                buffer.put(item, synchronizedMode);
                operations.add("Produced item " + item);
                publish(listener);
                Thread.sleep(Math.max(1, delay));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        producerState = ThreadState.STOPPED;
        publish(listener);
    }

    private void consume(long delay, Consumer<BufferSnapshot> listener) {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                if (paused) { consumerState = ThreadState.WAITING; Thread.sleep(20); continue; }
                consumerState = ThreadState.RUNNING;
                Integer item = buffer.take(synchronizedMode);
                if (item != null) operations.add("Consumed item " + item);
                publish(listener);
                Thread.sleep(Math.max(1, delay));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        consumerState = ThreadState.STOPPED;
        publish(listener);
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
            executor = null;
        }
    }

    public synchronized void reset() {
        stop();
        buffer = new BoundedBuffer(capacity);
        operations.clear();
        nextItem = 0;
        producerState = ThreadState.IDLE;
        consumerState = ThreadState.IDLE;
    }

    public boolean isRunning() { return running; }
    public void pause() { paused = true; }
    public void resume() { paused = false; }
    public BufferSnapshot snapshot() {
        List<Integer> items = buffer.snapshot();
        return new BufferSnapshot(items, capacity, capacity - items.size(), items.size(),
                capacity - items.size(), items.size(), producerState, consumerState,
                new ArrayList<>(operations));
    }

    private void publish(Consumer<BufferSnapshot> listener) {
        if (listener != null) listener.accept(snapshot());
    }

    @Override public void close() { stop(); }
}
