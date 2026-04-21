package com.studi.server.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class DtoTest {

    @Test
    void authRequestStoresUsernameAndPassword() {
        AuthRequest request = new AuthRequest();

        request.setUsername("emmanuel");
        request.setPassword("secret");

        assertThat(request.getUsername()).isEqualTo("emmanuel");
        assertThat(request.getPassword()).isEqualTo("secret");
    }

    @Test
    void authResponseExposesConstructorValues() {
        AuthResponse response = new AuthResponse("token", 7L, "emmanuel");

        assertThat(response.getToken()).isEqualTo("token");
        assertThat(response.getUserId()).isEqualTo(7L);
        assertThat(response.getUsername()).isEqualTo("emmanuel");
    }

    @Test
    void createStudyPlanResponseExposesConstructorValues() {
        StepResponse step = new StepResponse(1L, "Title", "Description", true, 2);
        CreateStudyPlanResponse response = new CreateStudyPlanResponse(
                4L,
                "Plan",
                "Goal",
                List.of(step));

        assertThat(response.getId()).isEqualTo(4L);
        assertThat(response.getTitle()).isEqualTo("Plan");
        assertThat(response.getGoal()).isEqualTo("Goal");
        assertThat(response.getSteps()).containsExactly(step);
    }

    @Test
    void stepResponseExposesConstructorValues() {
        StepResponse response = new StepResponse(1L, "Title", "Description", true, 2);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Title");
        assertThat(response.getDescription()).isEqualTo("Description");
        assertThat(response.isCompleted()).isTrue();
        assertThat(response.getPosition()).isEqualTo(2);
    }
}
