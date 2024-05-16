package edu.tinkoff.imageprocessor.service.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tinkoff.imageprocessor.entity.FilterType;
import edu.tinkoff.imageprocessor.dto.ImaggaTagsResponseDto;
import edu.tinkoff.imageprocessor.dto.ImaggaUploadResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.AttributedString;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
@ConditionalOnProperty(name = "filters.object-recognition", havingValue = "true")
public class ObjectRecognitionProcessor implements ImageFilterProcessor {

    @Value("${filters.object-recognition.api-key}")
    private static String imaggaApiKey;
    @Value("${filters.object-recognition.api-secret}")
    private static String imaggaApiSecret;

    private static final String CREDENTIALS_TO_ENCODE = String.format("%s:%s", imaggaApiKey, imaggaApiSecret);
    private static final String BASIC_AUTH = Base64.getEncoder()
            .encodeToString(CREDENTIALS_TO_ENCODE.getBytes(StandardCharsets.UTF_8));

    private static final String IMAGGA_URL =  "https://api.imagga.com/v2";

    private final ObjectMapper mapper;

    @Override
    public FilterType getFilterType() {
        return FilterType.OBJECT_RECOGNITION;
    }

    @Override
    public InputStream process(final InputStream stream) throws IOException {

        // Cloning InputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stream.transferTo(baos);
        InputStream firstStream = new ByteArrayInputStream(baos.toByteArray());
        InputStream secondStream = new ByteArrayInputStream(baos.toByteArray());

        // Upload image
        String uploadRes = upload(firstStream);
        var uploadResObj = mapper.readValue(uploadRes, ImaggaUploadResponseDto.class);
        if (!uploadResObj.getStatus().getType().equals("success")) {
            log.error("Unable to process imagga upload, received response: {}", uploadRes);
            throw new RuntimeException("Processor failed");
        }

        // Get tags
        String tagsRes = getTags(uploadResObj.getResult().getUploadId());
        var tagsResObj = mapper.readValue(tagsRes, ImaggaTagsResponseDto.class);
        if (!tagsResObj.getStatus().getType().equals("success")) {
            log.error("Unable to process imagga tags, received response: {}", tagsRes);
            throw new RuntimeException("Processor failed");
        }

        var topTags = Arrays.stream(tagsResObj.getResult().getTags())
                .sorted(Comparator.comparingDouble(ImaggaTagsResponseDto.Tag::getConfidence))
                .limit(3).toList();

        return markTags(secondStream, topTags);
    }

    private InputStream markTags(final InputStream stream,
                                 final List<ImaggaTagsResponseDto.Tag> tags) throws IOException {
        StringBuilder stringBuilder = new StringBuilder("Tags: ");
        for (var tag : tags) {
            stringBuilder.append("%s(%.2f%%) ".formatted(tag.getTag().getEn(), tag.getConfidence()));
        }
        String text = stringBuilder.toString();

        BufferedImage image = ImageIO.read(stream);
        Graphics graphics = image.getGraphics();

        // Text style
        Font font = new Font("Arial", Font.BOLD, 18);
        AttributedString attributedText = new AttributedString(text);
        attributedText.addAttribute(TextAttribute.FONT, font);
        attributedText.addAttribute(TextAttribute.BACKGROUND, Color.BLACK);
        attributedText.addAttribute(TextAttribute.FOREGROUND, Color.WHITE);

        // Drawing text
        FontMetrics metrics = graphics.getFontMetrics(font);
        int posX = 0;
        int posY = image.getHeight() - metrics.getHeight() + metrics.getAscent();
        graphics.drawString(attributedText.getIterator(), posX, posY);

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private static String upload(final InputStream inputStream) throws IOException {
        // Change the file path here
        String filepath = "path_to_image";
        File fileToUpload = new File(filepath);

        String endpoint = "/uploads";

        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "Image Upload";

        HttpURLConnection connection = getHttpURLConnectionForUpload(endpoint, BASIC_AUTH, boundary);

        DataOutputStream request = new DataOutputStream(connection.getOutputStream());

        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\""
                + fileToUpload.getName() + "\"" + crlf);
        request.writeBytes(crlf);

        int bytesRead;
        byte[] dataBuffer = new byte[1024];
        while ((bytesRead = inputStream.read(dataBuffer)) != -1) {
            request.write(dataBuffer, 0, bytesRead);
        }

        request.writeBytes(crlf);
        request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
        request.flush();
        request.close();

        InputStream responseStream = new BufferedInputStream(connection.getInputStream());

        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

        String line;
        StringBuilder stringBuilder = new StringBuilder();

        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();

        String response = stringBuilder.toString();

        responseStream.close();
        connection.disconnect();

        return response;
    }

    private static HttpURLConnection getHttpURLConnectionForUpload(
            final String endpoint, final String basicAuth, final String boundary) throws IOException {
        URL urlObject = new URL("https://api.imagga.com/v2" + endpoint);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + basicAuth);
        connection.setUseCaches(false);
        connection.setDoOutput(true);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty(
                "Content-Type", "multipart/form-data;boundary=" + boundary);
        return connection;
    }

    private static String getTags(final String imageId) throws IOException {
        URL url = new URL(IMAGGA_URL + "/tags" + "?image_upload_id=" + imageId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Authorization", "Basic " + BASIC_AUTH);

        BufferedReader connectionInput = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String jsonResponse = connectionInput.readLine();

        connectionInput.close();

        return jsonResponse;
    }

}
