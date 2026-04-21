package com.studi.server.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studi.server.dto.CreateStudyPlanRequest;
import com.studi.server.dto.CreateStudyPlanResponse;
import com.studi.server.dto.StepRequest;
import com.studi.server.dto.StepResponse;
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
        AppUser user = findUser(userId);
        CreateStudyPlan hardcodedPlan = buildCreateStudyPlan(request);

        StudyPlan studyPlan = new StudyPlan();
        studyPlan.setUser(user);
        studyPlan.setTitle(hardcodedPlan.title());
        studyPlan.setGoal(hardcodedPlan.goal());
        StudyPlan savedPlan = studyPlanRepository.save(studyPlan);

        List<StepResponse> savedSteps = hardcodedPlan.steps()
                .stream()
                .map(stepRequest -> stepService.createForStudyPlan(userId, savedPlan, stepRequest))
                .toList();

        return toResponseFromStepResponses(savedPlan, savedSteps);
    }

    public CreateStudyPlanResponse getById(Long userId, Long planId) {
        StudyPlan studyPlan = studyPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("Study plan not found: " + planId));
        List<Step> steps = stepRepository.findAllByStudyPlanIdAndUserIdOrderByPositionAsc(planId, userId);
        return toResponse(studyPlan, steps);
    }

    private CreateStudyPlan buildCreateStudyPlan(CreateStudyPlanRequest request) {
        String goal = request.getGoal() == null || request.getGoal().isBlank()
                ? "Build a strong study routine"
                : request.getGoal().trim();

        return new CreateStudyPlan(
                "Foundations Study Plan",
                goal,
                List.of(
                        stepRequest("Clarify the goal", "Write down what success looks like for: " + goal, 1),
                        stepRequest("Gather learning material", "Pick one primary resource and one backup reference.", 2),
                        stepRequest("Study the fundamentals", "Spend focused time on the core concepts before practicing.", 3),
                        stepRequest("Practice actively", "Create examples, answer questions, and explain the topic out loud.", 4),
                        stepRequest("Review and adjust", "Check what worked, mark completed work, and revise the next session.", 5)));
    }

    private StepRequest stepRequest(String title, String description, int position) {
        StepRequest request = new StepRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCompleted(false);
        request.setPosition(position);
        return request;
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
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    private record CreateStudyPlan(String title, String goal, List<StepRequest> steps) {}
}
