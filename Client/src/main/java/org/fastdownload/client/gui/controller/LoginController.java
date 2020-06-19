package org.fastdownload.client.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.fastdownload.client.entity.User;
import org.fastdownload.client.service.CheckLogin;
import org.fastdownload.client.util.UserType;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane root;

    @FXML
    private VBox vBox;

    @FXML
    private TextField id;

    @FXML
    private PasswordField pwd;

    @FXML
    private HBox hBox3;

    @FXML
    private ChoiceBox<UserType> choice;

    @FXML
    private HBox hBox4;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnVLogin;

    @FXML
    void login(ActionEvent event) {
        CheckLogin checkLogin = new CheckLogin();
        User user = new User(id.getText(), pwd.getText(), choice.getValue().getCode());
        int i = checkLogin.handle(user);
        if (UserType.GENERAL.getCode() == i) {
            System.err.println("跳转一般用户界面");
        } else if (UserType.ADMINISTRATOR.getCode() == i) {
            System.err.println("跳转管理员界面");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.titleProperty().set("错误");
            alert.headerTextProperty().set("账号、密码或者类型错误");
            alert.showAndWait();
        }
    }

    @FXML
    void vLogin(ActionEvent event) {
        System.err.println("跳转游客界面");
    }


    @FXML
    void initialize() {
        vBox.prefWidthProperty().bind(root.widthProperty());
        vBox.prefHeightProperty().bind(root.heightProperty());
        ObservableList<UserType> userTypes = FXCollections.observableArrayList(
                UserType.ADMINISTRATOR,
                UserType.GENERAL
        );
        choice.setItems(userTypes);
        choice.getSelectionModel().select(UserType.GENERAL);
        choice.prefHeightProperty().bind(hBox3.heightProperty());

        System.out.println(choice.getValue());
        choice.setOnAction(event -> System.out.println(choice.getValue()));

    }
}
