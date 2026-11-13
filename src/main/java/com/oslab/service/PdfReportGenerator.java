package com.oslab.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.oslab.model.analytics.SimulationRecord;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PdfReportGenerator {
    public void write(Path path, List<SimulationRecord> records) {
        try (OutputStream output = Files.newOutputStream(path)) {
            Document document = new Document();
            PdfWriter.getInstance(document, output);
            document.open();
            document.add(new Paragraph("OS Lab – Performance Report"));
            document.add(new Paragraph("Generated: " + java.time.LocalDateTime.now()));
            for (SimulationRecord record : records) {
                document.add(new Paragraph("\nModule: " + record.module()
                        + "\nAlgorithm: " + record.algorithm()
                        + "\nInput Parameters: " + record.inputParameters()
                        + "\nResults: " + record.results()
                        + "\nExecution Date: " + record.executedAt()));
            }
            document.close();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to write PDF report", exception);
        }
    }
}
