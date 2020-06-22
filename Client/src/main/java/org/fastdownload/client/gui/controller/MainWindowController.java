/*
 * Sample Skeleton for 'MainWindow.fxml' Controller Class
 */


package org.fastdownload.client.gui.controller;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.fastdownload.client.Client;
import org.fastdownload.client.entity.FileTable;
import org.fastdownload.client.entity.FileTableEntity;
import org.fastdownload.client.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainWindowController {
    @FXML
    private AnchorPane root;

    @FXML
    private VBox vBox;

    @FXML
    private MenuBar menuBar;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private TableView<FileTable> fileTable;

    @FXML
    private TableColumn<String, Number> no;

    @FXML
    private TableColumn<FileTable, String> fileNameList;

    @FXML
    private TableColumn<FileTable, Number> fileSizeList;

    @FXML
    private TableColumn<FileTable, String> fileStateList;

    @FXML
    private TableColumn<FileTable, String> connectTimeList;

    @Getter
    private ObservableList<FileTable> data = FXCollections.observableArrayList();

    private final static Object lock = new Object();


    @FXML
    void aboutThis(ActionEvent event) {
        System.out.println("关于软件");
    }

    @FXML
    void exit(ActionEvent event) {
        System.out.println("退出");
        Platform.exit();
    }

    @FXML
    void upload(ActionEvent event) {
        System.err.println("上传文件");

        Stage stage = new Stage();
        stage.initOwner(Client.stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/fastdownload/client/gui/view/UploadWindow.fxml"));
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.setTitle("上传文件");
        assert root != null;
        stage.setScene(new Scene(root, 600, 400));
        stage.setResizable(false);
        stage.show();

        stage.setOnCloseRequest(e -> System.out.println("上传文件窗口关闭"));
    }

    @FXML
    void getHelp(ActionEvent event) {
        System.out.println("获取帮助");
    }

    @FXML
    void newDownload(ActionEvent event) {
        System.out.println("新建下载");

        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/fastdownload/client/gui/view/DownloadWindow.fxml"));
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.setTitle("新建下载");
        assert root != null;
        stage.setScene(new Scene(root, 600, 400));
        stage.setResizable(false);
        stage.show();

        stage.setOnCloseRequest(e -> System.out.println("下载窗口关闭"));
    }

    @FXML
    void initialize() {
        Client.controllers.put(this.getClass().getName(), this);

        // 宽度绑定
        vBox.prefWidthProperty().bind(root.widthProperty());
        vBox.prefHeightProperty().bind(root.heightProperty());

        menuBar.prefWidthProperty().bind(vBox.widthProperty());

        contentPane.prefWidthProperty().bind(vBox.widthProperty());
        contentPane.prefHeightProperty().bind(vBox.heightProperty().subtract(menuBar.heightProperty()));

        fileTable.prefWidthProperty().bind(contentPane.widthProperty());
        fileTable.prefHeightProperty().bind(contentPane.heightProperty());

        // 设置表格数据
        fileNameList.setCellValueFactory(param -> param.getValue().getFileName());
        fileSizeList.setCellValueFactory(param -> param.getValue().getFileSize());
        fileStateList.setCellValueFactory(param -> param.getValue().getFileState());
        connectTimeList.setCellValueFactory(param -> param.getValue().getConnectTime());

        no.setCellFactory(new Callback<TableColumn<String, Number>, TableCell<String, Number>>() {
            @Override
            public TableCell<String, Number> call(TableColumn<String, Number> param) {
                return new TableCell<String, Number>() {
                    @Override
                    protected void updateItem(Number item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            int rowIndex = this.getIndex() + 1;
                            this.setText("" + rowIndex);
                        }
                    }
                };
            }
        });

        try {
            // 读取下载历史
            String json = FileUtils.readFileToString(new File("docs/record.json"), "utf-8");
            if (json != null) {
                List<FileTableEntity> list = JsonUtils.toObject(json, new TypeToken<List<FileTableEntity>>() {
                }.getType());
                list.forEach(e -> data.add(new FileTable(e)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileTable.setItems(data);
    }

    public synchronized int addData(FileTable data) {
        synchronized (this) {
            this.data.add(data);
            return this.data.size() - 1;
        }
    }

    public synchronized void update(int fileTableIndex, FileTable fileTable) {
        this.data.get(fileTableIndex).setFileName(fileTable.getFileName().getValue());
        this.data.get(fileTableIndex).setFileSize(fileTable.getFileSize().getValue());
        this.data.get(fileTableIndex).setFileState(fileTable.getFileState().getValue());
        this.data.get(fileTableIndex).setConnectTime(fileTable.getConnectTime().getValue());
    }
}
