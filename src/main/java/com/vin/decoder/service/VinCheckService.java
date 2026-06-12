package com.vin.decoder.service;

import com.vin.decoder.model.CarInfo;
import com.vin.decoder.model.VinRequest;
import com.vin.decoder.repository.VinHistoryRepository;
import com.vin.decoder.service.external.VinDecoderAdapter;
import com.vin.decoder.service.state.RequestStatus;
import com.vin.decoder.service.validation.VinValidationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VinCheckService {

    private final VinDecoderAdapter vinDecoderAdapter;
    private final VinHistoryRepository historyRepository;
    private final VinValidationStrategy validationStrategy;

    @Transactional
    public CarInfo checkVin(String vin, Long userId) {
        // 1. Валидация формата VIN
        if (!validationStrategy.isValid(vin)) {
            throw new IllegalArgumentException("Неверный формат VIN");
        }

        try {
            // 2. Запрос к внешнему API (NHTSA)
            CarInfo carInfo = vinDecoderAdapter.decodeVin(vin);

            // 3. Сохранение успешного результата
            saveRequest(vin, userId, carInfo, null, "SUCCESS");

            log.info("VIN {} успешно проверен для пользователя {}", vin, userId);
            return carInfo;

        } catch (Exception e) {
            log.error("Ошибка при проверке VIN {}: {}", vin, e.getMessage());
            saveRequest(vin, userId, null, e.getMessage(), "ERROR");
            throw new RuntimeException("Ошибка при проверке VIN: " + e.getMessage());
        }
    }

    private void saveRequest(String vin, Long userId, CarInfo carInfo, String errorMessage, String status) {
        VinRequest request = VinRequest.builder()
                .vin(vin)
                .userId(userId)
                .status(RequestStatus.valueOf(status))
                .createdAt(LocalDateTime.now())
                .result(carInfo)
                .errorMessage(errorMessage)
                .build();
        historyRepository.save(request);
    }

    public List<VinRequest> getUserHistory(Long userId) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

}