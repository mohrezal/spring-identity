package com.github.mohrezal.identity.integration.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.model.UserOauthConnection;
import com.github.mohrezal.identity.domain.auth.repository.UserOauthConnectionRepository;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class EmailCanonicalizationIT extends IntegrationTestSupport {

    private static final String RAW_EMAIL = " User@Example.COM ";
    private static final String CANONICAL_EMAIL = "user@example.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserOauthConnectionRepository userOauthConnectionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persistenceCanonicalizesUserAndOAuthConnectionEmails() {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(RAW_EMAIL).firstName("Test").lastName("User").build());
        var connection =
                userOauthConnectionRepository.saveAndFlush(
                        UserOauthConnection.builder()
                                .user(user)
                                .provider(OAuthProviderType.GOOGLE)
                                .providerUserId(UUID.randomUUID().toString())
                                .email(RAW_EMAIL)
                                .build());

        assertThat(user.getEmail()).isEqualTo(CANONICAL_EMAIL);
        assertThat(connection.getEmail()).isEqualTo(CANONICAL_EMAIL);
        assertThat(userRepository.findByEmail(CANONICAL_EMAIL)).contains(user);
    }

    @Test
    void databaseRejectsNonCanonicalUserEmail() {
        assertThatThrownBy(
                        () ->
                                jdbcTemplate.update(
                                        "INSERT INTO users (email) VALUES (?)", RAW_EMAIL))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsNonCanonicalOAuthConnectionEmail() {
        var user =
                userRepository.saveAndFlush(
                        User.builder()
                                .email(CANONICAL_EMAIL)
                                .firstName("Test")
                                .lastName("User")
                                .build());

        assertThatThrownBy(
                        () ->
                                jdbcTemplate.update(
                                        "INSERT INTO user_oauth_connections"
                                                + " (user_id, provider, provider_user_id, email)"
                                                + " VALUES (?, ?, ?, ?)",
                                        user.getId(),
                                        OAuthProviderType.GOOGLE.name(),
                                        UUID.randomUUID().toString(),
                                        RAW_EMAIL))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
