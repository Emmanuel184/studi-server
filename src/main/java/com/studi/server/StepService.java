package com.studi.server;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StepService {

    private final StepRepository repo;

    public StepService(StepRepository repo) {
        this.repo = repo;
    }

    public List<Step> getAll() {
        return repo.findAllByOrderByPositionAsc();
    }

    public Step getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Step not found: " + id));
    }

    public Step create(Step step) {
        return repo.save(step);
    }

    public Step update(Long id, Step incoming) {
        Step existing = getById(id);
        existing.setTitle(incoming.getTitle());
        existing.setDescription(incoming.getDescription());
        existing.setCompleted(incoming.isCompleted());
        existing.setPosition(incoming.getPosition());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
