package io.github.selenajiro.europolitik.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String DEV_SECRET = "dev-only-secret-for-tests-0123456789-abcdef";
    private static final String REAL_SECRET = "a-long-random-secret-for-production-use-0123456789";
    private static final String OTHER_SECRET = "another-long-random-secret-for-testing-0123456789";

    private static MockEnvironment env(String... activeProfiles) {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(activeProfiles);
        return env;
    }

    @Test
    void rejectsSecretShorterThan32Bytes() {
        assertThatThrownBy(() -> new JwtService("too-short", env()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void refusesDevelopmentSecretInProdProfile() {
        assertThatThrownBy(() -> new JwtService(DEV_SECRET, env("prod")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void acceptsDevelopmentSecretOutsideProd() {
        assertThatCode(() -> new JwtService(DEV_SECRET, env())).doesNotThrowAnyException();
        assertThatCode(() -> new JwtService(DEV_SECRET, env("local"))).doesNotThrowAnyException();
    }

    @Test
    void acceptsRealSecretInProdProfile() {
        assertThatCode(() -> new JwtService(REAL_SECRET, env("prod"))).doesNotThrowAnyException();
    }

    @Test
    void tokenRoundTripKeepsUsernameAndRole() {
        JwtService service = new JwtService(REAL_SECRET, env());

        String token = service.generateToken("selena", "USER");

        assertThat(service.isValid(token)).isTrue();
        assertThat(service.extractUsername(token)).isEqualTo("selena");
        assertThat(service.extractRole(token)).isEqualTo("USER");
    }

    @Test
    void tokenSignedWithAnotherSecretIsInvalid() {
        JwtService issuer = new JwtService(REAL_SECRET, env());
        JwtService other = new JwtService(OTHER_SECRET, env());

        assertThat(other.isValid(issuer.generateToken("selena", "USER"))).isFalse();
    }

    @Test
    void garbageTokenIsInvalid() {
        JwtService service = new JwtService(REAL_SECRET, env());

        assertThat(service.isValid("not-a-token")).isFalse();
    }
}
