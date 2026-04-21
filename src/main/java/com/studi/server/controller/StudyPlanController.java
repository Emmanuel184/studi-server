package com.studi.server.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studi.server.dto.CreateStudyPlanRequest;
import com.studi.server.dto.CreateStudyPlanResponse;
import com.studi.server.security.UserPrincipal;
import com.studi.server.service.StudyPlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/study-plans")
@SecurityRequirement(name = "bearerAuth")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    @PostMapping
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"))
    public CreateStudyPlanResponse create(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CreateStudyPlanRequest request) {
        return studyPlanService.createStudyPlan(user.getId(), request);
    }

    @GetMapping("/{id}")
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"))
    public CreateStudyPlanResponse getById(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        return studyPlanService.getById(user.getId(), id);
    }
}
