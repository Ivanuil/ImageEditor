package edu.tinkoff.imageeditorapi.kafka.messages;

import edu.tinkoff.imageeditorapi.entity.FilterType;
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
