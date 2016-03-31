package com.pelletier.controller;

import com.pelletier.util.DirectoryViewManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.IOException;

/**
 * Created by ryanb on 3/31/2016.
 */
public class DirectoryViewController extends TitledPane {

    @FXML public TreeView<String> directoryView;


    public void initialize(){
        DirectoryViewManager directoryViewUtil = new DirectoryViewManager(this, directoryView);
        directoryViewUtil.populateLocalDirectoryView();
    }

    public DirectoryViewController(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/directory_view.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    //will need this for the listener
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
