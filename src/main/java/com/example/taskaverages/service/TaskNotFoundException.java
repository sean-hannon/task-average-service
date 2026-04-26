package com.example.taskaverages.service;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(String taskId) {
        super("No durations have been recorded for task '" + taskId + "'.");
    }
}
