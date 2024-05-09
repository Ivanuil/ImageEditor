package edu.tinkoff.imageprocessor.repository;

import edu.tinkoff.imageprocessor.entity.ProcessedRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedRequestsRepository extends JpaRepository<ProcessedRequestEntity, Integer> {

    Boolean existsByRequestIdAndImageId(UUID requestId, UUID imageId);

}
