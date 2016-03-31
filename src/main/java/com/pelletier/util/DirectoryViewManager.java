package com.pelletier.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ryanb on 3/14/2016.
 */
public class DirectoryViewManager {

    String startingPath = "C:/";
    String currentFilePath;
    TreeView<String> treeView;
    TitledPane titledPane;

    public DirectoryViewManager(TitledPane titledPane, TreeView<String> treeView){
        this.titledPane = titledPane;
        titledPane.setText("Local Site: ");
        this.treeView = treeView;
    }

    public void populateLocalDirectoryView(){

        TreeItem<String> root = new TreeItem<>(startingPath, new ImageView(new Image(getClass().getResourceAsStream("/folder.PNG"))));

        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentFilePath = (buildCurrentFilePathFromTreeItem((TreeItem<String>) observable.getValue()));   //it doesn't seem like this is updating the title pane
            titledPane.setText("Local Site: " + currentFilePath);//this won't do anything unfortunately
            File file = new File(currentFilePath);
            if(file.listFiles() != null){
                addTreeItems(observable.getValue(), currentFilePath);
            }
        });

        treeView.setRoot(root);
        addTreeItems(root, startingPath);
        root.setExpanded(false);
    }

    private  void addTreeItems(TreeItem<String> treeItem, String filePath){
        treeItem.getChildren().remove(0, treeItem.getChildren().size());
        File rootFile = new File(filePath);
        List<File> files = Arrays.asList(rootFile.listFiles()).stream().filter(file1 -> {
            return (!file1.getName().equals("Documents and Settings")) && !file1.isHidden() && (file1.isDirectory() || file1.isFile() && file1.canRead() && file1.canWrite());
        }).collect(Collectors.toList());

        for(File file: files){
            if(file.isDirectory()){
                //put a temp child, we will need to not allow clicking on the temp child
                //maybe don't add temp, and find a way to show arrow
                treeItem.getChildren().add(new TreeItem<>(file.getName(), new ImageView(new Image(getClass().getResourceAsStream("/folder.PNG")))));
            }else{
                treeItem.getChildren().add(new TreeItem<>(file.getName(), new ImageView(new Image(getClass().getResourceAsStream("/file.PNG")))));
            }
        }
    }

    public String buildCurrentFilePathFromTreeItem(TreeItem<String> treeItem){
        if(treeItem == null){
            return "";
        }
        if(treeItem.getParent() == null){
            return treeItem.getValue();
        }
        if(treeItem.getParent().getValue().equals("C:/")){
            return buildCurrentFilePathFromTreeItem(treeItem.getParent()) + treeItem.getValue();
        }
        return buildCurrentFilePathFromTreeItem(treeItem.getParent()) + "/" + treeItem.getValue();
    }
}
