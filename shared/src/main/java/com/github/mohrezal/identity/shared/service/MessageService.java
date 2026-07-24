package com.github.mohrezal.identity.shared.service;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageService {
    private final MessageSource messageSource;

    public String resolve(ExceptionCode key, Locale locale) {
        return messageSource.getMessage(key.messageKey(), null, locale);
    }
}
