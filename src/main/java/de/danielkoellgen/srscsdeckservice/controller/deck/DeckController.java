package de.danielkoellgen.srscsdeckservice.controller.deck;

import de.danielkoellgen.srscsdeckservice.controller.deck.dto.DeckRequestDto;
import de.danielkoellgen.srscsdeckservice.controller.deck.dto.DeckResponseDto;
import de.danielkoellgen.srscsdeckservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsdeckservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsdeckservice.domain.deck.domain.DeckName;
import de.danielkoellgen.srscsdeckservice.domain.deck.repository.DeckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
public class DeckController {

    private final DeckService deckService;
    private final DeckRepository deckRepository;

    private final Logger logger = LoggerFactory.getLogger(DeckController.class);

    @Autowired
    public DeckController(DeckService deckService, DeckRepository deckRepository) {
        this.deckService = deckService;
        this.deckRepository = deckRepository;
    }

    @PostMapping(value = "/decks", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<DeckResponseDto> createDeck(@RequestBody DeckRequestDto requestDto) {
        UUID transactionId = UUID.randomUUID();
        logger.trace("POST /decks: Create deck '{}'. [tid={}, payload={}]",
                requestDto.deckName(), transactionId, requestDto);

        DeckName deckName;
        try {
            deckName = requestDto.getMappedDeckName();
        } catch (Exception e) {
            logger.trace("Request failed. Mapping error. Responding 400. [tid={}, error={}]",
                    transactionId, e.getStackTrace());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to map deck-name to primitive.", e);
        }
        Deck deck;
        try {
            deck = deckService.createNewDeck(transactionId, requestDto.userId(), deckName);
        } catch (NoSuchElementException e) {
            logger.trace("Request failed. [tid={}, error={}]",
                    transactionId, e.getStackTrace());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.trace("Deck '{}' for '{}' created. [tid={}]",
                deck.getDeckName().getName(), deck.getEmbeddedUser().getUsername().getUsername(), transactionId);
        return new ResponseEntity<>(new DeckResponseDto(deck), HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/decks/{deck-id}")
    public HttpStatus disableDeck(@PathVariable("deck-id") UUID deckId){
        UUID transactionId = UUID.randomUUID();
        deckService.deleteDeck(transactionId, deckId);
        return HttpStatus.OK;
    }

    @GetMapping(value = "/decks/{deck-id}", produces = {"application/json"})
    public ResponseEntity<DeckResponseDto> getDeck(@PathVariable("deck-id") UUID deckId) {
        UUID transactionId = UUID.randomUUID();
        Deck deck;
        try {
            deck = deckRepository.findById(deckId).get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new DeckResponseDto(deck), HttpStatus.OK);
    }

    @GetMapping(value = "/decks", produces = {"application/json"})
    public List<DeckResponseDto> getDecksByUserId(@RequestParam("user-id") UUID userId) {
        return deckRepository.findDecksByEmbeddedUser_UserId(userId)
                .stream().map(DeckResponseDto::new).toList();
    }

    @PutMapping(value = "/decks/{deck-id}/scheduler-presets/{scheduler-preset-id}")
    public HttpStatus updateSchedulerPresetForDeck(
            @PathVariable("deck-id") UUID deckId, @PathVariable("scheduler-preset-id") UUID presetId) {
        UUID transactionId = UUID.randomUUID();
        try {
            deckService.changePreset(transactionId, deckId, presetId);
        } catch (NoSuchElementException e) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.OK;
    }
}
