package com.github.mohrezal.identity.domain.user.command;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static com.github.mohrezal.identity.support.data.TestConstants.Account.PASSWORD;
import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.EMAIL_VERIFICATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.privilege.service.UserRoleAssignmentService;
import com.github.mohrezal.identity.domain.user.command.param.RegisterCommandParams;
import com.github.mohrezal.identity.domain.user.dto.RegisterRequest;
import com.github.mohrezal.identity.domain.user.exception.context.RegistrationAuditExceptionContext;
import com.github.mohrezal.identity.domain.user.exception.type.UserEmailAlreadyExistsException;
import com.github.mohrezal.identity.domain.user.mapper.UserMapper;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.HashService;
import com.github.mohrezal.identity.shared.service.MessageService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegisterCommandTest {

    @Mock
    private RedirectValidationService redirectValidationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MessageService messageService;

    @Mock
    private HashService hashService;

    @Mock
    private RedisService redisService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserRoleAssignmentService userRoleAssignmentService;

    @InjectMocks
    private RegisterCommand command;

    @Test
    void execute_whenConcurrentInsertClaimsEmail_returnsEmailConflict() {
        var request = new RegisterRequest("Test", "User", EMAIL, PASSWORD);
        var params = new RegisterCommandParams(request, EMAIL_VERIFICATION);
        var auditRequestContext = new AuditRequestContext("trace-id", null, null, null, null, null);
        var user = User.builder().email(EMAIL).build();
        var persistenceFailure = new DataIntegrityViolationException("duplicate email");

        when(hashService.hashHex(EMAIL)).thenReturn("00000000");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(redirectValidationService.isValid(EMAIL_VERIFICATION)).thenReturn(true);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed-password");
        when(userMapper.toUser(request)).thenReturn(user);
        when(userRepository.saveAndFlush(user)).thenThrow(persistenceFailure);

        var exception =
                catchThrowableOfType(
                        UserEmailAlreadyExistsException.class,
                        () -> command.execute(params, auditRequestContext));

        assertThat(exception.getContext())
                .isEqualTo(new RegistrationAuditExceptionContext(auditRequestContext, EMAIL));
        assertThat(exception).hasCause(persistenceFailure);
        verifyNoInteractions(
                userRoleAssignmentService,
                userCredentialRepository,
                redisService,
                eventPublisher,
                messageService);
    }
}
