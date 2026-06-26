package com.ayush.ems.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TaskPerformance {
    private String month;
    private int totalTasks;
    private int completedTasks;
    private int pendingTasks;
    private int inProgressTasks;
    private int overdueTasks;
    private int lateCompletedTasks;
    private int onTimeCompletedTasks;

    private String username;

    private List<String> completedTaskList;
    private List<String> pendingTaskList;
    private List<String> inProgressTaskList;
    private List<String> overdueTaskList;
    private List<String> lateCompletedTaskList;
    private List<String> onTimeCompletedTaskList;
    private List<String> totalTasksList;
}
