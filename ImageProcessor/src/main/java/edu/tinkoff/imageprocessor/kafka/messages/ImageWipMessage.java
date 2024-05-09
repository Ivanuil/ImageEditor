package edu.tinkoff.imageprocessor.kafka.messages;

import edu.tinkoff.imageprocessor.entity.FilterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageWipMessage {

    private UUID imageId;

    private UUID requestId;

    private FilterType[] filters;

}
