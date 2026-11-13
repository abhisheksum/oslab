# OS Lab - Interactive Operating System Simulator

OS Lab is a JavaFX desktop application for learning operating-system concepts through
interactive simulations. It separates algorithms, services, persistence, and JavaFX
controllers so the demonstrations remain testable and easy to extend.

## Problem statement

Operating-system algorithms are difficult to understand from static textbook examples.
OS Lab makes scheduling, memory allocation, deadlocks, synchronization, and performance
trade-offs observable through editable inputs, step controls, visual timelines, graphs,
live thread states, and persisted simulation reports.

## Features

- CPU scheduling: FCFS, SJF, SRTF, priority scheduling, and Round Robin
- Gantt charts, playback controls, scheduling metrics, and algorithm comparison
- Contiguous memory allocation: First Fit, Best Fit, and Worst Fit
- Page replacement: FIFO, LRU, and Optimal
- Memory partitions, page-frame steps, faults, hits, and fragmentation metrics
- Banker's algorithm, deadlock detection, and resource-allocation graph analysis
- Producer-consumer, dining philosophers, and reader-writer simulations
- Managed background threads with pause, resume, stop, and reset controls
- SQLite simulation history, filters, CSV export, and PDF export
- Central analytics dashboard with charts based only on recorded results

## Technology stack

Java 17+, JavaFX 21, Maven, SQLite JDBC, OpenPDF, JUnit 5, and an MVC architecture
with service and repository layers.

## Architecture and structure

See [docs/architecture.md](docs/architecture.md) for the full design and
[docs/algorithms.md](docs/algorithms.md) for algorithm notes.

```text
src/main/java/com/oslab/
  algorithms/       algorithm implementations and simulation engines
  controller/       JavaFX screens and event coordination
  model/            immutable domain and result models
  repository/       persistence interfaces and SQLite implementation
  service/          application use cases and report generators
  scheduling/       CPU scheduling algorithms
  memory/           memory and paging result types
  view/             reusable JavaFX components
```

## Installation and run

Requirements: JDK 17 or later and Maven 3.9 or later.

```bash
mvn clean test
mvn clean package
mvn javafx:run
```

The packaged artifact is created under `target/`. SQLite creates `oslab.db` in the
working directory on first use. Reports are written to `oslab-report.csv` and
`oslab-report.pdf` by default.

## Testing

Run the complete suite with `mvn clean test`. The test strategy and latest verified
results are documented in [docs/testing-report.md](docs/testing-report.md).

## Database

The schema and persistence lifecycle are documented in
[docs/database-schema.md](docs/database-schema.md). Database files and generated
reports are intentionally ignored by Git.

## Screenshots

Screenshots can be added to `docs/screenshots/` and linked here when captured on the
target demonstration environment. The application is also fully runnable with the
commands above, so screenshots are not required to build or test the project.

## Future enhancements

- Date-range analytics filters and richer cross-module comparisons
- User-selected export destinations
- Dedicated normalized metric columns for SQL aggregation
- Additional starvation and contention measurements
- Automated UI smoke tests on supported desktop platforms

## Contributors

OS Lab is an educational college project. Add project contributors here before
publishing the repository.
