# Database schema

The default SQLite file is `oslab.db`.

## `simulation_history`

| Column | Type | Description |
|---|---|---|
| `id` | INTEGER | Auto-increment primary key |
| `module` | TEXT | OS Lab module |
| `algorithm` | TEXT | Algorithm or simulation strategy |
| `input_parameters` | TEXT | User inputs captured for the run |
| `results` | TEXT | Actual result metrics as key-value pairs |
| `executed_at` | TEXT | ISO-8601 execution timestamp |

## `algorithm_metadata`

Stores module, algorithm, and description metadata for future catalog and help views.

The repository creates both tables and the module/algorithm index on startup using
`CREATE TABLE IF NOT EXISTS`. Prepared statements are used for all values, and
repository failures are surfaced as `IllegalStateException` with the SQL cause.

Generated databases and reports are excluded by `.gitignore`.
