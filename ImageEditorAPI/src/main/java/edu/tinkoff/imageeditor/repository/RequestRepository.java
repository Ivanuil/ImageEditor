package edu.tinkoff.imageeditor.repository;

import edu.tinkoff.imageeditor.entity.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestRepository extends JpaRepository<RequestEntity, UUID> {

}
