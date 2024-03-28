package edu.tinkoff.imageeditor.service;

import edu.tinkoff.imageeditor.entity.ImageMetaEntity;
import edu.tinkoff.imageeditor.repository.exception.FileReadException;
import edu.tinkoff.imageeditor.repository.exception.FileWriteException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

public interface ImageStorageService {

    MinioFileStorageService.FileSaveResult saveFile(MultipartFile file) throws FileWriteException;

    InputStream get(UUID id) throws FileReadException;

    ImageMetaEntity getMeta(UUID id);

    void delete(String filename) throws FileWriteException;
}
