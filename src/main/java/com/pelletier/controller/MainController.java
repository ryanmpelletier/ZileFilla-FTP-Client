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
    @FXML public LocalDirectoryViewController localDirectoryView;
    @FXML public RemoteDirectoryViewController remoteDirectoryView;
    @FXML public TextArea console;

    public void initialize(){
        loginBar.isLoggedInProperty().addListener((observable, loggedOut, loggedIn) -> {
            if(loggedIn){
                remoteDirectoryView.setDirectoryViewManager(new DirectoryViewManager(remoteDirectoryView, remoteDirectoryView.getDirectoryView(), "/", new RemoteFileItemProvider(loginBar.getFtpClient())));
                remoteDirectoryView.getDirectoryViewManager().populateDirectoryView();
                actionBar.setFtpClient(loginBar.getFtpClient());
                actionBar.localFilePathProperty().bind(localDirectoryView.getDirectoryViewManager().currentFilePathProperty());
                actionBar.remoteFilePathProperty().bind(remoteDirectoryView.getDirectoryViewManager().currentFilePathProperty());
            }else{
                remoteDirectoryView.getDirectoryView().getRoot().getChildren().remove(0, remoteDirectoryView.getDirectoryView().getRoot().getChildren().size());
            }
        });
    }

    public void exit(ActionEvent event){
        Platform.exit();
    }
}
