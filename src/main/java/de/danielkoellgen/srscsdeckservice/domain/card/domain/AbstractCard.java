package de.danielkoellgen.srscsdeckservice.domain.card.domain;

import de.danielkoellgen.srscsdeckservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsdeckservice.domain.schedulerpreset.domain.SchedulerPreset;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Getter
public class AbstractCard {

    @Id
    @NotNull
    private final UUID cardId;

    @Nullable
    @Field("parent_card_id")
    private final UUID parentCardId;

    @Nullable
    @Transient
    private final Deck deck;

    @NotNull
    @Field("deck")
    private final EmbeddedDeck embeddedDeck;

    @NotNull
    @Field("scheduler")
    private final Scheduler scheduler;

    @NotNull
    @Field("is_active")
    private Boolean isActive;

    @Transient
    private final Logger log = LoggerFactory.getLogger(AbstractCard.class);

    public AbstractCard(@NotNull UUID cardId, @Nullable UUID parentCardId, @Nullable Deck deck,
            @NotNull EmbeddedDeck embeddedDeck, @NotNull Scheduler scheduler, @NotNull Boolean isActive) {
        this.cardId = cardId;
        this.parentCardId = parentCardId;
        this.deck = deck;
        this.embeddedDeck = embeddedDeck;
        this.scheduler = scheduler;
        this.isActive = isActive;
    }

    @PersistenceConstructor
    public AbstractCard(@NotNull UUID cardId, @Nullable UUID parentCardId, @NotNull EmbeddedDeck embeddedDeck,
            @NotNull Scheduler scheduler, @NotNull Boolean isActive) {
        this.cardId = cardId;
        this.deck = null;
        this.parentCardId = parentCardId;
        this.embeddedDeck = embeddedDeck;
        this.scheduler = scheduler;
        this.isActive = isActive;
    }

    public void disableCard() {
        isActive = false;
        log.debug("AbstractCard.isActive has been set to '{}'.", isActive);
    }

    public void resetScheduler() {
        scheduler.reset();
    }

    public void graduateCard() {
        scheduler.graduate();
    }

    public void reviewCard(@NotNull ReviewAction reviewAction) {
        scheduler.review(reviewAction);
    }

    public void replaceSchedulerPreset(@NotNull SchedulerPreset schedulerPreset) {
        scheduler.updateSchedulerPreset(schedulerPreset);
    }

    @Override
    public String toString() {
        return "AbstractCard{" +
                "cardId=" + cardId +
                ", parentCardId=" + parentCardId +
                ", deck=" + deck +
                ", embeddedDeck=" + embeddedDeck +
                ", isActive=" + isActive +
                '}';
    }
}
