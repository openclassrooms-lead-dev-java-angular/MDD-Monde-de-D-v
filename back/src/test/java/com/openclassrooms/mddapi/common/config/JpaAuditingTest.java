package com.openclassrooms.mddapi.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import static org.assertj.core.api.Assertions.assertThat;

class JpaAuditingTest {

    @Test
    void shouldEnableJpaAuditingOutsideTestProfile() {
        Profile profile = JpaAuditing.class.getAnnotation(Profile.class);

        assertThat(profile).isNotNull();
        assertThat(profile.value()).containsExactly("!test");
    }

    @Test
    void shouldBeAnnotatedWithEnableJpaAuditing() {
        assertThat(JpaAuditing.class)
                .hasAnnotation(
                        org.springframework.data.jpa.repository.config.EnableJpaAuditing.class
                );
    }
}