package com.gachirex.mengBooru.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : lämmchen
 * @mailto : gr.lammchen@gmail.com
 * @created : 06/02/2025
 **/
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private String uploadDir = "uploads";
    private long sizeThreshold = 2 * 1024 * 1024; // 2MB -> seuil par défaut
    private String defaultCollection = "board";

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public long getSizeThreshold() {
        return sizeThreshold;
    }

    public void setSizeThreshold(long sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    public String getDefaultCollection() {
        return defaultCollection;
    }

    public void setDefaultCollection(String defaultCollection) {
        this.defaultCollection = defaultCollection;
    }
}
