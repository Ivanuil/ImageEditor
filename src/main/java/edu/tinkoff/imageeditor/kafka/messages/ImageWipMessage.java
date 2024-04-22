package edu.tinkoff.imageeditor.kafka.messages;

import edu.tinkoff.imageeditor.entity.FilterType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ImageWipMessage {

    private UUID imageId;

    private UUID requestId;

    private FilterType[] filters;

}
