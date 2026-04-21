package com.studi.server.service;

import org.springframework.stereotype.Service;
import java.util.List;

import com.studi.server.dto.StepRequest;
import com.studi.server.dto.StepResponse;
import com.studi.server.model.AppUser;
import com.studi.server.model.Step;
import com.studi.server.model.StudyPlan;
import com.studi.server.repository.StepRepository;
import com.studi.server.repository.UserRepository;

@Service
public class StepService {

    private final StepRepository repo;
    private final UserRepository userRepository;

    public StepService(StepRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    public List<StepResponse> getAll(Long userId) {
        return repo.findAllByUserIdOrderByPositionAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StepResponse getById(Long userId, Long id) {
        return toResponse(findStep(userId, id));
    }

    public StepResponse createForStudyPlan(Long userId, StudyPlan studyPlan, StepRequest request) {
        Step step = new Step();
        step.setUser(findUser(userId));
        step.setStudyPlan(studyPlan);
        applyRequest(step, request);
        return toResponse(repo.save(step));
    }

    public StepResponse update(Long userId, Long id, StepRequest request) {
        Step existing = findStep(userId, id);
        applyRequest(existing, request);
        return toResponse(repo.save(existing));
    }

    public void delete(Long userId, Long id) {
        repo.delete(findStep(userId, id));
    }

    private Step findStep(Long userId, Long id) {
        return repo.findByIdAndUserId(id, userId).orElseThrow(() -> new RuntimeException("Step not found: " + id));
    }

    private AppUser findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    private void applyRequest(Step step, StepRequest request) {
        step.setTitle(request.getTitle());
        step.setDescription(request.getDescription());
        step.setCompleted(request.isCompleted());
        step.setPosition(request.getPosition());
    }

    private StepResponse toResponse(Step step) {
        return new StepResponse(
                step.getId(),
                step.getTitle(),
                step.getDescription(),
                step.isCompleted(),
                step.getPosition());
    }
}
