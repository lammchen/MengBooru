package com.gachirex.mengBooru.controller;

import com.gachirex.mengBooru.dto.MediaResponse;
import com.gachirex.mengBooru.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author : lämmchen
 * @mailto : gr.lammchen@gmail.com
 * @created : 06/03/2025
 **/
@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final MediaService mediaService;

    @Autowired
    public DashboardApiController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Récupérer les statistiques générales pour le dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = Map.of(
                "totalMedia", mediaService.countTotalMedia(),
                "totalCollections", mediaService.countTotalCollections(),
                "totalTags", mediaService.countTotalTags(),
                "recentUploads", mediaService.countRecentUploads(),
                "diskUsage", mediaService.calculateDiskUsage()
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Récupérer les collections avec leurs statistiques pour le dashboard
     */
    @GetMapping("/collections")
    public ResponseEntity<Map<String, Object>> getCollectionsWithStats() {
        Map<String, Object> collectionsWithStats = mediaService.getCollectionsWithStats();
        return ResponseEntity.ok(collectionsWithStats);
    }

    /**
     * Récupérer les tags avec leurs statistiques pour le dashboard
     */
    @GetMapping("/tags")
    public ResponseEntity<Map<String, Object>> getTagsWithStats() {
        Map<String, Object> tagsWithStats = mediaService.getTagsWithStats();
        return ResponseEntity.ok(tagsWithStats);
    }
}
