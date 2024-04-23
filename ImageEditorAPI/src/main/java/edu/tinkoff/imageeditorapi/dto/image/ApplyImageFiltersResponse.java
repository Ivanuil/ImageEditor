package edu.tinkoff.imageeditorapi.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@AllArgsConstructor
@Getter
@Setter
public class ApplyImageFiltersResponse {

    @Schema(description = "ИД запроса в системе", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID requestId;

}
