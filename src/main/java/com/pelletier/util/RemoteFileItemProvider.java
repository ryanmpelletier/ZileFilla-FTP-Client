package com.pelletier.util;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

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
        //may have to get parent, get all the FTPFiles, then find the one we are looking for and see if it is a directory unfortunately
        String fileName = getName(path);
        try{
            ftpClient.changeDirectoryUp();//might need to try to explicitely change directories to path
            if(ftpClient.currentDirectory().equals("/")){
                FTPFile[] ftpFiles = ftpClient.list();
                for(int i = 0; i < ftpFiles.length; i++){
                    if(ftpFiles[i].getName().equals(fileName)){
                        return ftpFiles[i].getType() == 1;
                    }
                }
            }else{

            }
        }catch(Exception e){
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
        List<String> children = null;
        try{
            ftpClient.changeDirectory(path);    //won't be able to change directly to this directory, actually yes we will because this should be a directory!
            children = Arrays.asList(ftpClient.listNames());
            for(String fileName : children){
                fileName = path + "/" + fileName;
            }
        }catch(Exception e){
            System.out.println("uh oh boss....");
        }
        return children;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
