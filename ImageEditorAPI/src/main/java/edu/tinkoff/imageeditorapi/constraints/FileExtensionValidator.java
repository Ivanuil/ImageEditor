package edu.tinkoff.imageeditorapi.constraints;

import edu.tinkoff.imageeditorapi.config.AllowedImageExtension;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileExtensionValidator implements ConstraintValidator<FileExtensionConstraint, MultipartFile> {

    @Override
    public boolean isValid(final MultipartFile file, final ConstraintValidatorContext constraintValidatorContext) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        try {
            AllowedImageExtension.caseIgnoreValueOf(extension);
        } catch (Exception e) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("File extension not allowed")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

}
