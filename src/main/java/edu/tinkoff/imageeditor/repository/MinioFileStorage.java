package edu.tinkoff.imageeditor.repository;

import edu.tinkoff.imageeditor.repository.exception.FileReadException;
import edu.tinkoff.imageeditor.repository.exception.FileWriteException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MinioFileStorage {

    @Value("${minio.bucket-name}")
    private String bucketName;

    private final MinioClient minioClient;

    @PostConstruct
    @SneakyThrows
    public void createBucket() {
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());

        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName).build());
        }
    }

    /**
     * Saves object (file) to storage.
     * All object are saved to one bucket (${minio.bucket-name})
     * @param objectName Name to save object by
     * @param size Object size (bytes)
     * @param object Object as a stream
     * @return Response, holding data about written object
     * @throws FileWriteException If a file writing error accrued (e.g. object with this name already exists in storage)
     */
    public ObjectWriteResponse saveObject(String objectName, Long size, InputStream object) throws FileWriteException {
        try {
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(object, size, -1).build());
        } catch (Exception e) {
            throw new FileWriteException(e);
        }
    }

    /**
     * Checks if there is an object by this name in storage
     */
    public boolean isObjectExist(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName).build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getObject(String objectName) throws FileReadException {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName).build());
        } catch (Exception e) {
            throw new FileReadException(e);
        }
    }

    public void deleteObject(String objectName) throws FileWriteException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new FileWriteException(e);
        }
    }

    public List<String> getObjectList() {
        var iterable = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName).build());

        List<String> objectNameList = new LinkedList<>();
        for (Result<Item> result : iterable) {
            try {
                objectNameList.add(result.get().objectName());
            } catch (Exception ignored) {}
        }
        return objectNameList;
    }
}
