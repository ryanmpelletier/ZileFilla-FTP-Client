package com.pelletier.controller;

/**
 * Created by ryanb on 4/6/2016.
 */
public class RemoteDirectoryViewController extends DirectoryViewController {
    @Override
    public void initialize(){
        this.type = "remote";
    }
}
