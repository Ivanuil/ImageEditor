package edu.tinkoff.imageprocessor.service.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tinkoff.imageprocessor.exceptions.ProcessorFailedException;
import edu.tinkoff.imageprocessor.exceptions.RequestFailedException;
import edu.tinkoff.imageprocessor.entity.FilterType;
import edu.tinkoff.imageprocessor.dto.ImaggaTagsResponseDto;
import edu.tinkoff.imageprocessor.dto.ImaggaUploadResponseDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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
    private String imaggaApiKey;
    @Value("${filters.object-recognition.api-secret}")
    private String imaggaApiSecret;

    private static String credentialsToEncode;
    private static String basicAuth;

    private static final String IMAGGA_URL =  "https://api.imagga.com/v2";

    private final ObjectMapper mapper;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private CircuitBreaker circuitBreaker;

    @PostConstruct
    public void init() {
        credentialsToEncode = String.format("%s:%s", imaggaApiKey, imaggaApiSecret);
        basicAuth = Base64.getEncoder()
                .encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8));

        circuitBreaker = circuitBreakerFactory.create("imagga-integration-circuit-breaker");
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.OBJECT_RECOGNITION;
    }

    @Override
    public InputStream process(final InputStream stream) throws IOException {
        System.out.println(credentialsToEncode);
        System.out.println(basicAuth);

        // Cloning InputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stream.transferTo(baos);
        InputStream firstStream = new ByteArrayInputStream(baos.toByteArray());
        InputStream secondStream = new ByteArrayInputStream(baos.toByteArray());

        var topTags = circuitBreaker.run(() -> {
                    try {
                        return getTagsForImage(firstStream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                throwable -> {
                    log.warn("Circuit breaker failed", throwable);
                    throw new ProcessorFailedException("Circuit breaker failed");
                });

        return markTags(secondStream, topTags);
    }

    private List<ImaggaTagsResponseDto.Tag> getTagsForImage(final InputStream stream) throws IOException {

        // Upload image
        String uploadRes = uploadImage(stream);
        var uploadResObj = mapper.readValue(uploadRes, ImaggaUploadResponseDto.class);
        if (!uploadResObj.getStatus().getType().equals("success")) {
            log.error("Unable to process imagga upload, received response: {}", uploadRes);
            throw new RuntimeException("Processor failed");
        }

        // Get tags
        String tagsRes = fetchTags(uploadResObj.getResult().getUploadId());
        var tagsResObj = mapper.readValue(tagsRes, ImaggaTagsResponseDto.class);
        if (!tagsResObj.getStatus().getType().equals("success")) {
            log.error("Unable to process imagga tags, received response: {}", tagsRes);
            throw new RuntimeException("Processor failed");
        }

        return Arrays.stream(tagsResObj.getResult().getTags())
                .sorted(Comparator.comparingDouble(ImaggaTagsResponseDto.Tag::getConfidence))
                .limit(3).toList();
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

    @Retryable(retryFor = {RequestFailedException.class}, backoff = @Backoff(delay = 100))
    private static String uploadImage(final InputStream inputStream) throws IOException {
        // Change the file path here
        String filepath = "path_to_image";
        File fileToUpload = new File(filepath);

        String endpoint = "/uploads";

        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "Image Upload";

        HttpURLConnection connection = getHttpURLConnectionForUpload(endpoint, boundary);

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

        int responseCode = connection.getResponseCode();
        if (responseCode == 429 || (responseCode >= 500 && responseCode < 600)) {
            throw new RequestFailedException("Upload image request failed with response code: " + responseCode);
        } else if (responseCode != 200) {
            throw new ProcessorFailedException("Upload image request failed. Response code: " + responseCode
                + " Response message: " + connection.getResponseMessage());
        }

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
            final String endpoint, final String boundary) throws IOException {
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

    @Retryable(retryFor = {RequestFailedException.class}, backoff = @Backoff(delay = 100))
    private static String fetchTags(final String imageId) throws IOException {
        URL url = new URL(IMAGGA_URL + "/tags" + "?image_upload_id=" + imageId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Authorization", "Basic " + basicAuth);

        int responseCode = connection.getResponseCode();
        if (responseCode == 429 || (responseCode >= 500 && responseCode < 600)) {
            throw new RequestFailedException("Fetch tags request failed with response code: " + responseCode);
        } else if (responseCode != 200) {
            throw new ProcessorFailedException("Fetch tags request failed with response code: " + responseCode);
        }

        BufferedReader connectionInput = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String jsonResponse = connectionInput.readLine();

        connectionInput.close();

        return jsonResponse;
    }

}
