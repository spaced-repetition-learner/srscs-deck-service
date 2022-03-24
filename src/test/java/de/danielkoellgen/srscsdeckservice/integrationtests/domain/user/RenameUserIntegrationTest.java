package de.danielkoellgen.srscsdeckservice.integrationtests.domain.user;

import de.danielkoellgen.srscsdeckservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsdeckservice.domain.user.application.UserService;
import de.danielkoellgen.srscsdeckservice.domain.user.domain.User;
import de.danielkoellgen.srscsdeckservice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RenameUserIntegrationTest {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public RenameUserIntegrationTest(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @AfterEach
    public void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    public void shouldPersistRenamedUser() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        Username username = new Username("dadepu");
        userService.addNewExternallyCreatedUser(UUID.randomUUID(), userId, username);

        // when
        Username newUsername = new Username("hk1160");
        userService.renameUser(UUID.randomUUID(), userId, newUsername);

        // then
        User fetchedUser = userRepository.findById(userId).get();
        assertThat(fetchedUser.getUsername())
                .isEqualTo(newUsername);
    }
}