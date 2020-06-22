package org.fastdownload.client.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.fastdownload.client.FastDownloadClientApplication;
import org.fastdownload.client.udp.DownloadClientThread;
import org.fastdownload.client.utils.DataPackage;

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

    private Thread thread = null;

    private ObservableList<String> list = FXCollections.observableArrayList();

    @FXML
    void download(ActionEvent event) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.titleProperty().set("信息");
//        alert.headerTextProperty().set("开始下载");
//        alert.showAndWait();

        hBox1.getChildren().clear();
        message.setText("正在下载中，请等待...");
        message.setFont(new Font(20));
        hBox1.getChildren().addAll(message);
        hBox2.setVisible(true);

        thread = new DownloadClientThread(input.getText(), this);
        thread.start();


    }

    @FXML
    void initialize() {
//        Client.controllers.put(this.getClass().getName(), this);
        FastDownloadClientApplication.controllers.put(this.getClass().getName(), this);

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

    public void update(double c, double total, String dataLength) {
        getProgressBar().setProgress(c / total);
        getProgressIndicator().setProgress(c / total);
        if (c > 30) {
            getList().remove(0);
        }
        getList().add("已下载第 " + c + " 块数据块, 该数据块长度为 " + dataLength + " 位, 总共 " + total + " 个数据块, 已完成 " + String.format("%.2f", c / total * 100) + "%.");
        getListView().scrollTo(getList().size());
    }

    public AnchorPane getRoot() {
        return root;
    }

    public VBox getvBox() {
        return vBox;
    }

    public HBox gethBox1() {
        return hBox1;
    }

    public TextField getInput() {
        return input;
    }

    public HBox gethBox2() {
        return hBox2;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public HBox gethBox3() {
        return hBox3;
    }

    public ListView<String> getListView() {
        return listView;
    }

    public Label getMessage() {
        return message;
    }

    public Thread getThread() {
        return thread;
    }

    public ObservableList<String> getList() {
        return list;
    }
}
