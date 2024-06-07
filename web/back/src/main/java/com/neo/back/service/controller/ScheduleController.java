package com.neo.back.service.controller;

import com.neo.back.authorization.entity.User;
import com.neo.back.service.dto.ScheduledTaskDto;
import com.neo.back.service.service.ScheduleService;
import com.neo.back.service.utility.GetCurrentUser;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final GetCurrentUser getCurrentUser;

    @GetMapping("/tasks")
    public List<ScheduledTaskDto> getScheduledTasks() {
        return scheduleService.getScheduledTasks();
    }

    @GetMapping("/tasks2")
    public List<ScheduledTaskDto> getUserScheduledTasks() {
        return scheduleService.getUserScheduledTasks();
    }

    @GetMapping("/stop")
    public void stopSchedule() {
        User user = getCurrentUser.getUser();
        scheduleService.startScheduling(user);
    }

}