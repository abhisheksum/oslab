package com.oslab.controller;

import com.oslab.model.deadlock.BankersResult;
import com.oslab.model.deadlock.BankersStep;
import com.oslab.model.deadlock.DeadlockDetectionResult;
import com.oslab.model.deadlock.ResourceEdge;
import com.oslab.model.deadlock.ResourceGraphResult;
import com.oslab.model.deadlock.ResourceNode;
import com.oslab.model.deadlock.ResourceState;
import com.oslab.service.DeadlockService;
import com.oslab.service.AnalyticsService;
import com.oslab.repository.SqliteAnalyticsRepository;
import com.oslab.view.UiComponents;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;

public class DeadlockController {
    private final DeadlockService service;
    private final AnalyticsService analytics = new AnalyticsService(
            new SqliteAnalyticsRepository(Path.of("oslab.db")));
    private final TextArea bankersAvailable = area("3 3 2");
    private final TextArea bankersAllocation = area("0 1 0; 2 0 0; 3 0 2; 2 1 1; 0 0 2");
    private final TextArea bankersMaximum = area("7 5 3; 3 2 2; 9 0 2; 2 2 2; 4 3 3");
    private final Label bankersStatus = new Label("Enter matrices and analyze the state.");
    private final VBox bankersSteps = new VBox(6);
    private BankersResult bankersResult;
    private int bankerStep;
    private Timeline bankerPlayback;
    private final TextArea detectionAvailable = area("1 0 0");
    private final TextArea detectionAllocation = area("1 0 0; 0 1 0; 0 0 1");
    private final TextArea detectionRequest = area("0 1 0; 0 0 1; 1 0 0");
    private final Label detectionStatus = new Label("Enter detection matrices and run detection.");
    private final TableView<ProgressRow> detectionTable = new TableView<>();
    private final Pane graphPane = new Pane();
    private final List<ResourceNode> nodes = new ArrayList<>();
    private final List<ResourceEdge> edges = new ArrayList<>();
    private final TextField nodeId = new TextField();
    private final TextField edgeFrom = new TextField();
    private final TextField edgeTo = new TextField();
    private final javafx.scene.control.ComboBox<String> edgeType = new javafx.scene.control.ComboBox<>();
    private final Label graphStatus = new Label("Add graph nodes and edges to analyze cycles.");

    public DeadlockController() {
        this(new DeadlockService());
    }

    public DeadlockController(DeadlockService service) {
        this.service = service;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(createHeader());
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("memory-tabs");
        tabs.getTabs().addAll(tab("Banker's Algorithm", bankersView()),
                tab("Deadlock Detection", detectionView()),
                tab("Resource Graph", graphView()),
                tab("Learn", educationView()));
        root.setCenter(tabs);
        Scene scene = new Scene(root, 1280, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        return scene;
    }

    private HBox createHeader() {
        Button back = actionButton("←  Dashboard");
        back.setOnAction(event -> ((javafx.stage.Stage) back.getScene().getWindow())
                .setScene(new DashboardController().createScene()));
        Label title = new Label("Deadlock Management Simulator");
        title.getStyleClass().add("page-title");
        HBox header = new HBox(22, back, title);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25, 40, 18, 40));
        return header;
    }

    private Tab tab(String title, VBox view) {
        Tab tab = new Tab(title, scroll(view));
        tab.setClosable(false);
        return tab;
    }

    private ScrollPane scroll(VBox content) {
        content.setPadding(new Insets(22, 40, 40, 40));
        content.getStyleClass().add("content");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("content-scroll");
        return scroll;
    }

    private VBox bankersView() {
        Button analyze = actionButton("Analyze State");
        analyze.setOnAction(event -> analyzeBankers());
        Button reset = actionButton("Reset");
        reset.setOnAction(event -> resetBankers());
        Button previous = actionButton("Previous Step");
        previous.setOnAction(event -> showBankerStep(-1));
        Button next = actionButton("Next Step");
        next.setOnAction(event -> showBankerStep(1));
        Button auto = actionButton("Run Automatically");
        auto.setOnAction(event -> runBankerAutomatically());
        HBox controls = new HBox(10, analyze, previous, next, auto, reset);
        controls.setAlignment(Pos.CENTER_LEFT);
        VBox input = UiComponents.card("deadlock-card");
        input.getChildren().addAll(new Label("Available resources (space-separated)"),
                bankersAvailable, new Label("Allocation matrix (rows separated by ';')"),
                bankersAllocation, new Label("Maximum demand matrix"), bankersMaximum, controls);
        bankersStatus.getStyleClass().add("deadlock-status");
        VBox output = UiComponents.card("deadlock-card");
        output.getChildren().addAll(new Label("Need matrix / safe sequence"), bankersStatus, bankersSteps);
        return new VBox(18, input, output);
    }

    private void analyzeBankers() {
        try {
            ResourceState state = new ResourceState(vector(bankersAvailable.getText()),
                    matrix(bankersAllocation.getText()), matrix(bankersMaximum.getText()));
            bankersResult = service.analyzeBankers(state);
            analytics.recordBankers("available=" + bankersAvailable.getText()
                    + ",allocation=" + bankersAllocation.getText()
                    + ",maximum=" + bankersMaximum.getText(), bankersResult);
            bankerStep = 0;
            bankersSteps.getChildren().clear();
            bankersSteps.getChildren().add(new Label("Need: " + bankersResult.need()));
            bankersStatus.setText(bankersResult.safe()
                    ? "SAFE STATE  |  Sequence: " + bankersResult.safeSequence()
                    : "UNSAFE STATE  |  No complete safe sequence exists");
            showBankerStep(1);
        } catch (IllegalArgumentException ex) {
            showError("Banker's input error", ex.getMessage());
        }
    }

    private void showBankerStep(int delta) {
        if (bankersResult == null) return;
        bankerStep = Math.max(0, Math.min(bankersResult.steps().size(), bankerStep + delta));
        if (bankerStep == 0) return;
        BankersStep step = bankersResult.steps().get(bankerStep - 1);
        bankersSteps.getChildren().removeIf(node -> node.getStyleClass().contains("step-row"));
        Label row = new Label("Step " + bankerStep + ": " + step.processId()
                + (step.executable() ? " can execute" : " cannot execute")
                + " | Available: " + step.availableBefore()
                + (step.executable() ? " -> " + step.availableAfter() : ""));
        row.getStyleClass().addAll("step-row", step.executable() ? "step-complete" : "step-blocked");
        bankersSteps.getChildren().add(row);
    }

    private void runBankerAutomatically() {
        if (bankersResult == null) {
            analyzeBankers();
        }
        if (bankersResult == null) return;
        if (bankerPlayback != null) bankerPlayback.stop();
        bankerPlayback = new Timeline(new KeyFrame(Duration.seconds(0.9), event -> {
            if (bankerStep >= bankersResult.steps().size()) bankerPlayback.stop();
            else showBankerStep(1);
        }));
        bankerPlayback.setCycleCount(Timeline.INDEFINITE);
        bankerPlayback.play();
    }

    private void resetBankers() {
        if (bankerPlayback != null) bankerPlayback.stop();
        bankersResult = null;
        bankerStep = 0;
        bankersSteps.getChildren().clear();
        bankersStatus.setText("Enter matrices and analyze the state.");
    }

    private VBox detectionView() {
        Button run = actionButton("Run Detection");
        run.setOnAction(event -> analyzeDetection());
        VBox input = UiComponents.card("deadlock-card");
        input.getChildren().addAll(new Label("Available resources"), detectionAvailable,
                new Label("Allocation matrix"), detectionAllocation,
                new Label("Request matrix"), detectionRequest, run);
        detectionStatus.getStyleClass().add("deadlock-status");
        configureDetectionTable();
        VBox output = UiComponents.card("deadlock-card");
        output.getChildren().addAll(detectionStatus, detectionTable);
        return new VBox(18, input, output);
    }

    private void configureDetectionTable() {
        if (!detectionTable.getColumns().isEmpty()) return;
        TableColumn<ProgressRow, Integer> step = new TableColumn<>("Step");
        step.setCellValueFactory(new PropertyValueFactory<>("step"));
        TableColumn<ProgressRow, String> work = new TableColumn<>("Work");
        work.setCellValueFactory(new PropertyValueFactory<>("work"));
        TableColumn<ProgressRow, String> finish = new TableColumn<>("Finish");
        finish.setCellValueFactory(new PropertyValueFactory<>("finish"));
        detectionTable.getColumns().addAll(step, work, finish);
        detectionTable.setPrefHeight(190);
    }

    private void analyzeDetection() {
        try {
            DeadlockDetectionResult result = service.detect(vector(detectionAvailable.getText()),
                    matrix(detectionAllocation.getText()), matrix(detectionRequest.getText()));
            analytics.recordDetection("available=" + detectionAvailable.getText()
                    + ",allocation=" + detectionAllocation.getText()
                    + ",request=" + detectionRequest.getText(), result);
            detectionStatus.setText(result.deadlockDetected()
                    ? "DEADLOCK DETECTED  |  Processes: " + result.deadlockedProcesses()
                    : "NO DEADLOCK DETECTED");
            List<ProgressRow> rows = new ArrayList<>();
            for (int i = 0; i < result.workProgression().size(); i++) {
                rows.add(new ProgressRow(i, result.workProgression().get(i).toString(),
                        result.finishProgression().get(i).toString()));
            }
            detectionTable.setItems(FXCollections.observableArrayList(rows));
        } catch (IllegalArgumentException ex) {
            showError("Detection input error", ex.getMessage());
        }
    }

    private VBox graphView() {
        Button addProcess = actionButton("Add Process");
        addProcess.setOnAction(event -> addNode(ResourceNode.NodeType.PROCESS));
        Button addResource = actionButton("Add Resource");
        addResource.setOnAction(event -> addNode(ResourceNode.NodeType.RESOURCE));
        Button removeNode = actionButton("Remove Node");
        removeNode.setOnAction(event -> removeGraphNode());
        edgeType.setItems(FXCollections.observableArrayList("REQUEST", "ALLOCATION"));
        edgeType.setValue("REQUEST");
        Button addEdge = actionButton("Add Edge");
        addEdge.setOnAction(event -> addGraphEdge());
        Button remove = actionButton("Remove Last Edge");
        remove.setOnAction(event -> {
            if (!edges.isEmpty()) edges.remove(edges.size() - 1);
            renderGraph();
        });
        Button analyze = actionButton("Detect Cycle");
        analyze.setOnAction(event -> analyzeGraph());
        HBox controls = new HBox(8, new Label("Node ID"), nodeId, addProcess, addResource, removeNode,
                new Label("From"), edgeFrom, new Label("To"), edgeTo, edgeType, addEdge, remove, analyze);
        controls.setAlignment(Pos.CENTER_LEFT);
        graphPane.setPrefHeight(370);
        graphPane.getStyleClass().add("graph-pane");
        return new VBox(18, UiComponents.card("deadlock-card"), controls, graphPane, graphStatus);
    }

    private void addNode(ResourceNode.NodeType type) {
        String id = nodeId.getText().trim();
        try {
            if (id.isBlank() || nodes.stream().anyMatch(node -> node.id().equals(id))) {
                throw new IllegalArgumentException("Node ID must be unique and non-empty");
            }
            nodes.add(new ResourceNode(id, type, 1));
            nodeId.clear();
            renderGraph();
        } catch (IllegalArgumentException ex) {
            showError("Graph node error", ex.getMessage());
        }
    }

    private void addGraphEdge() {
        try {
            if (nodes.stream().noneMatch(node -> node.id().equals(edgeFrom.getText().trim()))
                    || nodes.stream().noneMatch(node -> node.id().equals(edgeTo.getText().trim()))) {
                throw new IllegalArgumentException("Both edge endpoints must exist");
            }

            edges.add(new ResourceEdge(edgeFrom.getText().trim(), edgeTo.getText().trim(),
                    ResourceEdge.EdgeType.valueOf(edgeType.getValue())));
            renderGraph();
        } catch (IllegalArgumentException ex) {
            showError("Graph edge error", ex.getMessage());
        }
    }

    private void removeGraphNode() {
        String id = nodeId.getText().trim();
        if (id.isBlank()) {
            showError("Graph node error", "Enter the node ID to remove");
            return;
        }
        boolean removed = nodes.removeIf(node -> node.id().equals(id));
        if (!removed) {
            showError("Graph node error", "Node does not exist");
            return;
        }
        edges.removeIf(edge -> edge.from().equals(id) || edge.to().equals(id));
        nodeId.clear();
        renderGraph();
    }

    private void analyzeGraph() {
        try {
            ResourceGraphResult result = service.analyzeGraph(nodes, edges);
            graphStatus.setText(result.cycleDetected()
                    ? "CYCLE DETECTED: " + result.cycleNodes()
                    : "NO CYCLE DETECTED");
            renderGraph(result.cycleNodes());
        } catch (IllegalArgumentException ex) {
            showError("Graph analysis error", ex.getMessage());
        }
    }

    private void renderGraph() { renderGraph(List.of()); }

    private void renderGraph(List<String> cycle) {
        graphPane.getChildren().clear();
        Map<String, double[]> positions = new LinkedHashMap<>();
        int processIndex = 0;
        int resourceIndex = 0;
        for (ResourceNode node : nodes) {
            double x = node.type() == ResourceNode.NodeType.PROCESS ? 100 : 350;
            double y = 55 + (node.type() == ResourceNode.NodeType.PROCESS ? processIndex++ : resourceIndex++) * 85;
            positions.put(node.id(), new double[]{x, y});
        }
        for (ResourceEdge edge : edges) {
            double[] from = positions.get(edge.from());
            double[] to = positions.get(edge.to());
            Line line = new Line(from[0], from[1], to[0], to[1]);
            line.setStroke(cycle.contains(edge.from()) && cycle.contains(edge.to()) ? Color.web("#ef6b86") : Color.web("#7892bc"));
            line.setStrokeWidth(2);
            graphPane.getChildren().add(line);
            graphPane.getChildren().add(arrowHead(from[0], from[1], to[0], to[1], line.getStroke()));
        }
        for (ResourceNode node : nodes) {
            double[] position = positions.get(node.id());
            Label label = new Label(node.id());
            label.setLayoutX(position[0] - 25);
            label.setLayoutY(position[1] - 12);
            label.getStyleClass().add(node.type() == ResourceNode.NodeType.PROCESS
                    ? (cycle.contains(node.id()) ? "graph-cycle-process" : "graph-process")
                    : (cycle.contains(node.id()) ? "graph-cycle-resource" : "graph-resource"));
            graphPane.getChildren().add(label);
        }

    }

    private Polygon arrowHead(double fromX, double fromY, double toX, double toY,
                              javafx.scene.paint.Paint color) {
        double angle = Math.atan2(toY - fromY, toX - fromX);
        double length = 10;
        double spread = Math.PI / 7;
        double leftX = toX - length * Math.cos(angle - spread);
        double leftY = toY - length * Math.sin(angle - spread);
        double rightX = toX - length * Math.cos(angle + spread);
        double rightY = toY - length * Math.sin(angle + spread);
        Polygon arrow = new Polygon(toX, toY, leftX, leftY, rightX, rightY);
        arrow.setFill(color);
        return arrow;
    }

    private VBox educationView() {
        VBox view = UiComponents.card("education-card");
        Label title = new Label("Understanding deadlocks");
        title.getStyleClass().add("section-title");
        view.getChildren().addAll(title,
                explanation("Mutual Exclusion", "At least one resource is held in a non-shareable mode."),
                explanation("Hold and Wait", "A process holds resources while waiting for additional resources."),
                explanation("No Preemption", "Resources cannot be forcibly taken from a process."),
                explanation("Circular Wait", "Processes form a cycle where each waits for a resource held by the next."),
                explanation("Deadlock Prevention", "Break at least one of the four necessary conditions."),
                explanation("Deadlock Avoidance", "Grant requests only when the system remains in a safe state, as in Banker's Algorithm."),
                explanation("Detection and Recovery", "Allow deadlocks, detect them, then release resources, roll back, or terminate processes."),
                explanation("Resource graph note", "A cycle is sufficient for deadlock with single-instance resources. With multiple instances, a cycle alone does not necessarily establish deadlock."));
        return new VBox(view);
    }

    private Label explanation(String heading, String text) {
        Label label = new Label(heading + ": " + text);
        label.setWrapText(true);
        label.getStyleClass().add("education-line");
        return label;
    }

    private List<Integer> vector(String text) {
        try {
            return java.util.Arrays.stream(text.trim().split("[,\\s]+")).map(Integer::parseInt).toList();
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Vectors must contain whole numbers");
        }
    }

    private List<List<Integer>> matrix(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Matrix is required");
        try {
            return java.util.Arrays.stream(text.trim().split(";"))
                    .map(this::vector).toList();
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Matrices use ';' between rows and spaces between values");
        }
    }

    private TextArea area(String text) {
        TextArea area = new TextArea(text);
        area.setPrefRowCount(2);
        area.setWrapText(true);
        return area;
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

    public record ProgressRow(int step, String work, String finish) {}
}
