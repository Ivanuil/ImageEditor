package edu.tinkoff.imageprocessor.kafka.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ImageDoneMessage {

    private UUID imageId;

    private UUID requestId;

}
