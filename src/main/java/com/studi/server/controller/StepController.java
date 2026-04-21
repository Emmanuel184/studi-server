package com.studi.server.controller;

import java.util.List;

import com.studi.server.dto.StepRequest;
import com.studi.server.dto.StepResponse;
import com.studi.server.security.UserPrincipal;
import com.studi.server.service.StepService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/steps")
@SecurityRequirement(name = "bearerAuth")
public class StepController {

    private final StepService service;

    public StepController(StepService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"))
    public List<StepResponse> getAll(@AuthenticationPrincipal UserPrincipal user) {
        return service.getAll(user.getId());
    }

    @GetMapping("/{id}")
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"))
    public StepResponse getById(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        return service.getById(user.getId(), id);
    }

    @PutMapping("/{id}")
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"))
    public StepResponse update(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long id,
            @RequestBody StepRequest request) {
        return service.update(user.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"))
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        service.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
