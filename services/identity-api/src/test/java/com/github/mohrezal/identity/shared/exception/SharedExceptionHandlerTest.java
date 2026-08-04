package com.github.mohrezal.identity.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.ServletWebRequest;

class SharedExceptionHandlerTest {

    @AfterEach
    void resetLocaleContext() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void missingRequestParameter_returnsRequiredFieldError() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        var messageSource = new StaticMessageSource();
        messageSource.addMessage(
                ExceptionCode.VALIDATION_FAILED.messageKey(), Locale.ENGLISH, "Validation failed.");
        messageSource.addMessage(
                "shared.validation.required-parameter",
                Locale.ENGLISH,
                "This parameter is required.");
        var handler = new SharedExceptionHandler(messageSource);
        var request = new MockHttpServletRequest("POST", "/api/v1/users");
        var exception = new MissingServletRequestParameterException("redirectUrl", "String");

        var response =
                handler.handleMissingServletRequestParameterException(
                        exception, new ServletWebRequest(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .satisfies(
                        body -> {
                            assertThat(body.code())
                                    .isEqualTo(ExceptionCode.VALIDATION_FAILED.name());
                            assertThat(body.message()).isEqualTo("Validation failed.");
                            assertThat(body.errors())
                                    .containsEntry("redirectUrl", "This parameter is required.");
                            assertThat(body.path()).isEqualTo("/api/v1/users");
                        });
    }
}
