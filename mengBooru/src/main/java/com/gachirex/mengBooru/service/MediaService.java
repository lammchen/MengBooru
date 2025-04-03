package com.gachirex.mengBooru.service;

import com.gachirex.mengBooru.config.StorageProperties;
import com.gachirex.mengBooru.dto.MediaRequest;
import com.gachirex.mengBooru.dto.MediaResponse;
import com.gachirex.mengBooru.model.Media;
import com.gachirex.mengBooru.repository.MediaRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author : lämmchen
 * @mailto : gr.lammchen@gmail.com
 * @created : 06/02/2025
 **/
@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final StorageService storageService;
    private final StorageProperties storageProperties;

    @Autowired
    public MediaService(MediaRepository mediaRepository, StorageService storageService, StorageProperties storageProperties) {
        this.mediaRepository = mediaRepository;
        this.storageService = storageService;
        this.storageProperties = storageProperties;
    }

    public MediaResponse saveMedia(MultipartFile file, MediaRequest request) {
        try {
            Media media = new Media();

            // Définition du Filename (original ou pas)
            String name = (request.getName() != null && !request.getName().isEmpty())
                    ? request.getName()
                    : file.getOriginalFilename();
            Optional<Media> existingMedia = mediaRepository.findByName(name);
            if (existingMedia.isPresent()) {
                name = generateUniqueName(name);
            }


            
            media.setName(name);
            media.setOriginalFilename(file.getOriginalFilename());
            media.setContentType(file.getContentType());
            media.setSize(file.getSize());
            media.setTags(request.getTags());

            // Définition de la Collection (default ou pas)
            String collection = (request.getCollection() != null && !request.getCollection().isEmpty())
                    ? request.getCollection()
                    : storageProperties.getDefaultCollection();

            media.setCollection(collection);
            media.setUploadDate(LocalDateTime.now());

            // Size management
            if (file.getSize() <= storageProperties.getSizeThreshold()) {
                // -> bDD
                media.setContent(file.getBytes());
            } else {
                // système de fichiers
                String filePath = storageService.store(file);
                media.setFilePath(filePath);
            }

            Media savedMedia = mediaRepository.save(media);
            return mapToMediaResponse(savedMedia);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store media: " + e.getMessage(), e);
        }
    }

    public MediaResponse getMediaById(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + id));

        return mapToMediaResponse(media);
    }

    public List<MediaResponse> findMedia(String collection, String tag) {
        List<Media> mediaList;

        if (collection != null && tag != null) {
            mediaList = mediaRepository.findByTagAndCollection(tag, collection);
        } else if (collection != null) {
            mediaList = mediaRepository.findByCollection(collection);
        } else if (tag != null) {
            mediaList = mediaRepository.findByTag(tag);
        } else {
            mediaList = mediaRepository.findAll();
        }

        return mediaList.stream()
                .map(this::mapToMediaResponse)
                .collect(Collectors.toList());
    }

    public List<MediaResponse> findRelatedMedia(Long currentMediaId) {
        // Récupérer le média actuel pour ses tags et sa collection
        Media currentMedia = mediaRepository.findById(currentMediaId)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + currentMediaId));

        // Si le média n'a pas de tags ni de collection, retourner une liste vide
        if ((currentMedia.getTags() == null || currentMedia.getTags().isEmpty())
                && (currentMedia.getCollection() == null || currentMedia.getCollection().isEmpty())) {
            return new ArrayList<>();
        }

        // Rechercher par tags ou collection
        List<Media> relatedMedia = new ArrayList<>();

        // D'abord essayer de trouver des médias avec les mêmes tags
        if (currentMedia.getTags() != null && !currentMedia.getTags().isEmpty()) {
            // Utiliser le premier tag pour trouver des médias similaires
            String firstTag = currentMedia.getTags().iterator().next();
            List<Media> mediaWithSameTag = mediaRepository.findByTag(firstTag);
            relatedMedia.addAll(mediaWithSameTag);
        }

        // Si on n'a pas trouvé assez de médias avec des tags, compléter avec la collection
        if (relatedMedia.size() < 6 && currentMedia.getCollection() != null && !currentMedia.getCollection().isEmpty()) {
            List<Media> mediaInSameCollection = mediaRepository.findByCollection(currentMedia.getCollection());

            // Ajouter uniquement les médias qui ne sont pas déjà dans la liste
            for (Media media : mediaInSameCollection) {
                if (relatedMedia.stream().noneMatch(m -> m.getId().equals(media.getId()))) {
                    relatedMedia.add(media);
                }
            }
        }

        // Filtrer pour exclure le média actuel et limiter à 6 résultats
        return relatedMedia.stream()
                .filter(m -> !m.getId().equals(currentMediaId))
                .distinct()
                .limit(6)
                .map(this::mapToMediaResponse)
                .collect(Collectors.toList());
    }

    public Resource getMediaResource(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + id));

        if (media.isStoredInDatabase()) {
            return storageService.loadAsResource(media.getContent(), media.getName());
        } else {
            return storageService.loadAsResource(media.getFilePath());
        }
    }

    public String getMediaContentType(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + id));

        return media.getContentType();
    }

    public Map<String, Object> getCollectionsWithStats() {
        List<String> collections = mediaRepository.findAllCollections();
        Map<String, Object> result = new HashMap<>();

        for (String collection : collections) {
            Map<String, Object> stats = new HashMap<>();

            // Compter le nombre de médias dans cette collection
            List<Media> mediaInCollection = mediaRepository.findByCollection(collection);
            stats.put("count", mediaInCollection.size());

            // Trouver un média pour la prévisualisation (préférer les images)
            Optional<Media> previewMedia = mediaInCollection.stream()
                    .filter(media -> media.getContentType() != null && media.getContentType().startsWith("image/"))
                    .findFirst();

            // Si aucune image n'est trouvée, utiliser le premier média
            if (previewMedia.isEmpty() && !mediaInCollection.isEmpty()) {
                previewMedia = Optional.of(mediaInCollection.get(0));
            }

            if (previewMedia.isPresent()) {
                stats.put("previewMedia", mapToMediaResponse(previewMedia.get()));
            }

            result.put(collection, stats);
        }

        return result;
    }

    public Map<String, Object> getTagsWithStats() {
        List<String> tags = mediaRepository.findAllTags();
        Map<String, Object> result = new HashMap<>();

        for (String tag : tags) {
            Map<String, Object> stats = new HashMap<>();

            // Compter le nombre de médias avec ce tag
            List<Media> mediaWithTag = mediaRepository.findByTag(tag);
            stats.put("count", mediaWithTag.size());

            // Trouver un média pour la prévisualisation (préférer les images)
            Optional<Media> previewMedia = mediaWithTag.stream()
                    .filter(media -> media.getContentType() != null && media.getContentType().startsWith("image/"))
                    .findFirst();

            // Si aucune image n'est trouvée, utiliser le premier média
            if (previewMedia.isEmpty() && !mediaWithTag.isEmpty()) {
                previewMedia = Optional.of(mediaWithTag.get(0));
            }

            if (previewMedia.isPresent()) {
                stats.put("previewMedia", mapToMediaResponse(previewMedia.get()));
            }

            result.put(tag, stats);
        }

        return result;
    }

    public List<String> getAllCollections() {
        return mediaRepository.findAllCollections();
    }

    public List<String> getAllTags() {
        return mediaRepository.findAllTags();
    }

    private MediaResponse mapToMediaResponse(Media media) {
        MediaResponse response = new MediaResponse();
        response.setId(media.getId());
        response.setName(media.getName());
        response.setOriginalFilename(media.getOriginalFilename());
        response.setContentType(media.getContentType());
        response.setSize(media.getSize());
        response.setTags(media.getTags());
        response.setCollection(media.getCollection());
        response.setUploadDate(media.getUploadDate());
        response.setStorageType(media.getStorageType());

        return response;
    }

    private String generateUniqueName(String originalName) {
        String baseName = FilenameUtils.getBaseName(originalName);
        String extension = FilenameUtils.getExtension(originalName);
        return baseName + "_" + System.currentTimeMillis() +
                (extension.isEmpty() ? "" : "." + extension);
    }


    /**
     *  Dashboard Methods
     */

    // Ajouter ces méthodes à ta classe MediaService

    /**
     * Compte le nombre total de médias
     */
    public long countTotalMedia() {
        return mediaRepository.count();
    }

    /**
     * Compte le nombre total de collections
     */
    public long countTotalCollections() {
        return mediaRepository.findAllCollections().size();
    }

    /**
     * Compte le nombre total de tags
     */
    public long countTotalTags() {
        return mediaRepository.findAllTags().size();
    }

    /**
     * Compte le nombre de médias ajoutés récemment (dernières 24h)
     */
    public long countRecentUploads() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return mediaRepository.countByUploadDateAfter(yesterday);
    }

    /**
     * Calcule l'espace disque utilisé par les médias
     */
    public Map<String, Object> calculateDiskUsage() {
        // Taille totale des médias stockés en base
        long dbStorageBytes = 0;
        List<Media> dbMedia = mediaRepository.findByContentIsNotNull();
        for (Media media : dbMedia) {
            dbStorageBytes += media.getSize();
        }

        // Taille totale des médias stockés sur le système de fichiers
        long fileStorageBytes = 0;
        List<Media> fileMedia = mediaRepository.findByFilePathIsNotNull();
        for (Media media : fileMedia) {
            fileStorageBytes += media.getSize();
        }

        // Nombre de médias par type de stockage
        long dbMediaCount = dbMedia.size();
        long fileMediaCount = fileMedia.size();

        return Map.of(
                "totalBytes", dbStorageBytes + fileStorageBytes,
                "databaseBytes", dbStorageBytes,
                "fileSystemBytes", fileStorageBytes,
                "databaseMediaCount", dbMediaCount,
                "fileSystemMediaCount", fileMediaCount
        );
    }

    /**
     * Estime la taille des fichiers stockés sur le système de fichiers
     */
    private long estimateFileSystemStorage() {
        long totalSize = 0;

        try {
            // Récupérer tous les médias stockés sur le système de fichiers
            List<Media> fileSystemMedia = mediaRepository.findByFilePathIsNotNull();

            for (Media media : fileSystemMedia) {
                // Utiliser la taille connue du média
                totalSize += media.getSize();
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner une estimation basée sur la moyenne
            // et le nombre de médias stockés sur le système de fichiers
            return mediaRepository.countByFilePathIsNotNull() * 1024 * 1024; // ~1MB par média en moyenne
        }

        return totalSize;
    }
}
