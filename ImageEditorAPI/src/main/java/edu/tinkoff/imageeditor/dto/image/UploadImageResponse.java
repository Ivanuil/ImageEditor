package edu.tinkoff.imageeditor.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class UploadImageResponse {

    @Schema(description = "ИД файла", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID imageId;

}
