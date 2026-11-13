package com.oslab.analytics;

import com.oslab.model.analytics.SimulationRecord;
import com.oslab.repository.SqliteAnalyticsRepository;
import com.oslab.service.CsvReportGenerator;
import com.oslab.service.PdfReportGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticsPersistenceTest {
    @TempDir
    Path tempDir;

    @Test
    void sqliteRepositorySupportsCrudAndFiltering() throws Exception {
        Path database = tempDir.resolve("history.db");
        try (SqliteAnalyticsRepository repository = new SqliteAnalyticsRepository(database)) {
            SimulationRecord saved = repository.save(new SimulationRecord("CPU Scheduling", "FCFS",
                    "processes=3", "avgWaiting=2.0", LocalDateTime.now()));
            assertTrue(saved.id() > 0);
            assertEquals(1, repository.find("CPU Scheduling", "FCFS").size());
            assertTrue(repository.findById(saved.id()).isPresent());
            repository.deleteById(saved.id());
            assertTrue(repository.find("All", "All").isEmpty());
        }
        assertTrue(Files.size(database) > 0);
    }

    @Test
    void emptyDatabaseReturnsEmptyHistory() {
        try (SqliteAnalyticsRepository repository = new SqliteAnalyticsRepository(tempDir.resolve("empty.db"))) {
            assertTrue(repository.find("All", "All").isEmpty());
            assertFalse(repository.findById(99).isPresent());
        }
    }

    @Test
    void csvExportEscapesValues() throws Exception {
        SimulationRecord record = new SimulationRecord("Memory Management", "FIFO",
                "refs=1,2", "faults=2", LocalDateTime.now());
        Path output = tempDir.resolve("report.csv");
        new CsvReportGenerator().write(output, List.of(record));
        String content = Files.readString(output);
        assertTrue(content.startsWith("Project,Module,Algorithm"));
        assertTrue(content.contains("\"refs=1,2\""));
    }

    @Test
    void pdfExportCreatesNonEmptyDocument() throws Exception {
        SimulationRecord record = new SimulationRecord("Deadlock Management", "Banker's Algorithm",
                "processes=2", "safe=true", LocalDateTime.now());
        Path output = tempDir.resolve("report.pdf");
        new PdfReportGenerator().write(output, List.of(record));
        assertTrue(Files.size(output) > 100);
        assertEquals("%PDF", Files.readString(output, java.nio.charset.StandardCharsets.ISO_8859_1)
                .substring(0, 4));
    }
}
