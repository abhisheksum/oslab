# Algorithms

## CPU scheduling

- **FCFS** runs ready processes in arrival order.
- **SJF non-preemptive** selects the shortest burst among processes that have arrived.
- **SRTF** repeatedly selects the ready process with the smallest remaining burst.
- **Priority scheduling** selects the highest-priority ready process, either
  non-preemptively or preemptively.
- **Round Robin** rotates ready processes using a configurable time quantum.

All scheduling results include per-process completion, turnaround, waiting, and
response times plus aggregate utilization and throughput.

## Memory management

First Fit, Best Fit, and Worst Fit select free contiguous partitions using their
respective placement rules. FIFO, LRU, and Optimal replace pages using queue order,
recent use, and future-use distance.

## Deadlocks

Banker's algorithm computes Need and searches for a safe sequence. Deadlock detection
advances Work and Finish vectors to identify processes that cannot complete. The
resource-allocation graph analyzer reports cycles; a cycle proves deadlock for
single-instance resources, but not necessarily for multiple instances.

## Synchronization

Producer-consumer uses bounded-buffer permits and mutex protection. Dining philosophers
supports naive acquisition and ordered fork acquisition. Reader-writer uses a
`ReentrantReadWriteLock`, with optional writer preference.
