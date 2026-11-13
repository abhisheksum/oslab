package com.oslab.algorithms.synchronization;

import com.oslab.model.synchronization.ReaderWriterSnapshot;
import com.oslab.model.synchronization.ThreadState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class ReaderWriterSimulation implements AutoCloseable {
    private final int readerCount;
    private final int writerCount;
    private final ReentrantReadWriteLock lock;
    private final List<ThreadState> readers;
    private final List<ThreadState> writers;
    private final List<String> operations = new CopyOnWriteArrayList<>();
    private volatile int activeReaders;
    private volatile int currentWriter = -1;
    private volatile int sharedValue;
    private volatile boolean running;
    private volatile boolean paused;
    private ExecutorService executor;

    public ReaderWriterSimulation(int readerCount, int writerCount, boolean writerPreference) {
        if (readerCount <= 0 || writerCount <= 0) throw new IllegalArgumentException("Thread counts must be positive");
        this.readerCount = readerCount;
        this.writerCount = writerCount;
        lock = new ReentrantReadWriteLock(writerPreference);
        readers = new CopyOnWriteArrayList<>(java.util.Collections.nCopies(readerCount, ThreadState.IDLE));
        writers = new CopyOnWriteArrayList<>(java.util.Collections.nCopies(writerCount, ThreadState.IDLE));
    }

    public synchronized void start(long delayMillis, Consumer<ReaderWriterSnapshot> listener) {
        stop();
        running = true;
        paused = false;
        executor = Executors.newFixedThreadPool(readerCount + writerCount, runnable -> {
            Thread thread = new Thread(runnable, "oslab-reader-writer");
            thread.setDaemon(true);
            return thread;
        });
        for (int i = 0; i < readerCount; i++) {
            int id = i; executor.submit(() -> read(id, delayMillis, listener));
        }
        for (int i = 0; i < writerCount; i++) {
            int id = i; executor.submit(() -> write(id, delayMillis, listener));
        }
    }

    private void read(int id, long delay, Consumer<ReaderWriterSnapshot> listener) {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                if (paused) { readers.set(id, ThreadState.WAITING); Thread.sleep(20); continue; }
                readers.set(id, ThreadState.WAITING); publish(listener);
                lock.readLock().lockInterruptibly();
                activeReaders++;
                readers.set(id, ThreadState.RUNNING);
                operations.add("Reader " + id + " read value " + sharedValue);
                publish(listener);
                Thread.sleep(Math.max(1, delay));
                activeReaders--;
                lock.readLock().unlock();
                Thread.sleep(Math.max(1, delay));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        readers.set(id, ThreadState.STOPPED); publish(listener);
    }

    private void write(int id, long delay, Consumer<ReaderWriterSnapshot> listener) {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                if (paused) { writers.set(id, ThreadState.WAITING); Thread.sleep(20); continue; }
                writers.set(id, ThreadState.WAITING); publish(listener);
                lock.writeLock().lockInterruptibly();
                currentWriter = id; writers.set(id, ThreadState.RUNNING);
                sharedValue++;
                operations.add("Writer " + id + " wrote value " + sharedValue);
                publish(listener);
                Thread.sleep(Math.max(1, delay));
                currentWriter = -1; writers.set(id, ThreadState.COMPLETED);
                lock.writeLock().unlock();
                Thread.sleep(Math.max(1, delay));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                if (lock.writeLock().isHeldByCurrentThread()) lock.writeLock().unlock();
                break;
            }
        }
        writers.set(id, ThreadState.STOPPED); publish(listener);
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
        stop(); sharedValue = 0; activeReaders = 0; currentWriter = -1;
        for (int i = 0; i < readers.size(); i++) readers.set(i, ThreadState.IDLE);
        for (int i = 0; i < writers.size(); i++) writers.set(i, ThreadState.IDLE);
        operations.clear();
    }

    public ReaderWriterSnapshot snapshot() {
        return new ReaderWriterSnapshot(new ArrayList<>(readers), new ArrayList<>(writers),
                activeReaders, currentWriter, sharedValue, new ArrayList<>(operations));
    }

    private void publish(Consumer<ReaderWriterSnapshot> listener) {
        if (listener != null) listener.accept(snapshot());
    }

    public boolean isRunning() { return running; }
    public void pause() { paused = true; }
    public void resume() { paused = false; }
    @Override public void close() { stop(); }
}
