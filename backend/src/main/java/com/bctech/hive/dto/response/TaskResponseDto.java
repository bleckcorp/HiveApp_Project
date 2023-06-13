package com.bctech.hive.dto.response;

import com.bctech.hive.constant.Status;
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
public class TaskResponseDto {

    private String jobType;
    @Size(max = 250)
    private String taskDescription;
    private BigDecimal budgetRate;
    private String taskAddress;
    private String taskDeliveryAddress;
    private Integer estimatedTime;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String taskDuration;
    private Status status;
    private String taskId;
}
