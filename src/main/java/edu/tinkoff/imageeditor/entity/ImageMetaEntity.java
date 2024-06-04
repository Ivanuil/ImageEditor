package edu.tinkoff.imageeditor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity(name = "image_meta")
@Setter
@Getter
public class ImageMetaEntity {

    @Id
    private UUID id;

    @Column(name = "origin_name")
    private String originalName;

    private Integer size;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_username")
    private UserEntity author;

}
