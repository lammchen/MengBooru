package com.gachirex.mengBooru.controller;

import com.gachirex.mengBooru.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * @author : lämmchen
 * @mailto : gr.lammchen@gmail.com
 * @created : 06/02/2025
 **/
@Controller
public class NavigationController {

    private final MediaService mediaService;

    @Autowired
    public NavigationController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     *  Obtenir des stats pour chaque collections (nb médias, prévisualisation)
     */
    @GetMapping("/collections")
    public String showCollections(Model model) {
        List<String> collections = mediaService.getAllCollections();
        Map<String, Object> collectionsWithStats = mediaService.getCollectionsWithStats();

        model.addAttribute("collections", collections);
        model.addAttribute("collectionsWithStats", collectionsWithStats);

        return "collections";
    }

    /**
     *  Obtenir des stats pour chaque tag (nb médias, prévisualisation)
     */
    @GetMapping("/tags")
    public String showTags(Model model) {
        List<String> tags = mediaService.getAllTags();
        Map<String, Object> tagsWithStats = mediaService.getTagsWithStats();

        model.addAttribute("tags", tags);
        model.addAttribute("tagsWithStats", tagsWithStats);

        return "tags";
    }

    /**
     *  Détail d'un media selon l'id. Charge le média et les médias similaires
     *  Renvoie à la page d'accueil si l'id est invalide
     */
    @GetMapping("/media/{id}")
    public String mediaDetail(@PathVariable Long id, Model model) {
        try {
            var media = mediaService.getMediaById(id);
            model.addAttribute("media", media);

            var relatedMedia = mediaService.findRelatedMedia(id);
            model.addAttribute("relatedMedia", relatedMedia);

            return "media-detail";
        } catch (Exception e) {
            return "redirect:/";
        }
    }
}