package com.vin.decoder.service;

import com.vin.decoder.model.CarInfo;
import com.vin.decoder.model.VinRequest;
import com.vin.decoder.repository.VinHistoryRepository;
import com.vin.decoder.service.external.VinDecoderAdapter;
import com.vin.decoder.service.state.RequestStatus;
import com.vin.decoder.service.validation.VinValidationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "vinCache", key = "#vin", unless = "#result == null")
    @Transactional
    public CarInfo checkVin(String vin, Long userId) {
        log.info("Запрос к NHTSA API для VIN: {} (кеш не задействован)", vin);

        if (!validationStrategy.isValid(vin)) {
            log.warn("Неверный формат VIN: {}", vin);
            saveRequest(vin, userId, null, "Неверный формат VIN", RequestStatus.ERROR);
            throw new IllegalArgumentException("Неверный формат VIN");
        }

        try {
            CarInfo carInfo = vinDecoderAdapter.decodeVin(vin);
            saveRequest(vin, userId, carInfo, null, RequestStatus.SUCCESS);
            log.info("VIN {} успешно проверен", vin);
            return carInfo;
        } catch (Exception e) {
            log.error("Ошибка проверки VIN {}: {}", vin, e.getMessage());
            saveRequest(vin, userId, null, e.getMessage(), RequestStatus.ERROR);
            throw new RuntimeException("Ошибка проверки VIN: " + e.getMessage());
        }
    }

    private void saveRequest(String vin, Long userId, CarInfo carInfo, String errorMessage, RequestStatus status) {
        VinRequest request = VinRequest.builder()
                .vin(vin)
                .userId(userId)
                .status(status)
                .result(carInfo)
                .errorMessage(errorMessage)
                .createdAt(LocalDateTime.now())
                .build();
        historyRepository.save(request);
    }

    public List<VinRequest> getUserHistory(Long userId) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void clearCache() {
        log.info("Очистка кэша VIN");
    }
}