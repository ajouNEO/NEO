package com.neo.back.service.controller;

import com.neo.back.service.dto.ScheduledTaskDto;
import com.neo.back.service.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/tasks")
    public List<ScheduledTaskDto> getScheduledTasks() {
        return scheduleService.getScheduledTasks();
    }

    @GetMapping("/tasks2")
    public List<ScheduledTaskDto> getUserScheduledTasks() {
        return scheduleService.getUserScheduledTasks();
    }

}