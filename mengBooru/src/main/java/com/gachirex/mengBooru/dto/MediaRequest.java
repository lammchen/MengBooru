package com.gachirex.mengBooru.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * @author : lämmchen
 * @mailto : gr.lammchen@gmail.com
 * @created : 06/02/2025
 **/
public class MediaRequest {

    private String name;
    private Set<String> tags = new HashSet<>();
    private String collection;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
