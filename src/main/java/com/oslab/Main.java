package com.oslab;

import javafx.application.Application;
import com.oslab.controller.DashboardController;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        DashboardController controller = new DashboardController();
        stage.setScene(controller.createScene());
        stage.setMinWidth(1050);
        stage.setMinHeight(700);
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
