package com.pelletier.controller;

import it.sauronsoftware.ftp4j.FTPClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToolBar;

import java.io.IOException;

/**
 * Created by ryanb on 4/3/2016.
 */
public class ActionBar extends ToolBar {

    @FXML Button upload;
    @FXML Button download;
    @FXML ProgressBar progressBar;

    StringProperty localFilePath = new SimpleStringProperty();
    StringProperty remoteFilePath = new SimpleStringProperty();

    FTPClient ftpClient;

    public ActionBar(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/action_bar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void upload(ActionEvent event){
        System.out.println("Clicked Upload");
        System.out.println("Uploading " + localFilePath.get() + " to " + remoteFilePath.get());
    }

    public void download(ActionEvent event){
        System.out.println("Clicked Download");
        System.out.println("Uploading " + remoteFilePath.get() + " to " + localFilePath.get());

    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public String getLocalFilePath() {
        return localFilePath.get();
    }

    public StringProperty localFilePathProperty() {
        return localFilePath;
    }

    public String getRemoteFilePath() {
        return remoteFilePath.get();
    }

    public StringProperty remoteFilePathProperty() {
        return remoteFilePath;
    }
}
