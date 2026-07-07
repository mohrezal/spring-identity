package com.github.mohrezal.identity.integration.notification;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static com.github.mohrezal.identity.support.data.TestConstants.Account.PASSWORD;
import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.EMAIL_VERIFICATION;
import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.PASSWORD_RESET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.auth.dto.ForgotPasswordRequest;
import com.github.mohrezal.identity.domain.auth.listener.message.PasswordResetEmailMessage;
import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.domain.user.dto.RegisterRequest;
import com.github.mohrezal.identity.domain.user.listener.message.UserEmailVerificationMessage;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

class NotificationTransactionIT extends IntegrationTestSupport {

    private static final String REGISTER_PATH =
            RouteConstants.build(RouteConstants.User.BASE, RouteConstants.User.REGISTER);
    private static final String FORGOT_PASSWORD_PATH =
            RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.FORGOT_PASSWORD);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void register_publishesVerificationEmailAfterTransactionCommit() throws Exception {
        purgeQueue(RabbitMQConstants.Notification.Queue.EMAIL);
        var configuredUserRole = applicationProperties.privilege().role().user();
        if (!roleRepository.existsByKey(configuredUserRole.key())) {
            roleRepository.saveAndFlush(
                    Role.builder()
                            .key(configuredUserRole.key())
                            .name(configuredUserRole.name())
                            .build());
        }
        var email = UUID.randomUUID() + "-" + EMAIL;
        var request = new RegisterRequest("Test", "User", email, PASSWORD);

        mockMvc.perform(
                        post(REGISTER_PATH)
                                .with(csrf())
                                .queryParam("redirectUrl", EMAIL_VERIFICATION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated());

        var message = receiveRequiredMessage(RabbitMQConstants.Notification.Queue.EMAIL);
        var payload = objectMapper.readValue(message.getBody(), UserEmailVerificationMessage.class);

        assertThat(message.getMessageProperties().getReceivedRoutingKey())
                .isEqualTo(RabbitMQConstants.Notification.RoutingKey.TRANSACTIONAL_EMAIL);
        assertThat(message.getMessageProperties().getMessageId()).isNotBlank();
        assertThat(
                        (String)
                                message.getMessageProperties()
                                        .getHeader(RabbitMQConstants.Header.MESSAGE_ID))
                .isEqualTo(message.getMessageProperties().getMessageId());
        assertThat(payload.to()).isEqualTo(email);
        assertThat(payload.activationUrl()).startsWith(EMAIL_VERIFICATION + "?token=");
    }

    @Test
    void forgotPassword_publishesPriorityResetEmailAfterTransactionCommit() throws Exception {
        purgeQueue(RabbitMQConstants.Notification.Queue.PASSWORD_RESET_EMAIL);
        var email = UUID.randomUUID() + "-" + EMAIL;
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(email).firstName("Test").lastName("User").build());
        var request = new ForgotPasswordRequest(email);

        mockMvc.perform(
                        post(FORGOT_PASSWORD_PATH)
                                .with(csrf())
                                .queryParam("redirectUrl", PASSWORD_RESET)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        var message =
                receiveRequiredMessage(RabbitMQConstants.Notification.Queue.PASSWORD_RESET_EMAIL);
        var payload = objectMapper.readValue(message.getBody(), PasswordResetEmailMessage.class);

        assertThat(message.getMessageProperties().getReceivedRoutingKey())
                .isEqualTo(RabbitMQConstants.Notification.RoutingKey.PASSWORD_RESET_EMAIL);
        assertThat(message.getMessageProperties().getPriority())
                .isEqualTo(RabbitMQConstants.Notification.Priority.PASSWORD_RESET_EMAIL);
        assertThat(message.getMessageProperties().getMessageId()).isNotBlank();
        assertThat(
                        (String)
                                message.getMessageProperties()
                                        .getHeader(RabbitMQConstants.Header.MESSAGE_ID))
                .isEqualTo(message.getMessageProperties().getMessageId());
        assertThat(payload.userId()).isEqualTo(user.getId());
        assertThat(payload.to()).isEqualTo(email);
        assertThat(payload.resetUrl()).startsWith(PASSWORD_RESET + "?token=");
    }

    @Test
    void forgotPassword_forUnknownEmailDoesNotPublishResetEmail() throws Exception {
        purgeQueue(RabbitMQConstants.Notification.Queue.PASSWORD_RESET_EMAIL);
        var request = new ForgotPasswordRequest(UUID.randomUUID() + "-" + EMAIL);

        mockMvc.perform(
                        post(FORGOT_PASSWORD_PATH)
                                .with(csrf())
                                .queryParam("redirectUrl", PASSWORD_RESET)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertThat(
                        rabbitTemplate.receive(
                                RabbitMQConstants.Notification.Queue.PASSWORD_RESET_EMAIL, 500))
                .isNull();
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
