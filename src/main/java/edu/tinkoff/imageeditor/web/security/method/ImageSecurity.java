package edu.tinkoff.imageeditor.web.security.method;

import edu.tinkoff.imageeditor.repository.ImageMetaRepository;
import edu.tinkoff.imageeditor.web.security.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component("imageSecurity")
public class ImageSecurity {

    private final ImageMetaRepository metaRepository;

    public boolean isOwner(Authentication auth, UUID imageId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        var image = metaRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("No image with such id"));
        return image.getAuthor().getUsername().equals(userDetails.getUsername());
    }

}
