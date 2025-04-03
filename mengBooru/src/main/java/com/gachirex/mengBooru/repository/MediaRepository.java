package com.gachirex.mengBooru.repository;

import com.gachirex.mengBooru.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author : lämmchen
 * @mailto : gr.lammchen@gmail.com
 * @created : 06/02/2025
 **/
@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    Optional<Media> findByName(String name);
    List<Media> findByCollection(String collection);

    @Query("SELECT m FROM Media m JOIN m.tags t WHERE t = :tag")
    List<Media> findByTag(@Param("tag") String tag);

    @Query("SELECT m FROM Media m JOIN m.tags t WHERE t = :tag AND m.collection = :collection")
    List<Media> findByTagAndCollection(@Param("tag") String tag, @Param("collection") String collection);

    @Query("SELECT DISTINCT m.collection FROM Media m")
    List<String> findAllCollections();

    @Query("SELECT DISTINCT t FROM Media m JOIN m.tags t")
    List<String> findAllTags();

    // Dashboard Methods

    List<Media> findByContentIsNotNull();
    long countByFilePathIsNotNull();
    long countByUploadDateAfter(LocalDateTime date);
    List<Media> findByFilePathIsNotNull();
}

