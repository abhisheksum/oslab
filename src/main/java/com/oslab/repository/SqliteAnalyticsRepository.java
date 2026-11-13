package com.oslab.repository;

import com.oslab.model.analytics.SimulationRecord;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteAnalyticsRepository implements AnalyticsRepository {
    private final Connection connection;

    public SqliteAnalyticsRepository(Path databasePath) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath.toAbsolutePath());
            initializeSchema();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to open analytics database", exception);
        }
    }

    private void initializeSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS simulation_history (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      module TEXT NOT NULL,
                      algorithm TEXT NOT NULL,
                      input_parameters TEXT NOT NULL,
                      results TEXT NOT NULL,
                      executed_at TEXT NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS algorithm_metadata (
                      algorithm TEXT PRIMARY KEY,
                      module TEXT NOT NULL,
                      description TEXT NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS idx_simulation_history_module_algorithm
                    ON simulation_history(module, algorithm)
                    """);
        }
    }

    @Override
    public synchronized SimulationRecord save(SimulationRecord record) {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO simulation_history(module, algorithm, input_parameters, results, executed_at)
                VALUES (?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, record.module());
            statement.setString(2, record.algorithm());
            statement.setString(3, record.inputParameters());
            statement.setString(4, record.results());
            statement.setString(5, record.executedAt().toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new IllegalStateException("Database did not return a simulation ID");
                return new SimulationRecord(keys.getLong(1), record.module(), record.algorithm(),
                        record.inputParameters(), record.results(), record.executedAt());
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save simulation history", exception);
        }
    }

    @Override
    public synchronized List<SimulationRecord> find(String module, String algorithm) {
        StringBuilder sql = new StringBuilder("SELECT id,module,algorithm,input_parameters,results,executed_at FROM simulation_history WHERE 1=1");
        List<String> filters = new ArrayList<>();
        if (module != null && !module.isBlank() && !"All".equals(module)) {
            sql.append(" AND module = ?");
            filters.add(module);
        }
        if (algorithm != null && !algorithm.isBlank() && !"All".equals(algorithm)) {
            sql.append(" AND algorithm = ?");
            filters.add(algorithm);
        }
        sql.append(" ORDER BY executed_at DESC");
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < filters.size(); i++) statement.setString(i + 1, filters.get(i));
            try (ResultSet results = statement.executeQuery()) {
                List<SimulationRecord> records = new ArrayList<>();
                while (results.next()) records.add(read(results));
                return records;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load simulation history", exception);
        }
    }

    @Override
    public synchronized Optional<SimulationRecord> findById(long id) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id,module,algorithm,input_parameters,results,executed_at FROM simulation_history WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(read(results)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load simulation detail", exception);
        }
    }

    @Override
    public synchronized void deleteById(long id) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM simulation_history WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to delete simulation history", exception);
        }
    }

    private SimulationRecord read(ResultSet results) throws SQLException {
        return new SimulationRecord(results.getLong("id"), results.getString("module"),
                results.getString("algorithm"), results.getString("input_parameters"),
                results.getString("results"), LocalDateTime.parse(results.getString("executed_at")));
    }

    @Override
    public synchronized void close() {
        try {
            connection.close();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to close analytics database", exception);
        }
    }
}
