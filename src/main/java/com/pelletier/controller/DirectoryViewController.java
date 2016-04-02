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
 *
 * When the SimpleBooleanProperty changes on the login bar this needs to population
 */
public class DirectoryViewController extends TitledPane {
    public String type;
    @FXML public TreeView<String> directoryView;
    DirectoryViewManager directoryViewManager = null;

    public void initialize(){
        if(type.equals("local")) {//if local we can initialize now
            directoryViewManager = new DirectoryViewManager(this, directoryView, "C:/", new LocalFileItemProvider());
            directoryViewManager.populateDirectoryView();
        }
    }

    public DirectoryViewController(@NamedArg("type") String type){
        if((!type.equals("local") && !type.equals("remote"))){
            type = "local";
        }
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
