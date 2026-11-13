# Testing report

The project uses JUnit 5 and Maven Surefire. The suite covers:

- CPU scheduling arrivals, idle time, priorities, preemption, and Round Robin
- Memory fit strategies and page replacement edge cases
- Banker's algorithm, deadlock detection, and graph cycles
- Buffer bounds, synchronization lifecycle, fork release, and reader-writer state
- SQLite CRUD/filtering, empty history, CSV escaping, and PDF validity

Verified command:

```bash
mvn clean test
```

The latest baseline run completed with 36 tests, 0 failures, and 0 errors. Re-run the
command after any local change; the count is intentionally reported from an executed
build rather than treated as a permanent guarantee.
