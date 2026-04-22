package com.studi.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.studi.server.dto.CreateStudyPlanRequest;
import com.studi.server.dto.CreateStudyPlanResponse;
import com.studi.server.dto.StudyPlanSummaryResponse;
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

    @GetMapping
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"))
    public List<StudyPlanSummaryResponse> getAll(@AuthenticationPrincipal UserPrincipal user) {
        return studyPlanService.getAll(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Study plan created"),
                    @ApiResponse(responseCode = "400", description = "title or goal missing"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
            })
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

    @DeleteMapping("/{id}")
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Study plan and its steps deleted"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
                    @ApiResponse(responseCode = "404", description = "Study plan not found")
            })
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        studyPlanService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
