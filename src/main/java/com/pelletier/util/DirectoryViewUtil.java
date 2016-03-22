package com.pelletier.util;

import com.pelletier.components.DirectoryView;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ryanb on 3/14/2016.
 */
public class DirectoryViewUtil {

    String path = "C:/";

    public DirectoryView<String> getDirectoryView(){

        DirectoryView<String> directoryView = new DirectoryView<>(path);
        TreeItem<String> root = new TreeItem<>(path, new ImageView(new Image(getClass().getResourceAsStream("/folder.PNG"))));

        directoryView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            File file = new File(directoryView.getCurrentFilePath());
            if(file.listFiles() != null){
                addTreeItems(observable.getValue(), directoryView.getCurrentFilePath());
            }
        });

        directoryView.setRoot(root);

        addTreeItems(root,path);

        root.setExpanded(false);
        return directoryView;
    }

    private  void addTreeItems(TreeItem<String> treeItem, String filePath){
        treeItem.getChildren().remove(0, treeItem.getChildren().size());
        File rootFile = new File(filePath);
        List<File> files = Arrays.asList(rootFile.listFiles()).stream().filter(file1 -> {
            return (!file1.getName().equals("Documents and Settings")) && !file1.isHidden() && (file1.isDirectory() || file1.isFile() && file1.canRead() && file1.canWrite());
        }).collect(Collectors.toList());

        for(File file: files){
            if(file.isDirectory()){
                treeItem.getChildren().add(new TreeItem<>(file.getName(), new ImageView(new Image(getClass().getResourceAsStream("/folder.PNG")))));
            }else{
                treeItem.getChildren().add(new TreeItem<>(file.getName(), new ImageView(new Image(getClass().getResourceAsStream("/file.PNG")))));
            }
        }
    }
}
