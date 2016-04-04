package com.pelletier.controller;

import com.pelletier.util.DirectoryViewManager;
import com.pelletier.util.RemoteFileItemProvider;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;


public class MainController {

    @FXML public LoginBar loginBar;
    @FXML public ActionBar actionBar;
    @FXML public DirectoryViewController localDirectoryView;
    @FXML public DirectoryViewController remoteDirectoryView;
    @FXML public TextArea console;

    public void initialize(){
        loginBar.isLoggedInProperty().addListener((observable, loggedOut, loggedIn) -> {
            if(loggedIn){
                remoteDirectoryView.setDirectoryViewManager(new DirectoryViewManager(remoteDirectoryView, remoteDirectoryView.getDirectoryView(), "/", new RemoteFileItemProvider(loginBar.getFtpClient())));
                remoteDirectoryView.getDirectoryViewManager().populateDirectoryView();
                actionBar.setFtpClient(loginBar.getFtpClient());
                actionBar.localFilePathProperty().bind(localDirectoryView.getDirectoryViewManager().currentFilePathProperty());
                actionBar.remoteFilePathProperty().bind(remoteDirectoryView.getDirectoryViewManager().currentFilePathProperty());
            }
        });
    }

    public void exit(ActionEvent event){
        Platform.exit();
    }
}
