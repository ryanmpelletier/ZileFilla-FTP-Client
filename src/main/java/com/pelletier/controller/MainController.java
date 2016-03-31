package com.pelletier.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;


public class MainController {

    @FXML public LoginBar loginBar;
    @FXML public DirectoryViewController localDirectoryView;
    @FXML public DirectoryViewController remoteDirectoryView;


    public void initialize(){
    }

    public void exit(ActionEvent event){
        Platform.exit();
    }
}
