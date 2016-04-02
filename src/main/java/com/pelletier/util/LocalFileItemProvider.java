package com.pelletier.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ryanb on 4/1/2016.
 */
public class LocalFileItemProvider implements FileItemProvider {
    @Override
    public boolean isDirectory(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    @Override
    public String getName(String path) {
        File file = new File(path);
        return file.getName();
    }

    @Override
    public List<String> children(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if(files == null){
            return null;
        }
        return Arrays.asList(files).stream().filter(file1 -> {
            return (!file1.getName().equals("Documents and Settings")) && !file1.isHidden() && (file1.isDirectory() || file1.isFile() && file1.canRead() && file1.canWrite());
        }).map(File::getAbsolutePath).collect(Collectors.toList());
    }
}
