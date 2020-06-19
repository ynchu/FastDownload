/*
 * Sample Skeleton for 'MainWindow.fxml' Controller Class
 */


package org.fastdownload.client.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fastdownload.client.FastDownloadClientApplication;
import org.fastdownload.client.entity.FileTable;
import org.fastdownload.client.udp.DownloadClientThread;

import java.io.IOException;

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
    private TableColumn<FileTable, String> fileNameList;

    @FXML
    private TableColumn<FileTable, Long> fileSizeList;

    @FXML
    private TableColumn<FileTable, String> fileStateList;

    @FXML
    private TableColumn<FileTable, String> connectTimeList;


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
    void getHelp(ActionEvent event) {
        System.out.println("获取帮助");
    }

    @FXML
    void newDownload(ActionEvent event) throws IOException {
        System.out.println("新建下载");

        Stage stage = new Stage();
//        stage.initOwner(Client.stage);
//        stage.initModality(Modality.APPLICATION_MODAL);

//        URL location = getClass().getResource("/gui/client/DownloadWindow.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/client/DownloadWindow.fxml"));
//        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();

//        Parent root = FXMLLoader.load(getClass().getResource("/gui/client/DownloadWindow.fxml"));
        stage.setTitle("新建下载");
        stage.setScene(new Scene(root, 600, 400));
        stage.setResizable(false);
        stage.show();

//        DownloadWindowController controller = (DownloadWindowController) FastDownloadClientApplication.controllers.get(DownloadWindowController.class.getName());
        // 获取Controller的实例对象
        DownloadWindowController controller = fxmlLoader.getController();
        stage.setOnCloseRequest(e -> {
            System.out.println("sgfh");
            ((DownloadClientThread) controller.getThread()).close();
        });
    }

    @FXML
    void initialize() {
//        Client.controllers.put(this.getClass().getName(), this);
        FastDownloadClientApplication.controllers.put(this.getClass().getName(), this);

        // 宽度绑定
        vBox.prefWidthProperty().bind(root.widthProperty());
        vBox.prefHeightProperty().bind(root.heightProperty());

        menuBar.prefWidthProperty().bind(vBox.widthProperty());

        contentPane.prefWidthProperty().bind(vBox.widthProperty());
        contentPane.prefHeightProperty().bind(vBox.heightProperty().subtract(menuBar.heightProperty()));

        fileTable.prefWidthProperty().bind(contentPane.widthProperty());
        fileTable.prefHeightProperty().bind(contentPane.heightProperty());
    }
}
