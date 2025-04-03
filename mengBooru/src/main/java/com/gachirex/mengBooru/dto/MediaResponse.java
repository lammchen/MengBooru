package com.gachirex.mengBooru.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author : lämmchen
 * @mailto : gr.lammchen@gmail.com
 * @created : 06/02/2025
 **/
public class MediaResponse {

    private Long id;
    private String name;
    private String originalFilename;
    private String contentType;
    private long size;
    private Set<String> tags;
    private String collection;
    private LocalDateTime uploadDate;
    private String storageType; // "DATABASE" ou "FILESYSTEM"

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }
}
