package com.pelletier.controller;

import com.pelletier.util.DirectoryViewManager;
import com.pelletier.util.LocalFileItemProvider;
import com.pelletier.util.RemoteFileItemProvider;
import javafx.beans.NamedArg;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeView;

import java.io.IOException;

/**
 * Created by ryanb on 3/31/2016.
 */
public class DirectoryViewController extends TitledPane {

    public String type;
    @FXML public TreeView<String> directoryView;
    public DirectoryViewManager directoryViewManager = null;

    public void initialize(){
    }

    public DirectoryViewController(){
        this.type = type;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/directory_view.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public DirectoryViewManager getDirectoryViewManager() {
        return directoryViewManager;
    }

    public void setDirectoryViewManager(DirectoryViewManager directoryViewManager) {
        this.directoryViewManager = directoryViewManager;
    }

    public TreeView<String> getDirectoryView() {
        return directoryView;
    }

    public void setDirectoryView(TreeView<String> directoryView) {
        this.directoryView = directoryView;
    }
}
