package com.pelletier.util;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ryanb on 4/1/2016.
 * Problems
 * 1. Trying to find children of paths that are not directories!
 * 2. May also need to set currentDirectory to what it was before function.
 */
public class RemoteFileItemProvider implements FileItemProvider {

    public FTPClient ftpClient;

    public RemoteFileItemProvider(FTPClient ftpClient){
        this.ftpClient = ftpClient;
    }

    @Override
    public boolean isDirectory(String path) {
        if(path.equals("/")){   //this covers that "case" of me being in the root directory
            return true;
        }
        System.out.println("Is Directory Path " + path);
        String fileName = getName(path);    //can't call getName if path is "/"
        try{
            ftpClient.changeDirectory(path);
            ftpClient.changeDirectoryUp();
            FTPFile[] ftpFiles = ftpClient.list();//TODO
            for(int i = 0; i < ftpFiles.length; i++){
                if(ftpFiles[i].getName().equals(fileName)){
                    return ftpFiles[i].getType() == 1;
                }
            }

        }catch(Exception e){
            System.out.println("Path: " +path);
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public String getName(String path) {
        return path.split("/")[path.split("/").length - 1]; //don't know if this will work or not
    }

    @Override
    public List<String> children(String path) { //needs to return absolute paths
        if(!path.equals("/") && !isDirectory(path)){
            return null;
        }
        System.out.println("Children Path: " + path);
        String[] children = null;
        try{
            ftpClient.changeDirectory(path);    //won't be able to change directly to this directory, actually yes we will because this should be a directory! (this may be throwing my error)
            children = ftpClient.listNames();
            for(int i = 0; i < children.length; i++){
                if(path.equals("/")){
                    children[i] = "/" + children[i];   //this might be janky
                }else{
                    children[i] = path + "/" + children[i];
                }
            }
        }catch(Exception e){
            System.out.println("Path: " + path);
            e.printStackTrace();
        }
        return Arrays.asList(children);
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
