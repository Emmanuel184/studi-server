package com.studi.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.studi.server.dto.StepRequest;
import com.studi.server.dto.StepResponse;
import com.studi.server.model.AppUser;
import com.studi.server.model.Step;
import com.studi.server.model.StudyPlan;
import com.studi.server.support.InMemoryRepositories;

class StepServiceTest {

    @Test
    void createForStudyPlanSavesStepAttachedToPlanAndUser() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AppUser user = fakes.saveUser("emmanuel");
        StudyPlan plan = fakes.saveStudyPlan(user, "Plan", "Goal");
        StepService service = new StepService(fakes.stepRepository(), fakes.userRepository());

        StepRequest request = stepRequest("Read docs", "Read the Spring Security docs", 1);

        StepResponse response = service.createForStudyPlan(user.getId(), plan, request);

        Step saved = fakes.steps().get(response.getId());
        assertThat(saved.getTitle()).isEqualTo("Read docs");
        assertThat(saved.getStudyPlan().getId()).isEqualTo(plan.getId());
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void getAllAndGetByIdReturnUserScopedStepsInPositionOrder() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AppUser owner = fakes.saveUser("owner");
        AppUser other = fakes.saveUser("other");
        StudyPlan plan = fakes.saveStudyPlan(owner, "Plan", "Goal");
        StudyPlan otherPlan = fakes.saveStudyPlan(other, "Other Plan", "Other Goal");
        StepService service = new StepService(fakes.stepRepository(), fakes.userRepository());

        StepResponse second = service.createForStudyPlan(owner.getId(), plan, stepRequest("Second", "Second", 2));
        StepResponse first = service.createForStudyPlan(owner.getId(), plan, stepRequest("First", "First", 1));
        service.createForStudyPlan(other.getId(), otherPlan, stepRequest("Other", "Other", 0));

        assertThat(service.getAll(owner.getId()))
                .extracting(StepResponse::getTitle)
                .containsExactly("First", "Second");
        assertThat(service.getById(owner.getId(), first.getId()).getDescription()).isEqualTo("First");
        assertThat(service.getById(owner.getId(), second.getId()).getPosition()).isEqualTo(2);
    }

    @Test
    void createForStudyPlanRequiresExistingUser() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AppUser owner = fakes.saveUser("owner");
        StudyPlan plan = fakes.saveStudyPlan(owner, "Plan", "Goal");
        StepService service = new StepService(fakes.stepRepository(), fakes.userRepository());

        assertThatThrownBy(() -> service.createForStudyPlan(404L, plan, stepRequest("Missing", "Missing", 1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found: 404");
    }

    @Test
    void updateAndDeleteAreScopedToUser() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AppUser owner = fakes.saveUser("owner");
        AppUser other = fakes.saveUser("other");
        StudyPlan plan = fakes.saveStudyPlan(owner, "Plan", "Goal");
        StepService service = new StepService(fakes.stepRepository(), fakes.userRepository());
        StepResponse created = service.createForStudyPlan(owner.getId(), plan, stepRequest("Old", "Old", 1));

        StepResponse updated = service.update(owner.getId(), created.getId(), stepRequest("New", "New", 2));

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getPosition()).isEqualTo(2);
        assertThatThrownBy(() -> service.update(other.getId(), created.getId(), stepRequest("Bad", "Bad", 1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Step not found");

        service.delete(owner.getId(), created.getId());
        assertThat(fakes.steps()).doesNotContainKey(created.getId());
    }

    private StepRequest stepRequest(String title, String description, int position) {
        StepRequest request = new StepRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCompleted(false);
        request.setPosition(position);
        return request;
    }
}
