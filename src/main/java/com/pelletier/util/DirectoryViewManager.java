package com.pelletier.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    /*
        I basically need something that can provide a list of items, these items will need to be able to tell me
        1. is it a directory?
        2. am I hidden?
        3. what are my children (the children are these items)
        4. Would really like to have remoteFileProvider and localFileProvider, (These should really operate with String parameters only)
        5. I would like the only difference between my directoryViewManagers to be which fileProvider is "injected" into them
     */


    public DirectoryViewManager(TitledPane titledPane, TreeView<String> treeView){
        this.titledPane = titledPane;
        titledPane.setText("Local Site: ");
        this.treeView = treeView;
    }


    public void populateLocalDirectoryView(){
        TreeItem<String> root = new TreeItem<>(startingPath, new ImageView(new Image(getClass().getResourceAsStream("/images/folder.PNG"))));

        //add listener for clicks on treeItems, I want it to do the same thing for an expand on a tree item
        treeView.getSelectionModel().selectedItemProperty().addListener((treeItem, oldValue, newValue) -> {
            currentFilePath = buildCurrentFilePathFromTreeItem((TreeItem<String>) treeItem.getValue());   //it doesn't seem like this is updating the title pane
            titledPane.setText("Local Site: " + currentFilePath);
            File file = new File(currentFilePath);
            if(file.listFiles() != null){
                addTreeItems(treeItem.getValue(), currentFilePath);
            }
        });


        treeView.setRoot(root);
        addTreeItems(root, startingPath);
        root.setExpanded(false);
    }

    //given a tree item and an absolute filePath, creates children for given tree item from list of files from filePath
    private void addTreeItems(TreeItem<String> treeItem, String filePath){
        treeItem.getChildren().remove(0, treeItem.getChildren().size());
        File rootFile = new File(filePath);
        List<File> files = Arrays.asList(rootFile.listFiles()).stream().filter(file1 -> {
            return (!file1.getName().equals("Documents and Settings")) && !file1.isHidden() && (file1.isDirectory() || file1.isFile() && file1.canRead() && file1.canWrite());
        }).collect(Collectors.toList());

        for(File file: files){
            if(file.isDirectory()){

                TreeItem<String> directoryTreeItem = new TreeItem<>(file.getName(), new ImageView(new Image(getClass().getResourceAsStream("/images/folder.PNG"))));
                directoryTreeItem.expandedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        BooleanProperty booleanProperty = (BooleanProperty) observable;
                        TreeItem<String> t = (TreeItem<String>) booleanProperty.getBean();
                        if(!newValue){
                            t.getChildren().remove(0, t.getChildren().size());
                            t.getChildren().add(new TreeItem<String>(""));
                        }else{
                            currentFilePath = buildCurrentFilePathFromTreeItem(t);  //it doesn't seem like this is updating the title pane
                            addTreeItems(t, currentFilePath);
                        }
                    }
                });
                directoryTreeItem.getChildren().add(new TreeItem<>(""));
                treeItem.getChildren().add(directoryTreeItem);
            }else{
                treeItem.getChildren().add(new TreeItem<>(file.getName(), new ImageView(new Image(getClass().getResourceAsStream("/images/file.PNG")))));
            }
        }
    }

    //Gets a treeItem, follows its parents up the hierarchy, building a string for the absolute path
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
