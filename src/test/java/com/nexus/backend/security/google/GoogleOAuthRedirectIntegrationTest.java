package com.nexus.backend.security.google;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.google.client-id=id-de-prueba.apps.googleusercontent.com",
        "app.google.client-secret=secreto-de-prueba",
        "app.public-url=https://primercommit.test/"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleOAuthRedirectIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void theButtonIsShownWhenGoogleIsConfigured() throws Exception {
        mvc.perform(get("/api/auth/providers")).andExpect(jsonPath("$.google").value(true));
    }

    @Test
    void goingToGoogleAsksOnlyForTheBasicDataAndComesBackThroughTheWeb() throws Exception {
        MvcResult result = mvc.perform(get("/api/oauth2/authorize/google").param("lang", "en"))
                .andExpect(status().isFound())
                .andReturn();

        var location = UriComponentsBuilder.fromUriString(result.getResponse().getRedirectedUrl()).build();
        assertThat(location.getHost()).isEqualTo("accounts.google.com");
        assertThat(location.getQueryParams().getFirst("client_id")).isEqualTo("id-de-prueba.apps.googleusercontent.com");
        assertThat(decode(location.getQueryParams().getFirst("redirect_uri")))
                .isEqualTo("https://primercommit.test/api/oauth2/callback/google");
        assertThat(decode(location.getQueryParams().getFirst("scope"))).isEqualTo("openid email profile");
        assertThat(location.getQueryParams().getFirst("state")).isNotBlank();
    }

    @Test
    void cancellingInGoogleReturnsToTheSignUpPageInTheSameLanguage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MvcResult start = mvc.perform(get("/api/oauth2/authorize/google").param("lang", "en").session(session))
                .andReturn();
        String state = UriComponentsBuilder.fromUriString(start.getResponse().getRedirectedUrl()).build()
                .getQueryParams().getFirst("state");

        mvc.perform(get("/api/oauth2/callback/google")
                        .param("error", "access_denied")
                        .param("state", decode(state))
                        .session(session))
                .andExpect(status().isFound())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .isEqualTo("https://primercommit.test/en/sign-up/google#error=cancelled"));
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void aReturnThatDidNotStartHereIsRejected() throws Exception {
        mvc.perform(get("/api/oauth2/callback/google").param("code", "x").param("state", "inventado"))
                .andExpect(status().isFound())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .isEqualTo("https://primercommit.test/registro/google#error=failed"));
    }

    private static String decode(String value) {
        return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
