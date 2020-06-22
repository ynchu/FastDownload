package org.fastdownload.client.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.fastdownload.client.service.UploadFileService;

import java.io.File;
import java.util.List;

/**
 * 上传主窗口
 *
 * @author Administrator
 */
public class UploadWindowController {
    @FXML
    private AnchorPane root;

    @FXML
    private VBox vBox1;

    @FXML
    private VBox selectNode;

    @FXML
    private VBox vBox2;

    @FXML
    private Label msg;

    @FXML
    private HBox hBox1;

    @FXML
    private Button btUpload;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ProgressIndicator progressIndicator;


    String fileName;

    @FXML
    void upload(ActionEvent event) {
        hBox1.setVisible(true);
        btUpload.setVisible(false);

        UploadFileService thread = new UploadFileService(fileName, this);
        thread.start();
    }

    @FXML
    void initialize() {
        // 长度绑定
        vBox1.prefWidthProperty().bind(root.widthProperty());
        selectNode.prefWidthProperty().bind(vBox1.widthProperty());
        vBox2.prefWidthProperty().bind(vBox1.widthProperty());
        hBox1.prefWidthProperty().bind(vBox1.widthProperty());

        // 拖拽事件
        selectNode.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        selectNode.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                success = true;
                File selectedFile = files.get(0);
                msg.setText("您选择了 " + selectedFile.getAbsolutePath() + " 文件，点击上传按钮上传");
                fileName = selectedFile.getAbsolutePath();
                vBox2.setVisible(true);
            } else {
                vBox2.setVisible(false);
                hBox1.setVisible(false);
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // 点击选择文件
        selectNode.setOnMouseClicked(event -> {
            Window mainStage = new Stage();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择上传文件");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("所有文件", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(mainStage);
            if (selectedFile != null) {
                msg.setText("您选择了 " + selectedFile.getAbsolutePath() + " 文件，点击上传按钮上传");
                fileName = selectedFile.getAbsolutePath();
                vBox2.setVisible(true);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.titleProperty().set("信息");
                alert.headerTextProperty().set("您没有选择文件");
                alert.showAndWait();
                msg.setText("您没有选择文件");
                vBox2.setVisible(false);
                hBox1.setVisible(false);
            }
        });
    }

    public void update(double progress) {
        this.progressBar.setProgress(progress);
        this.progressIndicator.setProgress(progress);
    }
}
