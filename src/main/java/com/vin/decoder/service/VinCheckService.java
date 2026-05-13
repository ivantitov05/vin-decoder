package com.vin.decoder.service;

import com.vin.decoder.model.CarInfo;
import com.vin.decoder.model.VinRequest;
import com.vin.decoder.repository.VinHistoryRepository;
import com.vin.decoder.service.state.RequestStatus;
import com.vin.decoder.service.validation.VinValidationStrategy;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

public class VinCheckService {
    private final VinHistoryRepository vinHistoryRepository;

    public VinCheckService(VinHistoryRepository vinHistoryRepository){
        this.vinHistoryRepository = vinHistoryRepository;
    }

    @Transactional
    public CarInfo checkVIN(String vin,Long userId){
        if (!VinValidationStrategy.isValid(vin)) {
            log.warn("Некорректный VIN: {}", vin);
            throw new IllegalArgumentException("VIN не соответствует формату: 17 символов, допустимые символы: 0-9, A-H, J-N, P, R-Z");
        }

        var existing = vinHistoryRepository.findByVinAndUserId(vin, userId);
        if (existing.isPresent() && existing.get().getResult() != null) {
            log.info("Возвращаем кэшированный результат для VIN: {}", vin);
            return existing.get().getResult();
        }

        VinRequest request = VinRequest.builder()
                .vin(vin)
                .userId(userId)
                .status(RequestStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();
        request = vinHistoryRepository.save(request);

        try {
            CarInfo carInfo = dadataAdapter.decodeVin(vin);

            request.setStatus(VinRequestStatus.DONE);
            request.setResult(carInfo);
            vinHistoryRepository.save(request);

            log.info("VIN {} успешно обработан", vin);
            return carInfo;

        } catch (Exception e) {
            request.setStatus(VinRequestStatus.ERROR);
            request.setErrorMessage(e.getMessage());
            vinHistoryRepository.save(request);

            log.error("Ошибка при обработке VIN {}: {}", vin, e.getMessage(), e);
            throw new RuntimeException("Не удалось получить данные от внешнего сервиса", e);
        }
    }
}
