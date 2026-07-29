package com.github.mohrezal.identity.domain.user.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.mohrezal.identity.domain.user.query.param.CheckEmailAvailabilityQueryParams;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckEmailAvailabilityQueryTest {

    private static final String EMAIL = "user@client.test";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CheckEmailAvailabilityQuery query;

    @Test
    void execute_whenEmailIsUnknown_returnsAvailableTrue() {
        when(userRepository.existsUserByEmail(EMAIL)).thenReturn(false);

        var result = query.execute(new CheckEmailAvailabilityQueryParams(EMAIL), null);

        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.available()).isTrue();
    }

    @Test
    void execute_whenEmailExists_returnsAvailableFalse() {
        when(userRepository.existsUserByEmail(EMAIL)).thenReturn(true);

        var result = query.execute(new CheckEmailAvailabilityQueryParams(EMAIL), null);

        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.available()).isFalse();
    }
}
