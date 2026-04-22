package com.studi.server.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studi.server.dto.CreateStudyPlanRequest;
import com.studi.server.dto.CreateStudyPlanResponse;
import com.studi.server.dto.StepRequest;
import com.studi.server.dto.StepResponse;
import com.studi.server.dto.StudyPlanSummaryResponse;
import com.studi.server.model.AppUser;
import com.studi.server.model.Step;
import com.studi.server.model.StudyPlan;
import com.studi.server.repository.StepRepository;
import com.studi.server.repository.StudyPlanRepository;
import com.studi.server.repository.UserRepository;

@Service
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final StepRepository stepRepository;
    private final UserRepository userRepository;
    private final StepService stepService;

    public StudyPlanService(
            StudyPlanRepository studyPlanRepository,
            StepRepository stepRepository,
            UserRepository userRepository,
            StepService stepService) {
        this.studyPlanRepository = studyPlanRepository;
        this.stepRepository = stepRepository;
        this.userRepository = userRepository;
        this.stepService = stepService;
    }

    @Transactional
    public CreateStudyPlanResponse createStudyPlan(Long userId, CreateStudyPlanRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (request.getGoal() == null || request.getGoal().isBlank()) {
            throw new IllegalArgumentException("goal is required");
        }

        AppUser user = findUser(userId);

        StudyPlan studyPlan = new StudyPlan();
        studyPlan.setUser(user);
        studyPlan.setTitle(request.getTitle().trim());
        studyPlan.setGoal(request.getGoal().trim());
        StudyPlan savedPlan = studyPlanRepository.save(studyPlan);

        List<StepRequest> steps = request.getSteps() != null ? request.getSteps() : List.of();
        List<StepResponse> savedSteps = steps.stream()
                .map(stepRequest -> stepService.createForStudyPlan(userId, savedPlan, stepRequest))
                .toList();

        return toResponseFromStepResponses(savedPlan, savedSteps);
    }

    public List<StudyPlanSummaryResponse> getAll(Long userId) {
        return studyPlanRepository.findAllByUserIdOrderByIdDesc(userId).stream()
                .map(p -> new StudyPlanSummaryResponse(p.getId(), p.getTitle(), p.getGoal()))
                .toList();
    }

    public CreateStudyPlanResponse getById(Long userId, Long planId) {
        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("Study plan not found: " + planId));
        List<Step> steps = stepRepository.findAllByStudyPlanIdAndUserIdOrderByPositionAsc(planId, userId);
        return toResponse(studyPlan, steps);
    }

    @Transactional
    public void delete(Long userId, Long planId) {
        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("Study plan not found: " + planId));
        stepRepository.deleteAllByStudyPlanId(studyPlan.getId());
        studyPlanRepository.delete(studyPlan);
    }

    private CreateStudyPlanResponse toResponse(StudyPlan studyPlan, List<Step> steps) {
        return new CreateStudyPlanResponse(
                studyPlan.getId(),
                studyPlan.getTitle(),
                studyPlan.getGoal(),
                steps.stream().map(this::toStepResponse).toList());
    }

    private CreateStudyPlanResponse toResponseFromStepResponses(StudyPlan studyPlan, List<StepResponse> steps) {
        return new CreateStudyPlanResponse(
                studyPlan.getId(),
                studyPlan.getTitle(),
                studyPlan.getGoal(),
                steps);
    }

    private StepResponse toStepResponse(Step step) {
        return new StepResponse(
                step.getId(),
                step.getTitle(),
                step.getDescription(),
                step.isCompleted(),
                step.getPosition());
    }

    private AppUser findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}
