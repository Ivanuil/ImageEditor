package edu.tinkoff.imageprocessor.service.processors;

import edu.tinkoff.imageprocessor.entity.FilterType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
@ConditionalOnProperty(name = "filters.gaussian-blur", havingValue = "true")
public class GaussianBlurProcessor implements ImageFilterProcessor {

    @Override
    public FilterType getFilterType() {
        return FilterType.GAUSSIAN_BLUR;
    }

    @Override
    public InputStream process(final InputStream stream) throws IOException {
        BufferedImage image = ImageIO.read(stream);

        BufferedImage newImage = blur(toGrayScale(image));

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(newImage, "PNG", outputStream);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public static BufferedImage toGrayScale(final BufferedImage img) {
        BufferedImage grayImage = new BufferedImage(
                img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return grayImage;
    }

    public static BufferedImage blur(final BufferedImage img) {
        BufferedImage blurImg = new BufferedImage(
                img.getWidth() - 2, img.getHeight() - 2, BufferedImage.TYPE_BYTE_GRAY);
        int pix;
        for (int y = 0; y < blurImg.getHeight(); y++) {
            for (int x = 0; x < blurImg.getWidth(); x++) {
                pix = (4 * (img.getRGB(x + 1, y + 1) & 0xFF)
                        + 2 * (img.getRGB(x + 1, y) & 0xFF)
                        + 2 * (img.getRGB(x + 1, y + 2) & 0xFF)
                        + 2 * (img.getRGB(x, y + 1) & 0xFF)
                        + 2 * (img.getRGB(x + 2, y + 1) & 0xFF)
                        + (img.getRGB(x, y) & 0xFF)
                        + (img.getRGB(x, y + 2) & 0xFF)
                        + (img.getRGB(x + 2, y) & 0xFF)
                        + (img.getRGB(x + 2, y + 2) & 0xFF)) / 16;
                int p = (255 << 24) | (pix << 16) | (pix << 8) | pix;
                blurImg.setRGB(x, y, p);
            }
        }
        return blurImg;
    }

}
