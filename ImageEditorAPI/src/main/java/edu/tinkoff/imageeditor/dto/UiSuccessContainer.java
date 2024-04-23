package edu.tinkoff.imageeditor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UiSuccessContainer {

    @Schema(description = "Признак успеха", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean success;

    @Schema(description = "Сообщение об ошибке")
    private String message;

}
