package de.danielkoellgen.srscsdeckservice.controller.card;

import de.danielkoellgen.srscsdeckservice.controller.card.dto.*;
import de.danielkoellgen.srscsdeckservice.controller.deck.DeckController;
import de.danielkoellgen.srscsdeckservice.controller.deck.dto.DeckRequestDto;
import de.danielkoellgen.srscsdeckservice.controller.deck.dto.DeckResponseDto;
import de.danielkoellgen.srscsdeckservice.domain.card.domain.ImageElement;
import de.danielkoellgen.srscsdeckservice.domain.card.domain.TextElement;
import de.danielkoellgen.srscsdeckservice.domain.card.repository.CardRepository;
import de.danielkoellgen.srscsdeckservice.domain.card.repository.DefaultCardRepository;
import de.danielkoellgen.srscsdeckservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsdeckservice.domain.user.application.UserService;
import de.danielkoellgen.srscsdeckservice.domain.user.domain.User;
import de.danielkoellgen.srscsdeckservice.domain.user.domain.Username;
import de.danielkoellgen.srscsdeckservice.domain.user.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CardControllerIntegrationTest {

    private final WebTestClient webTestClientCard;
    private final WebTestClient webTestClientDeck;

    private final UserService userService;
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;

    private User user1;
    private User user2;
    private DeckResponseDto deck1;

    @Autowired
    public CardControllerIntegrationTest(CardController cardController, DeckController deckController,
            UserService userService, CardRepository cardRepository, DeckRepository deckRepository,
            UserRepository userRepository) {
        this.webTestClientCard = WebTestClient.bindToController(cardController).build();
        this.webTestClientDeck = WebTestClient.bindToController(deckController).build();
        this.userService = userService;
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    public void setUp() throws Exception {
        user1 = userService.addNewExternallyCreatedUser(UUID.randomUUID(), UUID.randomUUID(), new Username("anyName"));
        user2 = userService.addNewExternallyCreatedUser(UUID.randomUUID(), UUID.randomUUID(), new Username("anyName2"));

        DeckRequestDto deckRequestDto = new DeckRequestDto(user1.getUserId(), "ANYNAME");
        deck1 = externallyCreateDeck(deckRequestDto);
    }

    @AfterEach
    public void cleanUp() {
        cardRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void shouldAllowToCreateDefaultCards() {
        // given
        CardRequestDto requestDto = new CardRequestDto(deck1.deckId(), "default",
                new HintDto(
                        List.of(
                                ContentElementDto.makeAsText(new TextElement("text 1")),
                                ContentElementDto.makeAsImage(new ImageElement("url 1"))
                        )
                ), new ViewDto(
                List.of(
                        ContentElementDto.makeAsText(new TextElement("text 2")),
                        ContentElementDto.makeAsImage(new ImageElement("url 2"))
                )
        ), new ViewDto(
                List.of(
                        ContentElementDto.makeAsText(new TextElement("text 3")),
                        ContentElementDto.makeAsImage(new ImageElement("url 3"))
                )
        ));

        // when
        CardResponseDto responseDto = webTestClientCard.post().uri("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CardResponseDto.class)
                .returnResult()
                .getResponseBody();
        assert responseDto != null;
    }

    @Test
    public void shouldAllowToFetchCardsById() {
        // given
        CardResponseDto createdCard = externallyCreateDefaultCard(deck1.deckId());

        // when
        CardResponseDto fetchedCard = webTestClientCard.get().uri("/cards/" + createdCard.cardId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardResponseDto.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(fetchedCard)
                .isEqualTo(createdCard);
    }

    @Test
    public void shouldAllowToFetchCardsByDeck() {
        // given
        CardResponseDto card1 = externallyCreateDefaultCard(deck1.deckId());
        CardResponseDto card2 = externallyCreateDefaultCard(deck1.deckId());
        CardResponseDto card3 = externallyCreateDefaultCard(deck1.deckId());

        // when
        List<CardResponseDto> fetchedCards = webTestClientCard.get().uri("/cards?deck-id=" + deck1.deckId() +
                        "&card-status=active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CardResponseDto.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(fetchedCards)
                .hasSize(3);
        assertThat(fetchedCards)
                .contains(card1)
                .contains(card2)
                .contains(card3);
    }

    private @NotNull DeckResponseDto externallyCreateDeck(DeckRequestDto requestDto) {
        DeckResponseDto responseDto = webTestClientDeck.post().uri("/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DeckResponseDto.class)
                .returnResult()
                .getResponseBody();
        assert responseDto != null;
        return responseDto;
    }

    private @NotNull CardResponseDto externallyCreateDefaultCard(UUID deckId) {
        CardRequestDto requestDto = new CardRequestDto(deckId, "default",
                new HintDto(
                        List.of(
                                ContentElementDto.makeAsText(new TextElement("text 1")),
                                ContentElementDto.makeAsImage(new ImageElement("url 1"))
                        )
                ), new ViewDto(
                List.of(
                        ContentElementDto.makeAsText(new TextElement("text 2")),
                        ContentElementDto.makeAsImage(new ImageElement("url 2"))
                )
        ), new ViewDto(
                List.of(
                        ContentElementDto.makeAsText(new TextElement("text 3")),
                        ContentElementDto.makeAsImage(new ImageElement("url 3"))
                )
        ));

        // when
        CardResponseDto responseDto = webTestClientCard.post().uri("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CardResponseDto.class)
                .returnResult()
                .getResponseBody();
        assert responseDto != null;
        return responseDto;
    }
}
