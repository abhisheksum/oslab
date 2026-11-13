package com.oslab.service;

import com.oslab.model.analytics.SimulationRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvReportGenerator {
    public void write(Path path, List<SimulationRecord> records) {
        try {
            List<String> lines = new java.util.ArrayList<>();
            lines.add("Project,Module,Algorithm,Input Parameters,Results,Execution Date");
            for (SimulationRecord record : records) {
                lines.add(String.join(",", quote("OS Lab"), quote(record.module()), quote(record.algorithm()),
                        quote(record.inputParameters()), quote(record.results()), quote(record.executedAt().toString())));
            }
            Files.write(path, lines);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write CSV report", exception);
        }
    }

    private String quote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
