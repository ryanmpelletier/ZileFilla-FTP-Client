package com.pelletier.util;

import com.pelletier.components.DirectoryView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ryanb on 3/14/2016.
 */
public class DirectoryViewManager {

    String path = "C:/";
    String currentFilePath;

    public void populateLocalDirectoryView(TreeView<String> treeView){

        TreeItem<String> root = new TreeItem<>(path, new ImageView(new Image(getClass().getResourceAsStream("/folder.PNG"))));

        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            File file = new File(path);
            if(file.listFiles() != null){
//                addTreeItems(observable.getValue(), directoryView.getCurrentFilePath());
//                addTreeItems(observable.getValue(), path);    //This is basically where you say, starting at this path, add all the children
                System.out.println("Also be updating the title pane here");
                System.out.println("Would be adding items here");
            }
        });

        treeView.setRoot(root);
        addTreeItems(root,path);
        root.setExpanded(false);
    }

    public void testTreeView(TreeView<String> treeView){
        TreeItem<String> root = new TreeItem<>("Root");
        for(int i = 0; i < 10; i++){
            root.getChildren().add(new TreeItem<String>("test" + i));
        }

        treeView.setRoot(root);
    }

    private  void addTreeItems(TreeItem<String> treeItem, String filePath){
        treeItem.getChildren().remove(0, treeItem.getChildren().size());
        File rootFile = new File(filePath);
        List<File> files = Arrays.asList(rootFile.listFiles()).stream().filter(file1 -> {
            return (!file1.getName().equals("Documents and Settings")) && !file1.isHidden() && (file1.isDirectory() || file1.isFile() && file1.canRead() && file1.canWrite());
        }).collect(Collectors.toList());

        for(File file: files){
            if(file.isDirectory()){
                //also put a temp child on it??
                treeItem.getChildren().add(new TreeItem<>(file.getName(), new ImageView(new Image(getClass().getResourceAsStream("/folder.PNG")))));
            }else{
                treeItem.getChildren().add(new TreeItem<>(file.getName(), new ImageView(new Image(getClass().getResourceAsStream("/file.PNG")))));
            }
        }
    }
}
