package com.pelletier.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class MainController {
    @FXML public LoginBar loginBar;

    public void exit(ActionEvent event){
        Platform.exit();
    }
}
