package com.github.mohrezal.identity.domain.user.command;

import com.github.mohrezal.identity.domain.user.command.param.RegisterCommandParams;
import com.github.mohrezal.identity.shared.interfaces.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterCommand implements Command<RegisterCommandParams, Boolean> {
    @Override
    public Boolean execute(RegisterCommandParams params) {
        return null;
    }
}
