package com.neo.back.service.controller;

import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.service.dto.ScheduledTaskDto;
import com.neo.back.service.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final UserRepository userRepository;

    @PostMapping("/start")
    public String scheduleServiceEndWithPoints(@RequestParam String username, @RequestParam String dockerId, @RequestParam Long points) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "User not found";
        }
        Instant startTime = Instant.now();
        scheduleService.scheduleServiceEndWithPoints(user, dockerId, startTime, points, 2);
        return "Service scheduled to end with points";
    }

    @PostMapping("/cancel")
    public String cancelScheduledEnd(@RequestParam String username, @RequestParam String dockerId) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "User not found";
        }
        Instant startTime = Instant.now();
        Instant endTime = scheduleService.calculateEndTime(user.getPoints());
        scheduleService.cancelScheduledEnd(user, dockerId, startTime, endTime);
        return "Scheduled service canceled";
    }

    @GetMapping("/tasks")
    public List<ScheduledTaskDto> getScheduledTasks() {
        return scheduleService.getScheduledTasks();
    }
}