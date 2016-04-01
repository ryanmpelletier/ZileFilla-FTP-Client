package com.pelletier.util;

import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Created by ryanb on 3/31/2016.
 */
public abstract class DirectoryViewManager {

    public TreeView<String> treeView;
    public TitledPane titledPane;

    public DirectoryViewManager(TitledPane titledPane, TreeView<String> treeView){
        this.titledPane = titledPane;
        this.treeView = treeView;
    }

    //I would feel so much better if this returned a TreeView
    public abstract void populateDirectoryView();

    //Gets a treeItem, follows its parents up the hierarchy, building a string for the absolute path
    //this is safe from either local or remote fileProvider, only uses tree items
    public String buildCurrentFilePathFromTreeItem(TreeItem<String> treeItem){
        if(treeItem == null){
            return "";
        }
        if(treeItem.getParent() == null){
            return treeItem.getValue();
        }
        if(treeItem.getParent().getValue().equals("C:/")){
            return buildCurrentFilePathFromTreeItem(treeItem.getParent()) + treeItem.getValue();
        }
        return buildCurrentFilePathFromTreeItem(treeItem.getParent()) + "/" + treeItem.getValue();
    }
}
