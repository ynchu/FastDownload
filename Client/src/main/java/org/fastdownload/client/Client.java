package org.fastdownload.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Client extends Application {
    /**
     * 控制器类MAP
     */
    public static Map<String, Object> controllers = new HashMap<>();

    /**
     * 主舞台
     */
    public static Stage stage;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/org/fastdownload/client/gui/view/Login.fxml"));
        stage.setTitle("17201127-易天明");
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        stage.setOnCloseRequest(event -> System.exit(0));
    }

    public static void setScene(Scene scene) {
        stage.setScene(scene);
    }
}
