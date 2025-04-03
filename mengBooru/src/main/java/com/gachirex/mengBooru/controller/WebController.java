package com.gachirex.mengBooru.controller;

import com.gachirex.mengBooru.dto.MediaResponse;
import com.gachirex.mengBooru.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author : lämmchen
 * @mailto : gr.lammchen@gmail.com
 * @created : 06/02/2025
 **/
@Controller
public class WebController {

    private final MediaService mediaService;

    @Autowired
    public WebController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(value = "collection", required = false) String collection,
            @RequestParam(value = "tag", required = false) String tag,
            Model model) {

        // Récupérer les médias avec les filtres éventuels
        List<MediaResponse> mediaList = mediaService.findMedia(collection, tag);

        // Récupérer toutes les collections pour le menu déroulant
        List<String> collections = mediaService.getAllCollections();
        List<String> tags = mediaService.getAllTags();

        // Ajouter les données au modèle
        model.addAttribute("mediaList", mediaList);
        model.addAttribute("collections", collections);
        model.addAttribute("allTags", tags);
        model.addAttribute("selectedCollection", collection);
        model.addAttribute("selectedTag", tag);

        return "media-catalog";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "collection", required = false) String collection,
            Model model) {

        return home(collection, tag, model);
    }
}