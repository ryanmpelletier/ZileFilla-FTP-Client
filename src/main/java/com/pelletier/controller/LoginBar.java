package com.pelletier.controller;


import com.pelletier.util.ConsoleManager;
import it.sauronsoftware.ftp4j.FTPClient;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.Console;
import java.io.IOException;

/**
 * Created by ryanb on 3/5/2016.
 */
public class LoginBar extends ToolBar {

    @FXML private TextField host;
    @FXML private TextField port;
    @FXML private TextField password;
    @FXML private TextField username;
    @FXML private Label loggedInUser;
    @FXML private Circle circle;
    @FXML private ToggleButton toggleButton;


    BooleanProperty isLoggedIn = new SimpleBooleanProperty();

    public LoginBar(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login_bar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    FTPClient ftpClient = new FTPClient();

    public void connectOrDisconnect(Event event){

        TextField textField = new TextField();
        try{
            if(getIsLoggedIn()){
                ftpClient.disconnect(true); //not sure what this does
                loggedInUser.setText("");
                toggleButton.setText("  Connect ");
                isLoggedIn.setValue(false);
                circle.setFill(Paint.valueOf("#ff3300"));
                ConsoleManager.writeText("Disconnected");
            }else{
                ftpClient.connect(host.getText(),Integer.parseInt(port.getText()));
                ftpClient.login(username.getText(),password.getText());

                if(ftpClient.isAuthenticated()){
                    ConsoleManager.writeText("Connected");
                    loggedInUser.setText("User: " + username.getText());
                    circle.setFill(Paint.valueOf("#66ff66"));
                    toggleButton.setText("Disconnect");
                    isLoggedIn.setValue(true);
                }else{
                    ConsoleManager.writeText("Not Authenticated");
                }
            }

        }catch(Exception e){
            ConsoleManager.writeText(e.getLocalizedMessage());
        }

    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public boolean getIsLoggedIn() {
        return isLoggedIn.get();
    }

    public BooleanProperty isLoggedInProperty() {
        return isLoggedIn;
    }
}
