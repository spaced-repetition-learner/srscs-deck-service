package de.danielkoellgen.srscsdeckservice.events.producer.deck;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscsdeckservice.domain.domainprimitive.EventDateTime;
import de.danielkoellgen.srscsdeckservice.events.producer.AbstractProducerEvent;
import de.danielkoellgen.srscsdeckservice.events.producer.deck.dto.DeckCreatedDto;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeckCreated extends AbstractProducerEvent {

    @NotNull
    private final DeckCreatedDto payloadDto;

    public static final String eventName = "deck-created";

    public static final String eventTopic = "cdc.decks-cards.0";

    public DeckCreated(@NotNull String transactionId, @Nullable UUID correlationId,
            @NotNull DeckCreatedDto payloadDto) {
        super(UUID.randomUUID(), transactionId, correlationId, eventName, eventTopic,
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

    @Override
    public String toString() {
        return "DeckCreated{" +
                "payloadDto=" + payloadDto +
                ", " + super.toString() +
                '}';
    }
}
