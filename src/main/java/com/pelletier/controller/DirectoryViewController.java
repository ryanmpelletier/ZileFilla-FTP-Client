package com.pelletier.controller;

import com.pelletier.util.DirectoryViewManager;
import com.pelletier.util.LocalDirectoryViewManager;
import com.pelletier.util.RemoteDirectoryViewManager;
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


    public void initialize(){
        DirectoryViewManager directoryViewManager = null;
        if(type.equals("local"))
             directoryViewManager = new LocalDirectoryViewManager(this, directoryView);
        else if(type.equals("remote"))
            directoryViewManager = new RemoteDirectoryViewManager(this,directoryView);

        directoryViewManager.populateDirectoryView();
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
}
