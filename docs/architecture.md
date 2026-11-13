# Architecture

OS Lab uses MVC with explicit service and repository boundaries.

```text
JavaFX View
    |
Controllers  ->  Services  ->  Algorithms
    |              |
    +----------> Models
                   |
             Repository -> SQLite
```

- **Models** are immutable records or state snapshots and contain validation for
  domain invariants.
- **Algorithms** implement scheduling, allocation, deadlock, and synchronization
  behavior without JavaFX dependencies.
- **Services** coordinate algorithms, convert results to analytics records, and
  generate exports.
- **Repositories** isolate persistence and expose CRUD/filter operations.
- **Controllers** validate form input, update JavaFX controls, and marshal background
  simulation callbacks through `Platform.runLater`.
- **Views** are reusable JavaFX components and the shared dark stylesheet.

Long-running synchronization simulations use managed daemon executors and interruptible
locks/semaphores. Stop and reset operations shut down workers before replacing state.
