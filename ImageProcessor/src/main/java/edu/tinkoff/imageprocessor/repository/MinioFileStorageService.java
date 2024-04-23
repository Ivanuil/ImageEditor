package edu.tinkoff.imageprocessor.repository;

import edu.tinkoff.imageprocessor.repository.exception.FileReadException;
import edu.tinkoff.imageprocessor.repository.exception.FileWriteException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements ImageStorageService {

    private final MinioFileStorage imageStorage;

    /**
     * Save file in the Minio storage with file extension as prefix
     *
     * @param file file to save
     * @return generated file name
     */
    public UUID saveFile(final InputStream inputStream) throws FileWriteException {

        UUID generatedFileName = UUID.randomUUID();
        try {
            imageStorage.saveObject(String.valueOf(generatedFileName), inputStream);
        } catch (FileWriteException e) {
            throw new FileWriteException(e);
        }

        return generatedFileName;
    }

    @Override
    public InputStream get(final UUID id) throws FileReadException {
        return imageStorage.getObject(id.toString());
    }

}
