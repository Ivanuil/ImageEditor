package edu.tinkoff.imageprocessor.dto;

import lombok.Data;

// Example:
// {
//  "result": {
//    "tags": [
//      {
//        "confidence": 61.4116096496582,
//        "tag": {
//          "en": "mountain"
//        }
//      },
//      {
//        "confidence": 54.3507270812988,
//        "tag": {
//          "en": "landscape"
//        }
//      }
//    ]
//  },
//  "status": {
//    "text": "",
//    "type": "success"
//  }
//}
@Data
public class ImaggaTagsResponseDto {

    private Result result;
    private Status status;

    @Data
    public static class Result {
        private Tag[] tags;
    }

    @Data
    public static class Tag {
        private double confidence;
        private TagInner tag;
    }

    @Data
    public static class TagInner {
        private String en;
    }

    @Data
    public static class Status {
        private String text;
        private String type;
    }

}
