package edu.tinkoff.imageeditorapi.dto.image;

import edu.tinkoff.imageeditorapi.entity.StatusResponse;
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
public class GetModifiedImageByRequestIdResponse {

    @Schema(description = "ИД модифицированного или оригинального файла в случае отсутствия первого",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID imageId;

    @Schema(description = "Статус обработки файла", requiredMode = Schema.RequiredMode.REQUIRED)
    private StatusResponse status;

}
