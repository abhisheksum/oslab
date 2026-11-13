package com.oslab.controller;

import com.oslab.model.SimulationActivity;
import com.oslab.model.SimulationSummary;
import com.oslab.service.DashboardService;
import com.oslab.repository.InMemorySimulationRepository;
import com.oslab.utils.FormatUtils;
import com.oslab.view.UiComponents;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class DashboardController {
    private final DashboardService service;

    public DashboardController() {
        this(new DashboardService(new InMemorySimulationRepository()));
    }

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setLeft(createSidebar());
        root.setCenter(createContent());
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        return scene;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(28, 16, 22, 16));
        Label brand = new Label("OS LAB");
        brand.getStyleClass().add("brand");
        Label tagline = new Label("INTERACTIVE SIMULATOR");
        tagline.getStyleClass().add("tagline");
        VBox brandBox = new VBox(4, brand, tagline);
        brandBox.setPadding(new Insets(0, 12, 30, 12));
        sidebar.getChildren().add(brandBox);

        String[] items = {"Dashboard", "CPU Scheduling", "Memory Management",
                "Deadlock Detection", "Process Synchronization", "Performance Analytics"};
        for (int i = 0; i < items.length; i++) {
            Button item = navButton(items[i], i == 0);
            if ("CPU Scheduling".equals(items[i])) {
                item.setOnAction(event -> ((javafx.stage.Stage) item.getScene().getWindow())
                        .setScene(new SchedulingController().createScene()));
            }
            if ("Memory Management".equals(items[i])) {
                item.setOnAction(event -> ((javafx.stage.Stage) item.getScene().getWindow())
                        .setScene(new MemoryController().createScene()));
            }
            if ("Deadlock Detection".equals(items[i])) {
                item.setOnAction(event -> ((javafx.stage.Stage) item.getScene().getWindow())
                        .setScene(new DeadlockController().createScene()));
            }
            if ("Process Synchronization".equals(items[i])) {
                item.setOnAction(event -> ((javafx.stage.Stage) item.getScene().getWindow())
                        .setScene(new SynchronizationController().createScene()));
            }
            if ("Performance Analytics".equals(items[i])) {
                item.setOnAction(event -> ((javafx.stage.Stage) item.getScene().getWindow())
                        .setScene(new AnalyticsController().createScene()));
            }
            sidebar.getChildren().add(item);
        }
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(spacer, navButton("Settings", false));
        return sidebar;
    }

    private Button navButton(String text, boolean selected) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("nav-button");
        if (selected) {
            button.getStyleClass().add("nav-selected");
        }
        return button;
    }

    private ScrollPane createContent() {
        VBox content = new VBox(26);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(34, 42, 42, 42));
        content.getChildren().addAll(createTopBar(), createWelcome(), createMetrics(), createLowerContent());
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-scroll");
        return scrollPane;
    }

    private HBox createTopBar() {
        Label breadcrumb = new Label("OVERVIEW  /  DASHBOARD");
        breadcrumb.getStyleClass().add("breadcrumb");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label user = new Label("Student Workspace");
        user.getStyleClass().add("user-label");
        HBox bar = new HBox(16, breadcrumb, spacer, user);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private VBox createWelcome() {
        Label title = new Label("Good morning, Alex");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Explore operating system concepts through interactive simulations.");
        subtitle.getStyleClass().add("page-subtitle");
        return new VBox(7, title, subtitle);
    }

    private GridPane createMetrics() {
        SimulationSummary summary = service.getSummary();
        GridPane metrics = new GridPane();
        metrics.setHgap(16);
        metrics.setVgap(16);
        metrics.getColumnConstraints().addAll(
                columnConstraint(), columnConstraint(), columnConstraint());
        metrics.add(metricCard("TOTAL SIMULATIONS", String.valueOf(summary.totalSimulations()),
                "↑ 18% from last month", "metric-blue"), 0, 0);
        metrics.add(metricCard("TOTAL PROCESSES", String.valueOf(summary.totalProcesses()),
                "Across all simulations", "metric-purple"), 1, 0);
        metrics.add(metricCard("AVAILABLE ALGORITHMS", String.valueOf(summary.availableAlgorithms()),
                "Across 4 categories", "metric-cyan"), 2, 0);
        return metrics;
    }

    private javafx.scene.layout.ColumnConstraints columnConstraint() {
        javafx.scene.layout.ColumnConstraints constraints = new javafx.scene.layout.ColumnConstraints();
        constraints.setPercentWidth(33.33);
        return constraints;
    }

    private VBox metricCard(String label, String value, String detail, String accent) {
        VBox card = UiComponents.card(accent);
        card.setPadding(new Insets(20, 22, 18, 22));
        Label labelView = new Label(label);
        labelView.getStyleClass().add("metric-label");
        Label valueView = new Label(value);
        valueView.getStyleClass().add("metric-value");
        Label detailView = new Label(detail);
        detailView.getStyleClass().add("metric-detail");
        card.getChildren().addAll(labelView, valueView, detailView);
        return card;
    }

    private GridPane createLowerContent() {
        GridPane lower = new GridPane();
        lower.setHgap(18);
        lower.setVgap(18);
        javafx.scene.layout.ColumnConstraints main = new javafx.scene.layout.ColumnConstraints();
        main.setPercentWidth(62);
        javafx.scene.layout.ColumnConstraints side = new javafx.scene.layout.ColumnConstraints();
        side.setPercentWidth(38);
        lower.getColumnConstraints().addAll(main, side);
        lower.add(createActivityCard(), 0, 0);
        lower.add(createQuickStartCard(), 1, 0);
        return lower;
    }

    private VBox createActivityCard() {
        VBox card = UiComponents.card("activity-card");
        HBox heading = UiComponents.sectionHeading("Recent simulation activity", "Your latest experiments");
        VBox rows = new VBox();
        List<SimulationActivity> activities = service.getRecentActivity();
        rows.getChildren().addAll(FXCollections.observableArrayList(activities).stream()
                .map(this::activityRow).toList());
        card.getChildren().addAll(heading, rows);
        return card;
    }

    private HBox activityRow(SimulationActivity activity) {
        Label name = new Label(activity.name());
        name.getStyleClass().add("activity-name");
        Label category = new Label(activity.category());
        category.getStyleClass().add("muted");
        VBox details = new VBox(4, name, category);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(FormatUtils.formatDateTime(activity.completedAt()));
        date.getStyleClass().add("activity-date");
        HBox row = new HBox(12, details, spacer, UiComponents.statusBadge(activity.status(),
                "Completed".equals(activity.status())), date);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("activity-row");
        return row;
    }

    private VBox createQuickStartCard() {
        VBox card = UiComponents.card("quick-card");
        Label title = new Label("Start a simulation");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Choose a topic to begin exploring.");
        subtitle.getStyleClass().add("muted");
        card.getChildren().addAll(title, subtitle);
        String[][] options = {
                {"CPU Scheduling", "Compare scheduling algorithms", "→"},
                {"Memory Management", "Visualize allocation strategies", "→"},
                {"Deadlock Detection", "Analyze safe and unsafe states", "→"}
        };
        for (String[] option : options) {
            Button button = new Button(option[0] + "\n" + option[1] + "                         " + option[2]);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.getStyleClass().add("quick-option");
            card.getChildren().add(button);
        }
        return card;
    }
}
