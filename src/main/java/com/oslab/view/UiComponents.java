package com.oslab.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public final class UiComponents {
    private UiComponents() {
    }

    public static VBox card(String styleClass) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("card", styleClass);
        return card;
    }

    public static HBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("muted");
        HBox heading = new HBox(14, titleLabel, subtitleLabel);
        heading.setAlignment(Pos.BASELINE_LEFT);
        return heading;
    }

    public static HBox statusBadge(String text, boolean active) {
        Circle dot = new Circle(4, active ? Color.web("#38d39f") : Color.web("#f5b657"));
        Label label = new Label(text);
        label.getStyleClass().add(active ? "status-success" : "status-progress");
        HBox badge = new HBox(7, dot, label);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(4, 9, 4, 9));
        badge.getStyleClass().add("status-badge");
        return badge;
    }
}
