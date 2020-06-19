package org.fastdownload.client.gui.model;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientStart extends Application {
    /**
     * 主窗口
     */
    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/org/fastdownload/client/gui/view/Login.fxml"));
        stage.setTitle("17201127-易天明");
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> System.exit(0));
    }

}
