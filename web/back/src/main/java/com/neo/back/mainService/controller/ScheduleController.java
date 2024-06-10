package com.neo.back.mainService.controller;

import com.neo.back.authorization.entity.User;
import com.neo.back.mainService.dto.ScheduledTaskDto;
import com.neo.back.mainService.service.ScheduleService;
import com.neo.back.mainService.utility.GetCurrentUser;

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