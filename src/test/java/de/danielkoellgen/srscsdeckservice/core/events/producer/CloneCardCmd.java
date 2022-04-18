package de.danielkoellgen.srscsdeckservice.core.events.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscsdeckservice.commands.deckcards.dto.CloneCardDto;
import de.danielkoellgen.srscsdeckservice.domain.domainprimitive.EventDateTime;
import de.danielkoellgen.srscsdeckservice.events.producer.AbstractProducerEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class CloneCardCmd extends AbstractProducerEvent {

    private final @NotNull CloneCardDto payloadDto;

    public static final String eventName = "clone-card";

    public static final String eventTopic = "cmd.decks-cards.0";

    public CloneCardCmd(@NotNull UUID transactionId, @NotNull CloneCardDto payloadDto) {
        super(UUID.randomUUID(), transactionId, eventName, eventTopic,
                EventDateTime.makeFromLocalDateTime(LocalDateTime.now())
        );
        this.payloadDto = payloadDto;
    }

    @Override
    public @NotNull String getSerializedContent() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        try {
            return objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            throw new RuntimeException("ObjectMapper conversion failed.");
        }
    }
}
