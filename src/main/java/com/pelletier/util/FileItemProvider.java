package com.pelletier.util;

import java.util.List;

/**
 * Created by ryanb on 4/1/2016.
 *
 * Depends on the fact that we have a "current" path placeholder
 */
public interface FileItemProvider {

    public boolean isDirectory(String path);
    public String getName(String path);
    public List<String> children(String path);

}

