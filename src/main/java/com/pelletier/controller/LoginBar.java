package com.pelletier.controller;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPClientTasker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.io.IOException;

/**
 * Created by ryanb on 3/5/2016.
 */
public class LoginBar extends ToolBar {


    @FXML private TextField host;
    @FXML private TextField port;
    @FXML private TextField password;
    @FXML private TextField username;
    @FXML private TextField loggedInUser;
    @FXML private Circle circle;
    @FXML private ToggleButton toggleButton;

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

    boolean connected = false;

    //I would love to create an interface for this so that any different FTPClient can be used
    //I want to use Spring Framework to inject this
    FTPClientTasker ftpClientTasker;

    public void connectOrDisconnect(Event event){
        //I would like to have a function that basically can highlight things in red that are not filled in, a way to form validate
        ftpClientTasker = new FTPClientTasker();

        TextField textField = new TextField();
        try{
            if(connected){
                //disconnect logic (probably clear user in "session")
                loggedInUser.setText("");
                circle.setFill(Paint.valueOf("red"));
                toggleButton.setText("  Connect ");
                connected = false;
            }else{
                ftpClientTasker.connect(host.getText(),Integer.parseInt(port.getText()));
                ftpClientTasker.login(username.getText(),password.getText());

                if(ftpClientTasker.isAuthenticated()){
                    loggedInUser.setText(username.getText());
                    circle.setFill(Paint.valueOf("green"));
                    toggleButton.setText("Disconnect");
                    connected = true;
                }else{
                    //client could not connect
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
