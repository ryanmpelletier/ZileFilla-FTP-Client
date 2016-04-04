package com.pelletier.controller;

import com.pelletier.util.ConsoleManager;
import com.pelletier.util.FileItemProvider;
import com.pelletier.util.UploadTransferListener;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToolBar;

import java.io.File;
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
        ConsoleManager.writeText("Uploading " + localFilePath.get() + " to " + remoteFilePath.get());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        File file = new File(localFilePath.get());
                        ftpClient.changeDirectory(remoteFilePath.get());
                        ftpClient.upload(new File(localFilePath.get()), new UploadTransferListener(progressBar,file.length()));
                    }catch (Exception e){
                        ConsoleManager.writeText(e.getLocalizedMessage());
                    }
                }
            }).start();

    }

    public void download(ActionEvent event){
        ConsoleManager.writeText("Downloading " + remoteFilePath.get() + " to " + localFilePath.get());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    File file = new File(localFilePath.get());

                    ftpClient.download(remoteFilePath.get(), new File(localFilePath.get() + "/" + getRemoteFileName(remoteFilePath.get())), new UploadTransferListener(progressBar,getRemoteFileSize(remoteFilePath.get())));
                }catch (Exception e){
                    ConsoleManager.writeText(e.getLocalizedMessage());
                }
            }
        }).start();
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

    public String getRemoteFileName(String absoluteRemotePath){
        return absoluteRemotePath.split("/")[absoluteRemotePath.split("/").length - 1];
    }

    public long getRemoteFileSize(String absoluteRemotePath){
        try{
            ftpClient.changeDirectory(absoluteRemotePath);
            FTPFile[] files = ftpClient.list();
            String remoteFileName = getRemoteFileName(absoluteRemotePath);
            for(FTPFile file : files){
                if(file.getName().equals(remoteFileName)){
                    return file.getSize();
                }
            }
        }catch(Exception e){
            return 0;
        }
        return 0;
    }

}
