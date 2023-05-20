package com.zdjizhi.json.pojo;

/**
 * @author qidaijie
 * @Package com.zdjizhi.json
 * @Description:
 * @date 2023/5/1918:42
 */
public class KnowlegeBaseMeta {
    private String name;
    private String sha256;
    private String format;
    private String path;

    public KnowlegeBaseMeta(String name, String sha256, String format, String path) {
        this.name = name;
        this.sha256 = sha256;
        this.format = format;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "KnowlegeBaseMeta{" +
                "name='" + name + '\'' +
                ", sha256='" + sha256 + '\'' +
                ", format='" + format + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
