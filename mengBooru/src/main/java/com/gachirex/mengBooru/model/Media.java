package com.gachirex.mengBooru.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String originalFilename;

    private String contentType;

    private long size;

    // Pour les petits fichiers stockés en base
    @Lob
    @Column(length = 5 * 1024 * 1024) // 5MB max pour les fichiers en BDD
    private byte[] content;

    // Pour les gros
    private String filePath;

    @ElementCollection
    @CollectionTable(name = "media_tags", joinColumns = @JoinColumn(name = "media_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    private String collection;

    private LocalDateTime uploadDate;

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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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



    // Helpers
    @Transient
    public boolean isStoredInDatabase() {
        return content != null && content.length > 0;
    }

    @Transient
    public String getStorageType() {
        return isStoredInDatabase() ? "DATABASE" : "FILESYSTEM";
    }
}