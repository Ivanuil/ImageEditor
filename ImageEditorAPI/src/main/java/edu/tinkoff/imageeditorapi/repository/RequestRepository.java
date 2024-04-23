package edu.tinkoff.imageeditorapi.repository;

import edu.tinkoff.imageeditorapi.entity.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestRepository extends JpaRepository<RequestEntity, UUID> {

}
