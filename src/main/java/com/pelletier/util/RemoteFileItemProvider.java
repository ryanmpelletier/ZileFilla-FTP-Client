package com.pelletier.util;

import it.sauronsoftware.ftp4j.FTPClient;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ryanb on 4/1/2016.
 */


public class RemoteFileItemProvider implements FileItemProvider {

    public FTPClient ftpClient;

    public RemoteFileItemProvider(FTPClient ftpClient){
        this.ftpClient = ftpClient;
    }

    @Override
    public boolean isDirectory(String path) {
        try{
            ftpClient.changeDirectory(path);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public String getName(String path) {
        return path.split("/")[path.split("/").length - 1]; //don't know if this will work or not
    }

    @Override
    public List<String> children(String path) {
        List<String> listOfChildren = null;
        int numberOfRetries = 0;

        if(isDirectory(path)){
            while(listOfChildren == null && (numberOfRetries <= 3)){
                String[] children = null;
                try{
                    ftpClient.changeDirectory(path);    //won't be able to change directly to this directory, actually yes we will because this should be a directory! (this may be throwing my error)
                    children = ftpClient.listNames();
                    if(children != null){
                        for(int i = 0; i < children.length; i++){
                            if(path.equals("/")){
                                children[i] = "/" + children[i];   //this might be janky
                            }else{
                                children[i] = path + "/" + children[i];
                            }
                        }
                        listOfChildren = Arrays.asList(children);
                    }else{
                        return null;
                    }
                }catch(Exception e){
                    numberOfRetries++;
                }
            }
        }
        return listOfChildren;
    }


    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
