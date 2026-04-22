package com.studi.server.dto;

import java.util.List;

public class CreateStudyPlanRequest {

    private String title;
    private String goal;
    private List<StepRequest> steps;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public List<StepRequest> getSteps() {
        return steps;
    }

    public void setSteps(List<StepRequest> steps) {
        this.steps = steps;
    }
}
