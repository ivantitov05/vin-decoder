package com.vin.decoder.repository;

import com.vin.decoder.model.VinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VinHistoryRepository extends JpaRepository<VinRequest, Long> {

    // Получить все запросы пользователя (от новых к старым)
    List<VinRequest> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // Поиск по VIN и пользователю (для кэша)
    Optional<VinRequest> findByVinAndUserId(String vin, Long userId);

    // Проверка прав доступа
    Optional<VinRequest> findByIdAndUserId(Long id, Long userId);

    List<VinRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}