package edu.tinkoff.imageeditor.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public class Image {

    @Schema(description = "ИД файла")
    public UUID imageId;

    @Schema(description = "Название изображения", requiredMode = Schema.RequiredMode.REQUIRED)
    public String filename;

    @Schema(description = "Размер файла в байтах", requiredMode = Schema.RequiredMode.REQUIRED)
    public Integer size;

}
