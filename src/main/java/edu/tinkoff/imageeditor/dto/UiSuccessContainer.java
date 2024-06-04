package edu.tinkoff.imageeditor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UiSuccessContainer {

    @Schema(description = "Признак успеха", requiredMode = Schema.RequiredMode.REQUIRED)
    public boolean success;

    @Schema(description = "Сообщение об ошибке")
    public String message;

}
