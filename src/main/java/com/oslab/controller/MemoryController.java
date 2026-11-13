package com.oslab.controller;

import com.oslab.algorithms.memory.BestFit;
import com.oslab.algorithms.memory.FIFO;
import com.oslab.algorithms.memory.FirstFit;
import com.oslab.algorithms.memory.LRU;
import com.oslab.algorithms.memory.MemoryAllocationAlgorithm;
import com.oslab.algorithms.memory.Optimal;
import com.oslab.algorithms.memory.PageReplacementAlgorithm;
import com.oslab.algorithms.memory.WorstFit;
import com.oslab.memory.MemoryAllocationResult;
import com.oslab.memory.MemoryBlock;
import com.oslab.memory.MemoryProcess;
import com.oslab.memory.PageReference;
import com.oslab.memory.PageReplacementResult;
import com.oslab.memory.PageReplacementStep;
import com.oslab.service.MemoryAllocationService;
import com.oslab.service.PageReplacementService;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;

public class MemoryController {
    private final MemoryAllocationService allocationService;
    private final PageReplacementService pageService;
    private final AnalyticsService analytics = new AnalyticsService(
            new SqliteAnalyticsRepository(Path.of("oslab.db")));
    private final Map<String, MemoryAllocationAlgorithm> allocationAlgorithms = new LinkedHashMap<>();
    private final Map<String, PageReplacementAlgorithm> pageAlgorithms = new LinkedHashMap<>();
    private final VBox memoryView = new VBox(10);
    private final VBox pageView = new VBox(10);
    private final VBox memoryDiagram = new VBox(8);
    private final VBox pageDiagram = new VBox(8);
    private final Label allocationMetrics = new Label("Configure memory to begin.");
    private final Label pageMetrics = new Label("Run a page replacement simulation to see metrics.");
    private final ComboBox<String> allocationChoice = new ComboBox<>();
    private final ComboBox<String> pageChoice = new ComboBox<>();
    private final TextField totalMemoryField = field("1024");
    private final TextField partitionsField = field("256, 128, 384");
    private final TextField processIdField = field("P1");
    private final TextField requiredMemoryField = field("120");
    private final TextField deallocateField = field("P1");
    private final TextArea referenceField = new TextArea("7 0 1 2 0 3 0 4 2 3 0 3");
    private final TextField framesField = field("3");
    private final Slider speedSlider = new Slider(0.25, 2, 1);
    private final TableView<MemoryBlockRow> memoryTable = new TableView<>();
    private final TableView<PageReplacementStepRow> pageTable = new TableView<>();
    private Timeline pagePlayback;
    private PageReplacementResult pageResult;
    private int pageStep;

    public MemoryController() {
        this(new MemoryAllocationService(), new PageReplacementService());
    }

    public MemoryController(MemoryAllocationService allocationService, PageReplacementService pageService) {
        this.allocationService = allocationService;
        this.pageService = pageService;
        allocationAlgorithms.put("First Fit", new FirstFit());
        allocationAlgorithms.put("Best Fit", new BestFit());
        allocationAlgorithms.put("Worst Fit", new WorstFit());
        pageAlgorithms.put("FIFO", new FIFO());
        pageAlgorithms.put("LRU", new LRU());
        pageAlgorithms.put("Optimal", new Optimal());
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(createHeader());
        memoryView.getChildren().setAll(createMemoryControls(), createMemoryVisuals(), createMemoryComparison());
        pageView.getChildren().setAll(createPageControls(), createPageVisuals(), createPageComparison());
        javafx.scene.control.TabPane tabs = new javafx.scene.control.TabPane();
        javafx.scene.control.Tab contiguous = new javafx.scene.control.Tab("Contiguous Allocation", scroll(memoryView));
        javafx.scene.control.Tab paging = new javafx.scene.control.Tab("Page Replacement", scroll(pageView));
        contiguous.setClosable(false);
        paging.setClosable(false);
        tabs.getTabs().addAll(contiguous, paging);
        tabs.getStyleClass().add("memory-tabs");
        root.setCenter(tabs);
        Scene scene = new Scene(root, 1280, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        return scene;
    }

    private HBox createHeader() {
        Button back = actionButton("←  Dashboard");
        back.setOnAction(event -> ((javafx.stage.Stage) back.getScene().getWindow())
                .setScene(new DashboardController().createScene()));
        Label title = new Label("Memory Management Simulator");
        title.getStyleClass().add("page-title");
        HBox header = new HBox(22, back, title);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25, 40, 18, 40));
        return header;
    }

    private ScrollPane scroll(VBox content) {
        content.setPadding(new Insets(22, 40, 40, 40));
        content.getStyleClass().add("content");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("content-scroll");
        return scroll;
    }

    private VBox createMemoryControls() {
        VBox card = UiComponents.card("sim-controls");
        allocationChoice.setItems(FXCollections.observableArrayList(allocationAlgorithms.keySet()));
        allocationChoice.setValue("First Fit");
        Button configure = actionButton("Configure Memory");
        configure.setOnAction(event -> configureMemory());
        Button allocate = actionButton("Allocate");
        allocate.setOnAction(event -> allocateMemory());
        Button deallocate = actionButton("Deallocate");
        deallocate.setOnAction(event -> deallocateMemory());
        HBox row = new HBox(8, new Label("Total size"), totalMemoryField,
                new Label("Partitions"), partitionsField, allocationChoice, configure,
                new Label("Process"), processIdField, new Label("Required"), requiredMemoryField,
                allocate, new Label("Free ID"), deallocateField, deallocate);
        row.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(new Label("Configure partitions, then allocate and release processes."), row);
        return card;
    }

    private VBox createMemoryVisuals() {
        VBox card = UiComponents.card("memory-card");
        Label title = new Label("Memory partition map");
        title.getStyleClass().add("section-title");
        allocationMetrics.getStyleClass().add("muted");
        memoryDiagram.setMinHeight(80);
        configureMemoryTable();
        card.getChildren().addAll(title, memoryDiagram, allocationMetrics, memoryTable);
        return card;
    }

    private void configureMemoryTable() {
        if (!memoryTable.getColumns().isEmpty()) return;
        memoryTable.getColumns().addAll(
                memoryColumn("Start", "start", 120),
                memoryColumn("Size", "size", 120),
                memoryColumn("Status", "status", 180));
        memoryTable.setPrefHeight(145);
    }

    private <T> TableColumn<MemoryBlockRow, T> memoryColumn(String title, String property, double width) {
        TableColumn<MemoryBlockRow, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    private VBox createMemoryComparison() {
        VBox card = UiComponents.card("comparison-card");
        Label title = new Label("Allocation algorithm comparison");
        title.getStyleClass().add("section-title");
        Button compare = actionButton("Compare First / Best / Worst Fit");
        compare.setOnAction(event -> compareAllocation());
        card.getChildren().addAll(new HBox(title, spacer(), compare));
        return card;
    }

    private VBox createPageControls() {
        VBox card = UiComponents.card("sim-controls");
        pageChoice.setItems(FXCollections.observableArrayList(pageAlgorithms.keySet()));
        pageChoice.setValue("FIFO");
        referenceField.setPrefRowCount(2);
        referenceField.setPrefWidth(500);
        Button run = actionButton("Run");
        run.setOnAction(event -> runPages());
        Button pause = actionButton("Pause");
        pause.setOnAction(event -> { if (pagePlayback != null) pagePlayback.pause(); });
        Button resume = actionButton("Resume");
        resume.setOnAction(event -> { if (pagePlayback != null) pagePlayback.play(); });
        Button reset = actionButton("Reset");
        reset.setOnAction(event -> resetPages());
        Button step = actionButton("Step Forward");
        step.setOnAction(event -> showNextPageStep());
        speedSlider.setPrefWidth(140);
        HBox row = new HBox(8, new Label("Algorithm"), pageChoice, new Label("Frames"), framesField,
                new Label("References"), referenceField, run, pause, resume, reset, step,
                new Label("Speed"), speedSlider);
        row.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(row);
        return card;
    }

    private VBox createPageVisuals() {
        VBox card = UiComponents.card("page-card");
        Label title = new Label("Frame-by-frame execution");
        title.getStyleClass().add("section-title");
        pageMetrics.getStyleClass().add("muted");
        configurePageTable();
        pageDiagram.setMinHeight(70);
        card.getChildren().addAll(title, pageDiagram, pageMetrics, pageTable);
        return card;
    }

    private void configurePageTable() {
        if (!pageTable.getColumns().isEmpty()) return;
        pageTable.getColumns().addAll(
                pageColumn("Step", "step", 80),
                pageColumn("Reference", "reference", 100),
                pageColumn("Frames", "frames", 250),
                pageColumn("Result", "result", 110),
                pageColumn("Replaced", "replaced", 110));
        pageTable.setPrefHeight(190);
    }

    private <T> TableColumn<PageReplacementStepRow, T> pageColumn(String title, String property, double width) {
        TableColumn<PageReplacementStepRow, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    private VBox createPageComparison() {
        VBox card = UiComponents.card("comparison-card");
        Label title = new Label("Page replacement comparison");
        title.getStyleClass().add("section-title");
        Button compare = actionButton("Compare FIFO / LRU / Optimal");
        compare.setOnAction(event -> comparePages(card));
        card.getChildren().add(new HBox(title, spacer(), compare));
        return card;
    }

    private void configureMemory() {
        try {
            allocationService.configure(parse(totalMemoryField), parseList(partitionsField.getText()));
            renderMemory(allocationService.blocks(), allocationService.blocks().stream()
                    .mapToInt(MemoryBlock::size).sum(), 0, 0, 0);
        } catch (IllegalArgumentException ex) {
            showError("Invalid memory configuration", ex.getMessage());
        }
    }

    private void allocateMemory() {
        try {
            MemoryAllocationResult result = allocationService.allocate(
                    allocationAlgorithms.get(allocationChoice.getValue()),
                    new MemoryProcess(processIdField.getText().trim(), parse(requiredMemoryField)));
            analytics.recordMemoryAllocation("total=" + totalMemoryField.getText()
                    + ",partitions=" + partitionsField.getText(), allocationChoice.getValue(), result);
            renderMemory(result.blocks(), result.freeMemory(), result.internalFragmentation(),
                    result.externalFragmentation(), result.utilizationPercentage());
            if (!result.allocated()) showError("Allocation failed", result.message());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError("Allocation failed", ex.getMessage());
        }
    }

    private void deallocateMemory() {
        try {
            MemoryAllocationResult result = allocationService.deallocate(deallocateField.getText().trim());
            renderMemory(result.blocks(), result.freeMemory(), result.internalFragmentation(),
                    result.externalFragmentation(), result.utilizationPercentage());
            if (!result.allocated()) showError("Deallocation", result.message());
        } catch (IllegalStateException | IllegalArgumentException ex) {
            showError("Deallocation failed", ex.getMessage());
        }
    }

    private void renderMemory(List<MemoryBlock> blocks, int free, int internal, int external, double utilization) {
        memoryDiagram.getChildren().clear();
        HBox map = new HBox(2);
        for (MemoryBlock block : blocks) {
            VBox segment = new VBox(3);
            segment.setAlignment(Pos.CENTER);
            segment.setMinWidth(Math.max(55, block.size() / 4.0));
            segment.setPrefWidth(Math.max(55, block.size() / 4.0));
            segment.getStyleClass().add(block.isFree() ? "memory-free" : "memory-allocated");
            segment.getChildren().addAll(new Label(block.isFree() ? "FREE" : block.processId()),
                    new Label(block.size() + " units"));
            map.getChildren().add(segment);
        }
        memoryDiagram.getChildren().add(map);
        memoryTable.setItems(FXCollections.observableArrayList(blocks.stream().map(MemoryBlockRow::new).toList()));
        allocationMetrics.setText(String.format("Free: %d units  |  Utilization: %.2f%%  |  Internal fragmentation: %d  |  External fragmentation: %d",
                free, utilization, internal, external));
    }

    private void runPages() {
        try {
            pageResult = pageService.run(pageAlgorithms.get(pageChoice.getValue()),
                    parseReferences(referenceField.getText()), parse(framesField));
            analytics.recordPageReplacement("references=" + referenceField.getText()
                    + ",frames=" + framesField.getText(), pageResult);
            pageStep = 0;
            pageTable.getItems().clear();
            pageDiagram.getChildren().clear();
            pageMetrics.setText(String.format("Faults: %d  |  Hits: %d  |  Fault ratio: %.2f%%  |  Hit ratio: %.2f%%",
                    pageResult.pageFaults(), pageResult.pageHits(), pageResult.pageFaultRatio(), pageResult.pageHitRatio()));
            if (pagePlayback != null) pagePlayback.stop();
            pagePlayback = new Timeline(new KeyFrame(Duration.seconds(1 / speedSlider.getValue()), event -> showNextPageStep()));
            pagePlayback.setCycleCount(pageResult.steps().size());
            pagePlayback.play();
        } catch (IllegalArgumentException ex) {
            showError("Page simulation failed", ex.getMessage());
        }
    }

    private void showNextPageStep() {
        if (pageResult == null || pageStep >= pageResult.steps().size()) return;
        PageReplacementStep step = pageResult.steps().get(pageStep++);
        pageTable.getItems().add(new PageReplacementStepRow(step));
        pageDiagram.getChildren().setAll(new Label("Reference " + step.reference().pageNumber()
                + (step.hit() ? "  •  PAGE HIT" : "  •  PAGE FAULT")
                + (step.replacedPage() == null ? "" : "  •  replaced " + step.replacedPage())));
    }

    private void resetPages() {
        if (pagePlayback != null) pagePlayback.stop();
        pageResult = null;
        pageStep = 0;
        pageTable.getItems().clear();
        pageDiagram.getChildren().clear();
        pageMetrics.setText("Run a page replacement simulation to see metrics.");
    }

    private void compareAllocation() {
        try {
            int total = parse(totalMemoryField);
            List<Integer> partitions = parseList(partitionsField.getText());
            MemoryProcess process = new MemoryProcess(processIdField.getText().trim(), parse(requiredMemoryField));
            BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
            chart.setTitle("Remaining free memory");
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (MemoryAllocationAlgorithm algorithm : allocationAlgorithms.values()) {
                MemoryAllocationService service = new MemoryAllocationService();
                service.configure(total, partitions);
                series.getData().add(new XYChart.Data<>(algorithm.name(),
                        algorithm.allocate(service.blocks(), process).freeMemory()));
            }
            chart.getData().add(series);
            showChart("Allocation comparison", chart);
        } catch (IllegalArgumentException ex) {
            showError("Comparison failed", ex.getMessage());
        }
    }

    private void comparePages(VBox card) {
        try {
            List<PageReference> references = parseReferences(referenceField.getText());
            int frames = parse(framesField);
            Map<String, PageReplacementResult> results = pageService.compare(
                    new ArrayList<>(pageAlgorithms.values()), references, frames);
            BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
            chart.setTitle("Page faults");
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            results.forEach((name, result) -> series.getData().add(new XYChart.Data<>(name, result.pageFaults())));
            chart.getData().add(series);
            card.getChildren().add(chart);
        } catch (IllegalArgumentException ex) {
            showError("Comparison failed", ex.getMessage());
        }
    }

    private void showChart(String title, BarChart<String, Number> chart) {
        VBox box = new VBox(chart);
        box.setPadding(new Insets(12));
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(box, 700, 420));
        stage.show();
    }

    private List<PageReference> parseReferences(String text) {
        if (text == null || text.isBlank()) return List.of();
        try {
            return java.util.Arrays.stream(text.trim().split("[,\\s]+"))
                    .map(Integer::parseInt).map(PageReference::new).toList();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Page references must be non-negative integers");
        }
    }

    private List<Integer> parseList(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Partition sizes are required");
        try {
            return java.util.Arrays.stream(text.split("[,\\s]+")).map(Integer::parseInt).toList();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Partition sizes must be whole numbers");
        }
    }

    private int parse(TextField field) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Values must be whole numbers");
        }
    }

    private TextField field(String text) {
        TextField field = new TextField(text);
        field.setPrefWidth(90);
        return field;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
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

    public record MemoryBlockRow(int start, int size, String status) {
        MemoryBlockRow(MemoryBlock block) {
            this(block.startAddress(), block.size(), block.isFree() ? "FREE" : "ALLOCATED: " + block.processId());
        }
    }

    public record PageReplacementStepRow(int step, int reference, String frames, String result, String replaced) {
        PageReplacementStepRow(PageReplacementStep step) {
            this(step.stepNumber(), step.reference().pageNumber(), step.frames().toString(),
                    step.hit() ? "HIT" : "FAULT",
                    step.replacedPage() == null ? "-" : String.valueOf(step.replacedPage()));
        }
    }
}
