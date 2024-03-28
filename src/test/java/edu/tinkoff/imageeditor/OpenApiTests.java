package edu.tinkoff.imageeditor;

import org.junit.jupiter.api.Test;
import org.openapitools.openapidiff.core.OpenApiCompare;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;
import org.openapitools.openapidiff.core.output.HtmlRender;
import org.openapitools.openapidiff.core.output.MarkdownRender;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenApiTests extends TestContext {

    private static final String specPath = "openAPI/spec.yaml";
    private static final String reportDiffPath = "openAPI/diff";
    private static final String reportDocPath = "openAPI/api-docs";
    private static final String apiDocsURL = "http://localhost:8080/api/v1/v3/api-docs";

    @Test
    void testApi() {
        ChangedOpenApi diff = OpenApiCompare.fromLocations(specPath, apiDocsURL);

        writeDiffToMD(diff);
        writeDiffToHTML(diff);
        try {
            writeDocToJSON();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertTrue(diff.isUnchanged());
    }

    private static void writeDiffToMD(ChangedOpenApi diff) {
        String render = new MarkdownRender().render(diff);
        try {
            FileWriter fw = new FileWriter(reportDiffPath + ".md");
            fw.write(render);
            fw.close();
        } catch (IOException ignored) {}
    }

    private static void writeDiffToHTML(ChangedOpenApi diff) {
        String html = new HtmlRender("Changelog",
                "https://deepoove.com/swagger-diff/stylesheets/demo.css")
                .render(diff);
        try {
            FileWriter fw = new FileWriter(reportDiffPath + ".html");
            fw.write(html);
            fw.close();
        } catch (IOException ignored) {}
    }

    private static void writeDocToJSON() throws IOException {
        FileUtils.copyURLToFile(
                new URL(apiDocsURL),
                new File(reportDocPath + ".json"),
                1000,
                1000);
    }

}
