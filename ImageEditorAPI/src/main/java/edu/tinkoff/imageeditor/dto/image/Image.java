package edu.tinkoff.imageeditor.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Image {

    @Schema(description = "ИД файла")
    private UUID imageId;

    @Schema(description = "Название изображения", requiredMode = Schema.RequiredMode.REQUIRED)
    private String filename;

    @Schema(description = "Размер файла в байтах", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer size;

}
