package com.oslab.controller;

import com.oslab.model.analytics.AnalyticsSummary;
import com.oslab.model.analytics.SimulationRecord;
import com.oslab.repository.SqliteAnalyticsRepository;
import com.oslab.service.AnalyticsService;
import com.oslab.service.CsvReportGenerator;
import com.oslab.service.PdfReportGenerator;
import com.oslab.view.UiComponents;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Function;

public class AnalyticsController {
    private final AnalyticsService service;
    private final TableView<SimulationRecord> historyTable = new TableView<>();
    private final ComboBox<String> moduleFilter = new ComboBox<>();
    private final ComboBox<String> algorithmFilter = new ComboBox<>();
    private final VBox charts = new VBox(12);
    private final Label emptyState = new Label("No simulation history yet. Run a simulation and record its result to see analytics.");

    public AnalyticsController() {
        this(new AnalyticsService(new SqliteAnalyticsRepository(Path.of("oslab.db"))));
    }

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(header());
        root.setCenter(content());
        Scene scene = new Scene(root, 1280, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        refresh();
        return scene;
    }

    private HBox header() {
        Button back = action("←  Dashboard");
        back.setOnAction(event -> ((javafx.stage.Stage) back.getScene().getWindow())
                .setScene(new DashboardController().createScene()));
        Label title = new Label("Performance Analytics");
        title.getStyleClass().add("page-title");
        HBox header = new HBox(22, back, title);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25, 40, 18, 40));
        return header;
    }

    private ScrollPane content() {
        VBox content = new VBox(18, summaryCards(), historyCard(), charts);
        content.setPadding(new Insets(22, 40, 40, 40));
        content.getStyleClass().add("content");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("content-scroll");
        return scroll;
    }

    private GridPane summaryCards() {
        AnalyticsSummary summary = service.summary();
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.getColumnConstraints().addAll(column(), column(), column(), column(), column());
        grid.add(metric("TOTAL", summary.totalSimulations()), 0, 0);
        grid.add(metric("CPU", summary.cpuSimulations()), 1, 0);
        grid.add(metric("MEMORY", summary.memorySimulations()), 2, 0);
        grid.add(metric("DEADLOCK", summary.deadlockSimulations()), 3, 0);
        grid.add(metric("SYNC", summary.synchronizationSimulations()), 4, 0);
        return grid;
    }

    private javafx.scene.layout.ColumnConstraints column() {
        javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();
        column.setPercentWidth(20);
        return column;
    }

    private VBox metric(String label, long value) {
        VBox card = UiComponents.card("analytics-metric");
        Label name = new Label(label);
        name.getStyleClass().add("metric-label");
        Label number = new Label(String.valueOf(value));
        number.getStyleClass().add("metric-value");
        card.getChildren().addAll(name, number);
        return card;
    }

    private VBox historyCard() {
        VBox card = UiComponents.card("analytics-card");
        moduleFilter.setItems(FXCollections.observableArrayList("All", "CPU Scheduling",
                "Memory Management", "Deadlock Management", "Process Synchronization"));
        moduleFilter.setValue("All");
        algorithmFilter.setItems(FXCollections.observableArrayList("All"));
        algorithmFilter.setValue("All");
        moduleFilter.setOnAction(event -> refreshAlgorithms());
        algorithmFilter.setOnAction(event -> refreshTable());
        Button delete = action("Delete Selected");
        delete.setOnAction(event -> {
            SimulationRecord selected = historyTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.delete(selected.id());
                refresh();
            }
        });
        Button csv = action("Export CSV");
        csv.setOnAction(event -> exportCsv());
        Button pdf = action("Export PDF");
        pdf.setOnAction(event -> exportPdf());
        HBox controls = new HBox(10, new Label("Module"), moduleFilter, new Label("Algorithm"),
                algorithmFilter, delete, csv, pdf);
        controls.setAlignment(Pos.CENTER_LEFT);
        configureTable();
        emptyState.getStyleClass().add("empty-state");
        card.getChildren().addAll(new Label("Simulation history"), controls, historyTable, emptyState);
        return card;
    }

    private void configureTable() {
        if (!historyTable.getColumns().isEmpty()) return;
        historyTable.getColumns().addAll(
                column("ID", SimulationRecord::id, 55),
                column("Module", SimulationRecord::module, 160),
                column("Algorithm", SimulationRecord::algorithm, 180),
                column("Inputs", SimulationRecord::inputParameters, 270),
                column("Results", SimulationRecord::results, 390),
                column("Executed", SimulationRecord::executedAt, 180));
        historyTable.setPrefHeight(220);
    }

    private <T> TableColumn<SimulationRecord, T> column(String title,
                                                         Function<SimulationRecord, T> value,
                                                         double width) {
        TableColumn<SimulationRecord, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    private void refresh() {
        refreshAlgorithms();
        refreshTable();
        refreshCharts();
    }

    private void refreshAlgorithms() {
        String module = moduleFilter.getValue() == null ? "All" : moduleFilter.getValue();
        List<String> algorithms = service.history(module, "All").stream()
                .map(SimulationRecord::algorithm).distinct().sorted().toList();
        algorithmFilter.setItems(FXCollections.observableArrayList(java.util.stream.Stream.concat(
                java.util.stream.Stream.of("All"), algorithms.stream()).toList()));
        algorithmFilter.setValue("All");
        refreshTable();
    }

    private void refreshTable() {
        if (moduleFilter.getValue() == null || algorithmFilter.getValue() == null) return;
        List<SimulationRecord> records = service.history(moduleFilter.getValue(), algorithmFilter.getValue());
        historyTable.setItems(FXCollections.observableArrayList(records));
        emptyState.setVisible(records.isEmpty());
    }

    private void refreshCharts() {
        charts.getChildren().clear();
        List<SimulationRecord> records = service.history("All", "All");
        if (records.isEmpty()) return;
        BarChart<String, Number> countChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        countChart.setTitle("Simulations by module");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        records.stream().map(SimulationRecord::module).distinct().forEach(module ->
                series.getData().add(new XYChart.Data<>(module,
                        records.stream().filter(record -> record.module().equals(module)).count())));
        countChart.getData().add(series);
        countChart.setPrefHeight(280);
        VBox countCard = UiComponents.card("analytics-card");
        countCard.getChildren().add(countChart);
        charts.getChildren().add(countCard);

        addMetricChart(records, "CPU Scheduling", "avgWaiting", "CPU average waiting time");
        addMetricChart(records, "CPU Scheduling", "avgTurnaround", "CPU average turnaround time");
        addMetricChart(records, "CPU Scheduling", "cpuUtilization", "CPU utilization");
        addMetricChart(records, "Memory Management", "faults", "Page faults");
        addMetricChart(records, "Memory Management", "utilization", "Memory utilization");
    }

    private void addMetricChart(List<SimulationRecord> records, String module,
                                String key, String title) {
        List<SimulationRecord> supported = records.stream()
                .filter(record -> record.module().equals(module))
                .filter(record -> metric(record.results(), key).isPresent())
                .toList();
        if (supported.isEmpty()) return;
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        chart.setTitle(title + " (recorded results)");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        supported.forEach(record -> series.getData().add(new XYChart.Data<>(
                record.algorithm(), metric(record.results(), key).orElseThrow())));
        chart.getData().add(series);
        chart.setPrefHeight(280);
        VBox card = UiComponents.card("analytics-card");
        card.getChildren().add(chart);
        charts.getChildren().add(card);
    }

    private OptionalDouble metric(String results, String key) {
        for (String part : results.split(",")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                try { return OptionalDouble.of(Double.parseDouble(pair[1])); }
                catch (NumberFormatException ignored) { return OptionalDouble.empty(); }
            }
        }
        return OptionalDouble.empty();
    }

    private void exportCsv() {
        try {
            new CsvReportGenerator().write(Path.of("oslab-report.csv"),
                    service.history(moduleFilter.getValue(), algorithmFilter.getValue()));
            showInfo("CSV exported to oslab-report.csv");
        } catch (IllegalStateException exception) { showError(exception.getMessage()); }
    }

    private void exportPdf() {
        try {
            new PdfReportGenerator().write(Path.of("oslab-report.pdf"),
                    service.history(moduleFilter.getValue(), algorithmFilter.getValue()));
            showInfo("PDF exported to oslab-report.pdf");
        } catch (IllegalStateException exception) { showError(exception.getMessage()); }
    }

    private Button action(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        return button;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
