package com.zdjizhi.common;

import java.io.Serializable;

public class CustomFile implements Serializable {

    private String fileName;

    private byte[] content;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }


}
