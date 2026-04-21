package com.studi.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.studi.server.dto.StepRequest;
import com.studi.server.dto.StepResponse;
import com.studi.server.model.AppUser;
import com.studi.server.security.UserPrincipal;
import com.studi.server.service.StepService;

class StepControllerTest {

    @Test
    void endpointsDelegateToServiceWithAuthenticatedUserId() {
        StepService service = mock(StepService.class);
        StepController controller = new StepController(service);
        UserPrincipal principal = principal(42L);
        StepRequest request = new StepRequest();
        StepResponse response = new StepResponse(5L, "Read", "Read docs", false, 1);
        when(service.getAll(42L)).thenReturn(List.of(response));
        when(service.getById(42L, 5L)).thenReturn(response);
        when(service.update(42L, 5L, request)).thenReturn(response);

        assertThat(controller.getAll(principal)).containsExactly(response);
        assertThat(controller.getById(principal, 5L)).isSameAs(response);
        assertThat(controller.update(principal, 5L, request)).isSameAs(response);
        assertThat(controller.delete(principal, 5L).getStatusCode().value()).isEqualTo(204);
        verify(service).delete(42L, 5L);
    }

    private UserPrincipal principal(Long id) {
        AppUser user = new AppUser("emmanuel", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return new UserPrincipal(user);
    }
}
