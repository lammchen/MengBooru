package com.gachirex.mengBooru.controller;

import com.gachirex.mengBooru.dto.MediaRequest;
import com.gachirex.mengBooru.dto.MediaResponse;
import com.gachirex.mengBooru.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : lämmchen
 * @mailto : gr.lammchen@gmail.com
 * @created : 06/02/2025
 **/
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping
    public ResponseEntity<MediaResponse> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "tags", required = false) String tagsString,
            @RequestParam(value = "collection", required = false) String collection) {

        // Traiter les tags comme une liste séparée par des virgules
        Set<String> tags = new HashSet<>();
        if (tagsString != null && !tagsString.isEmpty()) {
            tags = Arrays.stream(tagsString.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
        }

        // Créer une MediaRequest à partir des paramètres
        MediaRequest request = new MediaRequest();
        request.setName(name);
        request.setTags(tags);
        request.setCollection(collection);

        // Appeler le service pour sauvegarder le média
        MediaResponse response = mediaService.saveMedia(file, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MediaResponse>> getAllMedia(
            @RequestParam(value = "collection", required = false) String collection,
            @RequestParam(value = "tag", required = false) String tag) {

        List<MediaResponse> mediaList = mediaService.findMedia(collection, tag);
        return ResponseEntity.ok(mediaList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getMedia(@PathVariable Long id) {
        Resource resource = mediaService.getMediaResource(id);
        String contentType = mediaService.getMediaContentType(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/collections")
    public ResponseEntity<List<String>> getAllCollections() {
        List<String> collections = mediaService.getAllCollections();
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = mediaService.getAllTags();
        return ResponseEntity.ok(tags);
    }
}
