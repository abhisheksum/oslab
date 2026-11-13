package com.oslab.algorithms.synchronization;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer {
    private final int capacity;
    private final Deque<Integer> items = new ArrayDeque<>();
    private final Semaphore empty;
    private final Semaphore full = new Semaphore(0);
    private final ReentrantLock mutex = new ReentrantLock();

    public BoundedBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Buffer capacity must be positive");
        this.capacity = capacity;
        empty = new Semaphore(capacity);
    }

    public void put(int item, boolean synchronizedMode) throws InterruptedException {
        if (synchronizedMode) {
            empty.acquire();
            mutex.lockInterruptibly();
            try { items.addLast(item); } finally { mutex.unlock(); }
            full.release();
        } else {
            if (items.size() < capacity) items.addLast(item);
        }
    }

    public Integer take(boolean synchronizedMode) throws InterruptedException {
        if (synchronizedMode) {
            full.acquire();
            mutex.lockInterruptibly();
            try { return items.removeFirst(); } finally { mutex.unlock(); }
        }
        return items.isEmpty() ? null : items.removeFirst();
    }

    public List<Integer> snapshot() {
        mutex.lock();
        try { return new ArrayList<>(items); } finally { mutex.unlock(); }
    }

    public int size() { return items.size(); }
    public int capacity() { return capacity; }
    public int emptyPermits() { return empty.availablePermits(); }
    public int fullPermits() { return full.availablePermits(); }
}
