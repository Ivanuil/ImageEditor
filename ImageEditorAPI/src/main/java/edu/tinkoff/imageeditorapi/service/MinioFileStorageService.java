package edu.tinkoff.imageeditorapi.service;

import edu.tinkoff.imageeditorapi.entity.ImageMetaEntity;
import edu.tinkoff.imageeditorapi.repository.ImageMetaRepository;
import edu.tinkoff.imageeditorapi.repository.MinioFileStorage;
import edu.tinkoff.imageeditorapi.repository.exception.FileReadException;
import edu.tinkoff.imageeditorapi.repository.exception.FileWriteException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements ImageStorageService {

    private final MinioFileStorage imageStorage;
    private final ImageMetaRepository metaRepository;

    /**
     * Save file in the Minio storage with file extension as prefix
     * @param file file to save
     * @return generated file name
     */
    public FileSaveResult saveFile(final MultipartFile file) throws FileWriteException {

        UUID generatedFileName = UUID.randomUUID();
        try {
            imageStorage.saveObject(String.valueOf(generatedFileName), file.getSize(), file.getInputStream());
        } catch (FileWriteException | IOException e) {
            throw new FileWriteException(e);
        }

        return new FileSaveResult(file.getOriginalFilename(), generatedFileName);
    }

    @Override
    public InputStream get(final UUID id) throws FileReadException {
        return imageStorage.getObject(String.valueOf(id));
    }

    @Override
    public ImageMetaEntity getMeta(final UUID id) {
        return metaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Image not found"));
    }

    @Override
    public void delete(final String filename) throws FileWriteException {
        imageStorage.deleteObject(filename);
    }

    @Override
    public long getSize(final String filename) throws FileReadException {
        return imageStorage.getObjectSize(filename);
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class FileSaveResult {
        private String originalName;
        private UUID savedFilename;
    }
}
