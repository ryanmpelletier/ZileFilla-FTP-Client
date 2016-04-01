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
    public boolean isHidden(String path) {
        File file = new File(path);
        return file.isHidden();
    }

    @Override
    public boolean isDirectory(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    @Override
    public boolean canRead(String path) {
        File file = new File(path);
        return file.canRead();
    }

    @Override
    public boolean canWrite(String path) {
        File file = new File(path);
        return file.canWrite();    }

    @Override
    public String getName(String path) {
        File file = new File(path);
        return file.getName();
    }

    @Override
    public List<String> children(String path) {
        File file = new File(path);
        return Arrays.asList(file.listFiles()).stream().map(File::getName).collect(Collectors.toList());
    }
}
