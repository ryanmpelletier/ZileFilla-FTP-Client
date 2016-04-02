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
 * This needs to be able to handle a "user logged in event"
 */
public class DirectoryViewController extends TitledPane {
    public String type;
    @FXML public TreeView<String> directoryView;


    public void initialize(){
        DirectoryViewManager directoryViewManager = null;
        if(type.equals("local")) {
            directoryViewManager = new DirectoryViewManager(this, directoryView, "C:/", new LocalFileItemProvider());
            directoryViewManager.populateDirectoryView();
        }
        else if(type.equals("remote")){
            directoryViewManager = new DirectoryViewManager(this,directoryView, "/", new RemoteFileItemProvider());
            //add event handler that says we populate with a given FTP when fired. (We actually may want to have this handler in MainController
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
}
