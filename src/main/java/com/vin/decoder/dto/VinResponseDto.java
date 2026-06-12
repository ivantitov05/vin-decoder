package com.vin.decoder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VinResponseDto {
    private String vin;
    private String brand;
    private String model;
    private Integer year;
    private String status;
    private Long requestId;
    private LocalDateTime checkedAt;
}