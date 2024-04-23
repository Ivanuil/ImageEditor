package edu.tinkoff.imageprocessor.service.processors;

import edu.tinkoff.imageprocessor.entity.FilterType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

@Component
@ConditionalOnProperty(name = "filters.rotate", havingValue = "true")
public class ImageRotator implements ImageFilterProcessor {

    @Override
    public FilterType getFilterType() {
        return FilterType.ROTATE_90_DEGREES_CLOCKWISE;
    }

    @Override
    public InputStream process(final InputStream stream) throws IOException {
        BufferedImage image = ImageIO.read(stream);
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage newImage = new BufferedImage(width, height, image.getType());
        var graphics = newImage.createGraphics();
        graphics.rotate(Math.toRadians(90), (double) width / 2, (double) height / 2);
        graphics.drawImage(image, null, 0, 0);

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(newImage, "PNG", outputStream);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}
