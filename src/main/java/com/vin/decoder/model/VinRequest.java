package com.vin.decoder.model;

import com.vin.decoder.service.state.RequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VinRequest {
    private String vin;
    private Long userId;
    private RequestStatus status;
    private LocalDateTime createdAt;
}