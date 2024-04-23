package edu.tinkoff.imageeditorapi.web.controller;

import edu.tinkoff.imageeditorapi.dto.UiSuccessContainer;
import edu.tinkoff.imageeditorapi.dto.image.ApplyImageFiltersResponse;
import edu.tinkoff.imageeditorapi.dto.image.GetModifiedImageByRequestIdResponse;
import edu.tinkoff.imageeditorapi.entity.FilterType;
import edu.tinkoff.imageeditorapi.entity.StatusResponse;
import edu.tinkoff.imageeditorapi.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@RestController
@RequestMapping("/image")
@Tag(name = "Image Filters Controller",
        description = "Базовый CRUD API для работы с пользовательскими запросами на редактирование картинок")
@RequiredArgsConstructor
public class ImageFiltersController {

    private final RequestService requestService;

    @PostMapping("{image-id}/filters/apply")
    @Operation(summary = "Применение указанных фильтров к изображению",
            description = """
                    В рамках данного метода необходимо:
                    1. Проверить, есть ли у пользователя доступ к файлу
                    1. Сохранить в БД новый запрос на изменение файла:
                        1. статус = WIP
                        2. ИД оригинальной картинки = ИД оригинального файла
                        3. ИД измененной картинки = null
                        4. ИД запроса = уникальный ИД запроса в системе
                    1. Отправить в Kafka событие о создании запроса
                    1. Убедиться, что шаг 3 выполнен успешно, в противном случае выполнить повторную попытку
                    1. Вернуть пользователю ИД его запроса
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(schema = @Schema(implementation = ApplyImageFiltersResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))})
    })
    public ResponseEntity<?> applyImageFilters(@PathVariable("image-id") final UUID imageId,
                                               @RequestParam("filters") final FilterType[] filterTypes,
                                               @AuthenticationPrincipal final UserDetails userDetails) {
        var request = requestService.createRequest(imageId, userDetails.getUsername(), filterTypes);
        return ResponseEntity.ok(new ApplyImageFiltersResponse(request.getId()));
    }

    @GetMapping("{image-id}/filters/{request-id}")
    @Operation(summary = "Получение ИД измененного файла по ИД запроса",
            description = """
                    В рамках данного метода необходимо найти и вернуть по ИД пользовательского запроса\s
                    ИД соответсвующего ему файла и статус, в котором находится процесс применения фильтров.
                    По ИД оригинального изображения нужно убедиться, что ИД запроса относится к нему и\s
                    что у пользователя есть доступ к данному изображению (оригинальному).
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(schema = @Schema(implementation = GetModifiedImageByRequestIdResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))})
    })
    public ResponseEntity<?> getModifiedImageByRequestId(@PathVariable("image-id") final UUID imageId,
                                                         @PathVariable("request-id") final UUID requestId) {
        var request = requestService.getRequest(requestId, imageId);
        if (request.getStatus() == StatusResponse.WIP) {
            return ResponseEntity.ok(new GetModifiedImageByRequestIdResponse(
                    request.getOriginalImageId(), request.getStatus()));
        } else {
            return ResponseEntity.ok(new GetModifiedImageByRequestIdResponse(
                    request.getModifiedImageId(), request.getStatus()));
        }
    }

}
