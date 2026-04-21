package com.studi.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.studi.server.dto.CreateStudyPlanRequest;
import com.studi.server.dto.CreateStudyPlanResponse;
import com.studi.server.model.AppUser;
import com.studi.server.security.UserPrincipal;
import com.studi.server.service.StudyPlanService;

class StudyPlanControllerTest {

    @Test
    void endpointsDelegateToServiceWithAuthenticatedUserId() {
        StudyPlanService service = mock(StudyPlanService.class);
        StudyPlanController controller = new StudyPlanController(service);
        UserPrincipal principal = principal(42L);
        CreateStudyPlanRequest request = new CreateStudyPlanRequest();
        CreateStudyPlanResponse response = new CreateStudyPlanResponse(3L, "Plan", "Goal", List.of());
        when(service.createStudyPlan(42L, request)).thenReturn(response);
        when(service.getById(42L, 3L)).thenReturn(response);

        assertThat(controller.create(principal, request)).isSameAs(response);
        assertThat(controller.getById(principal, 3L)).isSameAs(response);
    }

    private UserPrincipal principal(Long id) {
        AppUser user = new AppUser("emmanuel", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return new UserPrincipal(user);
    }
}
