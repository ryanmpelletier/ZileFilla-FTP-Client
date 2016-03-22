package com.pelletier.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;


public class MainController {
    @FXML public LoginBar loginBar;
//    @FXML public FileExplorer localFileExplorer;
//    @FXML public FileExplorer remoteFileExplorer;

    public void exit(ActionEvent event){
        Platform.exit();
    }
}
