package com.studi.server.dto;

import java.util.List;

public class CreateStudyPlanResponse {

    private Long id;
    private String title;
    private String goal;
    private List<StepResponse> steps;

    public CreateStudyPlanResponse(Long id, String title, String goal, List<StepResponse> steps) {
        this.id = id;
        this.title = title;
        this.goal = goal;
        this.steps = steps;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGoal() {
        return goal;
    }

    public List<StepResponse> getSteps() {
        return steps;
    }
}
