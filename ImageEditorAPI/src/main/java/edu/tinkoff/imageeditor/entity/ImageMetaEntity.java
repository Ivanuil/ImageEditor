package edu.tinkoff.imageeditor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity(name = "image_meta")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
