package com.pelletier.util;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ProgressBar;

/**
 * Created by ryanb on 4/3/2016.
 */
public class UploadTransferListener implements FTPDataTransferListener {

    ProgressBar progressBar;
    DoubleProperty fractionComplete;
    long fileSize;
    double progress = 0;

    public UploadTransferListener(ProgressBar progressBar, long fileSize){
        this.progressBar = progressBar;
        progressBar.progressProperty().bind(this.valueProperty());
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

    }

    @Override
    public void aborted() {
    }

    @Override
    public void failed() {

    }

    public final DoubleProperty valueProperty() {
        if (fractionComplete == null) {
            fractionComplete = new SimpleDoubleProperty(0);
        }
        return fractionComplete;
    }
}
