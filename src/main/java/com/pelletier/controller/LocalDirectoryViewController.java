package com.pelletier.controller;

import com.pelletier.util.DirectoryViewManager;
import com.pelletier.util.LocalFileItemProvider;

/**
 * Created by ryanb on 4/6/2016.
 */
public class LocalDirectoryViewController extends DirectoryViewController {
    @Override
    public void initialize(){
        this.type = "local";
        directoryViewManager = new DirectoryViewManager(this, directoryView, "C:/", new LocalFileItemProvider());
        directoryViewManager.populateDirectoryView();
    }
}
