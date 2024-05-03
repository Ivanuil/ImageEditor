package edu.tinkoff.imageprocessor.service;

import edu.tinkoff.imageprocessor.entity.FilterType;
import edu.tinkoff.imageprocessor.entity.ProcessedRequestEntity;
import edu.tinkoff.imageprocessor.kafka.messages.ImageDoneMessage;
import edu.tinkoff.imageprocessor.kafka.messages.ImageWipMessage;
import edu.tinkoff.imageprocessor.kafka.producer.KafkaImageDoneProducer;
import edu.tinkoff.imageprocessor.kafka.producer.KafkaImageWipProducer;
import edu.tinkoff.imageprocessor.repository.ImageStorageService;
import edu.tinkoff.imageprocessor.repository.ProcessedRequestsRepository;
import edu.tinkoff.imageprocessor.repository.exception.FileReadException;
import edu.tinkoff.imageprocessor.repository.exception.FileWriteException;
import edu.tinkoff.imageprocessor.service.processors.ImageFilterProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;

@Service
@Slf4j
public class FilterProcessingService {

    private final Map<FilterType, ImageFilterProcessor> processorMap = new HashMap<>();
    private final KafkaImageWipProducer imageWipProducer;
    private final KafkaImageDoneProducer imageDoneProducer;
    private final ImageStorageService fileStorage;
    private final ProcessedRequestsRepository processedRequestsRepository;

    public FilterProcessingService(final Set<ImageFilterProcessor> processorSet,
                                   final KafkaImageWipProducer imageWipProducer,
                                   final KafkaImageDoneProducer imageDoneProducer,
                                   final ImageStorageService fileStorage,
                                   final ProcessedRequestsRepository processedRequestsRepository) {
        this.imageWipProducer = imageWipProducer;
        this.imageDoneProducer = imageDoneProducer;
        this.fileStorage = fileStorage;
        this.processedRequestsRepository = processedRequestsRepository;
        for (var processor : processorSet) {
            processorMap.put(processor.getFilterType(), processor);
        }
    }

    public boolean isFilterAvailable(final FilterType filterType) {
        return processorMap.containsKey(filterType);
    }

    public boolean isAlreadyProcessed(final UUID requestId, final UUID imageId) {
        return processedRequestsRepository.existsByRequestIdAndImageId(requestId, imageId);
    }

    public void applyFilter(final UUID requestId, final UUID imageId, final FilterType[] filters)
            throws FileReadException, IOException, FileWriteException {
        var inputStream = fileStorage.get(imageId);
        var processor = processorMap.get(filters[0]);
        var before = System.currentTimeMillis();
        var outputStream = processor.process(inputStream);
        var after = System.currentTimeMillis();

        UUID newImageId;
        if (filters.length == 1) {  // No filters left
            newImageId = fileStorage.saveFile(outputStream, false);
        } else {  // One or more filters left
            newImageId = fileStorage.saveFile(outputStream, true);
        }

        processedRequestsRepository.save(new ProcessedRequestEntity(null, requestId, imageId));

        if (filters.length == 1) {  // No filters left
            log.info("Finished processing request {} on image {} with filters {} in {} ms",
                    requestId, imageId, filters, after - before);
            imageDoneProducer.sendMessage(new ImageDoneMessage(newImageId, requestId));
        } else {  // One or more filters left
            FilterType[] newFilters = Arrays.copyOfRange(filters, 1, filters.length);
            imageWipProducer.sendMessage(new ImageWipMessage(newImageId, requestId, newFilters));
        }
    }

}
