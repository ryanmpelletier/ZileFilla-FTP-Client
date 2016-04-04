package com.pelletier.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * Created by ryanb on 4/3/2016.
 */
public class ConsoleController extends TitledPane{

    @FXML TextArea console;

    public ConsoleController(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/console.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}
