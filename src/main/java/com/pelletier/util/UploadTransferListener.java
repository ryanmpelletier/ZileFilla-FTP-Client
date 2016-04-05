package com.pelletier.util;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;

/**
 * Created by ryanb on 4/3/2016.
 */
public class UploadTransferListener implements FTPDataTransferListener {

    ProgressIndicator progressIndicator;
    ProgressBar progressBar;
    DoubleProperty fractionComplete;
    long fileSize;
    double progress = 0;

    public UploadTransferListener(ProgressBar progressBar,ProgressIndicator progressIndicator, long fileSize){
        this.progressBar = progressBar;
        this.progressIndicator = progressIndicator;
        progressBar.progressProperty().bind(this.valueProperty());
        progressIndicator.progressProperty().bind(this.valueProperty());
        this.fileSize = fileSize;
    }


    @Override
    public void started() {
    }

    @Override
    public void transferred(int i) {
        progress += i;
        fractionComplete.setValue(progress/fileSize);
    }

    @Override
    public void completed() {
        ConsoleManager.writeText("Completed " + fileSize + " byte transfer.");
    }

    @Override
    public void aborted() {
        ConsoleManager.writeText("File transfer aborted");
    }

    @Override
    public void failed() {
        ConsoleManager.writeText("Failed to transfer file");
    }

    public final DoubleProperty valueProperty() {
        if (fractionComplete == null) {
            fractionComplete = new SimpleDoubleProperty(0);
        }
        return fractionComplete;
    }
}
