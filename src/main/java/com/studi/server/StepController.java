package com.studi.server;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
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
