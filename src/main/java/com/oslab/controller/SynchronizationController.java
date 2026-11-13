package com.oslab.controller;

import com.oslab.algorithms.synchronization.DiningPhilosophersSimulation;
import com.oslab.algorithms.synchronization.ProducerConsumerSimulation;
import com.oslab.algorithms.synchronization.ReaderWriterSimulation;
import com.oslab.model.synchronization.BufferSnapshot;
import com.oslab.model.synchronization.PhilosopherSnapshot;
import com.oslab.model.synchronization.ReaderWriterSnapshot;
import com.oslab.model.synchronization.ThreadState;
import com.oslab.service.SynchronizationService;
import com.oslab.service.AnalyticsService;
import com.oslab.repository.SqliteAnalyticsRepository;
import com.oslab.view.UiComponents;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

public class SynchronizationController {
    private final SynchronizationService service;
    private final AnalyticsService analytics = new AnalyticsService(
            new SqliteAnalyticsRepository(Path.of("oslab.db")));
    private ProducerConsumerSimulation producerConsumer;
    private DiningPhilosophersSimulation philosophers;
    private ReaderWriterSimulation readersWriters;
    private final VBox bufferView = new VBox(8);
    private final VBox philosopherView = new VBox(8);
    private final VBox readerWriterView = new VBox(8);
    private final TextArea bufferLog = new TextArea();
    private final TextArea philosopherLog = new TextArea();
    private final TextArea readerWriterLog = new TextArea();

    public SynchronizationController() { this(new SynchronizationService()); }
    public SynchronizationController(SynchronizationService service) { this.service = service; }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(header());
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("memory-tabs");
        tabs.getTabs().addAll(tab("Producer / Consumer", producerConsumerView()),
                tab("Dining Philosophers", philosophersView()),
                tab("Reader / Writer", readerWriterView()),
                tab("Learn", educationView()));
        root.setCenter(tabs);
        Scene scene = new Scene(root, 1280, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        return scene;
    }

    private HBox header() {
        Button back = action("←  Dashboard");
        back.setOnAction(event -> ((javafx.stage.Stage) back.getScene().getWindow())
                .setScene(new DashboardController().createScene()));
        Label title = new Label("Process Synchronization Simulator");
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

    private VBox producerConsumerView() {
        TextField capacity = field("5");
        CheckBox synchronizedMode = new CheckBox("Synchronized mode (semaphores + mutex)");
        synchronizedMode.setSelected(true);
        Button start = action("Start");
        Button pause = action("Pause");
        Button resume = action("Resume");
        Button stop = action("Stop");
        Button reset = action("Reset");
        start.setOnAction(event -> {
            try {
                if (producerConsumer != null) producerConsumer.stop();
                producerConsumer = service.producerConsumer(Integer.parseInt(capacity.getText()), synchronizedMode.isSelected());
                producerConsumer.start(220, snapshot -> Platform.runLater(() -> renderBuffer(snapshot)));
            } catch (IllegalArgumentException ex) {
                showError("Invalid capacity", "Capacity must be a positive whole number");
            }
        });
        stop.setOnAction(event -> {
            if (producerConsumer != null) {
                BufferSnapshot snapshot = producerConsumer.snapshot();
                analytics.recordSynchronization("capacity=" + capacity.getText(),
                        synchronizedMode.isSelected() ? "Producer Consumer (synchronized)" : "Producer Consumer (unsynchronized)",
                        snapshot);
                producerConsumer.stop();
            }
        });
        pause.setOnAction(event -> { if (producerConsumer != null) producerConsumer.pause(); });
        resume.setOnAction(event -> { if (producerConsumer != null) producerConsumer.resume(); });
        reset.setOnAction(event -> { if (producerConsumer != null) producerConsumer.reset(); bufferView.getChildren().clear(); bufferLog.clear(); });
        HBox controls = new HBox(10, new Label("Capacity"), capacity, synchronizedMode, start, pause, resume, stop, reset);
        controls.setAlignment(Pos.CENTER_LEFT);
        bufferLog.setEditable(false);
        bufferLog.setPrefRowCount(6);
        bufferView.setMinHeight(100);
        VBox card = UiComponents.card("sync-card");
        card.getChildren().addAll(new Label("Bounded buffer"), controls, bufferView, bufferLog);
        return new VBox(card, explanation("Synchronized execution uses empty/full semaphores and a mutex. Unsynchronized execution intentionally allows the producer and consumer to race over shared buffer state."));
    }

    private void renderBuffer(BufferSnapshot snapshot) {
        bufferView.getChildren().clear();
        HBox slots = new HBox(6);
        for (int i = 0; i < snapshot.capacity(); i++) {
            Label slot = new Label(i < snapshot.items().size() ? String.valueOf(snapshot.items().get(i)) : "empty");
            slot.getStyleClass().add(i < snapshot.items().size() ? "sync-full" : "sync-empty");
            slots.getChildren().add(slot);
        }
        bufferView.getChildren().addAll(slots,
                new Label("Producer: " + snapshot.producerState() + "   Consumer: " + snapshot.consumerState()
                        + "   empty=" + snapshot.emptySemaphore() + " full=" + snapshot.fullSemaphore()));
        bufferLog.setText(String.join("\n", snapshot.operations()));
        bufferLog.positionCaret(bufferLog.getText().length());
    }

    private VBox philosophersView() {
        TextField speed = field("250");
        CheckBox deadlockFree = new CheckBox("Deadlock-free resource ordering");
        deadlockFree.setSelected(true);
        Button start = action("Start");
        Button pause = action("Pause");
        Button resume = action("Resume");
        Button stop = action("Stop");
        Button reset = action("Reset");
        start.setOnAction(event -> {
            try {
                if (philosophers != null) philosophers.stop();
                philosophers = service.philosophers(deadlockFree.isSelected());
                philosophers.start(parseSpeed(speed), snapshot -> Platform.runLater(() -> renderPhilosophers(snapshot)));
            } catch (IllegalArgumentException ex) {
                showError("Invalid speed", ex.getMessage());
            }
        });
        stop.setOnAction(event -> {
            if (philosophers != null) {
                analytics.recordPhilosophers("deadlockFree=" + deadlockFree.isSelected(),
                        philosophers.snapshot());
                philosophers.stop();
            }
        });
        pause.setOnAction(event -> { if (philosophers != null) philosophers.pause(); });
        resume.setOnAction(event -> { if (philosophers != null) philosophers.resume(); });
        reset.setOnAction(event -> { if (philosophers != null) philosophers.stop(); philosopherView.getChildren().clear(); philosopherLog.clear(); });
        HBox controls = new HBox(10, new Label("Delay ms"), speed, deadlockFree, start, pause, resume, stop, reset);
        controls.setAlignment(Pos.CENTER_LEFT);
        philosopherLog.setEditable(false);
        philosopherLog.setPrefRowCount(6);
        philosopherView.setMinHeight(190);
        VBox card = UiComponents.card("sync-card");
        card.getChildren().addAll(new Label("Five philosophers / five forks"), controls, philosopherView, philosopherLog);
        return new VBox(card, explanation("Naive mode acquires left then right forks and can deadlock. The prevention mode orders fork acquisition, preventing circular wait. A philosopher may still wait, which illustrates starvation risk."));
    }

    private void renderPhilosophers(PhilosopherSnapshot snapshot) {
        philosopherView.getChildren().clear();
        StackPane table = new StackPane();
        table.setPrefSize(760, 360);
        Pane layer = new Pane();
        Circle tableTop = new Circle(110);
        tableTop.getStyleClass().add("philosopher-table");
        tableTop.setCenterX(380);
        tableTop.setCenterY(180);
        layer.getChildren().add(tableTop);
        for (int i = 0; i < snapshot.states().size(); i++) {
            Label philosopher = new Label("P" + i + "\n" + snapshot.states().get(i));
            philosopher.setAlignment(Pos.CENTER);
            philosopher.setPrefSize(108, 54);
            philosopher.getStyleClass().add("philosopher-" + snapshot.states().get(i).name().toLowerCase());
            double angle = -Math.PI / 2 + i * (2 * Math.PI / 5);
            philosopher.setLayoutX(380 + Math.cos(angle) * 145 - 54);
            philosopher.setLayoutY(180 + Math.sin(angle) * 125 - 27);
            layer.getChildren().add(philosopher);
        }
        for (int i = 0; i < snapshot.forkOwners().size(); i++) {
            Rectangle fork = new Rectangle(18, 38);
            fork.getStyleClass().add(snapshot.forkOwners().get(i) >= 0
                    ? "fork-owned" : "fork-free");
            double angle = -Math.PI / 2 + (i + 0.5) * (2 * Math.PI / 5);
            fork.setLayoutX(380 + Math.cos(angle) * 92 - 9);
            fork.setLayoutY(180 + Math.sin(angle) * 92 - 19);
            layer.getChildren().add(fork);
        }
        Label center = new Label("Shared table\n5 forks");
        center.setAlignment(Pos.CENTER);
        center.getStyleClass().add("philosopher-center");
        center.setLayoutX(340);
        center.setLayoutY(155);
        layer.getChildren().add(center);
        table.getChildren().add(layer);
        philosopherView.getChildren().addAll(table,
                new Label("Fork ownership: " + snapshot.forkOwners()));
        philosopherLog.setText(String.join("\n", snapshot.operations()));
    }

    private VBox readerWriterView() {
        TextField readers = field("3");
        TextField writers = field("2");
        TextField speed = field("250");
        CheckBox writerPreference = new CheckBox("Writer preference");
        Button start = action("Start");
        Button pause = action("Pause");
        Button resume = action("Resume");
        Button stop = action("Stop");
        Button reset = action("Reset");
        start.setOnAction(event -> {
            try {
                if (readersWriters != null) readersWriters.stop();
                readersWriters = service.readersWriters(Integer.parseInt(readers.getText()),
                        Integer.parseInt(writers.getText()), writerPreference.isSelected());
                readersWriters.start(parseSpeed(speed), snapshot -> Platform.runLater(() -> renderReaderWriters(snapshot)));
            } catch (IllegalArgumentException ex) {
                showError("Invalid simulation input", ex.getMessage());
            }
        });
        stop.setOnAction(event -> {
            if (readersWriters != null) {
                analytics.recordReadersWriters("readers=" + readers.getText()
                        + ",writers=" + writers.getText(), readersWriters.snapshot());
                readersWriters.stop();
            }
        });
        pause.setOnAction(event -> { if (readersWriters != null) readersWriters.pause(); });
        resume.setOnAction(event -> { if (readersWriters != null) readersWriters.resume(); });
        reset.setOnAction(event -> { if (readersWriters != null) readersWriters.reset(); readerWriterView.getChildren().clear(); readerWriterLog.clear(); });
        HBox controls = new HBox(10, new Label("Readers"), readers, new Label("Writers"), writers,
                new Label("Delay ms"), speed, writerPreference, start, pause, resume, stop, reset);
        controls.setAlignment(Pos.CENTER_LEFT);
        readerWriterLog.setEditable(false);
        readerWriterLog.setPrefRowCount(6);
        readerWriterView.setMinHeight(160);
        VBox card = UiComponents.card("sync-card");
        card.getChildren().addAll(new Label("Shared resource"), controls, readerWriterView, readerWriterLog);
        return new VBox(card, explanation("Readers share the read lock, while writers require exclusive access. Writer preference uses a fair read-write lock to reduce writer starvation."));
    }

    private void renderReaderWriters(ReaderWriterSnapshot snapshot) {
        readerWriterView.getChildren().clear();
        readerWriterView.getChildren().addAll(
                new Label("Active readers: " + snapshot.activeReaders() + "   Current writer: "
                        + snapshot.currentWriter() + "   Shared value: " + snapshot.sharedValue()),
                new Label("Readers: " + snapshot.readerStates()),
                new Label("Writers: " + snapshot.writerStates()));
        readerWriterLog.setText(String.join("\n", snapshot.operations()));
    }

    private VBox educationView() {
        VBox card = UiComponents.card("education-card");
        Label title = new Label("Concurrency concepts");
        title.getStyleClass().add("section-title");
        card.getChildren().addAll(title,
                explanation("Process vs Thread", "A process owns an address space; threads are independent execution paths within a process."),
                explanation("Critical Section", "The code region that accesses shared state and must be protected."),
                explanation("Race Condition", "The result depends on timing because concurrent operations interleave unpredictably."),
                explanation("Mutex", "A mutual-exclusion lock permits one thread at a time into a critical section."),
                explanation("Semaphore", "A counter-based synchronization primitive controls access to a limited number of permits."),
                explanation("Monitor", "A higher-level object that combines mutual exclusion with condition-based waiting."),
                explanation("Deadlock", "Threads wait forever for resources held by one another."),
                explanation("Starvation", "A thread waits indefinitely because other threads repeatedly receive access."),
                explanation("Mutual Exclusion", "Only one thread may modify a protected shared resource at a time."),
                explanation("Real-world examples", "Bounded buffers model queues, readers/writers model databases, and dining philosophers model competing resource acquisition."));
        return new VBox(card);
    }

    private Label explanation(String heading, String text) {
        Label label = new Label(heading + ": " + text);
        label.setWrapText(true);
        label.getStyleClass().add("education-line");
        return label;
    }

    private Label explanation(String text) {
        return explanation("Concept", text);
    }

    private TextField field(String value) {
        TextField field = new TextField(value);
        field.setPrefWidth(85);
        return field;
    }

    private long parseSpeed(TextField field) {
        try {
            long speed = Long.parseLong(field.getText());
            if (speed <= 0) throw new IllegalArgumentException("Speed must be positive");
            return Math.max(10, speed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Speed must be a whole number", ex);
        }
    }

    private Button action(String text) {
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
