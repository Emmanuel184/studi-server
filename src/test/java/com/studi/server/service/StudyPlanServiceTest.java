package com.studi.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.studi.server.dto.CreateStudyPlanRequest;
import com.studi.server.dto.CreateStudyPlanResponse;
import com.studi.server.dto.StepResponse;
import com.studi.server.model.AppUser;
import com.studi.server.model.Step;
import com.studi.server.model.StudyPlan;
import com.studi.server.repository.StepRepository;
import com.studi.server.repository.StudyPlanRepository;
import com.studi.server.repository.UserRepository;
import com.studi.server.support.InMemoryRepositories;

class StudyPlanServiceTest {

    @Test
    void createStudyPlanSavesPlanAndAllStepsForUser() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AppUser user = fakes.saveUser("emmanuel");
        StepService stepService = new StepService(fakes.stepRepository(), fakes.userRepository());
        StudyPlanService service = new StudyPlanService(
                fakes.studyPlanRepository(),
                fakes.stepRepository(),
                fakes.userRepository(),
                stepService);

        CreateStudyPlanRequest request = new CreateStudyPlanRequest();
        request.setGoal("Learn Spring Boot authentication");

        CreateStudyPlanResponse response = service.createStudyPlan(user.getId(), request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Foundations Study Plan");
        assertThat(response.getGoal()).isEqualTo("Learn Spring Boot authentication");
        assertThat(response.getSteps()).hasSize(5);
        assertThat(response.getSteps()).extracting(StepResponse::getTitle)
                .containsExactly(
                        "Clarify the goal",
                        "Gather learning material",
                        "Study the fundamentals",
                        "Practice actively",
                        "Review and adjust");

        StudyPlan savedPlan = fakes.studyPlans().get(response.getId());
        assertThat(savedPlan.getUser().getId()).isEqualTo(user.getId());

        for (StepResponse stepResponse : response.getSteps()) {
            Step savedStep = fakes.steps().get(stepResponse.getId());
            assertThat(savedStep.getUser().getId()).isEqualTo(user.getId());
            assertThat(savedStep.getStudyPlan().getId()).isEqualTo(response.getId());
        }
    }

    @Test
    void getByIdOnlyReturnsPlansOwnedByUser() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AppUser owner = fakes.saveUser("owner");
        AppUser other = fakes.saveUser("other");
        StepService stepService = new StepService(fakes.stepRepository(), fakes.userRepository());
        StudyPlanService service = new StudyPlanService(
                fakes.studyPlanRepository(),
                fakes.stepRepository(),
                fakes.userRepository(),
                stepService);

        CreateStudyPlanRequest request = new CreateStudyPlanRequest();
        request.setGoal("Own the basics");
        CreateStudyPlanResponse created = service.createStudyPlan(owner.getId(), request);

        assertThat(service.getById(owner.getId(), created.getId()).getSteps()).hasSize(5);
        assertThatThrownBy(() -> service.getById(other.getId(), created.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Study plan not found");
    }

    @Test
    void createStudyPlanDefaultsBlankGoalAndTrimsProvidedGoal() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AppUser user = fakes.saveUser("emmanuel");
        StepService stepService = new StepService(fakes.stepRepository(), fakes.userRepository());
        StudyPlanService service = new StudyPlanService(
                fakes.studyPlanRepository(),
                fakes.stepRepository(),
                fakes.userRepository(),
                stepService);

        CreateStudyPlanRequest blankGoal = new CreateStudyPlanRequest();
        blankGoal.setGoal("   ");
        CreateStudyPlanResponse defaulted = service.createStudyPlan(user.getId(), blankGoal);

        CreateStudyPlanRequest spacedGoal = new CreateStudyPlanRequest();
        spacedGoal.setGoal("  Learn testing  ");
        CreateStudyPlanResponse trimmed = service.createStudyPlan(user.getId(), spacedGoal);

        assertThat(defaulted.getGoal()).isEqualTo("Build a strong study routine");
        assertThat(defaulted.getSteps().get(0).getDescription())
                .contains("Build a strong study routine");
        assertThat(trimmed.getGoal()).isEqualTo("Learn testing");
        assertThat(trimmed.getSteps().get(0).getDescription()).contains("Learn testing");
    }

    @Test
    void createStudyPlanDefaultsNullGoalAndRequiresExistingUser() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AppUser user = fakes.saveUser("emmanuel");
        StepService stepService = new StepService(fakes.stepRepository(), fakes.userRepository());
        StudyPlanService service = new StudyPlanService(
                fakes.studyPlanRepository(),
                fakes.stepRepository(),
                fakes.userRepository(),
                stepService);

        CreateStudyPlanRequest request = new CreateStudyPlanRequest();

        assertThat(service.createStudyPlan(user.getId(), request).getGoal())
                .isEqualTo("Build a strong study routine");
        assertThatThrownBy(() -> service.createStudyPlan(404L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found: 404");
    }
}
