package edu.tinkoff.imageeditorapi.service;

import edu.tinkoff.imageeditorapi.entity.ImageMetaEntity;
import edu.tinkoff.imageeditorapi.repository.exception.FileReadException;
import edu.tinkoff.imageeditorapi.repository.exception.FileWriteException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

public interface ImageStorageService {

    MinioFileStorageService.FileSaveResult saveFile(MultipartFile file) throws FileWriteException;

    InputStream get(UUID id) throws FileReadException;

    ImageMetaEntity getMeta(UUID id);

    void delete(String filename) throws FileWriteException;
}
