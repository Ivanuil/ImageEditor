package edu.tinkoff.imageeditor.service;

import edu.tinkoff.imageeditor.entity.ImageMetaEntity;
import edu.tinkoff.imageeditor.entity.UserEntity;
import edu.tinkoff.imageeditor.repository.ImageMetaRepository;
import edu.tinkoff.imageeditor.repository.exception.FileReadException;
import edu.tinkoff.imageeditor.repository.exception.FileWriteException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ImageService {

    private final ImageStorageService imageStorageService;
    private final ImageMetaRepository metaRepository;

    public UUID uploadImage(MultipartFile image,
                            String username) throws ConstraintViolationException {
        MinioFileStorageService.FileSaveResult res;
        try {
            res = imageStorageService.saveFile(image);
        } catch (FileWriteException e) {
            throw new RuntimeException(e);
        }

        ImageMetaEntity meta = new ImageMetaEntity();
        meta.setId(res.getSavedFilename());
        meta.setOriginalName(image.getOriginalFilename());
        meta.setSize((int) image.getSize());
        meta.setAuthor(new UserEntity(username, null, null));

        metaRepository.save(meta);
        return meta.getId();

    }

    public InputStreamResource downloadImage(UUID imageId) throws FileReadException {
        if (!metaRepository.existsById(imageId))
            throw new EntityNotFoundException("No image with id: " + imageId);

        InputStream fileInputStream = imageStorageService.get(imageId);
        return new InputStreamResource(fileInputStream);
    }

    public ImageMetaEntity getImageMeta(UUID imageID) {
        return imageStorageService.getMeta(imageID);
    }

    @SneakyThrows
    public void deleteImage(UUID imageID) {
        if (!metaRepository.existsById(imageID))
            throw new EntityNotFoundException("No image with id: " + imageID);
        imageStorageService.delete(imageID.toString());
        metaRepository.deleteById(imageID);
    }

    public List<ImageMetaEntity> getImages(String username) {
        return metaRepository.findAllByAuthor_Username(username);
    }

}
