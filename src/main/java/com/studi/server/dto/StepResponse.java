package com.studi.server.dto;

public class StepResponse {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private int position;

    public StepResponse(Long id, String title, String description, boolean completed, int position) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getPosition() {
        return position;
    }
}
