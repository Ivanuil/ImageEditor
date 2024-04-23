package edu.tinkoff.imageeditorapi.web.controller;

import edu.tinkoff.imageeditorapi.constraints.FileExtensionConstraint;
import edu.tinkoff.imageeditorapi.dto.image.GetImagesResponse;
import edu.tinkoff.imageeditorapi.dto.UiSuccessContainer;
import edu.tinkoff.imageeditorapi.dto.image.Image;
import edu.tinkoff.imageeditorapi.dto.image.UploadImageResponse;
import edu.tinkoff.imageeditorapi.mapper.ImageMetaMapper;
import edu.tinkoff.imageeditorapi.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Tag(name = "Image Controller", description = "Базовый CRUD API для работы с картинками")
public class ImageController {

    private final ImageService imageService;

    private final ImageMetaMapper metaMapper;

    @PostMapping(value = "/image", consumes = "multipart/form-data")
    @Operation(summary = "Загрузка нового изображения в систему",
            description = """
                    В рамках данного метода необходимо:
                    1. Провалидировать файл. Максимальный размер файла - 10Мб, поддерживаемые расширения - png, jpeg.
                    1. Загрузить файл в S3 хранилище.
                    1. Сохранить в БД мета-данные файла -"""
                    + " название; размер; ИД файла в S3; ИД пользователя, которому файл принадлежит.\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                content = {@Content(schema = @Schema(implementation = UploadImageResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Файл не прошел валидацию",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))})
    })
    public ResponseEntity<?> uploadImage(
            @RequestPart("file") @Valid @FileExtensionConstraint final MultipartFile image,
            @AuthenticationPrincipal final UserDetails user) {
        var imageUUID = imageService.uploadImage(image, user.getUsername());
        return ResponseEntity.ok(new UploadImageResponse(imageUUID));
    }

    @GetMapping("/image/{image-id}")
    @Operation(summary = "Скачивание файла по ИД",
            description = """
                    В рамках данного метода необходимо:
                    1. Проверить, есть ли такой файл в системе.
                    1. Проверить, доступен ли данный файл пользователю.
                    1. Скачать файл.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(mediaType = "*/*", schema = @Schema(implementation = MultipartFile.class))}),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))})
    })
    @SneakyThrows
    public ResponseEntity<?> downloadImage(@PathVariable("image-id") final UUID imageId,
                                           @AuthenticationPrincipal final UserDetails user) {
        if (!imageService.getImageMeta(imageId).getAuthor().getUsername().equals(user.getUsername())) {
            return ResponseEntity.status(404)
                    .body(new UiSuccessContainer(false, "Image not found, or unavailable"));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                        + URLEncoder.encode(imageService.getImageMeta(imageId).getOriginalName(),
                                StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(imageService.downloadImage(imageId));
    }

    @DeleteMapping("/image/{image-id}")
    @Operation(summary = "Удаление файла по ИД",
            description = """
                    В рамках данного метода необходимо:
                    1. Проверить, есть ли такой файл в системе.
                    1. Проверить, доступен ли данный файл пользователю.
                    1. Удалить файл.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))}),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))})
    })
    public ResponseEntity<?> deleteImage(@PathVariable("image-id") final UUID imageId,
                                         @AuthenticationPrincipal final UserDetails user) {
        if (!imageService.getImageMeta(imageId).getAuthor().getUsername().equals(user.getUsername())) {
            return ResponseEntity.status(404)
                    .body(new UiSuccessContainer(false, "Image not found, or unavailable"));
        }

        imageService.deleteImage(imageId);
        return ResponseEntity.ok(new UiSuccessContainer(true, null));
    }

    @GetMapping("/images")
    @Operation(summary = "Получение списка изображений, которые доступны пользователю",
            description = """
                    В рамках данного метода необходимо:
                    1. Получить мета-информацию о всех изображениях, которые доступны пользователю
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(schema = @Schema(implementation = GetImagesResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = UiSuccessContainer.class))})
    })
    public ResponseEntity<?> getImages(@AuthenticationPrincipal final UserDetails user) {
        var imageMetaEntities = imageService.getImages(user.getUsername());
        var images = metaMapper.toImageList(imageMetaEntities);
        return ResponseEntity.ok(new GetImagesResponse(images.toArray(new Image[0])));
    }

}
