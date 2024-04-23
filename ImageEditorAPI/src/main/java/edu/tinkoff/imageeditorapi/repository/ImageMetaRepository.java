package edu.tinkoff.imageeditorapi.repository;

import edu.tinkoff.imageeditorapi.entity.ImageMetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageMetaRepository extends JpaRepository<ImageMetaEntity, UUID> {

    List<ImageMetaEntity> findAllByAuthor_Username(String username);

}
