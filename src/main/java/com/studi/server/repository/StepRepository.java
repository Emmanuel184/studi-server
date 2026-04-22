package com.studi.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import com.studi.server.model.Step;

public interface StepRepository extends JpaRepository<Step, Long> {
    List<Step> findAllByUserIdOrderByPositionAsc(Long userId);

    Optional<Step> findByIdAndUserId(Long id, Long userId);

    List<Step> findAllByStudyPlanIdAndUserIdOrderByPositionAsc(Long studyPlanId, Long userId);
    void deleteAllByStudyPlanId(Long studyPlanId);
}
