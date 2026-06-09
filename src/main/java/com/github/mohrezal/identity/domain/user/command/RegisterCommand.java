package com.github.mohrezal.identity.domain.user.command;

import com.github.mohrezal.identity.domain.user.command.param.RegisterCommandParams;
import com.github.mohrezal.identity.domain.user.dto.RegisterResponse;
import com.github.mohrezal.identity.domain.user.exception.type.UserEmailAlreadyExistsException;
import com.github.mohrezal.identity.domain.user.mapper.UserMapper;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.InvalidRedirectUrlException;
import com.github.mohrezal.identity.shared.interfaces.Command;
import com.github.mohrezal.identity.shared.service.MessageService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterCommand implements Command<RegisterCommandParams, RegisterResponse> {
    private final RedirectValidationService redirectValidationService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserMapper userMapper;
    private final MessageService messageService;

    @Override
    public void validate(RegisterCommandParams params) {
        if (!redirectValidationService.isValid(params.redirectUrl())) {
            throw new InvalidRedirectUrlException();
        }
        if (userRepository.findByEmail(params.request().email()).isPresent()) {
            throw new UserEmailAlreadyExistsException();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RegisterResponse execute(RegisterCommandParams params) {
        validate(params);
        var request = params.request();
        var hashedPassword = passwordEncoder.encode(request.password());
        var user = userMapper.toUser(request);
        var savedUser = userRepository.save(user);
        var credential = userMapper.toCredential(savedUser, hashedPassword);
        userCredentialRepository.save(credential);
        var message =
                messageService.resolve(AppMessage.AUTH_REGISTERED, LocaleContextHolder.getLocale());
        return new RegisterResponse(message);
    }
}
