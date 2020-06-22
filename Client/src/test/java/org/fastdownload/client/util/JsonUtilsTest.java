package org.fastdownload.client.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.fastdownload.client.entity.FileTable;
import org.fastdownload.client.entity.User;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

class JsonUtilsTest {

    @Test
    void toJson() {
        User user = new User("123456", "123456", 1);
        String s = JsonUtils.toJson(user);
        System.out.println(s);
    }

    @Test
    void toObject() {
//        User user = new User("123456", "123456", 1);
//        String s = JsonUtils.toJson(user);
//
//        User object = JsonUtils.toObject(s, User.class);
//        System.out.println(object);


        try {
            ObservableList<FileTable> data = FXCollections.observableArrayList();

            data.add(new FileTable("13141", 231453, "345", "341543"));
            data.add(new FileTable("adfg", 231453, "345", "341543"));
            data.add(new FileTable("adfg", 231453, "345", "341543"));
            data.add(new FileTable("xcger", 231453, "345", "341543"));
            data.add(new FileTable("131erywr41", 231453, "345", "341543"));

            // 写
            FileUtils.writeStringToFile(new File("docs/1.json"), JsonUtils.toJson(data), "utf-8");

            // 读
            String json = FileUtils.readFileToString(new File("docs/1.json"), "utf-8");
            System.out.println(json);

            Gson gson = new Gson();
            List<FileTable> list = gson.fromJson(json, new TypeToken<List<FileTable>>() {
            }.getType());

            list.forEach(e -> {
                System.out.println(e.getFileName() + ", " + e.getFileSize() + ", " + e.getFileState() + ", " + e.getConnectTime());
            });

            ObservableList<FileTable> d = FXCollections.observableArrayList(list);
            d.forEach(e -> {
                System.out.println(e.getFileName() + ", " + e.getFileSize() + ", " + e.getFileState() + ", " + e.getConnectTime());
            });


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}