package com.FileStorage.Client;

public class PathLine {

    private String name;
    private long size;
    private boolean isFolder;
    private String type;
    private Integer imp;

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public Integer getImp() {
        return imp;
    }

    public PathLine(String name, long size, boolean fld, int imp) {
        this.name = name;
        this.size = size;
        this.isFolder = fld;
        this.type = name == "..." ? "A" : fld ? "D" : "F";
        this.imp = imp;

    }
}

