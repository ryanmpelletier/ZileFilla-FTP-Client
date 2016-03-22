package com.pelletier.components;

import com.pelletier.util.DirectoryViewUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.IOException;

/**
 * Created by ryanb on 3/14/2016.
 */
public class DirectoryView<T> extends TreeView<T> {

    StringProperty currentFilePath = new SimpleStringProperty();


    public DirectoryView(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/directory_view.fxml"));
        DirectoryViewUtil directoryViewUtil = new DirectoryViewUtil();
        fxmlLoader.setRoot(directoryViewUtil.getDirectoryView());
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public DirectoryView(String path){
        setCurrentFilePath(path);
        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setCurrentFilePath(buildCurrentFilePathFromTreeItem((TreeItem<String>) observable.getValue()));
        });
    }


    public final StringProperty currentFilePathProperty() {
        return currentFilePath;
    }
    public final String getCurrentFilePath() {
        return currentFilePath.get();
    }
    public final void setCurrentFilePath(String title) {
        this.currentFilePath.set(title);
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
