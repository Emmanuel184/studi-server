package com.studi.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studi.server.model.StudyPlan;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    Optional<StudyPlan> findByIdAndUserId(Long id, Long userId);
    List<StudyPlan> findAllByUserIdOrderByIdDesc(Long userId);
}
