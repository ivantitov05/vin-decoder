package com.vin.decoder.repository;

import com.vin.decoder.model.VinRequest;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class VinCacheProxy implements VinHistoryRepository {

    private final VinHistoryRepository repository;
    private final Map<String, VinRequest> cache = new ConcurrentHashMap<>();

    public VinCacheProxy(VinHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<VinRequest> findByVinAndUserId(String vin, Long userId) {
        // 1. Сначала смотрим в кэш
        VinRequest cached = cache.get(vin);
        if (cached != null) return Optional.of(cached);

        // 2. Потом в БД
        return repository.findByVinAndUserId(vin, userId);
    }

    @Override
    public VinRequest save(VinRequest request) {
        cache.put(request.getVin(), request);
        return repository.save(request);
    }
}
