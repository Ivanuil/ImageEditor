package edu.tinkoff.imageprocessor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "processed_requests")
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "processed_request_seq")
    @SequenceGenerator(name = "processed_request_seq", sequenceName = "processed_request_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "image_id")
    private UUID imageId;

}
