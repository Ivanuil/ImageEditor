package edu.tinkoff.imageprocessor.repository;

import edu.tinkoff.imageprocessor.repository.exception.FileReadException;
import edu.tinkoff.imageprocessor.repository.exception.FileWriteException;
import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.SetBucketLifecycleArgs;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.LifecycleRule;
import io.minio.messages.Status;
import io.minio.messages.Expiration;
import io.minio.messages.RuleFilter;
import io.minio.messages.Tag;
import io.minio.messages.LifecycleConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MinioFileStorage {

    @Value("${minio.bucket-name}")
    private String bucketName;
    @Value("${image.ttl-days}")
    private Integer ttlDays;

    private static final String TYPE_TAG_NAME = "type";
    private static final String TYPE_TEMPORARY = "temp";
    private static final String TYPE_PERMANENT = "permanent";

    private final MinioClient minioClient;

    @PostConstruct
    public void setupBuckets() {
        createBucket();
        addTTLRule();
    }

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

    @SneakyThrows
    public void addTTLRule() {
        List<LifecycleRule> rules = new LinkedList<>();
        rules.add(new LifecycleRule(
                Status.ENABLED,
                null,
                new Expiration((ZonedDateTime) null, ttlDays, null),
                new RuleFilter(new Tag(TYPE_TAG_NAME, TYPE_TEMPORARY)),
                "TTL-Rule",
                null,
                null,
                null
        ));
        LifecycleConfiguration config = new LifecycleConfiguration(rules);
        minioClient.setBucketLifecycle(
                SetBucketLifecycleArgs.builder()
                        .bucket(bucketName)
                        .config(config).build());
    }

    /**
     * Saves object (file) to storage.
     * All object are saved to one bucket (${minio.bucket-name})
     * @param objectName Name to save object by
     * @param object Object as a stream
     * @return Response, holding data about written object
     * @throws FileWriteException If a file writing error accrued (e.g. object with this name already exists in storage)
     */
    public ObjectWriteResponse saveObject(final String objectName, final InputStream object, final boolean isTemporary)
            throws FileWriteException {
        try {
            Map<String, String> tagMap = new HashMap<>();
            if (isTemporary) {
                tagMap.put(TYPE_TAG_NAME, TYPE_TEMPORARY);
            } else {
                tagMap.put(TYPE_TAG_NAME, TYPE_PERMANENT);
            }

            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .tags(tagMap)
                    .stream(object, -1, 5 * 1024 * 1024).build());
        } catch (Exception e) {
            throw new FileWriteException(e);
        }
    }

    /**
     * Checks if there is an object by this name in storage
     */
    public boolean isObjectExist(final String objectName) {
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

    public InputStream getObject(final String objectName) throws FileReadException {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName).build());
        } catch (Exception e) {
            throw new FileReadException(e);
        }
    }

}
