package edu.tinkoff.imageeditorapi.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
public class GetImagesResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Image[] images;

}
