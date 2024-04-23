package edu.tinkoff.imageprocessor.repository;

import edu.tinkoff.imageprocessor.repository.exception.FileReadException;
import edu.tinkoff.imageprocessor.repository.exception.FileWriteException;

import java.io.InputStream;
import java.util.UUID;

public interface ImageStorageService {

    UUID saveFile(InputStream inputStream) throws FileWriteException;

    InputStream get(UUID id) throws FileReadException;

}
