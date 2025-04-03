package com.gachirex.mengBooru.service;

import com.gachirex.mengBooru.config.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * @author : lämmchen
 * @mailto : gr.lammchen@gmail.com
 * @created : 06/02/2025
 **/
@Service
public class StorageService {

    private final Path rootLocation;

    @Autowired
    public StorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getUploadDir());
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            // Génération d'un nom de fichier unique pour qu'on puisse éviter les collisions
            String uniqueFilename = UUID.randomUUID() + "_" + filename;

            try (InputStream inputStream = file.getInputStream()) {
                Path destinationFile = this.rootLocation.resolve(uniqueFilename);
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                return destinationFile.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    public Resource loadAsResource(String filePath) {
        try {
            Path file = Paths.get(filePath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filePath, e);
        }
    }

    public Resource loadAsResource(byte[] content, String filename) {
        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
