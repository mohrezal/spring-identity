package com.github.mohrezal.identity.domain.user.command;

import com.github.mohrezal.identity.domain.user.command.param.RegisterCommandParams;
import com.github.mohrezal.identity.shared.interfaces.Command;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterCommand implements Command<RegisterCommandParams, Boolean> {
    private final RedirectValidationService redirectValidationService;

    @Override
    public void validate(RegisterCommandParams params) {
        if (!redirectValidationService.isValid(params.redirectUrl())) {
            log.error("Invalid Redirect URL");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean execute(RegisterCommandParams params) {
        return null;
    }
}
