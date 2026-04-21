package com.studi.server.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ModelTest {

    @Test
    void appUserNoArgConstructorAndSettersWork() {
        AppUser user = new AppUser();

        user.setUsername("emmanuel");
        user.setPasswordHash("hash");
        ReflectionTestUtils.setField(user, "id", 3L);

        assertThat(user.getId()).isEqualTo(3L);
        assertThat(user.getUsername()).isEqualTo("emmanuel");
        assertThat(user.getPasswordHash()).isEqualTo("hash");
    }

    @Test
    void appUserConstructorStoresValues() {
        AppUser user = new AppUser("josetta", "hash");

        assertThat(user.getUsername()).isEqualTo("josetta");
        assertThat(user.getPasswordHash()).isEqualTo("hash");
    }

    @Test
    void stepNoArgConstructorAndAccessorsWork() {
        AppUser user = new AppUser("emmanuel", "hash");
        StudyPlan studyPlan = new StudyPlan();
        Step step = new Step();

        step.setId(10L);
        step.setTitle("Read");
        step.setDescription("Read docs");
        step.setCompleted(true);
        step.setPosition(2);
        step.setUser(user);
        step.setStudyPlan(studyPlan);

        assertThat(step.getId()).isEqualTo(10L);
        assertThat(step.getTitle()).isEqualTo("Read");
        assertThat(step.getDescription()).isEqualTo("Read docs");
        assertThat(step.isCompleted()).isTrue();
        assertThat(step.getPosition()).isEqualTo(2);
        assertThat(step.getUser()).isSameAs(user);
        assertThat(step.getStudyPlan()).isSameAs(studyPlan);
    }
}
