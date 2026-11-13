package com.oslab.controller;

import com.oslab.algorithms.SchedulingAlgorithm;
import com.oslab.model.GanttChartEntry;
import com.oslab.model.Process;
import com.oslab.model.ProcessMetrics;
import com.oslab.model.SchedulingResult;
import com.oslab.scheduling.FcfsAlgorithm;
import com.oslab.scheduling.PriorityNonPreemptiveAlgorithm;
import com.oslab.scheduling.PriorityPreemptiveAlgorithm;
import com.oslab.scheduling.RoundRobinAlgorithm;
import com.oslab.scheduling.SjfNonPreemptiveAlgorithm;
import com.oslab.scheduling.SrtfAlgorithm;
import com.oslab.service.SchedulingService;
import com.oslab.service.AnalyticsService;
import com.oslab.repository.SqliteAnalyticsRepository;
import com.oslab.view.UiComponents;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;

public class SchedulingController {
    private final SchedulingService service;
    private final AnalyticsService analytics = new AnalyticsService(
            new SqliteAnalyticsRepository(Path.of("oslab.db")));
    private final List<Process> processes = new ArrayList<>();
    private final Map<String, SchedulingAlgorithm> algorithms = new LinkedHashMap<>();
    private final Map<String, CheckBox> comparisonChecks = new LinkedHashMap<>();
    private final VBox ganttContainer = new VBox(8);
    private final VBox resultContainer = new VBox(10);
    private final TableView<Process> processTable = new TableView<>();
    private final TableView<ProcessMetrics> metricsTable = new TableView<>();
    private final ComboBox<String> algorithmChoice = new ComboBox<>();
    private final TextField quantumField = new TextField("2");
    private final Slider speedSlider = new Slider(0.25, 2, 1);
    private TextField processIdField;
    private TextField arrivalField;
    private TextField burstField;
    private TextField priorityField;
    private Timeline playback;
    private List<GanttChartEntry> playbackEntries = List.of();
    private int playbackIndex;

    public SchedulingController() {
        this(new SchedulingService());
    }

    public SchedulingController(SchedulingService service) {
        this.service = service;
        algorithms.put("FCFS", new FcfsAlgorithm());
        algorithms.put("SJF (Non-preemptive)", new SjfNonPreemptiveAlgorithm());
        algorithms.put("SRTF (Preemptive)", new SrtfAlgorithm());
        algorithms.put("Priority (Non-preemptive)", new PriorityNonPreemptiveAlgorithm());
        algorithms.put("Priority (Preemptive)", new PriorityPreemptiveAlgorithm());
        algorithms.put("Round Robin", new RoundRobinAlgorithm(2));
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(createHeader());
        root.setCenter(createContent());
        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        return scene;
    }

    private HBox createHeader() {
        Button back = new Button("←  Dashboard");
        back.getStyleClass().add("back-button");
        back.setOnAction(event -> ((javafx.stage.Stage) back.getScene().getWindow())
                .setScene(new DashboardController().createScene()));
        Label title = new Label("CPU Scheduling Simulator");
        title.getStyleClass().add("page-title");
        HBox header = new HBox(22, back, title);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25, 40, 18, 40));
        return header;
    }

    private ScrollPane createContent() {
        VBox content = new VBox(18, createControls(), createProcessCard(),
                createGanttCard(), createResultsCard(), createComparisonCard());
        content.setPadding(new Insets(10, 40, 40, 40));
        content.getStyleClass().add("content");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("content-scroll");
        return scroll;
    }

    private VBox createControls() {
        VBox card = UiComponents.card("sim-controls");
        Label hint = new Label("Select an algorithm and run an interactive simulation.");
        hint.getStyleClass().add("muted");
        algorithmChoice.setItems(FXCollections.observableArrayList(algorithms.keySet()));
        algorithmChoice.setValue("FCFS");
        algorithmChoice.setPrefWidth(200);
        quantumField.setPrefWidth(70);
        Button run = actionButton("Run Simulation");
        run.setOnAction(event -> runSimulation());
        Button reset = actionButton("Reset");
        reset.setOnAction(event -> resetResults());
        Button step = actionButton("Step Forward");
        step.setOnAction(event -> stepForward());
        Button play = actionButton("Play / Pause");
        play.setOnAction(event -> togglePlayback());
        speedSlider.setPrefWidth(140);
        HBox row = new HBox(10, new Label("Algorithm"), algorithmChoice,
                new Label("Quantum"), quantumField, run, reset, step, play,
                new Label("Speed"), speedSlider);
        row.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(hint, row);
        return card;
    }

    private VBox createProcessCard() {
        VBox card = UiComponents.card("process-card");
        Label title = new Label("Process management");
        title.getStyleClass().add("section-title");
        TextField id = processIdField = field("P1");
        TextField arrival = arrivalField = field("0");
        TextField burst = burstField = field("5");
        TextField priority = priorityField = field("1");
        Button add = actionButton("Add process");
        add.setOnAction(event -> addProcess(id, arrival, burst, priority));
        Button delete = actionButton("Delete selected");
        delete.setOnAction(event -> {
            Process selected = processTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                processes.remove(selected);
                refreshProcessTable();
            }
        });
        Button edit = actionButton("Edit selected");
        edit.setOnAction(event -> editSelectedProcess());
        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.addRow(0, new Label("Process ID"), id, new Label("Arrival"), arrival,
                new Label("Burst"), burst, new Label("Priority"), priority, add, edit, delete);
        configureProcessTable();
        processTable.setPrefHeight(145);
        card.getChildren().addAll(title, form, processTable);
        return card;
    }

    private TextField field(String text) {
        TextField field = new TextField(text);
        field.setPrefWidth(68);
        return field;
    }

    private void configureProcessTable() {
        if (!processTable.getColumns().isEmpty()) {
            return;
        }
        processTable.getColumns().addAll(
                column("Process ID", "id", 130),
                column("Arrival Time", "arrivalTime", 130),
                column("Burst Time", "burstTime", 130),
                column("Priority", "priority", 130));
        processTable.setPlaceholder(new Label("Add processes to begin"));
        processTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> {
            if (selected != null) {
                processIdField.setText(selected.id());
                arrivalField.setText(String.valueOf(selected.arrivalTime()));
                burstField.setText(String.valueOf(selected.burstTime()));
                priorityField.setText(String.valueOf(selected.priority()));
            }
        });
        refreshProcessTable();
    }

    private <T> TableColumn<Process, T> column(String title, String property, double width) {
        TableColumn<Process, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    private void addProcess(TextField id, TextField arrival, TextField burst, TextField priority) {
        try {
            Process process = new Process(id.getText().trim(), parse(arrival), parse(burst), parse(priority));
            if (processes.stream().anyMatch(existing -> existing.id().equals(process.id()))) {
                throw new IllegalArgumentException("Process IDs must be unique");
            }
            processes.add(process);
            processes.sort(Comparator.comparingInt(Process::arrivalTime).thenComparing(Process::id));
            refreshProcessTable();
            id.setText("P" + (processes.size() + 1));
        } catch (IllegalArgumentException ex) {
            showError("Invalid process", ex.getMessage());
        }
    }

    private int parse(TextField field) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Times and priority must be whole numbers");
        }
    }

    private void refreshProcessTable() {
        processTable.setItems(FXCollections.observableArrayList(processes));
    }

    private void editSelectedProcess() {
        Process selected = processTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Edit process", "Select a process first");
            return;
        }
        try {
            Process updated = new Process(processIdField.getText().trim(), parse(arrivalField),
                    parse(burstField), parse(priorityField));
            if (!selected.id().equals(updated.id()) && processes.stream()
                    .anyMatch(existing -> existing.id().equals(updated.id()))) {
                throw new IllegalArgumentException("Process IDs must be unique");
            }
            processes.set(processes.indexOf(selected), updated);
            processes.sort(Comparator.comparingInt(Process::arrivalTime).thenComparing(Process::id));
            refreshProcessTable();
        } catch (IllegalArgumentException ex) {
            showError("Invalid process", ex.getMessage());
        }
    }

    private VBox createGanttCard() {
        VBox card = UiComponents.card("gantt-card");
        Label title = new Label("Gantt chart");
        title.getStyleClass().add("section-title");
        ganttContainer.getStyleClass().add("gantt-container");
        ganttContainer.setMinHeight(86);
        ganttContainer.getChildren().add(new Label("Run a simulation to view process execution."));
        card.getChildren().addAll(title, ganttContainer);
        return card;
    }

    private VBox createResultsCard() {
        VBox card = UiComponents.card("results-card");
        Label title = new Label("Performance metrics");
        title.getStyleClass().add("section-title");
        configureMetricsTable();
        resultContainer.getChildren().add(new Label("Average waiting time, turnaround time, utilization and throughput appear after execution."));
        card.getChildren().addAll(title, metricsTable, resultContainer);
        return card;
    }

    private void configureMetricsTable() {
        metricsTable.getColumns().addAll(
                metricsColumn("Process", "process", 150),
                metricsColumn("Completion", "completionTime", 100),
                metricsColumn("Turnaround", "turnaroundTime", 100),
                metricsColumn("Waiting", "waitingTime", 100),
                metricsColumn("Response", "responseTime", 100));
        metricsTable.setPrefHeight(175);
    }

    private <T> TableColumn<ProcessMetrics, T> metricsColumn(String title, String property, double width) {
        TableColumn<ProcessMetrics, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    private VBox createComparisonCard() {
        VBox card = UiComponents.card("comparison-card");
        Label title = new Label("Algorithm comparison");
        title.getStyleClass().add("section-title");
        Button compare = actionButton("Compare all algorithms");
        compare.setOnAction(event -> compareAlgorithms(card));
        HBox heading = new HBox(title, spacer(), compare);
        heading.setAlignment(Pos.CENTER_LEFT);
        HBox choices = new HBox(12);
        algorithms.keySet().forEach(name -> {
            CheckBox check = new CheckBox(name);
            check.setSelected(true);
            check.getStyleClass().add("comparison-check");
            comparisonChecks.put(name, check);
            choices.getChildren().add(check);
        });
        card.getChildren().addAll(heading, choices);
        return card;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void runSimulation() {
        try {
            if (processes.isEmpty()) {
                throw new IllegalArgumentException("Add at least one process");
            }
            SchedulingAlgorithm algorithm = selectedAlgorithm();
            SchedulingResult result = service.run(algorithm, processes);
            renderResult(result);
        } catch (IllegalArgumentException ex) {
            showError("Unable to run simulation", ex.getMessage());
        }
    }

    private SchedulingAlgorithm selectedAlgorithm() {
        if ("Round Robin".equals(algorithmChoice.getValue())) {
            int quantum = parse(quantumField);
            if (quantum <= 0) {
                throw new IllegalArgumentException("Time quantum must be positive");
            }
            return new RoundRobinAlgorithm(quantum);
        }
        return algorithms.get(algorithmChoice.getValue());
    }

    private void renderResult(SchedulingResult result) {
        analytics.recordScheduling(processes.toString(), result);
        playbackEntries = result.ganttEntries();
        playbackIndex = playbackEntries.size();
        renderGantt(playbackEntries);
        metricsTable.setItems(FXCollections.observableArrayList(result.processMetrics().values()));
        resultContainer.getChildren().clear();
        resultContainer.getChildren().add(new Label(String.format(
                "Average waiting: %.2f   |   Average turnaround: %.2f   |   CPU utilization: %.2f%%   |   Throughput: %.3f processes/unit",
                result.averageWaitingTime(), result.averageTurnaroundTime(),
                result.cpuUtilization(), result.throughput())));
    }

    private void renderGantt(List<GanttChartEntry> entries) {
        ganttContainer.getChildren().clear();
        if (entries.isEmpty()) {
            return;
        }
        HBox blocks = new HBox();
        int total = entries.get(entries.size() - 1).endTime();
        for (GanttChartEntry entry : entries) {
            VBox block = new VBox(3);
            block.setAlignment(Pos.CENTER);
            block.setMinWidth(Math.max(50, entry.duration() * 38));
            block.setPrefWidth(Math.max(50, entry.duration() * 38));
            block.getStyleClass().add(entry.isIdle() ? "gantt-idle" : "gantt-block");
            Label process = new Label(entry.processId());
            Label times = new Label(entry.startTime() + " – " + entry.endTime());
            process.getStyleClass().add("gantt-process");
            times.getStyleClass().add("gantt-time");
            block.getChildren().addAll(process, times);
            blocks.getChildren().add(block);
        }
        Label scale = new Label("0                                              " + total);
        scale.getStyleClass().add("muted");
        ganttContainer.getChildren().addAll(blocks, scale);
    }

    private void stepForward() {
        if (playbackEntries.isEmpty()) {
            runSimulation();
            return;
        }
        playbackIndex = Math.min(playbackIndex + 1, playbackEntries.size());
        renderGantt(playbackEntries.subList(0, playbackIndex));
    }

    private void togglePlayback() {
        if (playback != null && playback.getStatus() == Timeline.Status.RUNNING) {
            playback.pause();
            return;
        }
        playback = new Timeline(new KeyFrame(Duration.seconds(1 / speedSlider.getValue()), event -> {
            if (playbackIndex >= playbackEntries.size()) {
                playback.stop();
            } else {
                stepForward();
            }
        }));
        playback.setCycleCount(Timeline.INDEFINITE);
        playback.play();
    }

    private void resetResults() {
        if (playback != null) {
            playback.stop();
        }
        playbackEntries = List.of();
        playbackIndex = 0;
        ganttContainer.getChildren().setAll(new Label("Run a simulation to view process execution."));
        metricsTable.getItems().clear();
        resultContainer.getChildren().setAll(new Label("Average waiting time, turnaround time, utilization and throughput appear after execution."));
    }

    private void compareAlgorithms(VBox card) {
        try {
            if (processes.isEmpty()) {
                throw new IllegalArgumentException("Add at least one process");
            }
            List<SchedulingAlgorithm> selected = comparisonChecks.entrySet().stream()
                    .filter(entry -> entry.getValue().isSelected())
                    .map(entry -> "Round Robin".equals(entry.getKey()) ? selectedAlgorithm() : algorithms.get(entry.getKey()))
                    .toList();
            if (selected.isEmpty()) {
                throw new IllegalArgumentException("Select at least one algorithm");
            }
            Map<String, SchedulingResult> results = service.compare(selected, processes);
            if (card.getChildren().size() > 2) {
                card.getChildren().remove(2, card.getChildren().size());
            }
            BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
            chart.setTitle("Average waiting time");
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            results.forEach((name, result) -> series.getData().add(new XYChart.Data<>(name, result.averageWaitingTime())));
            chart.getData().add(series);
            chart.setPrefHeight(260);
            card.getChildren().add(chart);
        } catch (IllegalArgumentException ex) {
            showError("Unable to compare algorithms", ex.getMessage());
        }
    }

    private Button actionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        return button;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
