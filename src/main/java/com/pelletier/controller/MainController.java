package com.pelletier.controller;

import com.pelletier.util.DirectoryViewManager;
import com.pelletier.util.RemoteFileItemProvider;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;


public class MainController {

    @FXML public LoginBar loginBar;
    @FXML public DirectoryViewController localDirectoryView;
    @FXML public DirectoryViewController remoteDirectoryView;

    public void initialize(){
        loginBar.isLoggedInProperty().addListener((observable, loggedOut, loggedIn) -> {
            if(loggedIn){
                remoteDirectoryView.setDirectoryViewManager(new DirectoryViewManager(remoteDirectoryView, remoteDirectoryView.getDirectoryView(), "/", new RemoteFileItemProvider(loginBar.getFtpClient())));
                remoteDirectoryView.getDirectoryViewManager().populateDirectoryView();
            }else{
                //reset the directory view
            }
        });
    }

    public void exit(ActionEvent event){
        Platform.exit();
    }
}
