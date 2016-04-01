package com.pelletier.util;

import it.sauronsoftware.ftp4j.FTPClient;
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
 * Created by ryanb on 3/31/2016.
 */
public class RemoteDirectoryViewManager extends DirectoryViewManager {


    FTPClient ftpClient;
    public RemoteDirectoryViewManager(TitledPane titledPane, TreeView<String> treeView){//may take an FTP client
        super(titledPane,treeView);
        titledPane.setText("Remote Site: ");
    }

    @Override
    public void populateDirectoryView(){
        TreeItem<String> root = new TreeItem<>(getCurrentPath(), new ImageView(new Image(getClass().getResourceAsStream("/images/folder.PNG"))));

        //add listener for clicks on treeItems, I want it to do the same thing for an expand on a tree item
        treeView.getSelectionModel().selectedItemProperty().addListener((treeItem, oldValue, newValue) -> {
            currentFilePath = buildCurrentFilePathFromTreeItem((TreeItem<String>) treeItem.getValue());   //it doesn't seem like this is updating the title pane
            titledPane.setText("Local Site: " + getCurrentPath());
            File file = new File(getCurrentPath());
            if(file.listFiles() != null){
                addTreeItems(treeItem.getValue(), getCurrentPath());
            }
        });


        treeView.setRoot(root);
        addTreeItems(root, getCurrentPath());
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
                            addTreeItems(t, getCurrentPath());
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

    public String getCurrentPath(){
        String currentPath = null;
        try{
            currentPath = ftpClient.currentDirectory();
        }catch(Exception e){
            e.printStackTrace();
        }
        return currentPath;
    }
}
