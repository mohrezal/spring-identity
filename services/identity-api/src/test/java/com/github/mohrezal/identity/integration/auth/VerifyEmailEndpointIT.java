package com.github.mohrezal.identity.integration.auth;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.EMAIL_VERIFICATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.audit.contract.AuditEvent;
import com.github.mohrezal.identity.audit.enums.AuditEventType;
import com.github.mohrezal.identity.audit.enums.AuditOutcome;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

class VerifyEmailEndpointIT extends IntegrationTestSupport {

    private static final String VERIFY_EMAIL_PATH =
            RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.VERIFY_EMAIL);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void verifyEmail_withValidToken_redirectsMarksVerifiedRemovesTokenAndPublishesAudit()
            throws Exception {
        purgeQueue(RabbitMQConstants.Audit.Queue.AUDIT);

        var email = UUID.randomUUID() + "-" + EMAIL;
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(email).firstName("Test").lastName("User").build());
        var token = UUID.randomUUID();
        redisService.set(RedisKey.EMAIL_VERIFICATION_TOKEN, email, token.toString());

        mockMvc.perform(
                        get(VERIFY_EMAIL_PATH)
                                .queryParam("token", token.toString())
                                .queryParam("redirectUrl", EMAIL_VERIFICATION))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, EMAIL_VERIFICATION));

        var reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertThat(reloaded.getEmailVerifiedAt()).isNotNull();
        assertThat(
                        redisService.get(
                                RedisKey.EMAIL_VERIFICATION_TOKEN, String.class, token.toString()))
                .isEmpty();

        var message = receiveRequiredMessage(RabbitMQConstants.Audit.Queue.AUDIT);
        var event = objectMapper.readValue(message.getBody(), AuditEvent.class);
        assertThat(event.eventType()).isEqualTo(AuditEventType.EMAIL_VERIFIED);
        assertThat(event.outcome()).isEqualTo(AuditOutcome.SUCCESS);
        assertThat(event.subject().userId()).isEqualTo(user.getId());
        assertThat(event.subject().email()).isEqualTo(email);
    }

    @Test
    void verifyEmail_rejectsMissingToken() throws Exception {
        mockMvc.perform(
                        get(VERIFY_EMAIL_PATH)
                                .queryParam("token", UUID.randomUUID().toString())
                                .queryParam("redirectUrl", EMAIL_VERIFICATION))
                .andExpect(status().isNotFound());
    }

    private void purgeQueue(String queue) {
        rabbitTemplate.execute(
                channel -> {
                    channel.queuePurge(queue);
                    return null;
                });
    }

    private Message receiveRequiredMessage(String queue) {
        var message = rabbitTemplate.receive(queue, 5_000);
        assertThat(message).isNotNull();
        return message;
    }
}
