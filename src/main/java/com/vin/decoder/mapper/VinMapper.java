package com.vin.decoder.mapper;

import com.vin.decoder.dto.VinResponseDto;
import com.vin.decoder.model.CarInfo;
import com.vin.decoder.model.VinRequest;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VinMapper {

    public VinResponseDto toResponseDto(CarInfo carInfo, Long requestId, String status, LocalDateTime checkedAt) {
        if (carInfo == null) {
            return VinResponseDto.builder()
                    .status("ERROR")
                    .checkedAt(checkedAt)
                    .build();
        }

        return VinResponseDto.builder()
                .brand(carInfo.getBrand())
                .model(carInfo.getModel())
                .year(carInfo.getYear())
                .status(status)
                .requestId(requestId)
                .checkedAt(checkedAt)
                .build();
    }

    public VinResponseDto toResponseDto(VinRequest request) {
        CarInfo result = request.getResult();
        return VinResponseDto.builder()
                .vin(request.getVin())
                .brand(result != null ? result.getBrand() : null)
                .model(result != null ? result.getModel() : null)
                .year(result != null ? result.getYear() : null)
                .status(request.getStatus().toString())
                .requestId(request.getId())
                .checkedAt(request.getCreatedAt())
                .build();
    }

    public List<VinResponseDto> toResponseDtoList(List<VinRequest> requests) {
        return requests.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}