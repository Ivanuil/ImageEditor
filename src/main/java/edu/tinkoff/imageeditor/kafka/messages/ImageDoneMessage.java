package edu.tinkoff.imageeditor.kafka.messages;

import lombok.Data;

import java.util.UUID;

@Data
public class ImageDoneMessage {

    private UUID imageId;

    private UUID requestId;

}
