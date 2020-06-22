package org.fastdownload.client.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.fastdownload.client.Client;
import org.fastdownload.client.service.DownloadFileService;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载页面控制类
 *
 * @author Administrator
 */
public class DownloadWindowController {

    @FXML
    private AnchorPane root;

    @FXML
    private VBox vBox;

    @FXML
    private HBox hBox1;

    @FXML
    private TextField input;

    @FXML
    private HBox hBox2;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private HBox hBox3;

    @FXML
    private ListView<String> listView;

    private Label message = new Label();

    private ObservableList<String> list = FXCollections.observableArrayList();

    private final static int MAX_DOWNLOAD_COUNT = 2;
    private static List<DownloadFileService> downloadList = new ArrayList<>();

    @FXML
    void download(ActionEvent event) {
        // 计算当前下载数
        for (int i = 0; i < downloadList.size(); i++) {
            if (downloadList.get(i).isRun()) {
                downloadList.remove(i);
                i--;
            }
        }

        // 判断是否超出最大下载数
        if (downloadList.size() < MAX_DOWNLOAD_COUNT) {
            DownloadFileService server = new DownloadFileService(input.getText().toString(), this);
            downloadList.add(server);
            server.start();

            hBox1.getChildren().clear();
            message.setText("正在下载中，请等待...");
            message.setFont(new Font(20));
            hBox1.getChildren().addAll(message);
            hBox2.setVisible(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.titleProperty().set("信息");
            alert.headerTextProperty().set("超出最大下载数目");
            alert.showAndWait();
        }
    }

    @FXML
    void initialize() {
        Client.controllers.put(this.getClass().getName(), this);

        // 绑定长宽
        vBox.prefWidthProperty().bind(root.widthProperty());
        vBox.prefHeightProperty().bind(root.heightProperty());
        hBox1.prefWidthProperty().bind(vBox.widthProperty());
        hBox2.prefWidthProperty().bind(vBox.widthProperty());
        hBox3.prefWidthProperty().bind(vBox.widthProperty());
        hBox3.prefHeightProperty().bind(vBox.heightProperty().subtract(hBox1.heightProperty()).subtract(hBox2.heightProperty()).subtract(20));
        listView.prefWidthProperty().bind(hBox3.widthProperty().subtract(50));
        listView.prefHeightProperty().bind(hBox3.heightProperty());

        listView.setItems(list);
    }

    public void update(double progress, String msg) {
        this.progressBar.setProgress(progress);
        this.progressIndicator.setProgress(progress);
        if (this.list.size() > 30) {
            this.list.remove(0);
        }
        this.list.add(msg);
        this.listView.scrollTo(this.list.size());
    }
}
