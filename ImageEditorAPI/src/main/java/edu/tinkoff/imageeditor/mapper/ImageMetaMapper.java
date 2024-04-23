package edu.tinkoff.imageeditor.mapper;

import edu.tinkoff.imageeditor.dto.image.Image;
import edu.tinkoff.imageeditor.entity.ImageMetaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMetaMapper {

    List<Image> toImageList(List<ImageMetaEntity> meta);

    @Mapping(source = "id", target = "imageId")
    @Mapping(source = "originalName", target = "filename")
    Image toImage(ImageMetaEntity meta);

}
