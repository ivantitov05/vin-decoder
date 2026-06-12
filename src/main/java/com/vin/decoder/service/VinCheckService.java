package com.vin.decoder.service;

import com.vin.decoder.model.CarInfo;
import com.vin.decoder.model.VinRequest;
import com.vin.decoder.repository.VinCacheProxy;
import com.vin.decoder.service.external.VinDecoderAdapter;
import com.vin.decoder.service.state.RequestStatus;
import com.vin.decoder.service.validation.VinValidationStrategy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VinCheckService {

    private final VinCacheProxy vinCacheProxy;
    private final VinDecoderAdapter dadataAdapter;
    private final VinValidationStrategy validationStrategy;

    @Transactional
    public CarInfo checkVIN(String vin, Long userId) {
        if (!validationStrategy.isValid(vin)) {
            log.warn("Некорректный VIN: {}", vin);
            throw new IllegalArgumentException("VIN не соответствует формату: 17 символов");
        }

        var existing = vinCacheProxy.findByVinAndUserId(vin, userId);
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
        request = vinCacheProxy.save(request);

        try {
            CarInfo carInfo = dadataAdapter.decodeVin(vin);

            request.setStatus(RequestStatus.DONE);
            request.setResult(carInfo);
            vinCacheProxy.save(request);

            log.info("VIN {} успешно обработан", vin);
            return carInfo;

        } catch (Exception e) {
            request.setStatus(RequestStatus.ERROR);
            request.setErrorMessage(e.getMessage());
            vinCacheProxy.save(request);

            log.error("Ошибка при обработке VIN {}: {}", vin, e.getMessage(), e);
            throw new RuntimeException("Не удалось получить данные от внешнего сервиса", e);
        }
    }

    public List<VinRequest> getUserHistory(Long userId) {
        log.info("Запрос истории для пользователя: {}", userId);
        return vinCacheProxy.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
}