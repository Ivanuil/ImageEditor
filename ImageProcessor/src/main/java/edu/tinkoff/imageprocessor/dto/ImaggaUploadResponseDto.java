package edu.tinkoff.imageprocessor.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

// Example:
// {
//  "result": {
//    "upload_id": "i05e132196706b94b1d85efb5f3SaM1j"
//  },
//  "status": {
//    "text": "",
//    "type": "success"
//  }
// }
@Data
public class ImaggaUploadResponseDto {

    private Result result;

    private Status status;

    @Data
    public static class Result {

        @JsonAlias("upload_id")
        private String uploadId;

    }

    @Data
    public static class Status {

        private String text;

        private String type;

    }

}
