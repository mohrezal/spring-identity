package com.github.mohrezal.identity.integration.user;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class EmailAvailabilityEndpointIT extends IntegrationTestSupport {

    private static final String EMAIL_AVAILABILITY_PATH =
            RouteConstants.build(RouteConstants.User.BASE, RouteConstants.User.EMAIL_AVAILABILITY);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void emailAvailability_whenEmailIsUnknown_returnsAvailableTrue() throws Exception {
        mockMvc.perform(get(EMAIL_AVAILABILITY_PATH).param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void emailAvailability_whenEmailExists_returnsAvailableFalse() throws Exception {
        userRepository.saveAndFlush(
                User.builder().email(EMAIL).firstName("Test").lastName("User").build());

        mockMvc.perform(get(EMAIL_AVAILABILITY_PATH).param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void emailAvailability_whenEmailIsInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(get(EMAIL_AVAILABILITY_PATH).param("email", "not-an-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emailAvailability_whenEmailIsMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(get(EMAIL_AVAILABILITY_PATH)).andExpect(status().isBadRequest());
    }
}
