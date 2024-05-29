package com.neo.back.service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ScheduledTaskDto {
    private String dockerId;
    private String status; // e.g., "scheduled" or "cancelled"

    private Instant startDate;
    private Instant endDate;

}




