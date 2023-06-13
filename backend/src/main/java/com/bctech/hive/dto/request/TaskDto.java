package com.bctech.hive.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDto {
    private String jobType;
    @Size(max = 250)
    private String taskDescription;
    private BigDecimal budgetRate;
    private String taskAddress;
    private String taskDeliveryAddress;
    private Integer estimatedTime;
    private String taskDuration;
}
