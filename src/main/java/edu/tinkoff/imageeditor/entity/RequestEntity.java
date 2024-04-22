package edu.tinkoff.imageeditor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity(name = "request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestEntity {

    @Id
    private UUID id;

    @Column(name = "original_image_id")
    private UUID originalImageId;

    @Column(name = "modified_image_id")
    private UUID modifiedImageId;

    @Column(name = "requester_username")
    private String requesterUsername;

    @Enumerated(EnumType.STRING)
    private StatusResponse status;

}
