package com.studi.server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/steps")
public class StepController {

    private final StepService service;

    public StepController(StepService service) {
        this.service = service;
    }

    @GetMapping
    public List<Step> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Step getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Step create(@RequestBody Step step) {
        return service.create(step);
    }

    @PutMapping("/{id}")
    public Step update(@PathVariable Long id, @RequestBody Step step) {
        return service.update(id, step);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
