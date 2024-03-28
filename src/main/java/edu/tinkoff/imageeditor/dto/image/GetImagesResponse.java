package edu.tinkoff.imageeditor.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetImagesResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    public Image[] images;

}
