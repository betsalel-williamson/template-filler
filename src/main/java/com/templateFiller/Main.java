package com.templateFiller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Copyright (c) 12/1/17 Betsalel Williamson
 */
public class Main extends Application {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader loader = new FXMLLoader();

        loader.setResources(ResourceBundle.getBundle("application"));

        Parent root = loader.load(
                ClassLoader.getSystemResourceAsStream("main.fxml")
        );

        Scene scene = new Scene(root);

        primaryStage.setTitle("FXML Welcome");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest(e ->
        {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();

        Main.logger.debug("Finished loading main.");
    }
}
